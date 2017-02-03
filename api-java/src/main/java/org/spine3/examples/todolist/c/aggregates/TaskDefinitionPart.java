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
import com.google.protobuf.Timestamp;
import org.spine3.change.StringChange;
import org.spine3.change.TimestampChange;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.PriorityChange;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabels;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskStatus;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.c.commands.ReopenTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.c.events.DeletedTaskRestored;
import org.spine3.examples.todolist.c.events.LabelledTaskRestored;
import org.spine3.examples.todolist.c.events.TaskCompleted;
import org.spine3.examples.todolist.c.events.TaskCreated;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDraftCreated;
import org.spine3.examples.todolist.c.events.TaskDraftFinalized;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.examples.todolist.c.events.TaskReopened;
import org.spine3.examples.todolist.c.failures.CannotCompleteTask;
import org.spine3.examples.todolist.c.failures.CannotCreateBasicTask;
import org.spine3.examples.todolist.c.failures.CannotCreateDraft;
import org.spine3.examples.todolist.c.failures.CannotCreateTaskWithInappropriateDescription;
import org.spine3.examples.todolist.c.failures.CannotDeleteTask;
import org.spine3.examples.todolist.c.failures.CannotFinalizeDraft;
import org.spine3.examples.todolist.c.failures.CannotReopenTask;
import org.spine3.examples.todolist.c.failures.CannotRestoreDeletedTask;
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

import static com.google.common.collect.Lists.newLinkedList;
import static org.spine3.examples.todolist.c.aggregates.AggregateHelper.generateExceptionMessage;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.TASK_DELETED_OR_COMPLETED_EXCEPTION_MESSAGE;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotCompleteTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotCreateDraftFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotCreateTaskWithInappropriateDescription;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotDeleteTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotFinalizeDraftFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotReopenTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotRestoreDeletedTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotUpdateDescriptionFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotUpdateTaskDescriptionFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotUpdateTaskDueDateFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotUpdateTaskPriorityFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotUpdateTooShortDescriptionFailure;
import static org.spine3.examples.todolist.c.aggregates.MismatchHelper.of;
import static org.spine3.examples.todolist.c.aggregates.TaskFlowValidator.isValidCreateDraftCommand;
import static org.spine3.examples.todolist.c.aggregates.TaskFlowValidator.isValidTransition;
import static org.spine3.examples.todolist.c.aggregates.TaskFlowValidator.isValidUpdateTaskDueDateCommand;
import static org.spine3.examples.todolist.c.aggregates.TaskFlowValidator.isValidUpdateTaskPriorityCommand;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings({"OverlyCoupledClass", "ClassWithTooManyMethods"})
public class TaskDefinitionPart extends AggregatePart<TaskId, TaskDefinition, TaskDefinition.Builder> {

    private static final int MIN_DESCRIPTION_LENGTH = 3;

    /**
     * {@inheritDoc}
     *
     * @param id
     */
    protected TaskDefinitionPart(TaskId id) {
        super(id);
    }

