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

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Message;
import io.spine.core.ActorMessageEnvelope;
import io.spine.core.Command;
import io.spine.core.CommandEnvelope;
import io.spine.core.Event;
import io.spine.core.EventEnvelope;
import io.spine.core.MessageEnvelope;
import io.spine.core.Rejection;
import io.spine.core.RejectionEnvelope;
import io.spine.server.storage.kafka.Topic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.server.kafka.KafkaStreamsConfigs.prepareConfig;
import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static org.apache.kafka.common.serialization.Serdes.serdeFrom;

/**
 * Sends aggregate messages to the target {@code Aggregate} instances through Kafka.
 *
 * @param <I> the type of the aggregate IDs to which the messages are dispatched
 * @author Dmytro Dashenkov
 */
final class KafkaAggregateMessageBroker<I> {

    private static final Serde<Message> messageSerde = serdeFrom(serializer(), deserializer());
    private static final Map<Class<?>, Serde<?>> primitiveSerdes = ImmutableMap.of(
            String.class, Serdes.String(),
            Long.class, Serdes.Long(),
            Integer.class, Serdes.Integer()
    );

    private final KafkaProducer<I, Message> kafkaProducer;
    private final Properties kafkaStreamsConfig;
    private final Topic kafkaTopic;
    private final Serde<I> idSerde;
    private final Courier<I> courier;

    private KafkaAggregateMessageBroker(Builder<I> builder) {
        this.idSerde = idSerde(builder.idClass);
        this.kafkaProducer = createProducer(builder.kafkaProducerConfig, idSerde);
        final AggregateRepository<I, ?> repository = builder.repository;
        this.kafkaStreamsConfig = prepareConfig(builder.kafkaStreamsConfig,
                                                applicationId(repository));
        this.kafkaTopic = Topic.forAggregateMessages(repository.getEntityStateType());
        this.courier = new Courier<>(repository);
    }

    private static <I> KafkaProducer<I, Message> createProducer(Properties config,
                                                                Serde<I> idSerde) {
        final KafkaProducer<I, Message> producer = new KafkaProducer<>(config,
                                                                       idSerde.serializer(),
                                                                       serializer());
        return producer;
    }

    @SuppressWarnings("unchecked") // Logically checked.
    private static <I> Serde<I> idSerde(Class<I> idClass) {
        final Serde<I> idSerde = (Serde<I>) primitiveSerdes.getOrDefault(idClass, messageSerde);
        return idSerde;
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
        final KStream<I, Message> stream = builder.stream(kafkaTopic.getName(),
                                                               Consumed.with(idSerde,
                                                                             messageSerde));
        buildTopology(stream);
        final KafkaStreams streams = new KafkaStreams(builder.build(), kafkaStreamsConfig);
        streams.start();
    }

    /**
     * Publishes the given envelope into Kafka.
     *
     * @param receiverId the ID of the message dispatching target
     * @param envelope   the envelope to be dispatched
     */
    void sendMessage(I receiverId, MessageEnvelope<?, ? extends Message, ?> envelope) {
        final Message message = envelope.getOuterObject();
        final ProducerRecord<I, Message> record = new ProducerRecord<>(kafkaTopic.getName(),
                                                                       receiverId, message);
        kafkaProducer.send(record);
    }

    private void buildTopology(KStream<I, Message> stream) {
        stream.foreach(courier::deliver);
    }

    private static String applicationId(AggregateRepository<?, ?> repository) {
        return repository.getEntityClass().getName();
    }

    /**
     * Performs delivery of a given aggregate message to a specified receiver with
     * the {@link AggregateEndpointDelivery} from the given {@code AggregateRepository}.
     *
     * @param <I> the type of the {@code Aggregate} ID that receives the messages
     */
    private static class Courier<I> {

        /**
         * A map of {@code Class<T extends Message>} to lambda converting the message of type
         * {@code T} to a corresponding {@link MessageEnvelope}.
         */
        private static final
        Map<Class<? extends Message>,
            Function<? extends Message, ? extends MessageEnvelope<?, ?, ?>>> messageWrappers =
                ImmutableMap.of(
                        Command.class, message -> CommandEnvelope.of((Command) message),
                        Event.class, message -> EventEnvelope.of((Event) message),
                        Rejection.class, message -> RejectionEnvelope.of((Rejection) message)
                );

        /**
         * A map of {@code Class<T extends Message>} to {@link AggregateEndpointDelivery} instance
         * for type {@code T}.
         */
        private final Map<Class<? extends Message>, AggregateEndpointDelivery<?, ?, ?>> deliveries;

