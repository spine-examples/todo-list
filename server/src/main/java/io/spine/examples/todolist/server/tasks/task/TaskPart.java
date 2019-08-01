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

package io.spine.examples.todolist.server.tasks.task;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.change.TimestampChange;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.tasks.CompleteTaskRejected;
import io.spine.examples.todolist.tasks.CreateDraftRejected;
import io.spine.examples.todolist.tasks.DeleteTaskRejected;
import io.spine.examples.todolist.tasks.DescriptionChange;
import io.spine.examples.todolist.tasks.DescriptionUpdateRejected;
import io.spine.examples.todolist.tasks.FinalizeDraftRejected;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.PriorityChange;
import io.spine.examples.todolist.tasks.PriorityUpdateRejected;
import io.spine.examples.todolist.tasks.RejectedTaskCommandDetails;
import io.spine.examples.todolist.tasks.ReopenTaskRejected;
import io.spine.examples.todolist.tasks.RestoreDeletedTaskRejected;
import io.spine.examples.todolist.tasks.Task;
import io.spine.examples.todolist.tasks.TaskDescription;
import io.spine.examples.todolist.tasks.TaskDetails;
import io.spine.examples.todolist.tasks.TaskDueDateUpdateRejected;
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
import static io.spine.examples.todolist.server.tasks.task.MismatchHelper.valueMismatch;
import static io.spine.examples.todolist.server.tasks.task.TaskFlowValidator.ensureCompleted;
import static io.spine.examples.todolist.server.tasks.task.TaskFlowValidator.ensureDeleted;
import static io.spine.examples.todolist.server.tasks.task.TaskFlowValidator.ensureNeitherCompletedNorDeleted;
import static io.spine.examples.todolist.server.tasks.task.TaskFlowValidator.isValidCreateDraftCommand;
import static io.spine.examples.todolist.server.tasks.task.TaskFlowValidator.isValidTransition;
import static io.spine.examples.todolist.server.tasks.task.TaskFlowValidator.isValidUpdateTaskDueDateCommand;
import static io.spine.examples.todolist.server.tasks.task.TaskFlowValidator.isValidUpdateTaskPriorityCommand;
import static io.spine.examples.todolist.tasks.TaskStatus.COMPLETED;
import static io.spine.examples.todolist.tasks.TaskStatus.DRAFT;
import static io.spine.examples.todolist.tasks.TaskStatus.FINALIZED;
import static io.spine.examples.todolist.tasks.TaskStatus.OPEN;

/**
 * The aggregate managing the state of a {@link Task}.
 */
@SuppressWarnings({
        "ClassWithTooManyMethods" /* one method per signal type */,
        "OverlyCoupledClass" /* depends on handled signal types */
})
final class TaskPart extends AggregatePart<TaskId, Task, Task.Builder, TaskAggregateRoot> {

    TaskPart(TaskAggregateRoot root) {
        super(root);
    }

    @Assign
    TaskCreated handle(CreateBasicTask cmd) {
        TaskId taskId = cmd.getId();
        TaskDetails taskDetails = TaskDetails
                .newBuilder()
                .setStatus(OPEN)
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
            throw rejection(cmd);
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
            throw rejection(cmd, mismatch);
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
            throw rejection(cmd);
        }

        TimestampChange change = cmd.getDueDateChange();
        Timestamp actualDueDate = state.getDueDate();
        Timestamp expectedDueDate = change.getPreviousValue();

        boolean sameDate = Timestamps.compare(actualDueDate, expectedDueDate) == 0;
        if (!sameDate) {
            Timestamp newDueDate = change.getNewValue();
            ValueMismatch mismatch = unexpectedValue(expectedDueDate, actualDueDate, newDueDate);
            throw rejection(cmd, mismatch);
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
            throw rejection(cmd);
        }
        PriorityChange priorityChange = cmd.getPriorityChange();
        TaskPriority actualPriority = state.getPriority();
        TaskPriority expectedPriority = priorityChange.getPreviousValue();

