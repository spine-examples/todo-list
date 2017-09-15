/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.server.storage.kafka;

import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.server.entity.Entity;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.server.storage.kafka.Consistency.STRONG;
import static java.lang.Long.compare;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

/**
 * A wrapper for {@link KafkaConsumer} and {@link KafkaProducer} instances.
 *
 * <p>The high-level architecture is following:
 * <ul>
 *     <li>Each message type has a separate Kafka topic.
 *     <li>The records written into a single topic are distributed between all the topic partitions
 *         based on the {@code hashCode()} of the record key. A single partition may hold records
 *         with different keys, but all the records with the same key are put into the same
 *         partition.
 *     <li>Unless an ID is specified for a read operation, the read is conducted through all
 *         the partitions of a given topic.
 *     <li>If a record is supposed to be read with {@link #readAll(Class)} method, it should be
 *         written with {@link #write(Class, Topic, Object, Message)}.
 *     <li>Unless specified otherwise, the read operations are not lazy and fetch all the requested
 *         data in place.
 * </ul>
 *
 * <p>As a general note, the {@link KafkaConsumer} at a point in time is assigned only to the topic
 * partitions required at the current moment. All reads
 * {@linkplain KafkaConsumer#seekToBeginning seek} the consumer pointer in a partition to
 * the beginning. So, each read operation may poll the whole partition from the broker.
 *
 * <p>The wrapper takes care of synchronizing the methods of {@link KafkaConsumer} when required.
 *
 * @author Dmytro Dashenkov
 */
public class KafkaWrapper {

    private final Producer<Message, Message> producer;
    private final Consumer<Message, Message> consumer;
    private final Consistency consistencyLevel;
    private final long pollAwait;

    // TODO:2017-09-14:dmytro.dashenkov: Should be fair?
    private final Lock lock = new ReentrantLock();

    /**
     * Craetes new instance of {@code KafkaWrapper}.
     *
     * @param producer         the {@link Producer} send the records to Kafka
     * @param consumer         the {@link Consumer} fetch the records from Kafka
     * @param consistencyLevel the level of data consistency from the write operations. If set to
     *                         {@link Consistency#STRONG STRONG}, the {@code write} methods block
     *                         the execution until the record acknowledgement. See {@code acks}
     *                         property in the producer config for more on the record
     *                         acknowledgement
     * @param maxPollAwait     the maximum time a read operation may block the thread for; must be
     *                         a positive (i.e. non-{@linkplain Duration#isNegative() negative}
     *                         and non-{@linkplain Duration#isZero() zero}) duration; see
     *                         {@link Consumer#poll(long)} for more details
     */
    public KafkaWrapper(Producer<Message, Message> producer,
                        Consumer<Message, Message> consumer,
                        Consistency consistencyLevel,
                        Duration maxPollAwait) {
        this.producer = checkNotNull(producer);
        this.consumer = checkNotNull(consumer);
        this.consistencyLevel = checkNotNull(consistencyLevel);
        checkArgument(!maxPollAwait.isNegative() && !maxPollAwait.isZero(),
                      "Max poll await must be a positive Duration.");
        this.pollAwait = maxPollAwait.toMillis();
    }

    /**
     * Reads all the records under the given Kafka topic.
     *
     * <p>The result is sorted by the {@linkplain ConsumerRecord#timestamp() record timestamp}.
     *
     * @param topic the topic to fetch
     * @param <M>   the type of the records stored in within the topic
     * @return an non-lazy iterator over the records under the given topic
     */
    public <M extends Message> Iterator<M> read(Topic topic) {
        final Iterable<ConsumerRecord<Message, Message>> records = doRead(topic);
        final Iterator<Message> messages = stream(records)
                .sorted(ConsumerRecordComparator.DIRECT.get())
                .map(ConsumerRecord::value)
                .iterator();
        @SuppressWarnings("unchecked") // Expect caller to be aware of the type.
        final Iterator<M> result = (Iterator<M>) messages;
        return result;
    }