        private Courier(AggregateRepository<I, ?> repository) {
            this.deliveries = ImmutableMap.of(
                    Command.class, repository.getCommandEndpointDelivery(),
                    Event.class, repository.getEventEndpointDelivery(),
                    Rejection.class, repository.getRejectionEndpointDelivery()
            );
        }

        /**
         * Retrieves a {@linkplain BiConsumer} function of {@code receiverId} and {@code message}
         * that performs the {@code message} delivery.
         *
         * @param cls the {@code Class} of the message to deliver
         * @param <M> the type of the message to deliver
         * @param <E> the type of the {@linkplain MessageEnvelope envelope} for the given message
         *            type
         * @return a {@link BiConsumer} delivering the given {@code message} to the receiver with
         *         the given ID
         * @see #deliver(Object, Message)
         */
        // "unchecked" warnings suppressed due to internal invariants of `Courier`.
        private <M extends Message, E extends ActorMessageEnvelope<?, M, ?>> BiConsumer<I, Message>
        deliveryAction(Class<M> cls) {
            @SuppressWarnings("unchecked")
            final AggregateEndpointDelivery<I, ?, E> delivery =
                    (AggregateEndpointDelivery<I, ?, E>) deliveries.get(cls);
            checkArgument(delivery != null,
                          "Expected Command, Event or Rejection but encountered %s.",
                          cls.getName());
            @SuppressWarnings("unchecked")
            final Function<M, E> wrapper = (Function<M, E>) messageWrappers.get(cls);
            return (id, message) -> {
                @SuppressWarnings("unchecked")
                final M msg = (M) message;
                final E envelope = wrapper.apply(msg);
                delivery.deliverNow(id, envelope);
            };
        }

        /**
         * Delivers the given {@code message} to the receiver specified by the {@code receiverId}.
         *
         * @param receiverId the receiver to deliver the {@code message} to
         * @param message    the message to deliver
         */
        private void deliver(I receiverId, Message message) {
            final Class<? extends Message> messageClass = message.getClass();
            deliveryAction(messageClass).accept(receiverId, message);
        }
    }

    static <I> Builder<I> newBuilder() {
        return new Builder<>();
    }

    /**
     * A builder for the {@code KafkaAggregateMessageBroker} instances.
     *
     * @param <I> the type parameter of the resulting {@code KafkaAggregateMessageBroker}
     */
    static final class Builder<I> {

        private Properties kafkaProducerConfig;
        private AggregateRepository<I, ?> repository;
        private Properties kafkaStreamsConfig;
        private Class<I> idClass;

        private Builder() {
            // Prevent direct instantiation.
        }

        /**
         * @param kafkaProducerConfig the Kafka Producer configuration to build
         *                            {@link KafkaProducer} instances with
         * @return self for method chaining
         */
        Builder<I> setKafkaProducerConfig(Properties kafkaProducerConfig) {
            this.kafkaProducerConfig = checkNotNull(kafkaProducerConfig);
            return this;
        }

        /**
         * @param kafkaStreamsConfig the Kafka Streams config to start the Streams topology with;
         *                           {@code bootstrap.servers} is the only required property for
         *                           the config
         * @return self for method chaining
         */
        Builder<I> setKafkaStreamsConfig(Properties kafkaStreamsConfig) {
            this.kafkaStreamsConfig = checkNotNull(kafkaStreamsConfig);
            return this;
        }

        /**
         * @param repository the {@link AggregateRepository} to dispatch the messages with
         * @return self for method chaining
         */
        Builder<I> setRepository(AggregateRepository<I, ?> repository) {
            this.repository = checkNotNull(repository);
            return this;
        }

        /**
         * @param idClass the class of ID of Aggregates which are the dispatching target
         * @return self for method chaining
         */
        Builder<I> setIdClass(Class<I> idClass) {
            this.idClass = checkNotNull(idClass);
            return this;
        }

        /**
         * Creates a new instance of {@code KafkaAggregateMessageBroker}.
         *
         * @return new instance of {@code KafkaAggregateMessageBroker} with the given
         *         parameters
         */
        KafkaAggregateMessageBroker<I> build() {
            checkNotNull(kafkaProducerConfig);
            checkNotNull(repository);
            checkNotNull(kafkaProducerConfig);
            checkNotNull(idClass);
            return new KafkaAggregateMessageBroker<>(this);
        }
    }
}
