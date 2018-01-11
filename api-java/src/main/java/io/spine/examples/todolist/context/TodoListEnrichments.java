/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.examples.todolist.context;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.aggregate.TaskAggregate;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.server.event.EventBus;
import io.spine.server.event.EventEnricher;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Serves as class which adds enrichment fields to the {@link EventBus}.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("Guava") // Because com.google.common.base.Function is used
                           // until the migration of Spine to Java 8 is performed.
public class TodoListEnrichments {

    private final TaskRepository taskRepo;

    private TodoListEnrichments(Builder builder) {
        this.taskRepo = builder.taskRepo;
    }

    EventEnricher createEnricher() {
        final EventEnricher enricher =
                EventEnricher.newBuilder()
                             .add(TaskId.class, Task.class, taskIdToTask())
                             .build();
        return enricher;
    }

    private Function<TaskId, Task> taskIdToTask() {
        final Function<TaskId, Task> result = taskId -> {
            if (taskId == null) {
                return Task.getDefaultInstance();
            }
            final Optional<TaskAggregate> aggregate = taskRepo.find(taskId);
            if (!aggregate.isPresent()) {
                return Task.getDefaultInstance();
            }
            final Task task = aggregate.get()
                                       .getState();
            return task;
        };
        return result;
    }

    /**
     * Creates a new builder for (@code TodoListEnrichments).
     *
     * @return new builder instance
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for {@code EventEnricherSupplier} instances.
     */
    public static class Builder {

        private TaskRepository taskRepo;

        private Builder() {
        }

        public Builder setTaskRepository(TaskRepository definitionRepository) {
            checkNotNull(definitionRepository);
            this.taskRepo = definitionRepository;
            return this;
        }

        public TodoListEnrichments build() {
            return new TodoListEnrichments(this);
        }
    }
}
