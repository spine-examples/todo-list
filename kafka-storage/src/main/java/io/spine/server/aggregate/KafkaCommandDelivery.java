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

import io.spine.core.CommandEnvelope;

/**
 * An implementation of {@link AggregateCommandDelivery} based on Kafka.
 *
 * <p>The {@link #shouldPostpone(Object, CommandEnvelope)} method always returns {@code true} and
 * {@link KafkaAggregateMessageDispatcher#dispatchMessage dispatches} the given message to Kafka.
 *
 * @author Dmytro Dashenkov
 */
class KafkaCommandDelivery<I, A extends Aggregate<I, ?, ?>>
        extends AggregateCommandDelivery<I, A> {

    private final KafkaAggregateMessageDispatcher<I> dispatcher;

    KafkaCommandDelivery(AggregateRepository<I, A> repository,
                         KafkaAggregateMessageDispatcher<I> dispatcher) {
        super(repository);
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean shouldPostpone(I id, CommandEnvelope envelope) {
        dispatcher.dispatchMessage(id, envelope);
        return true;
    }
}
