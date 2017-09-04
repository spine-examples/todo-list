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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.server.storage.kafka.Consistency.STRONG;

/**
 * @author Dmytro Dashenkov
 */
public class KafkaWrapper {

    private static final long STANDARD_POLL_AWAIT = 100L;

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
        final Iterable<ConsumerRecord<Message, Message>> records = all.records(topic.name());
        final Optional<Message> lastMsg = StreamSupport.stream(records.spliterator(), false)
                                                       .reduce((left, right) -> right)
                                                       .map(ConsumerRecord::value);
        @SuppressWarnings("unchecked") // Expect caller to be aware of the type.
        final Optional<M> result = (Optional<M>) lastMsg;
        return result;
    }

    private void ensureSubscriptionOnto(Topic topic) {
        final String topicName = topic.name();
        final Set<String> subscription = consumer.subscription();
        if (!subscription.contains(topicName)) {
            final Collection<String> newSubscription = newLinkedList(subscription);
            newSubscription.add(topicName);
            consumer.subscribe(newSubscription);
        }
    }

    public <M extends Message> Iterator<M> read(Topic topic) {
        ensureSubscriptionOnto(topic);
        final ConsumerRecords<Message, Message> all = consumer.poll(STANDARD_POLL_AWAIT);
        final Iterable<ConsumerRecord<Message, Message>> records = all.records(topic.name());
        final Iterator<Message> messages = StreamSupport.stream(records.spliterator(), false)
                                                        .map(ConsumerRecord::value)
                                                        .collect(Collectors.toList())
                                                        .iterator();
        @SuppressWarnings("unchecked") // Expect caller to be aware of the type.
        final Iterator<M> result = (Iterator<M>) messages;
        return result;
    }

    public void write(Topic topic, Object id, Message value) {
        final Message key = toMessage(id);
        final ProducerRecord<Message, Message> record = new ProducerRecord<>(topic.name(),
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
}
