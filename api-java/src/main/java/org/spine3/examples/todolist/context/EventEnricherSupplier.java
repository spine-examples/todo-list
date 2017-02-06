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

import org.spine3.examples.todolist.LabelsList;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.c.aggregates.TaskDefinitionPart;
import org.spine3.examples.todolist.c.aggregates.TaskLabelsPart;
import org.spine3.examples.todolist.repositories.TaskDefinitionRepository;
import org.spine3.examples.todolist.repositories.TaskLabelsRepository;
import org.spine3.server.BoundedContext;
import org.spine3.server.event.enrich.EventEnricher;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Performs all necessary initializations to provide the ready-to-use {@link EventEnricher}.
 *
 * @author Illia Shepilov
 */
public class EventEnricherSupplier implements Supplier<EventEnricher> {

    private static final String REPOSITORY_IS_NOT_INITIALIZED = "Repository is not initialized.";
    private final TodoListRepositoryProvider repositoryProvider;

    private EventEnricherSupplier(Builder builder) {
        this.repositoryProvider = builder.repositoryProvider;
    }

    @Override
    public EventEnricher get() {
        final Function<TaskId, TaskDefinition> taskIdToTaskDefinition = initTaskIdToTaskFunction();
        final Function<TaskId, TaskDetails> taskIdToTaskDetails = initTaskIdToDetailsFunction();
        final Function<TaskId, LabelsList> taskIdToLabelList = initTaskIdToLabelListFunction();
        final EventEnricher result = EventEnricher.newBuilder()
                                                  .addFieldEnrichment(TaskId.class,
                                                                      TaskDetails.class,
                                                                      taskIdToTaskDetails::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      LabelsList.class,
                                                                      taskIdToLabelList::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      TaskDefinition.class,
                                                                      taskIdToTaskDefinition::apply)
                                                  .build();
        return result;
    }

    private Function<TaskId, TaskDefinition> initTaskIdToTaskFunction() {
        final Function<TaskId, TaskDefinition> result = taskId -> {
            if (taskId == null) {
                return TaskDefinition.getDefaultInstance();
            }
            final TaskDefinitionRepository taskDefinitionRepo =
                    repositoryProvider.getTaskDefinitionRepo()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(REPOSITORY_IS_NOT_INITIALIZED));
            final TaskDefinitionPart aggregate = taskDefinitionRepo.load(taskId)
                                                                   .get();
            final TaskDefinition taskDefinition = aggregate.getState();
            return taskDefinition;
        };
        return result;
    }

    private Function<TaskId, TaskDetails> initTaskIdToDetailsFunction() {
        final Function<TaskId, TaskDetails> result = taskId -> {
            if (taskId == null) {
                return TaskDetails.getDefaultInstance();
            }
            final TaskDefinitionRepository taskDefinitionRepo =
                    repositoryProvider.getTaskDefinitionRepo()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(REPOSITORY_IS_NOT_INITIALIZED));
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

    private Function<TaskId, LabelsList> initTaskIdToLabelListFunction() {
        final Function<TaskId, LabelsList> result = taskId -> {
            final TaskLabelsRepository taskLabelsRepo =
                    repositoryProvider.getTaskLabelsRepo()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(REPOSITORY_IS_NOT_INITIALIZED));
            final TaskLabelsPart aggregate = taskLabelsRepo.load(taskId)
                                                           .get();
            final List<TaskLabel> labelsList = aggregate.getState()
                                                        .getLabelsList();
            final LabelsList labelIdList = LabelsList.newBuilder()
                                                     .addAllLabels(labelsList)
                                                     .build();
            return labelIdList;
        };
        return result;
    }

    /**
     * Creates a new builder for (@code EventEnricherSupplier).
     *
     * @return new builder instance
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for producing {@code EventEnricherSupplier} instances.
     */
   public static class Builder {

        private TodoListRepositoryProvider repositoryProvider;

        private Builder() {
        }

        /**
         * Sets the {@link TodoListRepositoryProvider}.
         *
         * <p> We cannot use the constructor of the {@link EventEnricherSupplier} class
         * for the repositories initialization. Because in time, when we need the {@link EventEnricher} instance,
         * repositories are not initialized yet.
         *
         * <p> For initialize the {@link BoundedContext} we need the {@code EventEnricher},
         * for initialize the repositories we need the {@code BoundedContext}
         * and for the {@code EventEnricher} initialization we need the repositories.
         *
         * @param repositoryProvider the task aggregate repository
         */
       public Builder setRepositoryProvider(TodoListRepositoryProvider repositoryProvider) {
            this.repositoryProvider = repositoryProvider;
            return this;
        }

        /**
         * Returns the constructed {@link EventEnricherSupplier}.
         *
         * @return the {@code EventEnricherSupplier} instance
         */
        public EventEnricherSupplier build() {
            final EventEnricherSupplier result = new EventEnricherSupplier(this);
            return result;
        }
    }
}
