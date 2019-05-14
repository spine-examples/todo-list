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

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.change.TimestampChange;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.DescriptionChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabels;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.TaskVBuilder;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.c.events.DeletedTaskRestored;
import io.spine.examples.todolist.c.events.LabelledTaskRestored;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskCreated;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.events.TaskDraftFinalized;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;
import io.spine.examples.todolist.c.rejection.CannotCompleteTask;
import io.spine.examples.todolist.c.rejection.CannotCreateDraft;
import io.spine.examples.todolist.c.rejection.CannotDeleteTask;
import io.spine.examples.todolist.c.rejection.CannotFinalizeDraft;
import io.spine.examples.todolist.c.rejection.CannotReopenTask;
import io.spine.examples.todolist.c.rejection.CannotRestoreDeletedTask;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDescription;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDueDate;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskPriority;
import io.spine.server.aggregate.AggregatePart;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.base.Time.currentTime;
import static io.spine.examples.todolist.c.aggregate.MismatchHelper.valueMismatch;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.ensureCompleted;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.ensureDeleted;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.ensureNeitherCompletedNorDeleted;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidCreateDraftCommand;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidTransition;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidUpdateTaskDueDateCommand;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidUpdateTaskPriorityCommand;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.ChangeStatusRejections.throwCannotCompleteTask;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.ChangeStatusRejections.throwCannotDeleteTask;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.ChangeStatusRejections.throwCannotFinalizeDraft;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.ChangeStatusRejections.throwCannotReopenTask;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.ChangeStatusRejections.throwCannotRestoreDeletedTask;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.TaskCreationRejections.throwCannotCreateDraft;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.UpdateRejections.throwCannotUpdateDescription;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.UpdateRejections.throwCannotUpdateTaskDescription;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.UpdateRejections.throwCannotUpdateTaskDueDate;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.UpdateRejections.throwCannotUpdateTaskPriority;

/**
 * The aggregate managing the state of a {@link Task}.
 */
@SuppressWarnings({"ClassWithTooManyMethods", /* Task definition cannot be separated and should
                                                 process all commands and events related to it
                                                 according to the domain model.
                                                 The {@code AggregatePart} does it with methods
                                                 annotated as {@code Assign} and {@code Apply}.
                                                 In that case class has too many methods.*/
        "OverlyCoupledClass" /* Each method needs dependencies to perform execution.*/,
        "unused" /* Methods are used reflectively by Spine. */})
