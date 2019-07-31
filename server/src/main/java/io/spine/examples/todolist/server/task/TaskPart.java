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

package io.spine.examples.todolist.server.task;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.change.TimestampChange;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.tasks.DescriptionChange;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.PriorityChange;
import io.spine.examples.todolist.tasks.Task;
import io.spine.examples.todolist.tasks.TaskDescription;
import io.spine.examples.todolist.tasks.TaskDetails;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.TaskLabels;
import io.spine.examples.todolist.tasks.TaskPriority;
import io.spine.examples.todolist.tasks.TaskStatus;
import io.spine.examples.todolist.tasks.command.CompleteTask;
import io.spine.examples.todolist.tasks.command.CreateBasicTask;
import io.spine.examples.todolist.tasks.command.CreateDraft;
import io.spine.examples.todolist.tasks.command.DeleteTask;
import io.spine.examples.todolist.tasks.command.FinalizeDraft;
import io.spine.examples.todolist.tasks.command.ReopenTask;
import io.spine.examples.todolist.tasks.command.RestoreDeletedTask;
import io.spine.examples.todolist.tasks.command.UpdateTaskDescription;
import io.spine.examples.todolist.tasks.command.UpdateTaskDueDate;
import io.spine.examples.todolist.tasks.command.UpdateTaskPriority;
import io.spine.examples.todolist.tasks.event.DeletedTaskRestored;
import io.spine.examples.todolist.tasks.event.LabelledTaskRestored;
import io.spine.examples.todolist.tasks.event.TaskCompleted;
import io.spine.examples.todolist.tasks.event.TaskCreated;
import io.spine.examples.todolist.tasks.event.TaskDeleted;
import io.spine.examples.todolist.tasks.event.TaskDescriptionUpdated;
import io.spine.examples.todolist.tasks.event.TaskDraftCreated;
import io.spine.examples.todolist.tasks.event.TaskDraftFinalized;
import io.spine.examples.todolist.tasks.event.TaskDueDateUpdated;
import io.spine.examples.todolist.tasks.event.TaskPriorityUpdated;
import io.spine.examples.todolist.tasks.event.TaskReopened;
import io.spine.examples.todolist.tasks.rejection.CannotCompleteTask;
import io.spine.examples.todolist.tasks.rejection.CannotCreateDraft;
import io.spine.examples.todolist.tasks.rejection.CannotDeleteTask;
import io.spine.examples.todolist.tasks.rejection.CannotFinalizeDraft;
import io.spine.examples.todolist.tasks.rejection.CannotReopenTask;
import io.spine.examples.todolist.tasks.rejection.CannotRestoreDeletedTask;
import io.spine.examples.todolist.tasks.rejection.CannotUpdateTaskDescription;
import io.spine.examples.todolist.tasks.rejection.CannotUpdateTaskDueDate;
import io.spine.examples.todolist.tasks.rejection.CannotUpdateTaskPriority;
import io.spine.server.aggregate.AggregatePart;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.base.Time.currentTime;
import static io.spine.examples.todolist.server.task.MismatchHelper.valueMismatch;
import static io.spine.examples.todolist.server.task.TaskFlowValidator.ensureCompleted;
import static io.spine.examples.todolist.server.task.TaskFlowValidator.ensureDeleted;
import static io.spine.examples.todolist.server.task.TaskFlowValidator.ensureNeitherCompletedNorDeleted;
import static io.spine.examples.todolist.server.task.TaskFlowValidator.isValidCreateDraftCommand;
import static io.spine.examples.todolist.server.task.TaskFlowValidator.isValidTransition;
import static io.spine.examples.todolist.server.task.TaskFlowValidator.isValidUpdateTaskDueDateCommand;
import static io.spine.examples.todolist.server.task.TaskFlowValidator.isValidUpdateTaskPriorityCommand;
import static io.spine.examples.todolist.server.task.TaskPartRejections.ChangeStatusRejections.throwCannotCompleteTask;
import static io.spine.examples.todolist.server.task.TaskPartRejections.ChangeStatusRejections.throwCannotDeleteTask;
import static io.spine.examples.todolist.server.task.TaskPartRejections.ChangeStatusRejections.throwCannotFinalizeDraft;
import static io.spine.examples.todolist.server.task.TaskPartRejections.ChangeStatusRejections.throwCannotReopenTask;
import static io.spine.examples.todolist.server.task.TaskPartRejections.ChangeStatusRejections.throwCannotRestoreDeletedTask;
import static io.spine.examples.todolist.server.task.TaskPartRejections.TaskCreationRejections.throwCannotCreateDraft;
import static io.spine.examples.todolist.server.task.TaskPartRejections.UpdateRejections.throwCannotUpdateDescription;
import static io.spine.examples.todolist.server.task.TaskPartRejections.UpdateRejections.throwCannotUpdateTaskDescription;
import static io.spine.examples.todolist.server.task.TaskPartRejections.UpdateRejections.throwCannotUpdateTaskDueDate;
import static io.spine.examples.todolist.server.task.TaskPartRejections.UpdateRejections.throwCannotUpdateTaskPriority;

