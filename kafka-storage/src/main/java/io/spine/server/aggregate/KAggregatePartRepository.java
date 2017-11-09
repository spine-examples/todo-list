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
 * An {@link AggregatePartRepository} which applies the dispatched messages through a Kafka Streams
 * topology.
 *
 * <p>This class plays the same role to {@link AggregatePart} as the {@link KAggregateRepository}
 * to {@link Aggregate}.
 *
 * @author Dmytro Dashenkov
 * @see KAggregateRepository for further description of the Kafka-based repositories behavior
 */
public abstract class KAggregatePartRepository<I,
                                               A extends AggregatePart<I, ?, ?, R>,
                                               R extends AggregateRoot<I>>
        extends AggregatePartRepository<I, A, R> {

    private final KafkaAggregateMessageBroker<I> broker;

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
    protected KAggregatePartRepository(Properties streamConfig, Properties producerConfig) {
        super();
        this.broker = KafkaAggregateMessageBroker.<I>newBuilder()
                                                         .setRepository(this)
                                                         .setIdClass(getIdClass())
                                                         .setKafkaProducerConfig(producerConfig)
                                                         .setKafkaStreamsConfig(streamConfig)
                                                         .build();
        this.commandDelivery = new KafkaCommandDelivery<>(this, broker);
        this.eventDelivery = new KafkaEventDelivery<>(this, broker);
        this.rejectionDelivery = new KafkaRejectionDelivery<>(this, broker);
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@code KAggregatePartRepository.onRegistered()} also starts the Kafka streams processing
     * topology.
     */
    @OverridingMethodsMustInvokeSuper
    @Override
    public void onRegistered() {
        super.onRegistered();
        broker.start();
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
