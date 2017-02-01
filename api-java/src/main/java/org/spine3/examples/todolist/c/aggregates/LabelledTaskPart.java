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
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsChange;
import org.spine3.examples.todolist.LabelledTask;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskStatus;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.c.events.DeletedTaskRestored;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelCreated;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.events.LabelledTaskRestored;
import org.spine3.examples.todolist.c.failures.CannotAssignLabelToTask;
import org.spine3.examples.todolist.c.failures.CannotRemoveLabelFromTask;
import org.spine3.examples.todolist.c.failures.CannotRestoreDeletedTask;
import org.spine3.examples.todolist.c.failures.CannotUpdateLabelDetails;
import org.spine3.examples.todolist.c.failures.LabelNotFound;
import org.spine3.server.aggregate.AggregatePart;
import org.spine3.server.aggregate.Apply;
import org.spine3.server.command.Assign;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newLinkedList;
import static org.spine3.examples.todolist.c.aggregates.AggregateHelper.generateExceptionMessage;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotAssignLabelToTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotRemoveLabelFromTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotRestoreDeletedTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.MismatchHelper.of;
import static org.spine3.examples.todolist.c.aggregates.TaskFlowValidator.isValidAssignLabelToTaskCommand;
import static org.spine3.examples.todolist.c.aggregates.TaskFlowValidator.isValidRemoveLabelFromTaskCommand;
import static org.spine3.examples.todolist.c.aggregates.TaskFlowValidator.isValidTransition;

/**
 * @author Illia Shepilov
 */
public class LabelledTaskPart extends AggregatePart<TaskId, LabelledTask, LabelledTask.Builder> {

    private static final int NOT_FOUND = -1;

    /**
     * {@inheritDoc}
     *
     * @param id
     */
    protected LabelledTaskPart(TaskId id) {
        super(id);
    }

    @Assign
    List<? extends Message> handle(CreateBasicLabel cmd) {
        final LabelDetails.Builder labelDetails = LabelDetails.newBuilder()
                                                              .setColor(LabelColor.GRAY)
                                                              .setTitle(cmd.getLabelTitle());
        final LabelCreated result = LabelCreated.newBuilder()
                                                .setId(cmd.getLabelId())
                                                .setDetails(labelDetails)
                                                .build();
        return Collections.singletonList(result);
    }

    @Assign
    List<? extends Message> handle(UpdateLabelDetails cmd) throws CannotUpdateLabelDetails, LabelNotFound {
        final LabelledTask state = getState();
        final TaskLabelId labelId = cmd.getId();
        final int index = ensureLabel(labelId);
        final TaskLabel taskLabel = state.getLabels(index);
        final LabelDetails actualLabelDetails = LabelDetails.newBuilder()
                                                            .setColor(taskLabel.getColor())
                                                            .setTitle(taskLabel.getTitle())
                                                            .build();
        final LabelDetailsChange labelDetailsChange = cmd.getLabelDetailsChange();
        final LabelDetails expectedLabelDetails = labelDetailsChange.getPreviousDetails();

        final boolean isEquals = actualLabelDetails.equals(expectedLabelDetails);

        if (!isEquals) {
            final LabelDetails newLabelDetails = labelDetailsChange.getNewDetails();
            final ValueMismatch mismatch = of(expectedLabelDetails, actualLabelDetails, newLabelDetails, getVersion());
            FailureHelper.throwCannotUpdateLabelDetailsFailure(labelId, mismatch);
        }

        final LabelDetailsUpdated labelDetailsUpdated = LabelDetailsUpdated.newBuilder()
                                                                           .setLabelId(labelId)
                                                                           .setLabelDetailsChange(labelDetailsChange)
                                                                           .build();
        final List<? extends Message> result = Collections.singletonList(labelDetailsUpdated);
        return result;
    }

