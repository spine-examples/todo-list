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

import io.spine.annotation.Internal;
import io.spine.core.CommandEnvelope;
import io.spine.core.EventEnvelope;
import io.spine.core.RejectionEnvelope;
import io.spine.server.storage.kafka.KafkaWrapper;

/**
 * An {@link AggregateRepository} that uses Apache Kafka for dispatching the messages to
 * the Aggregates.
 *
 * <p>This interface should only be implemented by
 * the {@linkplain AggregateRepository Aggregate repositories} based on Kafka.
 *
 * <p>Note that all the methods of this interface are {@linkplain Internal internal}. Please use
 * the basic {@link AggregateRepository} API instead.
 *
 * <p>The methods {@code dispatch*Now} dispatch the messages directly to the receiver repository
 * in counterpart to the basic {@link AggregateRepository} API methods which send the messages to
 * Kafka.
 *
 * @author Dmytro Dashenkov
 * @see KAggregateRepository
 * @see KAggregatePartRepository
 */
interface KafkaAggregateRepository {

    /**
     * Dispatches the given {@link CommandEnvelope} to the target Aggregate(-s).
     *
     * @param command the command to be dispatched by this repository
     */
    @Internal
    void dispatchCommandNow(CommandEnvelope command);

    /**
     * Dispatches the given {@link EventEnvelope} to the target Aggregate(-s).
     *
     * @param event the event to be dispatched by this repository
     */
    @Internal
    void dispatchEventNow(EventEnvelope event);

    /**
     * Dispatches the given {@link RejectionEnvelope} to the target Aggregate(-s).
     *
     * @param rejection the rejection to be dispatched by this repository
     */
    @Internal
    void dispatchRejectionNow(RejectionEnvelope rejection);

    /**
     * @return a string identifier of this {@code AggregateRepository} type (defined by the type of
     *         the {@code Aggregate})
     */
    @Internal
    String key();

    /**
     * @return the {@link KafkaWrapper} instance used by this {@code KafkaAggregateRepository}
     */
    @Internal
    KafkaWrapper kafka();
}
