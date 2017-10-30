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

import com.google.common.base.Optional;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.c.aggregate.LabelAggregate;
import io.spine.examples.todolist.server.KafkaBoundedContextFactory;
import io.spine.server.BoundedContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author Dmytro Dashenkov
 */
@DisplayName("KAggregateRepository should")
class KAggregateRepositoryTest {

    private static AggregateRepository<LabelId, LabelAggregate> repository = null;

    @BeforeAll
    static void setUp() {
        final BoundedContext boundedContext = KafkaBoundedContextFactory.instance().create();
        @SuppressWarnings({
                "unchecked", // Logically checked
                "Guava" // Spine Java 7 API
        })
        final Optional<? extends AggregateRepository<LabelId, LabelAggregate>> repo =
                (Optional<? extends AggregateRepository<LabelId, LabelAggregate>>)
                        boundedContext.findRepository(TaskLabel.class);
        checkState(repo.isPresent());
        repository = repo.get();
    }

    @DisplayName("apply events onto an Aggregate")
    @Test
    void testEventsApplying() {

    }
}
