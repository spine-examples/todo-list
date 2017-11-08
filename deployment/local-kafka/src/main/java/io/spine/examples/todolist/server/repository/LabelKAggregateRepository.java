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

package io.spine.examples.todolist.server.repository;

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.c.aggregate.LabelAggregate;
import io.spine.server.aggregate.KAggregateRepository;

import java.util.Properties;

/**
 * An {@code AggregateRepository} for {@link LabelAggregate} which performs the message dispatching
 * through Kafka.
 *
 * @author Dmytro Dashenkov
 * @see io.spine.server.aggregate.KAggregateRepository for the detailes on the message
 *      dispatching
 */
public class LabelKAggregateRepository extends KAggregateRepository<LabelId, LabelAggregate> {

    /**
     * @see KAggregateRepository#KAggregateRepository(Properties, Properties)
     */
    public LabelKAggregateRepository(Properties streamConfig, Properties producerConfig) {
        super(streamConfig, producerConfig);
    }
}