    /**
     * Reads all the records corresponding to the state type of a given {@link Entity}.
     *
     * <p>In order to be read by this method, a record should belong to a topic marked as the state
     * of the given Entity. To mark a topic as an Entity state, write a record into this topic with
     * the {@link #write(Class, Topic, Object, Message)} specifying the required Entity class.
     *
     * <p>Consider the following example:
     * <pre>
     *     {@code
     *     final Topic topic = Topic.ofValue("my-topic");
     *     final String idA = ...
     *     final Customer recordA = ...
     *     final String idA = ...
     *     final Customer recordA = ...
     *     wrapper.write(CustomerProjection.class, topic, idA, recordA);
     *     wrapper.write(topic, idB, recordB);
     *     }
     * </pre>
     *
     * <p>In the example above, when reading records with {@code readAll(CustomerProjection.class)}
     * call, both {@code recordA} and {@code recordB} are returned, since the topic
     * {@code "my-topic"} has already been marked to belong to the {@code CustomerProjection} state
     * type. Though, if both {@code recordA} and {@code recordB} were written with
     * {@link #write(Topic, Object, Message)}, then the call
     * {@code readAll(CustomerProjection.class)} returns none of them.
     *
     * <p>The resulting iterator is partially lazy, i.e. the partitions are fetched in a lazy
     * manner, but the records are fetched all at once.
     *
     * @param type the class of {@link Entity} to get the states for
     * @param <M>  the type of the result
     * @return an iterator of the
     */
    public <M extends Message> Iterator<M> readAll(Class<? extends Entity> type) {
        final Topic topicOfTopics = Topics.superTopic(type);
        final Iterable<ConsumerRecord<Message, Message>> records = doRead(topicOfTopics);
        final Iterator<Message> messages = stream(records)
                .map(ConsumerRecord::value)
                .distinct()
                .map(msg -> {
                    final String topicName = ((StringValue) msg).getValue();
                    final Topic topic = Topic.ofValue(topicName);
                    final Iterable<ConsumerRecord<Message, Message>> result = doRead(topic);
                    return result;
                })
                .flatMap(KafkaWrapper::stream)
                .sorted(ConsumerRecordComparator.DIRECT.get())
                .map(ConsumerRecord::value)
                .iterator();
        @SuppressWarnings("unchecked") // Expect caller to be aware of the type.
        final Iterator<M> result = (Iterator<M>) messages;
        return result;
    }

    /**
     * Reads a single record by its ID from the given topic.
     *
     * @param topic the topic to read from
     * @param id    the ID to read by
     * @param <M>   the type of the record
     * @return the latest record with the given ID or {@link Optional#empty()} if there is no such
     *         record
     */
    public <M extends Message> Optional<M> read(Topic topic, Object id) {
        final int partition = partitionFor(topic, id);
        final Iterable<ConsumerRecord<Message, Message>> records = doRead(topic, partition);
        final Message key = toMessage(id);
        final Optional<Message> message = stream(records)
                .filter(record -> key.equals(record.key()))
                .sorted(ConsumerRecordComparator.DIRECT.get())
                .map(ConsumerRecord::value)
                .findFirst();
        @SuppressWarnings("unchecked") // Expect caller to be aware of the type.
        final Optional<M> result = (Optional<M>) message;
        return result;
    }

    /**
     * Reads the last record written to the given topic.
     *
     * <p>The latest record is the record with the biggest
     * {@linkplain ConsumerRecord#timestamp() timestamp} value.
     *
     * @param topic the topic to look up
     * @param <M> the type of the record
     * @return the latest record in the given topic or {@link Optional#empty()} if the topic has
     *         no records
     */
    public <M extends Message> Optional<M> readLast(Topic topic) {
        final Iterable<ConsumerRecord<Message, Message>> records = doRead(topic);
        final Optional<Message> lastMsg = stream(records)
                .max(ConsumerRecordComparator.DIRECT.get())
                .map(ConsumerRecord::value);
        @SuppressWarnings("unchecked") // Expect caller to be aware of the type.
        final Optional<M> result = (Optional<M>) lastMsg;
        return result;
    }

    /**
     * Writes the given record to the given topic.
     *
     * <p>Calling this method marks the given topic as state of the given {@link Entity} class.
     *
     * @param entityClass the type of the entity to bind this record to
     * @param topic       the topic to write to
     * @param id          the ID to store the record under
     * @param value       the record to write
     * @see #readAll(Class)
     * @see #write(Topic, Object, Message)
     */
    public void write(Class<? extends Entity> entityClass, Topic topic, Object id, Message value) {
        writeToSuperTopic(entityClass, topic);
        write(topic, id, value);
    }

