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

package org.spine3.examples.todolist.context;

import com.google.common.base.Function;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.LabelIdsList;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.c.aggregate.LabelAggregate;
import org.spine3.examples.todolist.c.aggregate.TaskDefinitionPart;
import org.spine3.examples.todolist.c.aggregate.TaskLabelsPart;
import org.spine3.examples.todolist.repository.LabelAggregateRepository;
import org.spine3.examples.todolist.repository.TaskDefinitionRepository;
import org.spine3.examples.todolist.repository.TaskLabelsRepository;
import org.spine3.server.event.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Serves as class which adds enrichment fields to the {@link EventBus}.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("Guava") // because com.google.common.base.Function is used
                           // until the migration of Spine to Java 8 is performed.
public class TodoListEventEnricher {

    private final TaskDefinitionRepository taskDefinitionRepo;
    private final TaskLabelsRepository taskLabelsRepo;
    private final LabelAggregateRepository labelRepository;
    private final EventBus eventBus;

    private TodoListEventEnricher(Builder builder) {
        this.taskDefinitionRepo = builder.taskDefinitionRepo;
        this.taskLabelsRepo = builder.taskLabelsRepo;
        this.labelRepository = builder.labelRepository;
        this.eventBus = builder.eventBus;
    }

    /**
     * Adds enrichment fields to the {@link EventBus}.
     */
    void addEnrichmentFields() {
        eventBus.addFieldEnrichment(LabelId.class, LabelDetails.class, labelIdToLabelDetails());
        eventBus.addFieldEnrichment(TaskId.class, TaskDetails.class, taskIdToTaskDetails());
        eventBus.addFieldEnrichment(TaskId.class, LabelIdsList.class, taskIdToLabelList());
        eventBus.addFieldEnrichment(TaskId.class, TaskDefinition.class, taskIdToTaskDefinition());
    }

    private Function<TaskId, TaskDefinition> taskIdToTaskDefinition() {
        final Function<TaskId, TaskDefinition> result = taskId -> {
            if (taskId == null) {
                return TaskDefinition.getDefaultInstance();
            }
            final TaskDefinitionPart aggregate = taskDefinitionRepo.load(taskId)
                                                                   .get();
            final TaskDefinition taskDefinition = aggregate.getState();
            return taskDefinition;
        };
        return result;
    }

    private Function<TaskId, TaskDetails> taskIdToTaskDetails() {
        final Function<TaskId, TaskDetails> result = taskId -> {
            if (taskId == null) {
                return TaskDetails.getDefaultInstance();
            }
            final TaskDefinitionPart aggregate = taskDefinitionRepo.load(taskId)
                                                                   .get();
            final TaskDefinition state = aggregate.getState();
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
            final TaskLabelsPart aggregate = taskLabelsRepo.load(taskId)
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
            final LabelAggregate aggregate = labelRepository.load(labelId)
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
     * Creates a new builder for (@code TodoListEventEnricher).
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

        private TaskDefinitionRepository taskDefinitionRepo;
        private TaskLabelsRepository taskLabelsRepo;
        private LabelAggregateRepository labelRepository;
        private EventBus eventBus;

        private Builder() {
        }

        public Builder setTaskDefinitionRepository(TaskDefinitionRepository definitionRepository) {
            checkNotNull(definitionRepository);
            this.taskDefinitionRepo = definitionRepository;
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

        public Builder setEventBus(EventBus eventBus) {
            checkNotNull(eventBus);
            this.eventBus = eventBus;
            return this;
        }

        public TodoListEventEnricher build() {
            final TodoListEventEnricher result = new TodoListEventEnricher(this);
            return result;
        }
    }
}
