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
import org.spine3.examples.todolist.LabelIdsList;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.aggregates.LabelAggregate;
import org.spine3.examples.todolist.c.aggregates.TaskDefinitionPart;
import org.spine3.examples.todolist.c.aggregates.TaskLabelsPart;
import org.spine3.examples.todolist.repositories.LabelAggregateRepository;
import org.spine3.examples.todolist.repositories.TaskDefinitionRepository;
import org.spine3.examples.todolist.repositories.TaskLabelsRepository;
import org.spine3.server.event.EventBus;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("Guava") // as we use Guava Function until we migrate the whole framework to Java 8.
public class EventBusEnricher {

    private static final String REPOSITORY_IS_NOT_INITIALIZED = "Repository is not initialized.";
    private final TodoListRepositoryProvider repositoryProvider;
    private final EventBus eventBus;

    private EventBusEnricher(Builder builder) {
        this.repositoryProvider = builder.repositoryProvider;
        this.eventBus = builder.eventBus;
    }

    void enrich() {
        eventBus.addFieldEnrichment(TaskLabelId.class, LabelDetails.class, labelIdToLabelDetails());
        eventBus.addFieldEnrichment(TaskId.class, TaskDetails.class, taskIdToTaskDetails());
        eventBus.addFieldEnrichment(TaskId.class, LabelIdsList.class, taskIdToLabelList());
        eventBus.addFieldEnrichment(TaskId.class, TaskDefinition.class, taskIdToTaskDefinition());
    }

    private Function<TaskId, TaskDefinition> taskIdToTaskDefinition() {
        final Function<TaskId, TaskDefinition> result = taskId -> {
            if (taskId == null) {
                return TaskDefinition.getDefaultInstance();
            }
            final TaskDefinitionRepository taskDefinitionRepo =
                    repositoryProvider.getTaskDefinitionRepo()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(
                                              REPOSITORY_IS_NOT_INITIALIZED));
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
            final TaskDefinitionRepository taskDefinitionRepo =
                    repositoryProvider.getTaskDefinitionRepo()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(
                                              REPOSITORY_IS_NOT_INITIALIZED));
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
            final TaskLabelsRepository taskLabelsRepo =
                    repositoryProvider.getTaskLabelsRepo()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(
                                              REPOSITORY_IS_NOT_INITIALIZED));
            final TaskLabelsPart aggregate = taskLabelsRepo.load(taskId)
                                                           .get();
            final LabelIdsList state = aggregate.getState()
                                                .getLabelIdsList();
            return state;
        };
        return result;
    }

    private Function<TaskLabelId, LabelDetails> labelIdToLabelDetails() {
        final Function<TaskLabelId, LabelDetails> result = labelId -> {
            if (labelId == null) {
                return LabelDetails.getDefaultInstance();
            }
            final LabelAggregateRepository labelRepository =
                    repositoryProvider.getLabelRepository()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(
                                              REPOSITORY_IS_NOT_INITIALIZED));
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private TodoListRepositoryProvider repositoryProvider;
        private EventBus eventBus;

        private Builder() {
        }

        public Builder setRepositoryProvider(TodoListRepositoryProvider repositoryProvider) {
            this.repositoryProvider = repositoryProvider;
            return this;
        }

        public Builder setEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public EventBusEnricher build() {
            final EventBusEnricher result = new EventBusEnricher(this);
            return result;
        }
    }
}
