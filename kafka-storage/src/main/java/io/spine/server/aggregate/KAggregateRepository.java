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

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Properties;

/**
 * An {@link AggregateRepository} which dispatches messages through a Kafka Streams topology.
 *
 * <p>Each subclass of {@code KAggregateRepository} starts a Kafka Streams processor upon
 * {@linkplain #onRegistered() registration} in a {@code BoundedContext}.
 *
 * <p>To enable the Kafka-based message dispatching, extend {@code KAggregateRepository} instead of
 * extending {@code AggregateRepository} directly.
 *
 * <p>All the commands, events and rejections dispatched to the repository are published into
 * a dedicated Kafka topic and then consumed by the repository itself. The Kafka processing
 * topology for this topic is built in a way that all the messages targeting the same
 * {@code Aggregate} instance are consumed by the same processor instance. Thereby no race
 * conditions happen when several messages targeting a single {@code Aggregate} are sent to
 * the system simultaneously.
 *
 * <p>The Kafka topic serving for this repository has the name of the Aggregate type name.
 * More formally, the name is equal to {@code repository.getEntityStateType().getTypeName()}, where
 * {@code repository} is an instance of {@code KAggregateRepository}. It's recommended that
 * the topic with such mane exists before the application start. It should have at least as many
 * partitions as there are instances of this specific type of {@code KAggregateRepository} in
 * the system. Also, consider having several replicas of the topic (i.e. set
 * {@code replication-factor} to a number greater than 1).
 *
 * <p>Note: once you define the number of partitions for Kafka topic, do not change it. This
 * constraint is connected to the way records are distributed among the Kafka topic partitions.
 * Once the partition number is changed, the instance applying the messages may also change,
 * causing race conditions and inconsistent states.
 *
 * @param <I> the type of the aggregate IDs
 * @param <A> the type of the aggregates managed by this repository
 * @author Dmytro Dashenkov
 * @see AggregateRepository for the detailed description of the Aggregate Repositories
 */
public abstract class KAggregateRepository<I, A extends Aggregate<I, ?, ?>>
        extends AggregateRepository<I, A> {

    private final KafkaAggregateMessageBroker<I> dispatcher;

    private final AggregateCommandDelivery<I, A> commandDelivery;
    private final AggregateEventDelivery<I, A> eventDelivery;
    private final AggregateRejectionDelivery<I, A> rejectionDelivery;

    /**
     * Creates a new instance of {@code KAggregateRepository}.
     *
     * @param streamConfig   the Kafka Streams configuration containing {@code bootstrap.servers}
     *                       property and (optionally) other Streams configs
     * @param producerConfig the Kafka Producer configuration
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction") // OK since the whole control
    protected KAggregateRepository(Properties streamConfig, Properties producerConfig) {
        super();
        this.dispatcher = KafkaAggregateMessageBroker.<I>newBuilder()
                                                         .setRepository(this)
                                                         .setIdClass(getIdClass())
                                                         .setKafkaProducerConfig(producerConfig)
                                                         .setKafkaStreamsConfig(streamConfig)
                                                         .build();
        this.commandDelivery = new KafkaCommandDelivery<>(this, dispatcher);
        this.eventDelivery = new KafkaEventDelivery<>(this, dispatcher);
        this.rejectionDelivery = new KafkaRejectionDelivery<>(this, dispatcher);
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@code KAggregateRepository.onRegistered()} also starts the Kafka streams processing
     * topology.
     */
    @OverridingMethodsMustInvokeSuper
    @Override
    public void onRegistered() {
        super.onRegistered();
        dispatcher.startDispatching();
    }

    @Override
    protected final AggregateCommandDelivery<I, A> getCommandEndpointDelivery() {
        return commandDelivery;
    }

    @Override
    protected final AggregateEventDelivery<I, A> getEventEndpointDelivery() {
        return eventDelivery;
    }

    @Override
    protected final AggregateRejectionDelivery<I, A> getRejectionEndpointDelivery() {
        return rejectionDelivery;
    }
}
