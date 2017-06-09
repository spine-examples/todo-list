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

package io.spine.examples.todolist.c.aggregate;

import com.google.protobuf.Message;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.LabelIdsList;
import io.spine.examples.todolist.TaskDefinition;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabels;
import io.spine.examples.todolist.TaskLabelsValidatingBuilder;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.failures.CannotAssignLabelToTask;
import io.spine.examples.todolist.c.failures.CannotRemoveLabelFromTask;
import io.spine.server.aggregate.AggregatePart;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidAssignLabelToTaskCommand;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidTaskStatusToRemoveLabel;
import static io.spine.examples.todolist.c.aggregate.failures.TaskLabelsPartFailures.throwCannotAssignLabelToTaskFailure;
import static io.spine.examples.todolist.c.aggregate.failures.TaskLabelsPartFailures.throwCannotRemoveLabelFromTaskFailure;
import static java.util.Collections.singletonList;

/**
 * The aggregate managing the state of a {@link TaskLabels}.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("unused" /* The methods annotated with {@link Apply}
                              are declared {@code private} by design. */)
public class TaskLabelsPart
        extends AggregatePart<TaskId, TaskLabels, TaskLabelsValidatingBuilder, TaskAggregateRoot> {

    /**
     * {@inheritDoc}
     *
     * @param root
     */
    public TaskLabelsPart(TaskAggregateRoot root) {
        super(root);
    }

    @Assign
    List<? extends Message> handle(RemoveLabelFromTask cmd) throws CannotRemoveLabelFromTask {
        final LabelId labelId = cmd.getLabelId();
        final TaskId taskId = cmd.getId();

        final TaskDefinition taskDefinitionState = getPartState(TaskDefinition.class);
        final boolean isLabelAssigned = getState().getLabelIdsList()
                                                  .getIdsList()
                                                  .contains(labelId);
        final boolean isValidTaskStatus =
                isValidTaskStatusToRemoveLabel(taskDefinitionState.getTaskStatus());

        if (!isLabelAssigned || !isValidTaskStatus) {
            throwCannotRemoveLabelFromTaskFailure(cmd);
        }

        final LabelRemovedFromTask labelRemoved = LabelRemovedFromTask.newBuilder()
                                                                      .setTaskId(taskId)
                                                                      .setLabelId(labelId)
                                                                      .build();
        return singletonList(labelRemoved);
    }

    @Assign
    List<? extends Message> handle(AssignLabelToTask cmd) throws CannotAssignLabelToTask {
        final TaskId taskId = cmd.getId();
        final LabelId labelId = cmd.getLabelId();

        final TaskDefinition state = getPartState(TaskDefinition.class);
        final boolean isValid = isValidAssignLabelToTaskCommand(state.getTaskStatus());

        if (!isValid) {
            throwCannotAssignLabelToTaskFailure(cmd);
        }

        final LabelAssignedToTask labelAssigned = LabelAssignedToTask.newBuilder()
                                                                     .setTaskId(taskId)
                                                                     .setLabelId(labelId)
                                                                     .build();
        return singletonList(labelAssigned);
    }

    @Apply
    private void labelAssignedToTask(LabelAssignedToTask event) {
        final Collection<LabelId> list = new ArrayList<>(getState().getLabelIdsList()
                                                                   .getIdsList());
        list.add(event.getLabelId());
        final LabelIdsList labelIdsList = LabelIdsList.newBuilder()
                                                      .addAllIds(list)
                                                      .build();
        getBuilder().setLabelIdsList(labelIdsList);
    }

    @Apply
    private void labelRemovedFromTask(LabelRemovedFromTask event) {
        final Collection<LabelId> list = new ArrayList<>(getState().getLabelIdsList()
                                                                   .getIdsList());
        list.remove(event.getLabelId());
        final LabelIdsList labelIdsList = LabelIdsList.newBuilder()
                                                      .addAllIds(list)
                                                      .build();
        getBuilder().setLabelIdsList(labelIdsList);
    }
}
