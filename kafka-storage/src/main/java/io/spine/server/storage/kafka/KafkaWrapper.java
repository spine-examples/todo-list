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

import com.google.common.collect.Iterators;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.server.entity.Entity;
import io.spine.server.entity.EntityRecord;
import io.spine.server.entity.LifecycleFlags;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.server.storage.kafka.Consistency.STRONG;
import static java.lang.Long.compare;

/**
 * @author Dmytro Dashenkov
 */
public class KafkaWrapper {

    private static final long STANDARD_POLL_AWAIT = 5000L;

    private final KafkaProducer<Message, Message> producer;
    private final KafkaConsumer<Message, Message> consumer;
    private final Consistency consistencyLevel;

    public KafkaWrapper(KafkaProducer<Message, Message> producer,
                        KafkaConsumer<Message, Message> consumer,
                        Consistency consistencyLevel) {
        this.producer = producer;
        this.consumer = consumer;
        this.consistencyLevel = consistencyLevel;
    }

    public <M extends Message> Optional<M> readLast(Topic topic) {
        ensureSubscriptionOnto(topic);
        final ConsumerRecords<Message, Message> all = consumer.poll(STANDARD_POLL_AWAIT);
        final Iterable<ConsumerRecord<Message, Message>> records = all.records(topic.getName());
        final Optional<Message> lastMsg = StreamSupport.stream(records.spliterator(), false)
                                                       .reduce((left, right) -> right)
                                                       .map(ConsumerRecord::value);
        @SuppressWarnings("unchecked") // Expect caller to be aware of the type.
        final Optional<M> result = (Optional<M>) lastMsg;
        return result;
    }

    private void ensureSubscriptionOnto(Topic topic) {
        final String topicName = topic.getName();
        final Set<String> subscription = consumer.subscription();
        if (!subscription.contains(topicName)) {
            final Collection<String> newSubscription = newLinkedList(subscription);
            newSubscription.add(topicName);
            consumer.subscribe(newSubscription);
            consumer.poll(0);
            consumer.seekToBeginning(consumer.assignment());
        }
    }

    public <M extends Message> Iterator<M> read(Topic topic) {
        ensureSubscriptionOnto(topic);
        final ConsumerRecords<Message, Message> all = consumer.poll(STANDARD_POLL_AWAIT);
        final Iterable<ConsumerRecord<Message, Message>> records = all.records(topic.getName());
        final Iterator<Message> messages = StreamSupport.stream(records.spliterator(), false)
                                                        .map(ConsumerRecord::value)
                                                        .filter(active())
                                                        .iterator();
        @SuppressWarnings("unchecked") // Expect caller to be aware of the type.
        final Iterator<M> result = (Iterator<M>) messages;
        return result;
    }

    public <M extends Message> Iterator<M> readAll(Class<? extends Entity> type) {
        final Topic topicOfTopics = Topic.forType(type);
        ensureSubscriptionOnto(topicOfTopics);
        final Iterable<ConsumerRecord<Message, Message>> records =
                consumer.poll(STANDARD_POLL_AWAIT)
                        .records(topicOfTopics.getName());
        final Iterator<M> result = StreamSupport.stream(records.spliterator(), false)
                                                .map(ConsumerRecord::value)
                                                .distinct()
                                                .map(msg -> ((StringValue) msg).getValue())
                                                .map(Topic::ofValue)
                                                .map(this::read)
                                                .collect(Collections::emptyIterator,
                                                         Iterators::concat,
                                                         Iterators::concat);
        return result;
    }

    public <M extends Message> Optional<M> read(Topic topic, Object id) {
        final Message key = toMessage(id);
        ensureSubscriptionOnto(topic);
        final ConsumerRecords<Message, Message> all = consumer.poll(STANDARD_POLL_AWAIT);
        final Iterable<ConsumerRecord<Message, Message>> records = all.records(topic.getName());
        final Optional<Message> message = StreamSupport.stream(records.spliterator(), false)
                                                       .filter(record -> key.equals(record.key()))
                                                       .sorted(KafkaWrapper::compareConsRecords)
                                                       .map(ConsumerRecord::value)
                                                       .findFirst();
        @SuppressWarnings("unchecked") // Expect caller to be aware of the type.
        final Optional<M> result = (Optional<M>) message;
        return result;
    }

    public void write(Class<? extends Entity> entityClass, Topic topic, Object id, Message value) {
        writeToSuperTopic(entityClass, topic);
        final Message key = toMessage(id);
        final ProducerRecord<Message, Message> record = new ProducerRecord<>(topic.getName(),
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
                new ProducerRecord<>(topicOfTopics.getName(), topicValue);
        producer.send(record);
    }

    private static int compareConsRecords(ConsumerRecord<?, ?> left,
                                          ConsumerRecord<?, ?> right) {
        return compare(right.timestamp(), left.timestamp());
    }

    private static Predicate<Message> active() {
        return record -> {
            final LifecycleFlags flags;
            if (record instanceof EntityRecord) {
                flags = ((EntityRecord) record).getLifecycleFlags();
                return !(flags.getArchived() && flags.getDeleted());
            } else {
                return true;
            }
        };
    }
}