public class TaskPart extends AggregatePart<TaskId,
                                            Task,
                                            TaskVBuilder,
                                            TaskAggregateRoot> {

    public TaskPart(TaskAggregateRoot root) {
        super(root);
    }

    @Assign
    TaskCreated handle(CreateBasicTask cmd) {
        TaskId taskId = cmd.getId();
        TaskDetails taskDetails = TaskDetails
                .vBuilder()
                .setStatus(TaskStatus.OPEN)
                .setDescription(cmd.getDescription())
                .build();
        TaskCreated result = TaskCreated
                .vBuilder()
                .setTaskId(taskId)
                .setDetails(taskDetails)
                .build();
        return result;
    }

    @Assign
    TaskDescriptionUpdated handle(UpdateTaskDescription cmd) throws CannotUpdateTaskDescription {
        boolean isValid = ensureNeitherCompletedNorDeleted(state().getTaskStatus());
        if (!isValid) {
            throwCannotUpdateTaskDescription(cmd);
        }
        DescriptionChange descriptionChange = cmd.getDescriptionChange();
        TaskDescription actualDescription = state().getDescription();
        TaskDescription expectedDescription = descriptionChange.getPreviousValue();
        boolean isEquals = actualDescription.equals(expectedDescription);

        if (!isEquals) {
            ValueMismatch mismatch = unexpectedValue(expectedDescription.getValue(),
                                                     actualDescription.getValue(),
                                                     descriptionChange.getNewValue()
                                                                      .getValue());
            throwCannotUpdateDescription(cmd, mismatch);
        }
        TaskId taskId = cmd.getId();
        TaskDescriptionUpdated result = TaskDescriptionUpdated
                .vBuilder()
                .setTaskId(taskId)
                .setDescriptionChange(descriptionChange)
                .build();
        return result;
    }

    @Assign
    TaskDueDateUpdated handle(UpdateTaskDueDate cmd) throws CannotUpdateTaskDueDate {
        Task state = state();
        TaskStatus taskStatus = state.getTaskStatus();
        boolean isValid = isValidUpdateTaskDueDateCommand(taskStatus);

        if (!isValid) {
            throwCannotUpdateTaskDueDate(cmd);
        }

        TimestampChange change = cmd.getDueDateChange();
        Timestamp actualDueDate = state.getDueDate();
        Timestamp expectedDueDate = change.getPreviousValue();

        boolean isEquals = Timestamps.compare(actualDueDate, expectedDueDate) == 0;

        if (!isEquals) {
            Timestamp newDueDate = change.getNewValue();
            ValueMismatch mismatch = unexpectedValue(expectedDueDate, actualDueDate, newDueDate);
            throwCannotUpdateTaskDueDate(cmd, mismatch);
        }
        TaskId taskId = cmd.getId();
        TaskDueDateUpdated result = TaskDueDateUpdated
                .vBuilder()
                .setTaskId(taskId)
                .setDueDateChange(cmd.getDueDateChange())
                .build();
        return result;
    }

    @Assign
    TaskPriorityUpdated handle(UpdateTaskPriority cmd) throws CannotUpdateTaskPriority {
        Task state = state();
        TaskStatus taskStatus = state.getTaskStatus();
        boolean isValid = isValidUpdateTaskPriorityCommand(taskStatus);

        if (!isValid) {
            throwCannotUpdateTaskPriority(cmd);
        }
        PriorityChange priorityChange = cmd.getPriorityChange();
        TaskPriority actualPriority = state.getPriority();
        TaskPriority expectedPriority = priorityChange.getPreviousValue();

        boolean isEquals = actualPriority == expectedPriority;

        if (!isEquals) {
            TaskPriority newPriority = priorityChange.getNewValue();
            ValueMismatch mismatch =
                    valueMismatch(expectedPriority, actualPriority, newPriority, getVersion());
            throwCannotUpdateTaskPriority(cmd, mismatch);
        }
        TaskId taskId = cmd.getId();
        TaskPriorityUpdated result = TaskPriorityUpdated
                .vBuilder()
                .setTaskId(taskId)
                .setPriorityChange(priorityChange)
                .build();
        return result;
    }

    @Assign
    TaskReopened handle(ReopenTask cmd) throws CannotReopenTask {
        Task state = state();
        TaskStatus currentStatus = state.getTaskStatus();
        boolean isValid = ensureCompleted(currentStatus);

        if (!isValid) {
            throwCannotReopenTask(cmd);
        }

        TaskId taskId = cmd.getId();
        TaskReopened result = TaskReopened
                .vBuilder()
                .setTaskId(taskId)
                .build();
        return result;
    }

    @Assign
    TaskDeleted handle(DeleteTask cmd) throws CannotDeleteTask {
        Task state = state();
        TaskStatus currentStatus = state.getTaskStatus();
        TaskStatus newStatus = TaskStatus.DELETED;
        boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            throwCannotDeleteTask(cmd);
        }

        TaskId taskId = cmd.getId();
        TaskDeleted result = TaskDeleted
                .vBuilder()
                .setTaskId(taskId)
                .build();
        return result;
    }

    @Assign
    TaskCompleted handle(CompleteTask cmd) throws CannotCompleteTask {
        Task state = state();
        TaskStatus currentStatus = state.getTaskStatus();
        TaskStatus newStatus = TaskStatus.COMPLETED;
        boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            throwCannotCompleteTask(cmd);
        }

        TaskId taskId = cmd.getId();
        TaskCompleted result = TaskCompleted
                .vBuilder()
                .setTaskId(taskId)
                .build();
        return result;
    }

    @Assign
    TaskDraftCreated handle(CreateDraft cmd) throws CannotCreateDraft {
        boolean isValid = isValidCreateDraftCommand(state().getTaskStatus());

        if (!isValid) {
            throwCannotCreateDraft(cmd);
        }

        TaskId taskId = cmd.getId();
        TaskDraftCreated result = TaskDraftCreated
                .vBuilder()
                .setTaskId(taskId)
                .setDraftCreationTime(currentTime())
                .build();
        return result;
    }

    @Assign
    TaskDraftFinalized handle(FinalizeDraft cmd) throws CannotFinalizeDraft {
        TaskStatus currentStatus = state().getTaskStatus();
        TaskStatus newStatus = TaskStatus.FINALIZED;
        boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            throwCannotFinalizeDraft(cmd);
        }

        TaskId taskId = cmd.getId();
        TaskDraftFinalized result = TaskDraftFinalized
                .vBuilder()
                .setTaskId(taskId)
                .build();
        return result;
    }

    @Assign
    List<? extends Message> handle(RestoreDeletedTask cmd)
            throws CannotRestoreDeletedTask {
        TaskStatus currentStatus = state().getTaskStatus();
        boolean isValid = ensureDeleted(currentStatus);
        if (!isValid) {
            throwCannotRestoreDeletedTask(cmd);
        }

        TaskId taskId = cmd.getId();
        DeletedTaskRestored deletedTaskRestored = DeletedTaskRestored
                .vBuilder()
                .setTaskId(taskId)
                .build();
        List<Message> result = newLinkedList();
        result.add(deletedTaskRestored);

        TaskLabels taskLabels = getPartState(TaskLabels.class);
        List<LabelId> labelIdsList = taskLabels.getLabelIdsList()
                                               .getIdsList();
        for (LabelId labelId : labelIdsList) {
            LabelledTaskRestored labelledTaskRestored = LabelledTaskRestored
                    .vBuilder()
                    .setTaskId(taskId)
                    .setLabelId(labelId)
                    .build();
            result.add(labelledTaskRestored);
        }
        return result;
    }

    /*
     * Event appliers
     *****************/

    @Apply
    private void taskCreated(TaskCreated event) {
        TaskDetails taskDetails = event.getDetails();
        builder().setId(event.getTaskId())
                 .setCreated(currentTime())
                 .setDescription(taskDetails.getDescription())
                 .setPriority(taskDetails.getPriority())
                 .setTaskStatus(TaskStatus.FINALIZED);
    }

    @Apply
    private void taskDescriptionUpdated(TaskDescriptionUpdated event) {
        TaskDescription newDescription = event.getDescriptionChange()
                                              .getNewValue();
        builder().setDescription(newDescription);
    }

    @Apply
    private void taskDueDateUpdated(TaskDueDateUpdated event) {
        Timestamp newDueDate = event.getDueDateChange()
                                    .getNewValue();
        builder().setDueDate(newDueDate);
    }

    @Apply
    private void taskPriorityUpdated(TaskPriorityUpdated event) {
        TaskPriority newPriority = event.getPriorityChange()
                                        .getNewValue();
        builder().setPriority(newPriority);
    }

    @Apply
    private void taskReopened(TaskReopened event) {
        builder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void taskDeleted(TaskDeleted event) {
        builder().setTaskStatus(TaskStatus.DELETED);
    }

    @Apply
    private void deletedTaskRestored(DeletedTaskRestored event) {
        builder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void labelledTaskRestored(LabelledTaskRestored event) {
        builder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void taskCompleted(TaskCompleted event) {
        builder().setTaskStatus(TaskStatus.COMPLETED);
    }

    @Apply
    private void taskDraftFinalized(TaskDraftFinalized event) {
        builder().setTaskStatus(TaskStatus.FINALIZED);
    }

    @Apply
    private void draftCreated(TaskDraftCreated event) {
        builder().setId(event.getTaskId())
                 .setCreated(event.getDraftCreationTime())
                 .setDescription(event.getDetails()
                                      .getDescription())
                 .setTaskStatus(TaskStatus.DRAFT);
    }
}
