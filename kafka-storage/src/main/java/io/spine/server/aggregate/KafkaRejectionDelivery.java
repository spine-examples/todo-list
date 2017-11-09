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

import io.spine.core.RejectionEnvelope;

/**
 * An implementation of {@link AggregateRejectionDelivery} based on Kafka.
 *
 * <p>The {@link #shouldPostpone(Object, RejectionEnvelope)} method always returns {@code true} and
 * {@link KafkaAggregateMessageBroker#sendMessage dispatches} the given message to Kafka.
 *
 * @author Dmytro Dashenkov
 */
final class KafkaRejectionDelivery<I, A extends Aggregate<I, ?, ?>>
        extends AggregateRejectionDelivery<I, A> {

    private final KafkaAggregateMessageBroker<I> broker;

    KafkaRejectionDelivery(AggregateRepository<I, A> repository,
                           KafkaAggregateMessageBroker<I> broker) {
        super(repository);
        this.broker = broker;
    }

    @Override
    public boolean shouldPostpone(I id, RejectionEnvelope envelope) {
        broker.sendMessage(id, envelope);
        return true;
    }
}
