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

package org.spine3.examples.todolist.server;

import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelIdList;
import org.spine3.examples.todolist.Task;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.aggregates.TaskAggregate;
import org.spine3.examples.todolist.c.aggregates.TaskLabelAggregate;
import org.spine3.server.BoundedContext;
import org.spine3.server.aggregate.AggregateRepository;
import org.spine3.server.event.enrich.EventEnricher;

import java.util.List;
import java.util.function.Function;

/**
 * Performs all necessary initializations to provide the ready-to-use {@link EventEnricher}.
 *
 * <p> This class provides the method to obtain the constructed {@link EventEnricher} instance.
 *
 * @author Illia Shepilov
 */
class EventEnricherFactory {

    private AggregateRepository<TaskId, TaskAggregate> taskAggregateRepository;
    private AggregateRepository<TaskLabelId, TaskLabelAggregate> taskLabelAggregateRepository;

    /**
     * Injects the {@link AggregateRepository}.
     *
     * <p> We cannot use the constructor for the repositories initialization.
     * <p> Because in time, when we need the {@link EventEnricher} instance, repositories are not initialized yet.
     * <p> For initialize the {@link BoundedContext} we need the {@code EventEnricher}, for initialize the repositories
     * <p> we need the {@code BoundedContext} and for the {@code EventEnricher} initialization we need the repositories.
     *
     * @param taskRepository  the task aggregate repository
     * @param labelRepository the task label aggregate repository
     */
    void injectRepositories(AggregateRepository<TaskId, TaskAggregate> taskRepository,
                            AggregateRepository<TaskLabelId, TaskLabelAggregate> labelRepository) {
        this.taskAggregateRepository = taskRepository;
        this.taskLabelAggregateRepository = labelRepository;
    }

    /**
     * Returns the constructed {@link EventEnricher}.
     *
     * @return the {@code EventEnricher} instance
     */
    EventEnricher getInstance() {
        final Function<TaskLabelId, LabelDetails> taskLabelIdToLabelDetails = initLabelIdToDetailsFunction();
        final Function<TaskId, Task> taskIdToTask = initTaskIdToTaskFunction();
        final Function<TaskId, TaskDetails> taskIdToTaskDetails = initTaskIdToDetailsFunction();
        final Function<TaskId, LabelIdList> taskIdToLabelList = initTaskIdToLabelListFunction();
        final EventEnricher result = EventEnricher.newBuilder()
                                                  .addFieldEnrichment(TaskLabelId.class,
                                                                      LabelDetails.class,
                                                                      taskLabelIdToLabelDetails::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      TaskDetails.class,
                                                                      taskIdToTaskDetails::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      LabelIdList.class,
                                                                      taskIdToLabelList::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      Task.class,
                                                                      taskIdToTask::apply)
                                                  .build();
        return result;
    }

    private Function<TaskId, Task> initTaskIdToTaskFunction() {
        final Function<TaskId, Task> result = taskId -> {
            if (taskId == null) {
                return Task.getDefaultInstance();
            }
            final TaskAggregate taskAggregate = taskAggregateRepository.load(taskId);
            final Task task = taskAggregate.getState();
            return task;
        };
        return result;
    }

    private Function<TaskLabelId, LabelDetails> initLabelIdToDetailsFunction() {
        final Function<TaskLabelId, LabelDetails> result = labelId -> {
            if (labelId == null) {
                return LabelDetails.getDefaultInstance();
            }
            final TaskLabelAggregate aggregate = taskLabelAggregateRepository.load(labelId);
            final TaskLabel state = aggregate.getState();
            final LabelDetails details = LabelDetails.newBuilder()
                                                     .setColor(state.getColor())
                                                     .setTitle(state.getTitle())
                                                     .build();
            return details;
        };

        return result;
    }

    private Function<TaskId, TaskDetails> initTaskIdToDetailsFunction() {
        final Function<TaskId, TaskDetails> result = taskId -> {
            if (taskId == null) {
                return TaskDetails.getDefaultInstance();
            }
            final TaskAggregate aggregate = taskAggregateRepository.load(taskId);
            final Task state = aggregate.getState();
            final TaskDetails details = TaskDetails.newBuilder()
                                                   .setDescription(state.getDescription())
                                                   .setPriority(state.getPriority())
                                                   .build();
            return details;
        };

        return result;
    }

    private Function<TaskId, LabelIdList> initTaskIdToLabelListFunction() {
        final Function<TaskId, LabelIdList> result = taskId -> {
            final TaskAggregate aggregate = taskAggregateRepository.load(taskId);
            final List<TaskLabelId> labelIdsList = aggregate.getState()
                                                            .getLabelIdsList();
            final LabelIdList labelIdList = LabelIdList.newBuilder()
                                                       .addAllLabelId(labelIdsList)
                                                       .build();
            return labelIdList;
        };
        return result;
    }
}
