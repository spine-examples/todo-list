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
import com.google.protobuf.Timestamp;
import io.spine.change.TimestampChange;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskDescriptionChange;
import io.spine.examples.todolist.Task;
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
import io.spine.examples.todolist.c.failures.CannotCompleteTask;
import io.spine.examples.todolist.c.failures.CannotCreateDraft;
import io.spine.examples.todolist.c.failures.CannotDeleteTask;
import io.spine.examples.todolist.c.failures.CannotFinalizeDraft;
import io.spine.examples.todolist.c.failures.CannotReopenTask;
import io.spine.examples.todolist.c.failures.CannotRestoreDeletedTask;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskDescription;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskDueDate;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskPriority;
import io.spine.server.aggregate.AggregatePart;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.examples.todolist.c.aggregate.MismatchHelper.of;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.ensureCompleted;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.ensureDeleted;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.ensureNeitherCompletedNorDeleted;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidCreateDraftCommand;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidTransition;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidUpdateTaskDueDateCommand;
import static io.spine.examples.todolist.c.aggregate.TaskFlowValidator.isValidUpdateTaskPriorityCommand;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.ChangeStatusFailures.throwCannotCompleteTask;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.ChangeStatusFailures.throwCannotDeleteTask;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.ChangeStatusFailures.throwCannotFinalizeDraft;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.ChangeStatusFailures.throwCannotReopenTask;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.ChangeStatusFailures.throwCannotRestoreDeletedTask;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.TaskCreationFailures.throwCannotCreateDraftFailure;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.UpdateFailures.throwCannotUpdateDescription;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.UpdateFailures.throwCannotUpdateTaskDescription;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.UpdateFailures.throwCannotUpdateTaskDueDate;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.UpdateFailures.throwCannotUpdateTaskPriority;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.UpdateFailures.throwCannotUpdateTooShortDescription;
import static io.spine.time.Time.getCurrentTime;
import static io.spine.time.Timestamps2.compare;
import static java.util.Collections.singletonList;

/**
 * The aggregate managing the state of a {@link Task}.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings({"ClassWithTooManyMethods", /* Task definition cannot be separated and should
                                                 process all commands and events related to it
                                                 according to the domain model.
                                                 The {@code AggregatePart} does it with methods
                                                 annotated as {@code Assign} and {@code Apply}.
                                                 In that case class has too many methods.*/
        "OverlyCoupledClass"}) /* As each method needs dependencies  necessary to perform execution
                                                 that class also overly coupled.*/
