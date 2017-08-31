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

package io.spine.examples.todolist.context;

import com.google.common.base.Function;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.LabelIdsList;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.c.aggregate.LabelAggregate;
import io.spine.examples.todolist.c.aggregate.TaskLabelsPart;
import io.spine.examples.todolist.c.aggregate.TaskPart;
import io.spine.examples.todolist.repository.LabelAggregateRepository;
import io.spine.examples.todolist.repository.TaskLabelsRepository;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.server.event.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Serves as class which adds enrichment fields to the {@link EventBus}.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("Guava") // Because com.google.common.base.Function is used
                           // until the migration of Spine to Java 8 is performed.
public class TodoListEnrichmentConfiguration {

    private final TaskRepository taskRepo;
    private final TaskLabelsRepository taskLabelsRepo;
    private final LabelAggregateRepository labelRepository;
    private final EventBus eventBus;

    private TodoListEnrichmentConfiguration(Builder builder) {
        this.taskRepo = builder.taskRepo;
        this.taskLabelsRepo = builder.taskLabelsRepo;
        this.labelRepository = builder.labelRepository;
        this.eventBus = builder.eventBus;
    }

    /**
     * Adds enrichment fields to the {@link EventBus}.
     */
    void addEnrichmentFields() {
//        eventBus.addFieldEnrichment(LabelId.class, LabelDetails.class, labelIdToLabelDetails());
//        eventBus.addFieldEnrichment(TaskId.class, TaskDetails.class, taskIdToTaskDetails());
//        eventBus.addFieldEnrichment(TaskId.class, LabelIdsList.class, taskIdToLabelList());
//        eventBus.addFieldEnrichment(TaskId.class, Task.class, taskIdToTask());
    }

    private Function<TaskId, Task> taskIdToTask() {
        final Function<TaskId, Task> result = taskId -> {
            if (taskId == null) {
                return Task.getDefaultInstance();
            }
            final TaskPart aggregate = taskRepo.find(taskId)
                                               .get();
            final Task task = aggregate.getState();
            return task;
        };
        return result;
    }

    private Function<TaskId, TaskDetails> taskIdToTaskDetails() {
        final Function<TaskId, TaskDetails> result = taskId -> {
            if (taskId == null) {
                return TaskDetails.getDefaultInstance();
            }
            final TaskPart aggregate = taskRepo.find(taskId)
                                               .get();
            final Task state = aggregate.getState();
            final TaskDetails details = TaskDetails.newBuilder()
                                                   .setDescription(state.getDescription())
                                                   .setPriority(state.getPriority())
                                                   .build();
            return details;
        };

        return result;
    }

    private Function<TaskId, LabelIdsList> taskIdToLabelList() {
        final Function<TaskId, LabelIdsList> result = taskId -> {
            if (taskId == null) {
                return LabelIdsList.getDefaultInstance();
            }
            final TaskLabelsPart aggregate = taskLabelsRepo.find(taskId)
                                                           .get();
            final LabelIdsList state = aggregate.getState()
                                                .getLabelIdsList();
            return state;
        };
        return result;
    }

    private Function<LabelId, LabelDetails> labelIdToLabelDetails() {
        final Function<LabelId, LabelDetails> result = labelId -> {
            if (labelId == null) {
                return LabelDetails.getDefaultInstance();
            }
            final LabelAggregate aggregate = labelRepository.find(labelId)
                                                            .get();
            final TaskLabel state = aggregate.getState();
            final LabelDetails labelDetails = LabelDetails.newBuilder()
                                                          .setColor(state.getColor())
                                                          .setTitle(state.getTitle())
                                                          .build();
            return labelDetails;
        };
        return result;
    }

    /**
     * Creates a new builder for (@code TodoListEnrichmentConfiguration).
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
        private TaskLabelsRepository taskLabelsRepo;
        private LabelAggregateRepository labelRepository;
        private EventBus eventBus;

        private Builder() {
        }

        public Builder setTaskRepository(TaskRepository definitionRepository) {
            checkNotNull(definitionRepository);
            this.taskRepo = definitionRepository;
            return this;
        }

        public Builder setTaskLabelsRepository(TaskLabelsRepository taskLabelsRepository) {
            checkNotNull(taskLabelsRepository);
            this.taskLabelsRepo = taskLabelsRepository;
            return this;
        }

        public Builder setLabelRepository(LabelAggregateRepository labelRepository) {
            checkNotNull(labelRepository);
            this.labelRepository = labelRepository;
            return this;
        }

        public TodoListEnrichmentConfiguration apply(EventBus eventBus) {
            checkNotNull(eventBus);
            this.eventBus = eventBus;
            final TodoListEnrichmentConfiguration result =
                    new TodoListEnrichmentConfiguration(this);
            return result;
        }
    }
}
