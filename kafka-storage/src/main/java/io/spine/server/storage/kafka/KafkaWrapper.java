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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
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
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.server.storage.kafka.Consistency.STRONG;
import static java.lang.Long.compare;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

/**
 * @author Dmytro Dashenkov
 */
public class KafkaWrapper {

    private final KafkaProducer<Message, Message> producer;
    private final KafkaConsumer<Message, Message> consumer;
    private final Consistency consistencyLevel;
    private final long pollAwait;

    // TODO:2017-09-14:dmytro.dashenkov: Should be fair?
    private final Lock lock = new ReentrantLock();

    public KafkaWrapper(KafkaProducer<Message, Message> producer,
                        KafkaConsumer<Message, Message> consumer,
                        Consistency consistencyLevel,
                        Duration maxPollAwait) {
        this.producer = producer;
        this.consumer = consumer;
        this.consistencyLevel = consistencyLevel;
        checkArgument(!maxPollAwait.isNegative() && !maxPollAwait.isZero(),
                      "Max poll await must be a positive Duration.");
        this.pollAwait = maxPollAwait.toMillis();
    }

    public <M extends Message> Optional<M> readLast(Topic topic) {
        final Iterable<ConsumerRecord<Message, Message>> records = doRead(topic);
        final Optional<Message> lastMsg = stream(records)
                .max(ConsumerRecordComparator.DIRECT.get())
                .map(ConsumerRecord::value);
        @SuppressWarnings("unchecked") // Expect caller to be aware of the type.
        final Optional<M> result = (Optional<M>) lastMsg;
        return result;
    }

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

    public <M extends Message> Iterator<M> readAll(Class<? extends Entity> type) {
        final Topic topicOfTopics = Topic.forType(type);
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

    public void write(Class<? extends Entity> entityClass, Topic topic, Object id, Message value) {
        writeToSuperTopic(entityClass, topic);
        write(topic, id, value);
    }

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

    private void writeToSuperTopic(Class<? extends Entity> entityClass, Topic topic) {
        final Topic topicOfTopics = Topic.forType(entityClass);
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