public class TaskPart extends AggregatePart<TaskId,
                                            Task,
                                            TaskVBuilder,
                                            TaskAggregateRoot> {

    private static final TaskDescription DEFAULT_DRAFT_DESCRIPTION =
            TaskDescription.newBuilder()
                           .setValue("Task description goes here.")
                           .build();

    /**
     * {@inheritDoc}
     *
     * @param root
     */
    public TaskPart(TaskAggregateRoot root) {
        super(root);
    }

    @Assign
    List<? extends Message> handle(CreateBasicTask cmd) {
        final TaskId taskId = cmd.getId();
        final TaskDetails.Builder taskDetails = TaskDetails.newBuilder()
                                                           .setDescription(cmd.getDescription());
        final TaskCreated result = TaskCreated.newBuilder()
                                              .setId(taskId)
                                              .setDetails(taskDetails)
                                              .build();
        return singletonList(result);
    }

    @Assign
    List<? extends Message> handle(UpdateTaskDescription cmd) throws CannotUpdateTaskDescription {
        boolean isValid = ensureNeitherCompletedNorDeleted(getState().getTaskStatus());
        if (!isValid) {
            throwCannotUpdateTaskDescription(cmd);
        }

        final TaskDescriptionChange change = cmd.getDescriptionChange();
        final TaskDescription actualDescription = getState().getDescription();
        final TaskDescription expectedDescription = change.getPreviousDescription();
        final boolean isEquals = actualDescription.equals(expectedDescription);

        if (!isEquals) {
            final ValueMismatch mismatch = unexpectedValue(expectedDescription, actualDescription,
                                                           change.getNewDescription());
            throwCannotUpdateDescription(cmd, mismatch);
        }

        final TaskId taskId = cmd.getId();
        final TaskDescriptionUpdated taskDescriptionUpdated =
                TaskDescriptionUpdated.newBuilder()
                                      .setTaskId(taskId)
                                      .setDescriptionChange(change)
                                      .build();
        return singletonList(taskDescriptionUpdated);
    }

    @Assign
    List<? extends Message> handle(UpdateTaskDueDate cmd) throws CannotUpdateTaskDueDate {
        final Task state = getState();
        final TaskStatus taskStatus = state.getTaskStatus();
        final boolean isValid = isValidUpdateTaskDueDateCommand(taskStatus);

        if (!isValid) {
            throwCannotUpdateTaskDueDate(cmd);
        }

        final TimestampChange change = cmd.getDueDateChange();
        final Timestamp actualDueDate = state.getDueDate();
        final Timestamp expectedDueDate = change.getPreviousValue();

        final boolean isEquals = compare(actualDueDate, expectedDueDate) == 0;

        if (!isEquals) {
            final Timestamp newDueDate = change.getNewValue();
            final ValueMismatch mismatch = unexpectedValue(expectedDueDate, actualDueDate,
                                                           newDueDate);
            throwCannotUpdateTaskDueDate(cmd, mismatch);
        }

        final TaskId taskId = cmd.getId();
        final TaskDueDateUpdated taskDueDateUpdated =
                TaskDueDateUpdated.newBuilder()
                                  .setTaskId(taskId)
                                  .setDueDateChange(cmd.getDueDateChange())
                                  .build();
        return singletonList(taskDueDateUpdated);
    }

    @Assign
    List<? extends Message> handle(UpdateTaskPriority cmd) throws CannotUpdateTaskPriority {
        final Task state = getState();
        final TaskStatus taskStatus = state.getTaskStatus();
        final boolean isValid = isValidUpdateTaskPriorityCommand(taskStatus);

        if (!isValid) {
            throwCannotUpdateTaskPriority(cmd);
        }

        final PriorityChange priorityChange = cmd.getPriorityChange();
        final TaskPriority actualPriority = state.getPriority();
        final TaskPriority expectedPriority = priorityChange.getPreviousValue();

        boolean isEquals = actualPriority == expectedPriority;

        if (!isEquals) {
            final TaskPriority newPriority = priorityChange.getNewValue();
            final ValueMismatch mismatch = of(expectedPriority, actualPriority, newPriority,
                                              getVersion());
            throwCannotUpdateTaskPriority(cmd, mismatch);
        }

        final TaskId taskId = cmd.getId();
        final TaskPriorityUpdated taskPriorityUpdated = TaskPriorityUpdated.newBuilder()
                                                                           .setTaskId(taskId)
                                                                           .setPriorityChange(
                                                                                   priorityChange)
                                                                           .build();
        return singletonList(taskPriorityUpdated);
    }

    @Assign
    List<? extends Message> handle(ReopenTask cmd) throws CannotReopenTask {
        final Task state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final boolean isValid = ensureCompleted(currentStatus);

        if (!isValid) {
            throwCannotReopenTask(cmd);
        }

        final TaskId taskId = cmd.getId();
        final TaskReopened taskReopened = TaskReopened.newBuilder()
                                                      .setTaskId(taskId)
                                                      .build();
        return singletonList(taskReopened);
    }

    @Assign
    List<? extends Message> handle(DeleteTask cmd) throws CannotDeleteTask {
        final Task state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.DELETED;
        final boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            throwCannotDeleteTask(cmd);
        }

        final TaskId taskId = cmd.getId();
        final TaskDeleted taskDeleted = TaskDeleted.newBuilder()
                                                   .setTaskId(taskId)
                                                   .build();
        return singletonList(taskDeleted);
    }

    @Assign
    List<? extends Message> handle(CompleteTask cmd) throws CannotCompleteTask {
        final Task state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.COMPLETED;
        final boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            throwCannotCompleteTask(cmd);
        }

        final TaskId taskId = cmd.getId();
        final TaskCompleted taskCompleted = TaskCompleted.newBuilder()
                                                         .setTaskId(taskId)
                                                         .build();
        return singletonList(taskCompleted);
    }

    @Assign
    List<? extends Message> handle(CreateDraft cmd) throws CannotCreateDraft {
        final boolean isValid = isValidCreateDraftCommand(getState().getTaskStatus());

        if (!isValid) {
            throwCannotCreateDraftFailure(cmd);
        }

        final TaskId taskId = cmd.getId();
        final TaskDraftCreated draftCreated =
                TaskDraftCreated.newBuilder()
                                .setId(taskId)
                                .setDraftCreationTime(getCurrentTime())
                                .setDetails(TaskDetails.newBuilder()
                                                       .setDescription(DEFAULT_DRAFT_DESCRIPTION))
                                .build();
        return singletonList(draftCreated);
    }

    @Assign
    List<? extends Message> handle(FinalizeDraft cmd) throws CannotFinalizeDraft {
        final TaskStatus currentStatus = getState().getTaskStatus();
        final TaskStatus newStatus = TaskStatus.FINALIZED;
        final boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            throwCannotFinalizeDraft(cmd);
        }

        final TaskId taskId = cmd.getId();
        final TaskDraftFinalized taskDraftFinalized = TaskDraftFinalized.newBuilder()
                                                                        .setTaskId(taskId)
                                                                        .build();
        return singletonList(taskDraftFinalized);
    }

    @Assign
    List<? extends Message> handle(RestoreDeletedTask cmd) throws CannotRestoreDeletedTask {
        final TaskStatus currentStatus = getState().getTaskStatus();
        final boolean isValid = ensureDeleted(currentStatus);
        if (!isValid) {
            throwCannotRestoreDeletedTask(cmd);
        }

        final TaskId taskId = cmd.getId();
        final DeletedTaskRestored deletedTaskRestored = DeletedTaskRestored.newBuilder()
                                                                           .setTaskId(taskId)
                                                                           .build();
        final List<Message> result = newLinkedList();
        result.add(deletedTaskRestored);

        final TaskLabels taskLabels = getPartState(TaskLabels.class);
        final List<LabelId> labelIdsList = taskLabels.getLabelIdsList()
                                                     .getIdsList();
        for (LabelId labelId : labelIdsList) {
            final LabelledTaskRestored labelledTaskRestored =
                    LabelledTaskRestored.newBuilder()
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
        final TaskDetails taskDetails = event.getDetails();
        getBuilder().setId(event.getId())
                    .setCreated(getCurrentTime())
                    .setDescription(taskDetails.getDescription())
                    .setPriority(taskDetails.getPriority())
                    .setTaskStatus(TaskStatus.FINALIZED);
    }

    @Apply
    private void taskDescriptionUpdated(TaskDescriptionUpdated event) {
        final TaskDescription newDescription = event.getDescriptionChange()
                                                    .getNewDescription();
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

    @Apply
    private void taskReopened(TaskReopened event) {
        getBuilder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void taskDeleted(TaskDeleted event) {
        getBuilder().setTaskStatus(TaskStatus.DELETED);
    }

    @Apply
    private void deletedTaskRestored(DeletedTaskRestored event) {
        getBuilder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void labelledTaskRestored(LabelledTaskRestored event) {
        getBuilder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void taskCompleted(TaskCompleted event) {
        getBuilder().setTaskStatus(TaskStatus.COMPLETED);
    }

    @Apply
    private void taskDraftFinalized(TaskDraftFinalized event) {
        getBuilder().setTaskStatus(TaskStatus.FINALIZED);
    }

    @Apply
    private void draftCreated(TaskDraftCreated event) {
        getBuilder().setId(event.getId())
                    .setCreated(event.getDraftCreationTime())
                    .setDescription(event.getDetails()
                                         .getDescription())
                    .setTaskStatus(TaskStatus.DRAFT);
    }
}
