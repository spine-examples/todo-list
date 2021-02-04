/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.server.tasks.task;

import io.spine.examples.todolist.tasks.AssignLabelToTaskRejected;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.LabelIdsList;
import io.spine.examples.todolist.tasks.RejectedTaskCommandDetails;
import io.spine.examples.todolist.tasks.RemoveLabelFromTaskRejected;
import io.spine.examples.todolist.tasks.Task;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.TaskLabels;
import io.spine.examples.todolist.tasks.command.AssignLabelToTask;
import io.spine.examples.todolist.tasks.command.RemoveLabelFromTask;
import io.spine.examples.todolist.tasks.event.LabelAssignedToTask;
import io.spine.examples.todolist.tasks.event.LabelRemovedFromTask;
import io.spine.examples.todolist.tasks.rejection.CannotAssignLabelToTask;
import io.spine.examples.todolist.tasks.rejection.CannotRemoveLabelFromTask;
import io.spine.server.aggregate.AggregatePart;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.server.tasks.task.TaskFlowValidator.isValidAssignLabelToTaskCommand;
import static io.spine.examples.todolist.server.tasks.task.TaskFlowValidator.isValidTaskStatusToRemoveLabel;

/**
 * The aggregate managing the state of a {@link TaskLabels}.
 */
final class TaskLabelsPart
        extends AggregatePart<TaskId, TaskLabels, TaskLabels.Builder, TaskAggregateRoot> {

    TaskLabelsPart(TaskAggregateRoot root) {
        super(root);
    }

    @Assign
    LabelRemovedFromTask handle(RemoveLabelFromTask cmd) throws CannotRemoveLabelFromTask {
        LabelId labelId = cmd.getLabelId();
        TaskId taskId = cmd.getId();

        Task taskState = partState(Task.class);
        boolean isLabelAssigned = state().getLabelIdsList()
                                         .getIdsList()
                                         .contains(labelId);
        boolean isValidTaskStatus = isValidTaskStatusToRemoveLabel(taskState.getTaskStatus());
        if (!isLabelAssigned || !isValidTaskStatus) {
            throw cannotRemove(cmd);
        }

        LabelRemovedFromTask result = LabelRemovedFromTask
                .newBuilder()
                .setTaskId(taskId)
                .setLabelId(labelId)
                .vBuild();
        return result;
    }

    @Assign
    LabelAssignedToTask handle(AssignLabelToTask cmd) throws CannotAssignLabelToTask {
        TaskId taskId = cmd.getId();
        LabelId labelId = cmd.getLabelId();

        Task state = partState(Task.class);
        boolean isValid = isValidAssignLabelToTaskCommand(state.getTaskStatus());

        if (!isValid) {
            throw cannotAssign(cmd);
        }

        LabelAssignedToTask result = LabelAssignedToTask
                .newBuilder()
                .setTaskId(taskId)
                .setLabelId(labelId)
                .vBuild();
        return result;
    }

    @Apply
    private void event(LabelAssignedToTask e) {
        LabelIdsList newLabelsList = LabelIdsList
                .newBuilder()
                .mergeFrom(builder().getLabelIdsList())
                .addIds(e.getLabelId())
                .vBuild();
        builder().setTaskId(e.getTaskId());
        builder().setLabelIdsList(newLabelsList);
    }

    @Apply
    private void event(LabelRemovedFromTask e) {
        int indexToRemove = builder().getLabelIdsList()
                                     .getIdsList()
                                     .indexOf(e.getLabelId());
        LabelIdsList newLabelsList = LabelIdsList
                .newBuilder()
                .mergeFrom(builder().getLabelIdsList())
                .removeIds(indexToRemove)
                .vBuild();
        builder().setLabelIdsList(newLabelsList);
    }

    /**
     * Constructs and throws the {@link CannotRemoveLabelFromTask} rejection according to
     * the passed parameters.
     *
     * @param cmd
     *         the {@code AssignLabelToTask} command which thrown the rejection
     * @throws CannotRemoveLabelFromTask
     *         the rejection to throw
     */
    private static CannotRemoveLabelFromTask cannotRemove(RemoveLabelFromTask cmd)
            throws CannotRemoveLabelFromTask {
        checkNotNull(cmd);
        RejectedTaskCommandDetails commandDetails = RejectedTaskCommandDetails
                .newBuilder()
                .setTaskId(cmd.getId())
                .vBuild();
        RemoveLabelFromTaskRejected removeLabelRejected = RemoveLabelFromTaskRejected
                .newBuilder()
                .setLabelId(cmd.getLabelId())
                .setCommandDetails(commandDetails)
                .vBuild();
        CannotRemoveLabelFromTask rejection = CannotRemoveLabelFromTask
                .newBuilder()
                .setRejectionDetails(removeLabelRejected)
                .build();
        throw rejection;
    }

    /**
     * Constructs and throws the {@link CannotAssignLabelToTask} rejection according to
     * the passed parameters.
     *
     * @param cmd
     *         the {@code AssignLabelToTask} command which thrown the rejection
     * @throws CannotAssignLabelToTask
     *         the rejection to throw
     */
    private static CannotAssignLabelToTask cannotAssign(AssignLabelToTask cmd)
            throws CannotAssignLabelToTask {
        checkNotNull(cmd);
        RejectedTaskCommandDetails commandDetails = RejectedTaskCommandDetails
                .newBuilder()
                .setTaskId(cmd.getId())
                .vBuild();
        AssignLabelToTaskRejected assignLabelToTaskRejected = AssignLabelToTaskRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .setLabelId(cmd.getLabelId())
                .vBuild();
        CannotAssignLabelToTask rejection = CannotAssignLabelToTask
                .newBuilder()
                .setRejectionDetails(assignLabelToTaskRejected)
                .build();
        throw rejection;
    }
}
