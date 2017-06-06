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
import io.spine.examples.todolist.TaskDefinitionValidatingBuilder;
import io.spine.examples.todolist.c.aggregate.failures.TaskDefinitionPartFailures;
import io.spine.base.CommandContext;
import io.spine.change.StringChange;
import io.spine.change.TimestampChange;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskDefinition;
import io.spine.examples.todolist.TaskDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabels;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.TaskStatus;
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
import io.spine.examples.todolist.c.failures.CannotCreateTaskWithInappropriateDescription;
import io.spine.examples.todolist.c.failures.CannotDeleteTask;
import io.spine.examples.todolist.c.failures.CannotFinalizeDraft;
import io.spine.examples.todolist.c.failures.CannotReopenTask;
import io.spine.examples.todolist.c.failures.CannotRestoreDeletedTask;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskDescription;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskDueDate;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskPriority;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskWithInappropriateDescription;
import io.spine.server.aggregate.AggregatePart;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import java.util.Collections;
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
import static io.spine.examples.todolist.c.aggregate.failures.TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateTaskDescription;
import static io.spine.examples.todolist.c.aggregate.failures.TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateTaskDueDate;
import static io.spine.examples.todolist.c.aggregate.failures.TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateTaskPriority;
import static io.spine.time.Time.getCurrentTime;
import static io.spine.time.Timestamps2.compare;

