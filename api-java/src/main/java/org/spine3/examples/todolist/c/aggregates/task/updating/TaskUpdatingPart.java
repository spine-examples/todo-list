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

package org.spine3.examples.todolist.c.aggregates.task.updating;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.spine3.change.StringChange;
import org.spine3.change.TimestampChange;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.PriorityChange;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskStatus;
import org.spine3.examples.todolist.TaskUpdating;
import org.spine3.examples.todolist.c.aggregates.FailureHelper;
import org.spine3.examples.todolist.c.aggregates.MismatchHelper;
import org.spine3.examples.todolist.c.aggregates.TaskFlowValidator;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDescription;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDueDate;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskPriority;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskWithInappropriateDescription;
import org.spine3.protobuf.Timestamps;
import org.spine3.server.aggregate.AggregatePart;
import org.spine3.server.aggregate.Apply;
import org.spine3.server.command.Assign;

import java.util.Collections;
import java.util.List;

import static org.spine3.examples.todolist.c.aggregates.FailureHelper.TASK_DELETED_OR_COMPLETED_EXCEPTION_MESSAGE;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotUpdateDescriptionFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotUpdateTooShortDescriptionFailure;
import static org.spine3.examples.todolist.c.aggregates.MismatchHelper.of;

/**
 * @author Illia Shepilov
 */
public class TaskUpdatingPart extends AggregatePart<TaskId, TaskUpdating, TaskUpdating.Builder> {

    private static final int MIN_DESCRIPTION_LENGTH = 3;

    /**
     * {@inheritDoc}
     *
     * @param id
     */
    protected TaskUpdatingPart(TaskId id) {
        super(id);
    }

    @Assign
    List<? extends Message> handle(UpdateTaskDescription cmd)
            throws CannotUpdateTaskDescription, CannotUpdateTaskWithInappropriateDescription {
        validateCommand(cmd);
        final TaskUpdating state = getState();
        final StringChange change = cmd.getDescriptionChange();

        final String actualDescription = state.getDescription();
        final String expectedDescription = change.getPreviousValue();
        final boolean isEquals = actualDescription.equals(expectedDescription);
        final TaskId taskId = cmd.getId();

        if (!isEquals) {
            final String newDescription = change.getNewValue();
            final ValueMismatch mismatch = of(expectedDescription, actualDescription, newDescription, getVersion());
            throwCannotUpdateDescriptionFailure(taskId, mismatch);
        }

        final TaskDescriptionUpdated taskDescriptionUpdated = TaskDescriptionUpdated.newBuilder()
                                                                                    .setTaskId(taskId)
                                                                                    .setDescriptionChange(change)
                                                                                    .build();
        final List<? extends Message> result = Collections.singletonList(taskDescriptionUpdated);
        return result;
    }

    @Assign
    List<? extends Message> handle(UpdateTaskDueDate cmd) throws CannotUpdateTaskDueDate {
        final TaskUpdating state = getState();
        final TaskStatus taskStatus = state.getTaskStatus();
        final boolean isValid = TaskFlowValidator.isValidUpdateTaskDueDateCommand(taskStatus);

        final TaskId taskId = cmd.getId();

        if (!isValid) {
            FailureHelper.throwCannotUpdateTaskDueDateFailure(taskId);
        }

        final TimestampChange change = cmd.getDueDateChange();
        final Timestamp actualDueDate = state.getDueDate();
        final Timestamp expectedDueDate = change.getPreviousValue();

        final boolean isEquals = Timestamps.compare(actualDueDate, expectedDueDate) == 0;

        if (!isEquals) {
            final Timestamp newDueDate = change.getNewValue();
            final ValueMismatch mismatch = of(expectedDueDate, actualDueDate, newDueDate, getVersion());
            FailureHelper.throwCannotUpdateTaskDueDateFailure(taskId, mismatch);
        }

        final TaskDueDateUpdated taskDueDateUpdated = TaskDueDateUpdated.newBuilder()
                                                                        .setTaskId(taskId)
                                                                        .setDueDateChange(cmd.getDueDateChange())
                                                                        .build();
        final List<? extends Message> result = Collections.singletonList(taskDueDateUpdated);
        return result;
    }

    @Assign
    List<? extends Message> handle(UpdateTaskPriority cmd) throws CannotUpdateTaskPriority {
        final TaskUpdating state = getState();
        final TaskStatus taskStatus = state.getTaskStatus();
        final boolean isValid = TaskFlowValidator.isValidUpdateTaskPriorityCommand(taskStatus);
        final TaskId taskId = cmd.getId();

        if (!isValid) {
            FailureHelper.throwCannotUpdateTaskPriorityFailure(taskId);
        }

        final PriorityChange priorityChange = cmd.getPriorityChange();
        final TaskPriority actualPriority = state.getPriority();
        final TaskPriority expectedPriority = priorityChange.getPreviousValue();

        boolean isEquals = actualPriority == expectedPriority;

        if (!isEquals) {
            final TaskPriority newPriority = priorityChange.getNewValue();
            final ValueMismatch mismatch = of(expectedPriority, actualPriority, newPriority, getVersion());
            FailureHelper.throwCannotUpdateTaskPriorityFailure(taskId, mismatch);
        }

        final TaskPriorityUpdated taskPriorityUpdated = TaskPriorityUpdated.newBuilder()
                                                                           .setTaskId(taskId)
                                                                           .setPriorityChange(priorityChange)
                                                                           .build();
        final List<? extends Message> result = Collections.singletonList(taskPriorityUpdated);
        return result;
    }

    @Apply
    private void taskDescriptionUpdated(TaskDescriptionUpdated event) {
        final String newDescription = event.getDescriptionChange()
                                           .getNewValue();
        getBuilder().setDescription(newDescription);
    }

    @Apply
    private void taskDueDateUpdated(TaskDueDateUpdated event) {
        final Timestamp newDueDate = event.getDueDateChange()
                                          .getNewValue();
        getBuilder().setDueDate(newDueDate);
    }

    @Apply
    private void taskPriorityUpdated(TaskPriorityUpdated event) {
        final TaskPriority newPriority = event.getPriorityChange()
                                              .getNewValue();
        getBuilder().setPriority(newPriority);
    }

    private void validateCommand(UpdateTaskDescription cmd)
            throws CannotUpdateTaskDescription, CannotUpdateTaskWithInappropriateDescription {
        final String description = cmd.getDescriptionChange()
                                      .getNewValue();
        final TaskId taskId = cmd.getId();

        if (description != null && description.length() < MIN_DESCRIPTION_LENGTH) {
            throwCannotUpdateTooShortDescriptionFailure(taskId);
        }

        boolean isValid = TaskFlowValidator.ensureNeitherCompletedNorDeleted(getState().getTaskStatus());

        if (!isValid) {
            FailureHelper.throwCannotUpdateTaskDescriptionFailure(taskId, TASK_DELETED_OR_COMPLETED_EXCEPTION_MESSAGE);
        }
    }
}