        boolean samePriority = actualPriority == expectedPriority;
        if (!samePriority) {
            TaskPriority newPriority = priorityChange.getNewValue();
            ValueMismatch mismatch =
                    valueMismatch(expectedPriority, actualPriority, newPriority, getVersion());
            throw rejection(cmd, mismatch);
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
            throw rejection(cmd);
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
            throw rejection(cmd);
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
        boolean isValid = isValidTransition(currentStatus, COMPLETED);
        if (!isValid) {
            throw rejection(cmd);
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
            throw rejection(cmd);
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
        boolean isValid = isValidTransition(currentStatus, FINALIZED);
        if (!isValid) {
            throw rejection(cmd);
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
            throw rejection(cmd);
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
    private void event(TaskCreated e) {
        TaskDetails taskDetails = e.getDetails();
        builder().setId(e.getTaskId())
                 .setCreated(currentTime())
                 .setDescription(taskDetails.getDescription())
                 .setPriority(taskDetails.getPriority())
                 .setTaskStatus(FINALIZED);
    }

    @Apply
    private void event(TaskDescriptionUpdated e) {
        TaskDescription newDescription = e.getDescriptionChange()
                                          .getNewValue();
        builder().setDescription(newDescription);
    }

    @Apply
    private void event(TaskDueDateUpdated e) {
        Timestamp newDueDate = e.getDueDateChange()
                                .getNewValue();
        builder().setDueDate(newDueDate);
    }

    @Apply
    private void event(TaskPriorityUpdated e) {
        TaskPriority newPriority = e.getPriorityChange()
                                    .getNewValue();
        builder().setPriority(newPriority);
    }

    @Apply
    private void event(@SuppressWarnings("unused") TaskReopened e) {
        builder().setTaskStatus(OPEN);
    }

    @Apply
    private void event(@SuppressWarnings("unused") TaskDeleted e) {
        builder().setTaskStatus(TaskStatus.DELETED);
    }

    @Apply
    private void event(@SuppressWarnings("unused") DeletedTaskRestored e) {
        builder().setTaskStatus(OPEN);
    }

    @Apply
    private void event(@SuppressWarnings("unused") LabelledTaskRestored e) {
        builder().setTaskStatus(OPEN);
    }

    @Apply
    private void event(@SuppressWarnings("unused") TaskCompleted e) {
        builder().setTaskStatus(COMPLETED);
    }

    @Apply
    private void event(@SuppressWarnings("unused") TaskDraftFinalized e) {
        builder().setTaskStatus(FINALIZED);
    }

    @Apply
    private void event(TaskDraftCreated e) {
        builder().setId(e.getTaskId())
                 .setCreated(e.getDraftCreationTime())
                 .setDescription(e.getDetails()
                                  .getDescription())
                 .setTaskStatus(DRAFT);
    }

    /*
     * Rejections
     **************/

    private static CannotUpdateTaskDescription rejection(UpdateTaskDescription cmd)
            throws CannotUpdateTaskDescription {
        RejectedTaskCommandDetails commandDetails =
                detailsOf(cmd.getId());
        DescriptionUpdateRejected descriptionUpdateRejected = DescriptionUpdateRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .vBuild();
        CannotUpdateTaskDescription rejection = CannotUpdateTaskDescription
                .newBuilder()
                .setRejectionDetails(descriptionUpdateRejected)
                .build();
        throw rejection;
    }

    private static CannotCreateDraft rejection(CreateDraft cmd) throws CannotCreateDraft {
        RejectedTaskCommandDetails commandDetails = detailsOf(cmd.getId());
        CreateDraftRejected createDraftRejected = CreateDraftRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .vBuild();
        CannotCreateDraft rejection = CannotCreateDraft
                .newBuilder()
                .setRejectionDetails(createDraftRejected)
                .build();
        throw rejection;
    }

    private static CannotReopenTask rejection(ReopenTask cmd) throws CannotReopenTask {
        RejectedTaskCommandDetails commandDetails =
                detailsOf(cmd.getId());
        ReopenTaskRejected reopenTaskRejected = ReopenTaskRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .vBuild();
        CannotReopenTask rejection = CannotReopenTask
                .newBuilder()
                .setRejectionDetails(reopenTaskRejected)
                .build();
        throw rejection;
    }

    private static CannotRestoreDeletedTask rejection(RestoreDeletedTask cmd)
            throws CannotRestoreDeletedTask {
        RejectedTaskCommandDetails commandDetails = detailsOf(cmd.getId());
        RestoreDeletedTaskRejected restoreTaskRejected = RestoreDeletedTaskRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .vBuild();
        CannotRestoreDeletedTask rejection = CannotRestoreDeletedTask
                .newBuilder()
                .setRejectionDetails(restoreTaskRejected)
                .build();
        throw rejection;
    }

    private static CannotDeleteTask rejection(DeleteTask cmd) throws CannotDeleteTask {
        RejectedTaskCommandDetails commandDetails = detailsOf(cmd.getId());
        DeleteTaskRejected deleteTaskRejected = DeleteTaskRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .vBuild();
        CannotDeleteTask rejection = CannotDeleteTask
                .newBuilder()
                .setRejectionDetails(deleteTaskRejected)
                .build();
        throw rejection;
    }

    private static CannotFinalizeDraft rejection(FinalizeDraft cmd) throws CannotFinalizeDraft {
        RejectedTaskCommandDetails commandDetails = detailsOf(cmd.getId());
        FinalizeDraftRejected finalizeDraftRejected = FinalizeDraftRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .vBuild();
        CannotFinalizeDraft rejection = CannotFinalizeDraft
                .newBuilder()
                .setRejectionDetails(finalizeDraftRejected)
                .build();
        throw rejection;
    }

    private static CannotCompleteTask rejection(CompleteTask cmd) throws CannotCompleteTask {
        RejectedTaskCommandDetails commandDetails = detailsOf(cmd.getId());
        CompleteTaskRejected completeTaskRejected = CompleteTaskRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .vBuild();
        CannotCompleteTask rejection = CannotCompleteTask
                .newBuilder()
                .setRejectionDetails(completeTaskRejected)
                .build();
        throw rejection;
    }

    private static CannotUpdateTaskDueDate
    rejection(UpdateTaskDueDate cmd) throws CannotUpdateTaskDueDate {
        RejectedTaskCommandDetails commandDetails = detailsOf(cmd.getId());
        TaskDueDateUpdateRejected dueDateUpdateRejected = TaskDueDateUpdateRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .vBuild();
        CannotUpdateTaskDueDate rejection = CannotUpdateTaskDueDate
                .newBuilder()
                .setRejectionDetails(dueDateUpdateRejected)
                .build();
        throw rejection;
    }

    private static CannotUpdateTaskDueDate
    rejection(UpdateTaskDueDate cmd, ValueMismatch mismatch) throws CannotUpdateTaskDueDate {
        RejectedTaskCommandDetails commandDetails =
                detailsOf(cmd.getId());
        TaskDueDateUpdateRejected dueDateUpdateRejected = TaskDueDateUpdateRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .setDueDateMismatch(mismatch)
                .vBuild();
        CannotUpdateTaskDueDate rejection = CannotUpdateTaskDueDate
                .newBuilder()
                .setRejectionDetails(dueDateUpdateRejected)
                .build();
        throw rejection;
    }

    private static CannotUpdateTaskDescription
    rejection(UpdateTaskDescription cmd, ValueMismatch mismatch)
            throws CannotUpdateTaskDescription {
        RejectedTaskCommandDetails commandDetails = detailsOf(cmd.getId());
        DescriptionUpdateRejected descriptionUpdateRejected = DescriptionUpdateRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .setDescriptionMismatch(mismatch)
                .vBuild();
        CannotUpdateTaskDescription rejection = CannotUpdateTaskDescription
                .newBuilder()
                .setRejectionDetails(descriptionUpdateRejected)
                .build();
        throw rejection;
    }

    private static CannotUpdateTaskPriority rejection(UpdateTaskPriority cmd)
            throws CannotUpdateTaskPriority {
        RejectedTaskCommandDetails commandDetails = detailsOf(cmd.getId());
        PriorityUpdateRejected priorityUpdateRejected = PriorityUpdateRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .vBuild();
        CannotUpdateTaskPriority rejection = CannotUpdateTaskPriority
                .newBuilder()
                .setRejectionDetails(priorityUpdateRejected)
                .build();
        throw rejection;
    }

    private static CannotUpdateTaskPriority
    rejection(UpdateTaskPriority cmd, ValueMismatch mismatch) throws CannotUpdateTaskPriority {
        RejectedTaskCommandDetails commandDetails = detailsOf(cmd.getId());
        PriorityUpdateRejected priorityUpdateRejected = PriorityUpdateRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .setPriorityMismatch(mismatch)
                .vBuild();
        CannotUpdateTaskPriority rejection = CannotUpdateTaskPriority
                .newBuilder()
                .setRejectionDetails(priorityUpdateRejected)
                .build();
        throw rejection;
    }

    private static RejectedTaskCommandDetails detailsOf(TaskId taskId) {
        return RejectedTaskCommandDetails.newBuilder()
                                         .setTaskId(taskId)
                                         .vBuild();
    }
}