/**
 * The aggregate managing the state of a {@link TaskDefinition}.
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
public class TaskDefinitionPart extends AggregatePart<TaskId,
                                                      TaskDefinition,
                                                      TaskDefinitionValidatingBuilder,
                                                      TaskAggregateRoot> {

    private static final int MIN_DESCRIPTION_LENGTH = 3;
    private static final String DEFAULT_DRAFT_DESCRIPTION = "Task description goes here.";

    /**
     * {@inheritDoc}
     *
     * @param root
     */
    public TaskDefinitionPart(TaskAggregateRoot root) {
        super(root);
    }

    @Assign
    List<? extends Message> handle(CreateBasicTask cmd, CommandContext ctx)
            throws CannotCreateTaskWithInappropriateDescription {
        validateCommand(cmd, ctx);
        final TaskId taskId = cmd.getId();

        final TaskDetails.Builder taskDetails = TaskDetails.newBuilder()
                                                           .setDescription(cmd.getDescription());
        final TaskCreated result = TaskCreated.newBuilder()
                                              .setId(taskId)
                                              .setDetails(taskDetails)
                                              .build();
        return Collections.singletonList(result);
    }

    @Assign
    List<? extends Message> handle(UpdateTaskDescription cmd, CommandContext ctx)
            throws CannotUpdateTaskDescription, CannotUpdateTaskWithInappropriateDescription {
        validateCommand(cmd, ctx);
        final TaskDefinition state = getState();
        final StringChange change = cmd.getDescriptionChange();

        final String actualDescription = state.getDescription();
        final String expectedDescription = change.getPreviousValue();
        final boolean isEquals = actualDescription.equals(expectedDescription);
        final TaskId taskId = cmd.getId();

        if (!isEquals) {
            final String newDescription = change.getNewValue();
            final ValueMismatch mismatch = of(expectedDescription, actualDescription,
                                              newDescription, getVersion());
            TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateDescription(cmd, mismatch);
        }

        final TaskDescriptionUpdated taskDescriptionUpdated =
                TaskDescriptionUpdated.newBuilder()
                                      .setTaskId(taskId)
                                      .setDescriptionChange(change)
                                      .build();
        final List<? extends Message> result = Collections.singletonList(taskDescriptionUpdated);
        return result;
    }

    @Assign
    List<? extends Message> handle(UpdateTaskDueDate cmd, CommandContext ctx)
            throws CannotUpdateTaskDueDate {
        final TaskDefinition state = getState();
        final TaskStatus taskStatus = state.getTaskStatus();
        final boolean isValid = isValidUpdateTaskDueDateCommand(taskStatus);

        final TaskId taskId = cmd.getId();

        if (!isValid) {
            TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateTaskDueDate(cmd);
        }

        final TimestampChange change = cmd.getDueDateChange();
        final Timestamp actualDueDate = state.getDueDate();
        final Timestamp expectedDueDate = change.getPreviousValue();

        final boolean isEquals = compare(actualDueDate, expectedDueDate) == 0;

        if (!isEquals) {
            final Timestamp newDueDate = change.getNewValue();
            final ValueMismatch mismatch = of(expectedDueDate, actualDueDate,
                                              newDueDate, getVersion());
            TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateTaskDueDate(cmd, mismatch);
        }

        final TaskDueDateUpdated taskDueDateUpdated =
                TaskDueDateUpdated.newBuilder()
                                  .setTaskId(taskId)
                                  .setDueDateChange(cmd.getDueDateChange())
                                  .build();
        final List<? extends Message> result = Collections.singletonList(taskDueDateUpdated);
        return result;
    }

    @Assign
    List<? extends Message> handle(UpdateTaskPriority cmd, CommandContext ctx)
            throws CannotUpdateTaskPriority {
        final TaskDefinition state = getState();
        final TaskStatus taskStatus = state.getTaskStatus();
        final boolean isValid = isValidUpdateTaskPriorityCommand(taskStatus);
        final TaskId taskId = cmd.getId();

        if (!isValid) {
            TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateTaskPriority(cmd);
        }

        final PriorityChange priorityChange = cmd.getPriorityChange();
        final TaskPriority actualPriority = state.getPriority();
        final TaskPriority expectedPriority = priorityChange.getPreviousValue();

        boolean isEquals = actualPriority.equals(expectedPriority);

        if (!isEquals) {
            final TaskPriority newPriority = priorityChange.getNewValue();
            final ValueMismatch mismatch = of(expectedPriority, actualPriority, newPriority,
                                              getVersion());
            TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateTaskPriority(cmd, mismatch);
        }

        final TaskPriorityUpdated taskPriorityUpdated = TaskPriorityUpdated.newBuilder()
                                                                           .setTaskId(taskId)
                                                                           .setPriorityChange(
                                                                                   priorityChange)
                                                                           .build();
        final List<? extends Message> result = Collections.singletonList(taskPriorityUpdated);
        return result;
    }

    @Assign
    List<? extends Message> handle(ReopenTask cmd, CommandContext ctx) throws CannotReopenTask {
        final TaskDefinition state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final boolean isValid = ensureCompleted(currentStatus);
        final TaskId taskId = cmd.getId();

        if (!isValid) {
            TaskDefinitionPartFailures.ChangeStatusFailures.throwCannotReopenTask(cmd);
        }

        final TaskReopened taskReopened = TaskReopened.newBuilder()
                                                      .setTaskId(taskId)
                                                      .build();
        final List<TaskReopened> result = Collections.singletonList(taskReopened);
        return result;
    }

    @Assign
    List<? extends Message> handle(DeleteTask cmd, CommandContext ctx) throws CannotDeleteTask {
        final TaskDefinition state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.DELETED;
        final TaskId taskId = cmd.getId();
        final boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            TaskDefinitionPartFailures.ChangeStatusFailures.throwCannotDeleteTask(cmd);
        }

        final TaskDeleted taskDeleted = TaskDeleted.newBuilder()
                                                   .setTaskId(taskId)
                                                   .build();
        final List<TaskDeleted> result = Collections.singletonList(taskDeleted);
        return result;
    }

    @Assign
    List<? extends Message> handle(CompleteTask cmd, CommandContext ctx) throws CannotCompleteTask {
        final TaskDefinition state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.COMPLETED;
        final TaskId taskId = cmd.getId();
        final boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            TaskDefinitionPartFailures.ChangeStatusFailures.throwCannotCompleteTask(cmd);
        }

        final TaskCompleted taskCompleted = TaskCompleted.newBuilder()
                                                         .setTaskId(taskId)
                                                         .build();
        final List<TaskCompleted> result = Collections.singletonList(taskCompleted);
        return result;
    }

    @Assign
    List<? extends Message> handle(CreateDraft cmd, CommandContext ctx) throws CannotCreateDraft {
        final TaskId taskId = cmd.getId();
        final boolean isValid = isValidCreateDraftCommand(getState().getTaskStatus());

        if (!isValid) {
            TaskDefinitionPartFailures.TaskCreationFailures.throwCannotCreateDraftFailure(cmd);
        }

        final TaskDraftCreated draftCreated =
                TaskDraftCreated.newBuilder()
                                .setId(taskId)
                                .setDraftCreationTime(getCurrentTime())
                                .setDetails(TaskDetails.newBuilder()
                                                       .setDescription(DEFAULT_DRAFT_DESCRIPTION))
                                .build();
        final List<TaskDraftCreated> result = Collections.singletonList(draftCreated);
        return result;
    }

    @Assign
    List<? extends Message> handle(FinalizeDraft cmd, CommandContext ctx) throws
                                                                          CannotFinalizeDraft {
        final TaskStatus currentStatus = getState().getTaskStatus();
        final TaskStatus newStatus = TaskStatus.FINALIZED;
        final TaskId taskId = cmd.getId();
        final boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            TaskDefinitionPartFailures.ChangeStatusFailures.throwCannotFinalizeDraft(cmd);
        }

        final TaskDraftFinalized taskDraftFinalized = TaskDraftFinalized.newBuilder()
                                                                        .setTaskId(taskId)
                                                                        .build();
        final List<TaskDraftFinalized> result = Collections.singletonList(taskDraftFinalized);
        return result;
    }

    @Assign
    List<? extends Message> handle(RestoreDeletedTask cmd, CommandContext ctx)
            throws CannotRestoreDeletedTask {
        final TaskStatus currentStatus = getState().getTaskStatus();
        final TaskId taskId = cmd.getId();
        final boolean isValid = ensureDeleted(currentStatus);
        if (!isValid) {
            TaskDefinitionPartFailures.ChangeStatusFailures.throwCannotRestoreDeletedTask(cmd);
        }

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

    private static void validateCommand(CreateBasicTask cmd, CommandContext ctx)
            throws CannotCreateTaskWithInappropriateDescription {
        final String description = cmd.getDescription();
        if (description != null && description.length() < MIN_DESCRIPTION_LENGTH) {
            TaskDefinitionPartFailures.TaskCreationFailures.throwCannotCreateTaskWithInappropriateDescriptionFailure(cmd);
        }
    }

    private void validateCommand(UpdateTaskDescription cmd, CommandContext ctx)
            throws CannotUpdateTaskDescription, CannotUpdateTaskWithInappropriateDescription {
        final String description = cmd.getDescriptionChange()
                                      .getNewValue();

        if (description != null && description.length() < MIN_DESCRIPTION_LENGTH) {
            TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateTooShortDescription(cmd);
        }

        boolean isValid = ensureNeitherCompletedNorDeleted(getState().getTaskStatus());

        if (!isValid) {
            TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateTaskDescription(cmd);
        }
    }
}