    @Assign
    List<? extends Message> handle(RemoveLabelFromTask cmd) throws CannotRemoveLabelFromTask {
        final TaskLabelId labelId = cmd.getLabelId();
        final TaskId taskId = cmd.getId();
        final LabelledTask state = getState();
        final boolean isValid = isValidRemoveLabelFromTaskCommand(state.getTaskStatus());

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
        final TaskLabelId labelId = cmd.getLabelId();
        final LabelledTask state = getState();
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

    @Assign
    List<? extends Message> handle(RestoreDeletedTask cmd) throws CannotRestoreDeletedTask {
        final LabelledTask state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.OPEN;
        final TaskId taskId = cmd.getId();
        final boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            final String message = generateExceptionMessage(currentStatus, newStatus);
            throwCannotRestoreDeletedTaskFailure(taskId, message);
        }

        final DeletedTaskRestored deletedTaskRestored = DeletedTaskRestored.newBuilder()
                                                                           .setTaskId(taskId)
                                                                           .build();
        final List<Message> result = newLinkedList();
        result.add(deletedTaskRestored);

        for (TaskLabel label : state.getLabelsList()) {

            final LabelledTaskRestored labelledTaskRestored = LabelledTaskRestored.newBuilder()
                                                                                  .setTaskId(taskId)
                                                                                  .setLabelId(label.getId())
                                                                                  .build();
            result.add(labelledTaskRestored);
        }
        return result;
    }

    private int getLabelIndex(TaskLabelId labelId) {
        final List<TaskLabel> commentList = getState().getLabelsList();
        final Optional<TaskLabel> firstWithId = commentList.stream()
                                                           .filter(label -> label.getId()
                                                                                 .equals(labelId))
                                                           .findFirst();
        final int result = firstWithId.map(commentList::indexOf)
                                      .orElse(NOT_FOUND);
        return result;
    }

    /**
     * Makes sure the aggregate has a label with the passed ID.
     *
     * @return index of the label
     * @throws LabelNotFound if the aggregate does not have a comment with the passed ID
     */
    private int ensureLabel(TaskLabelId labelId) throws LabelNotFound {
        final TaskId taskId = getId();
        final int result = getLabelIndex(labelId);
        if (result == NOT_FOUND) {
            throw new LabelNotFound(taskId, labelId);
        }
        return result;
    }

    @Apply
    private void labelCreated(LabelCreated event) {
        final TaskLabel taskLabel = TaskLabel.newBuilder()
                                             .setId(event.getId())
                                             .setTitle(event.getDetails()
                                                            .getTitle())
                                             .setColor(LabelColor.GRAY)
                                             .build();
        getBuilder().addLabels(taskLabel);
    }

    @Apply
    private void labelDetailsUpdated(LabelDetailsUpdated event) {
        final LabelDetails newDetails = event.getLabelDetailsChange()
                                             .getNewDetails();
        final TaskLabelId labelId = event.getLabelId();
        final TaskLabel taskLabel = TaskLabel.newBuilder()
                                             .setId(labelId)
                                             .setTitle(newDetails.getTitle())
                                             .setColor(newDetails.getColor())
                                             .build();
        final int index = getLabelIndex(labelId);
        getBuilder().getLabels(index)
                    .toBuilder()
                    .mergeFrom(taskLabel);
    }

    @Apply
    private void labelAssignedToTask(LabelAssignedToTask event) {
        List<TaskLabel> list = getState().getLabelsList()
                                         .stream()
                                         .collect(Collectors.toList());
        final TaskLabel label = TaskLabel.newBuilder()
                                         .setId(event.getLabelId())
                                         .build();
        list.add(label);
        getBuilder().clearLabels()
                    .addAllLabels(list);
    }

    @Apply
    private void labelRemovedFromTask(LabelRemovedFromTask event) {
        List<TaskLabel> list = getState().getLabelsList()
                                         .stream()
                                         .collect(Collectors.toList());
        list.stream()
            .filter(label -> !label.getId()
                                   .equals(event.getLabelId()))
            .collect(Collectors.toList());

        getBuilder().clearLabels()
                    .addAllLabels(list);
    }
}
