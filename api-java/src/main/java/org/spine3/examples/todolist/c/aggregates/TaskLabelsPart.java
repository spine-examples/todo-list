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

package org.spine3.examples.todolist.c.aggregates;

import com.google.protobuf.Message;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.LabelIdsList;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabels;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.failures.CannotAssignLabelToTask;
import org.spine3.examples.todolist.c.failures.CannotRemoveLabelFromTask;
import org.spine3.server.aggregate.AggregatePart;
import org.spine3.server.aggregate.Apply;
import org.spine3.server.command.Assign;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotAssignLabelToTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotRemoveLabelFromTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.TaskFlowValidator.isValidAssignLabelToTaskCommand;
import static org.spine3.examples.todolist.c.aggregates.TaskFlowValidator.isValidRemoveLabelFromTaskCommand;

/**
 * The aggregate managing the state of a {@link TaskLabels}.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("unused") // The methods annotated with {@link Apply} are declared {@code private} by design.
public class TaskLabelsPart extends AggregatePart<TaskId, TaskLabels, TaskLabels.Builder> {

    /**
     * {@inheritDoc}
     *
     * @param id
     */
    public TaskLabelsPart(TaskId id) {
        super(id);
    }

    @Assign
    List<? extends Message> handle(RemoveLabelFromTask cmd) throws CannotRemoveLabelFromTask {
        final LabelId labelId = cmd.getLabelId();
        final TaskId taskId = cmd.getId();

        final TaskAggregateRoot root = TaskAggregateRoot.get(taskId);
        final TaskDefinition taskDefinitionState = root.getTaskDefinitionState();
        final boolean isValid = isValidRemoveLabelFromTaskCommand(taskDefinitionState.getTaskStatus());

        if (!isValid) {
            throwCannotRemoveLabelFromTaskFailure(labelId, taskId);
        }

        final LabelRemovedFromTask labelRemoved = LabelRemovedFromTask.newBuilder()
                                                                      .setTaskId(taskId)
                                                                      .setLabelId(labelId)
                                                                      .build();
        final List<LabelRemovedFromTask> result = Collections.singletonList(labelRemoved);
        return result;
    }

    @Assign
    List<? extends Message> handle(AssignLabelToTask cmd) throws CannotAssignLabelToTask {
        final TaskId taskId = cmd.getId();
        final LabelId labelId = cmd.getLabelId();

        final TaskAggregateRoot root = TaskAggregateRoot.get(taskId);
        final TaskDefinition state = root.getTaskDefinitionState();
        final boolean isValid = isValidAssignLabelToTaskCommand(state.getTaskStatus());

        if (!isValid) {
            throwCannotAssignLabelToTaskFailure(taskId, labelId);
        }

        final LabelAssignedToTask labelAssigned = LabelAssignedToTask.newBuilder()
                                                                     .setTaskId(taskId)
                                                                     .setLabelId(labelId)
                                                                     .build();
        final List<LabelAssignedToTask> result = Collections.singletonList(labelAssigned);
        return result;
    }

    @Apply
    private void labelAssignedToTask(LabelAssignedToTask event) {
        List<LabelId> list = getState().getLabelIdsList()
                                       .getIdsList()
                                       .stream()
                                       .collect(Collectors.toList());

        list.add(event.getLabelId());
        final LabelIdsList labelIdsList = LabelIdsList.newBuilder()
                                                      .addAllIds(list)
                                                      .build();
        getBuilder().clearLabelIdsList()
                    .setLabelIdsList(labelIdsList);
    }

    @Apply
    private void labelRemovedFromTask(LabelRemovedFromTask event) {
        List<LabelId> list = getState().getLabelIdsList()
                                       .getIdsList()
                                       .stream()
                                       .collect(Collectors.toList());
        list.remove(event.getLabelId());
        final LabelIdsList labelIdsList = LabelIdsList.newBuilder()
                                                      .addAllIds(list)
                                                      .build();
        getBuilder().clearLabelIdsList()
                    .setLabelIdsList(labelIdsList);
    }
}
