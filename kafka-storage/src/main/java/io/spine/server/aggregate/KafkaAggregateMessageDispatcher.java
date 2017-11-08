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

package io.spine.server.aggregate;

import com.google.protobuf.Message;
import io.spine.core.Command;
import io.spine.core.CommandEnvelope;
import io.spine.core.Event;
import io.spine.core.EventEnvelope;
import io.spine.core.MessageEnvelope;
import io.spine.core.Rejection;
import io.spine.core.RejectionEnvelope;
import io.spine.server.storage.kafka.KafkaWrapper;
import io.spine.server.storage.kafka.Topic;
import io.spine.string.Stringifiers;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.server.kafka.KafkaStreamsConfigs.prepareConfig;
import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static io.spine.string.Stringifiers.fromString;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static org.apache.kafka.common.serialization.Serdes.serdeFrom;

/**
 * Dispatches aggregate messages to the target {@code Aggregate} instances through Kafka.
 *
 * @author Dmytro Dashenkov
 */
final class KafkaAggregateMessageDispatcher<I> {

    private final KafkaWrapper kafka;
    private final AggregateRepository<I, ?> repository;
    private final Properties config;
    private final Class<I> idClass;
    private final Topic kafkaTopic;

    private KafkaAggregateMessageDispatcher(Builder<I> builder) {
        this.kafka = checkNotNull(builder.kafka);
        this.repository = checkNotNull(builder.repository);
        final Properties streamProperties = checkNotNull(builder.config);
        this.config = prepareConfig(streamProperties, applicationId());
        this.idClass = checkNotNull(builder.idClass);
        this.kafkaTopic = Topic.forAggregateMessages(repository.getEntityStateType());
    }

    /**
     * Starts the Kafka Streams based Aggregate message dispatching.
     *
     * <p>Starts a Kafka Streams topology which sends all the messages dispatched to a single
     * {@link AggregateRepository} to a single processing instance, where those are applied onto
     * the entities.
     *
     * <p>In other words, if two instances of the application receive two commands to a single
     * {@code Aggregate}, the commands are sent through Kafka to a single instance which dispatches
     * them to the {@code Aggregate}.
     */
    void startDispatching() {
        final StreamsBuilder builder = new StreamsBuilder();
        final Serde<String> stringSerde = Serdes.String();
        final Serde<Message> messageSerde = serdeFrom(serializer(), deserializer());
        final KStream<String, Message> stream = builder.stream(kafkaTopic.getName(),
                                                               Consumed.with(stringSerde,
                                                                             messageSerde));
        buildTopology(stream);
        final KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();
    }

    /**
     * Publishes the given envelope into Kafka.
     *
     * @param id  the ID of the message dispatching target
     * @param msg the envelope to be dispatched
     */
    void dispatchMessage(I id, MessageEnvelope<?, ? extends Message, ?> msg) {
        final String key = idToString(id);
        final Message message = msg.getOuterObject();
        kafka.write(kafkaTopic, key, message);
    }

    private void buildTopology(KStream<String, Message> stream) {
        stream.foreach((key, value) -> doDispatch(toId(key), value));
    }

    @SuppressWarnings({"IfStatementWithTooManyBranches", "ChainOfInstanceofChecks"})
        // OK for this method as we want a *single* processing point for any kind of message.
    private void doDispatch(I id, Message message) {
        if (message instanceof Event) {
            final EventEnvelope envelope = EventEnvelope.of((Event) message);
            repository.getEventEndpointDelivery().deliverNow(id, envelope);
        } else if (message instanceof Rejection) {
            final RejectionEnvelope envelope = RejectionEnvelope.of((Rejection) message);
            repository.getRejectionEndpointDelivery().deliverNow(id, envelope);
        } else if (message instanceof Command) {
            final CommandEnvelope envelope = CommandEnvelope.of((Command) message);
            repository.getCommandEndpointDelivery().deliverNow(id, envelope);
        } else {
            throw newIllegalArgumentException(
                    "Expected Command, Event or Rejection but encountered %s.",
                    message
            );
        }
    }

    private I toId(String idString) {
        return fromString(idString, idClass);
    }

    private String idToString(I id) {
        return Stringifiers.toString(id);
    }

    private String applicationId() {
        return repository.getEntityClass().getName();
    }

    static <I> Builder<I> newBuilder() {
        return new Builder<>();
    }

    static final class Builder<I> {

        private KafkaWrapper kafka;
        private AggregateRepository<I, ?> repository;
        private Properties config;
        private Class<I> idClass;

        private Builder() {
            // Prevent direct instantiation.
        }

        Builder<I> setKafka(KafkaWrapper kafka) {
            this.kafka = checkNotNull(kafka);
            return this;
        }

        Builder<I> setRepository(AggregateRepository<I, ?> repository) {
            this.repository = checkNotNull(repository);
            return this;
        }

        Builder<I> setStreamsConfig(Properties config) {
            this.config = checkNotNull(config);
            return this;
        }

        Builder<I> setIdClass(Class<I> idClass) {
            this.idClass = checkNotNull(idClass);
            return this;
        }

        KafkaAggregateMessageDispatcher<I> build() {
            return new KafkaAggregateMessageDispatcher<>(this);
        }
    }
}