    @Assign
    List<? extends Message> handle(CreateBasicTask cmd)
            throws CannotCreateBasicTask, CannotCreateTaskWithInappropriateDescription {
        validateCommand(cmd);
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
    List<? extends Message> handle(UpdateTaskDescription cmd)
            throws CannotUpdateTaskDescription, CannotUpdateTaskWithInappropriateDescription {
        validateCommand(cmd);
        final TaskDefinition state = getState();
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
        final TaskDefinition state = getState();
        final TaskStatus taskStatus = state.getTaskStatus();
        final boolean isValid = isValidUpdateTaskDueDateCommand(taskStatus);

        final TaskId taskId = cmd.getId();

        if (!isValid) {
            throwCannotUpdateTaskDueDateFailure(taskId);
        }

        final TimestampChange change = cmd.getDueDateChange();
        final Timestamp actualDueDate = state.getDueDate();
        final Timestamp expectedDueDate = change.getPreviousValue();

        final boolean isEquals = Timestamps.compare(actualDueDate, expectedDueDate) == 0;

        if (!isEquals) {
            final Timestamp newDueDate = change.getNewValue();
            final ValueMismatch mismatch = of(expectedDueDate, actualDueDate, newDueDate, getVersion());
            throwCannotUpdateTaskDueDateFailure(taskId, mismatch);
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
        final TaskDefinition state = getState();
        final TaskStatus taskStatus = state.getTaskStatus();
        final boolean isValid = isValidUpdateTaskPriorityCommand(taskStatus);
        final TaskId taskId = cmd.getId();

        if (!isValid) {
            throwCannotUpdateTaskPriorityFailure(taskId);
        }

        final PriorityChange priorityChange = cmd.getPriorityChange();
        final TaskPriority actualPriority = state.getPriority();
        final TaskPriority expectedPriority = priorityChange.getPreviousValue();

        boolean isEquals = actualPriority == expectedPriority;

        if (!isEquals) {
            final TaskPriority newPriority = priorityChange.getNewValue();
            final ValueMismatch mismatch = of(expectedPriority, actualPriority, newPriority, getVersion());
            throwCannotUpdateTaskPriorityFailure(taskId, mismatch);
        }

        final TaskPriorityUpdated taskPriorityUpdated = TaskPriorityUpdated.newBuilder()
                                                                           .setTaskId(taskId)
                                                                           .setPriorityChange(priorityChange)
                                                                           .build();
        final List<? extends Message> result = Collections.singletonList(taskPriorityUpdated);
        return result;
    }

    @Assign
    List<? extends Message> handle(ReopenTask cmd) throws CannotReopenTask {
        final TaskDefinition state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.OPEN;
        final boolean isValid = isValidTransition(currentStatus, newStatus);
        final TaskId taskId = cmd.getId();

        if (!isValid) {
            final String message = generateExceptionMessage(currentStatus, newStatus);
            throwCannotReopenTaskFailure(taskId, message);
        }

        final TaskReopened taskReopened = TaskReopened.newBuilder()
                                                      .setTaskId(taskId)
                                                      .build();
        final List<TaskReopened> result = Collections.singletonList(taskReopened);
        return result;
    }

    @Assign
    List<? extends Message> handle(DeleteTask cmd) throws CannotDeleteTask {
        final TaskDefinition state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.DELETED;
        final TaskId taskId = cmd.getId();
        final boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            final String message = generateExceptionMessage(currentStatus, newStatus);
            throwCannotDeleteTaskFailure(taskId, message);
        }

        final TaskDeleted taskDeleted = TaskDeleted.newBuilder()
                                                   .setTaskId(taskId)
                                                   .build();
        final List<TaskDeleted> result = Collections.singletonList(taskDeleted);
        return result;
    }

    @Assign
    List<? extends Message> handle(CompleteTask cmd) throws CannotCompleteTask {
        final TaskDefinition state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.COMPLETED;
        final TaskId taskId = cmd.getId();
        final boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            final String message = generateExceptionMessage(currentStatus, newStatus);
            throwCannotCompleteTaskFailure(taskId, message);
        }

        final TaskCompleted taskCompleted = TaskCompleted.newBuilder()
                                                         .setTaskId(taskId)
                                                         .build();
        final List<TaskCompleted> result = Collections.singletonList(taskCompleted);
        return result;
    }

    @Assign
    List<? extends Message> handle(CreateDraft cmd) throws CannotCreateDraft {
        final TaskId taskId = cmd.getId();
        final boolean isValid = isValidCreateDraftCommand(getState().getTaskStatus());

        if (!isValid) {
            throwCannotCreateDraftFailure(taskId);
        }

        final TaskDraftCreated draftCreated = TaskDraftCreated.newBuilder()
                                                              .setId(taskId)
                                                              .setDraftCreationTime(Timestamps.getCurrentTime())
                                                              .build();
        final List<TaskDraftCreated> result = Collections.singletonList(draftCreated);
        return result;
    }

    @Assign
    List<? extends Message> handle(FinalizeDraft cmd) throws CannotFinalizeDraft {
        final TaskStatus currentStatus = getState().getTaskStatus();
        final TaskStatus newStatus = TaskStatus.FINALIZED;
        final TaskId taskId = cmd.getId();
        final boolean isValid = isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            final String message = generateExceptionMessage(currentStatus, newStatus);
            throwCannotFinalizeDraftFailure(taskId, message);
        }

        final TaskDraftFinalized taskDraftFinalized = TaskDraftFinalized.newBuilder()
                                                                        .setTaskId(taskId)
                                                                        .build();
        final List<TaskDraftFinalized> result = Collections.singletonList(taskDraftFinalized);
        return result;
    }

    @Assign
    List<? extends Message> handle(RestoreDeletedTask cmd) throws CannotRestoreDeletedTask {
        final TaskStatus currentStatus = getState().getTaskStatus();
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

        final TaskAggregateRoot root = TaskAggregateRoot.get(taskId);
        final TaskLabels taskLabels = root.getTaskLabelsState();
        for (TaskLabel label : taskLabels.getLabelsList()) {
            final LabelledTaskRestored labelledTaskRestored = LabelledTaskRestored.newBuilder()
                                                                                  .setTaskId(taskId)
                                                                                  .setLabelId(label.getId())
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
                    .setCreated(Timestamps.getCurrentTime())
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

    private static void validateCommand(CreateBasicTask cmd)
            throws CannotCreateBasicTask, CannotCreateTaskWithInappropriateDescription {
        final String description = cmd.getDescription();
        if (description != null && description.length() < MIN_DESCRIPTION_LENGTH) {
            final TaskId taskId = cmd.getId();
            throwCannotCreateTaskWithInappropriateDescription(taskId);
        }
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
            throwCannotUpdateTaskDescriptionFailure(taskId, TASK_DELETED_OR_COMPLETED_EXCEPTION_MESSAGE);
        }
    }
}
