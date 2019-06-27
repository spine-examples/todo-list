/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.c.aggregate;

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.LabelIdsList;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabels;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.rejection.CannotAssignLabelToTask;
import io.spine.examples.todolist.c.rejection.CannotRemoveLabelFromTask;
import io.spine.server.aggregate.AggregatePart;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidAssignLabelToTaskCommand;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidTaskStatusToRemoveLabel;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskLabelsPartRejections.throwCannotAssignLabelToTask;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskLabelsPartRejections.throwCannotRemoveLabelFromTask;

/**
 * The aggregate managing the state of a {@link TaskLabels}.
 */
public class TaskLabelsPart
        extends AggregatePart<TaskId, TaskLabels, TaskLabels.Builder, TaskAggregateRoot> {

    public TaskLabelsPart(TaskAggregateRoot root) {
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
            throwCannotRemoveLabelFromTask(cmd);
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
            throwCannotAssignLabelToTask(cmd);
        }

        LabelAssignedToTask result = LabelAssignedToTask
                .newBuilder()
                .setTaskId(taskId)
                .setLabelId(labelId)
                .vBuild();
        return result;
    }

    @Apply
    private void labelAssignedToTask(LabelAssignedToTask event) {
        LabelIdsList newLabelsList = LabelIdsList
                .newBuilder()
                .mergeFrom(builder().getLabelIdsList())
                .addIds(event.getLabelId())
                .vBuild();
        builder().setTaskId(event.getTaskId());
        builder().setLabelIdsList(newLabelsList);
    }

    @Apply
    private void labelRemovedFromTask(LabelRemovedFromTask event) {
        int indexToRemove = builder().getLabelIdsList()
                                     .getIdsList()
                                     .indexOf(event.getLabelId());
        LabelIdsList newLabelsList = LabelIdsList
                .newBuilder()
                .mergeFrom(builder().getLabelIdsList())
                .removeIds(indexToRemove)
                .vBuild();
        builder().setLabelIdsList(newLabelsList);
    }
}
