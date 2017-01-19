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
import org.spine3.server.aggregate.AggregateRepository;
import org.spine3.server.event.enrich.EventEnricher;

import java.util.List;
import java.util.function.Function;

/**
 * This class performs all necessary initializations
 * and constructions to provide ready to use the {@link EventEnricher}.
 *
 * @author Illia Shepilov
 */
class TodoListEnrichments {

    private Function<TaskLabelId, LabelDetails> taskLabelIdToLabelDetails;
    private Function<TaskId, TaskDetails> taskIdToTaskDetails;
    private Function<TaskId, LabelIdList> taskIdToLabelList;
    private Function<TaskId, Task> taskIdToTask;
    private AggregateRepository<TaskId, TaskAggregate> taskAggregateRepository;
    private AggregateRepository<TaskLabelId, TaskLabelAggregate> taskLabelAggregateRepository;
    private EventEnricher eventEnricher;

    TodoListEnrichments(AggregateRepository<TaskId, TaskAggregate> taskAggregateRepository,
                        AggregateRepository<TaskLabelId, TaskLabelAggregate> taskLabelAggregateRepository) {
        this.taskAggregateRepository = taskAggregateRepository;
        this.taskLabelAggregateRepository = taskLabelAggregateRepository;
        initEnricherFunctions();
        this.eventEnricher = initEventEnricher();
    }

    private void initEnricherFunctions() {
        initLabelIdToDetailsFunction();
        initTaskIdToDetailsFunction();
        initTaskIdToLabelListFunction();
        initTaskIdToTaskFunction();
    }

    private EventEnricher initEventEnricher() {
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

    private void initTaskIdToTaskFunction() {
        taskIdToTask = taskId -> {
            if (taskId == null) {
                return Task.getDefaultInstance();
            }
            final TaskAggregate taskAggregate = taskAggregateRepository.load(taskId);
            final Task task = taskAggregate.getState();
            return task;
        };
    }

    private void initLabelIdToDetailsFunction() {
        taskLabelIdToLabelDetails = labelId -> {
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
    }

    private void initTaskIdToDetailsFunction() {
        taskIdToTaskDetails = taskId -> {
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
    }

    private void initTaskIdToLabelListFunction() {
        taskIdToLabelList = taskId -> {
            final TaskAggregate aggregate = taskAggregateRepository.load(taskId);
            final List<TaskLabelId> labelIdsList = aggregate.getState()
                                                            .getLabelIdsList();
            final LabelIdList result = LabelIdList.newBuilder()
                                                  .addAllLabelId(labelIdsList)
                                                  .build();
            return result;
        };
    }

    /**
     * Returns constructed the {@link EventEnricher}.
     *
     * @return the {@code EventEnricher} instance
     */
    EventEnricher getEventEnricher() {
        return eventEnricher;
    }
}