/**
 * The aggregate managing the state of a {@link Task}.
 */
@SuppressWarnings({"ClassWithTooManyMethods", /* Task definition cannot be separated and should
                                                 process all commands and events related to it
                                                 according to the domain model.
                                                 The {@code AggregatePart} does it with methods
                                                 annotated as {@code Assign} and {@code Apply}.
                                                 In that case class has too many methods.*/
        "OverlyCoupledClass" /* Each method needs dependencies to perform execution.*/})
public class TaskPart extends AggregatePart<TaskId, Task, Task.Builder, TaskAggregateRoot> {

    public TaskPart(TaskAggregateRoot root) {
        super(root);
    }

    @Assign
    TaskCreated handle(CreateBasicTask cmd) {
        TaskId taskId = cmd.getId();
        TaskDetails taskDetails = TaskDetails
                .newBuilder()
                .setStatus(TaskStatus.OPEN)
                .setDescription(cmd.getDescription())
                .buildPartial();
        TaskCreated result = TaskCreated
                .newBuilder()
                .setTaskId(taskId)
                .setDetails(taskDetails)
                .vBuild();
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
                .newBuilder()
                .setTaskId(taskId)
                .setDescriptionChange(descriptionChange)
                .vBuild();
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
                .newBuilder()
                .setTaskId(taskId)
                .setDueDateChange(cmd.getDueDateChange())
                .vBuild();
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
                .newBuilder()
                .setTaskId(taskId)
                .setPriorityChange(priorityChange)
                .vBuild();
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
                .newBuilder()
                .setTaskId(taskId)
                .vBuild();
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
                .newBuilder()
                .setTaskId(taskId)
                .vBuild();
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
                .newBuilder()
                .setTaskId(taskId)
                .vBuild();
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
                .newBuilder()
                .setTaskId(taskId)
                .setDraftCreationTime(currentTime())
                .vBuild();
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
                .newBuilder()
                .setTaskId(taskId)
                .vBuild();
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
                .newBuilder()
                .setTaskId(taskId)
                .vBuild();
        List<Message> result = newLinkedList();
        result.add(deletedTaskRestored);

        TaskLabels taskLabels = partState(TaskLabels.class);
        List<LabelId> labelIdsList = taskLabels.getLabelIdsList()
                                               .getIdsList();
        for (LabelId labelId : labelIdsList) {
            LabelledTaskRestored labelledTaskRestored = LabelledTaskRestored
                    .newBuilder()
                    .setTaskId(taskId)
                    .setLabelId(labelId)
                    .vBuild();
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
    private void taskReopened(@SuppressWarnings("unused") TaskReopened event) {
        builder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void taskDeleted(@SuppressWarnings("unused") TaskDeleted event) {
        builder().setTaskStatus(TaskStatus.DELETED);
    }

    @Apply
    private void deletedTaskRestored(@SuppressWarnings("unused") DeletedTaskRestored event) {
        builder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void labelledTaskRestored(@SuppressWarnings("unused") LabelledTaskRestored event) {
        builder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void taskCompleted(@SuppressWarnings("unused") TaskCompleted event) {
        builder().setTaskStatus(TaskStatus.COMPLETED);
    }

    @Apply
    private void taskDraftFinalized(@SuppressWarnings("unused") TaskDraftFinalized event) {
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