    /**
     * Writes the given record to the given topic.
     *
     * <p>If the {@linkplain Consistency consistency level} of this wrapper instance is
     * {@link Consistency#EVENTUAL EVENTUAL}, the method exists immediately after sending
     * the record to Kafka. Otherwise, the method blocks the thread until the Kafka acknowledges
     * the record.
     *
     * @param topic       the topic to write to
     * @param id          the ID to store the record under
     * @param value       the record to write
     * @see Producer#send(ProducerRecord)
     */
    public void write(Topic topic, Object id, Message value) {
        final int partition = partitionFor(topic, id);
        final Message key = toMessage(id);
        final ProducerRecord<Message, Message> record = new ProducerRecord<>(topic.getName(),
                                                                             partition,
                                                                             key,
                                                                             value);
        final Future<?> ack = producer.send(record);
        if (consistencyLevel == STRONG) {
            try {
                ack.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Writes the given class -> topic mapping to the super-topic (a.k.a. topic of topics).
     *
     * <p>The super-topic contains the mapping of the Entity types to all the topics that store
     * the state of a that Entity types.
     */
    private void writeToSuperTopic(Class<? extends Entity> entityClass, Topic topic) {
        final Topic topicOfTopics = Topics.superTopic(entityClass);
        final StringValue topicValue = StringValue.newBuilder()
                                                  .setValue(topic.getName())
                                                  .build();
        final ProducerRecord<Message, Message> record =
                new ProducerRecord<>(topicOfTopics.getName(), topicValue, topicValue);
        producer.send(record);
    }

    private void ensureSubscriptionOnto(TopicPartition topicPartition) {
        final Collection<TopicPartition> assignment = consumer.assignment();
        final Collection<TopicPartition> activeAssignment = singleton(topicPartition);
        if (!assignment.contains(topicPartition)) {
            consumer.unsubscribe();
            consumer.assign(activeAssignment);
        }
        consumer.seekToBeginning(activeAssignment);
    }

    private void ensureSubscriptionOnto(Topic topic) {
        final String topicName = topic.getName();
        final Collection<String> subscription = consumer.subscription();
        if (!subscription.contains(topicName)) {
            consumer.unsubscribe();
            consumer.subscribe(singleton(topicName));
        }
        final Collection<TopicPartition> partitions =
                producer.partitionsFor(topicName)
                        .stream()
                        .map(info -> new TopicPartition(info.topic(), info.partition()))
                        .collect(toSet());
        consumer.poll(0);
        consumer.seekToBeginning(partitions);
    }

    private int partitionFor(Topic topic, Object id) {
        final int partitionCount = producer.partitionsFor(topic.getName())
                                           .size();
        return Math.abs(id.hashCode()) % partitionCount;
    }

    private Iterable<ConsumerRecord<Message, Message>> doRead(Topic topic) {
        return executeAndPoll(() -> ensureSubscriptionOnto(topic), topic);
    }

    private Iterable<ConsumerRecord<Message, Message>> doRead(Topic topic, int partition) {
        final TopicPartition topicPartition = new TopicPartition(topic.getName(), partition);
        return executeAndPoll(() -> ensureSubscriptionOnto(topicPartition), topic);
    }

    /**
     * Executes the given {@link Runnable} and {@linkplain Consumer#poll(long) polls} the records
     * under the given topic atomically.
     *
     * <p>The runnable typically subscribes the {@link #consumer} onto the required topic or its
     * partition(s).
     *
     * <p>Both the call to Runnable and polling are made in a single sync context.
     */
    private Iterable<ConsumerRecord<Message, Message>> executeAndPoll(Runnable toExecute,
                                                                      Topic toPoll) {
        final ConsumerRecords<Message, Message> all;
        lock.lock();
        try {
            toExecute.run();
            all = consumer.poll(pollAwait);
        } finally {
            lock.unlock();
        }
        final Iterable<ConsumerRecord<Message, Message>> records = all.records(toPoll.getName());
        return records;
    }

    private static Stream<ConsumerRecord<Message, Message>>
    stream(Iterable<ConsumerRecord<Message, Message>> records) {
        return StreamSupport.stream(records.spliterator(), false);
    }

    /**
     * The {@linkplain ConsumerRecord#timestamp() timestamp} comparator for {@link ConsumerRecord}
     * supplier.
     */
    private enum ConsumerRecordComparator implements Supplier<Comparator<ConsumerRecord<?, ?>>> {
        DIRECT {
            @Override
            public Comparator<ConsumerRecord<?, ?>> get() {
                return ConsumerRecordComparator::compareConsRecords;
            }
        };

        private static int compareConsRecords(ConsumerRecord<?, ?> left,
                                              ConsumerRecord<?, ?> right) {
            return compare(right.timestamp(), left.timestamp());
        }
    }
}