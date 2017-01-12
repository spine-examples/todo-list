/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.aggregate;

import com.google.common.base.Throwables;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CannotUpdateTaskDescription;
import org.spine3.examples.todolist.CannotUpdateTaskDueDate;
import org.spine3.examples.todolist.CannotUpdateTaskPriority;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.LabelledTaskRestored;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.Task;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskPriorityValue;
import org.spine3.examples.todolist.TaskStatus;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;
import org.spine3.protobuf.Timestamps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.TaskStatus.COMPLETED;
import static org.spine3.examples.todolist.TaskStatus.DELETED;
import static org.spine3.examples.todolist.TaskStatus.DRAFT;
import static org.spine3.examples.todolist.TaskStatus.FINALIZED;
import static org.spine3.examples.todolist.TaskStatus.OPEN;
import static org.spine3.examples.todolist.TaskStatus.TS_UNDEFINED;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DUE_DATE;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.LABEL_ID;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.TASK_ID;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.removeLabelFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static org.spine3.protobuf.AnyPacker.unpack;

/**
 * @author Illia Shepilov
 */
public class TaskAggregateShould {

    private static final CommandContext COMMAND_CONTEXT = createCommandContext();
    private static final String COMPLETED_TASK_EXCEPTION_MESSAGE = "Command cannot be applied to the completed task.";
    private static final String DELETED_TASK_EXCEPTION_MESSAGE = "Command cannot be applied to the deleted task.";
    private static final String INAPPROPRIATE_TASK_DESCRIPTION = "Description should contain " +
            "at least 3 alphanumeric symbols.";
    private TaskAggregate aggregate;

    private static final TaskId ID = TaskId.newBuilder()
                                           .setValue(newUuid())
                                           .build();

    @BeforeEach
    public void setUp() {
        aggregate = new TaskAggregate(ID);
    }

    @Test
    public void accept_Message_id_to_constructor() {
        try {
            final TaskAggregate aggregate = new TaskAggregate(ID);
            assertEquals(ID, aggregate.getId());
        } catch (Throwable e) {
            fail("Error accepting message ID due to " + e.getMessage());
        }
    }

    @Test
    public void emit_task_description_updated_event_upon_update_task_description_command() {
        final CreateBasicTask createBasicTask = createTaskInstance();
        aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);
        final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskDescriptionUpdated.class, messageList.get(0)
                                                              .getClass());
        final TaskDescriptionUpdated taskDescriptionUpdated = (TaskDescriptionUpdated) messageList.get(0);

        assertEquals(TASK_ID, taskDescriptionUpdated.getId());
        final String newDescription = taskDescriptionUpdated.getDescriptionChange()
                                                            .getNewValue();
        assertEquals(DESCRIPTION, newDescription);
    }

    @Test
    public void emit_task_created_event_upon_create_task_command() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        assertNotNull(aggregate.getState()
                               .getCreated());
        assertNotNull(aggregate.getId());

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskCreated.class, messageList.get(0)
                                                   .getClass());
        final TaskCreated taskCreated = (TaskCreated) messageList.get(0);

        assertEquals(TASK_ID, taskCreated.getId());
        assertEquals(DESCRIPTION, taskCreated.getDetails()
                                             .getDescription());
    }

    @Test
    public void emit_label_removed_from_task_event_upon_remove_label_from_task_command() {
        final RemoveLabelFromTask removeLabelFromTaskCmd = removeLabelFromTaskInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(removeLabelFromTaskCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(LabelRemovedFromTask.class, messageList.get(0)
                                                            .getClass());
        final LabelRemovedFromTask labelRemovedFromTask = (LabelRemovedFromTask) messageList.get(0);

        assertEquals(TASK_ID, labelRemovedFromTask.getId());
        assertEquals(LABEL_ID, labelRemovedFromTask.getLabelId());
    }

    @Test
    public void emit_label_assigned_to_task_event_upon_assign_label_to_task_command() {
        final AssignLabelToTask assignLabelToTaskCmd = assignLabelToTaskInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(assignLabelToTaskCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(LabelAssignedToTask.class, messageList.get(0)
                                                           .getClass());
        final LabelAssignedToTask labelAssignedToTask = (LabelAssignedToTask) messageList.get(0);

        assertEquals(TASK_ID, labelAssignedToTask.getTaskId());
        assertEquals(LABEL_ID, labelAssignedToTask.getLabelId());
    }

    @Test
    public void catch_exception_when_handle_update_task_description_when_description_contains_one_symbol() {
        try {
            UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance(TASK_ID, "", ".");
            updateTaskDescriptionCmd = updateTaskDescriptionInstance(TASK_ID, "", "description");
            aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(INAPPROPRIATE_TASK_DESCRIPTION, cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_update_task_description_when_task_is_deleted() {
        try {
            final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance();
            aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);

            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(DELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void catch_exception_handle_update_task_description_when_task_is_completed() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance();
            aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(COMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_update_task_due_date_when_task_is_completed() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance();
            aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(COMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_update_task_due_date_when_task_is_deleted() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance();
            aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(DELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void catch_exception_handle_update_task_priority_when_task_is_deleted() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance();
            aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(DELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void catch_exception_handle_update_task_priority_when_task_is_completed() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance();
            aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(COMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void emit_task_due_date_updated_event_upon_update_task_due_date_command() {
        final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskDueDateUpdated.class, messageList.get(0)
                                                          .getClass());
        final TaskDueDateUpdated taskDueDateUpdated = (TaskDueDateUpdated) messageList.get(0);

        assertEquals(TASK_ID, taskDueDateUpdated.getId());
        final Timestamp newDueDate = taskDueDateUpdated.getDueDateChange()
                                                       .getNewValue();
        assertEquals(DUE_DATE, newDueDate);
    }

    @Test
    public void emit_task_priority_updated_event_upon_update_task_priority_command() {
        final CreateBasicTask createBasicTask = createTaskInstance();
        aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

        final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskPriorityUpdated.class, messageList.get(0)
                                                           .getClass());
        final TaskPriorityUpdated taskPriorityUpdated = (TaskPriorityUpdated) messageList.get(0);

        assertEquals(TASK_ID, taskPriorityUpdated.getId());
        final TaskPriority newPriority = taskPriorityUpdated.getPriorityChange()
                                                            .getNewValue();
        assertEquals(TaskPriority.HIGH, newPriority);
    }

    @Test
    public void emit_task_completed_event_upon_complete_task_command() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        final CompleteTask completeTaskCmd = completeTaskInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskCompleted.class, messageList.get(0)
                                                     .getClass());
        final TaskCompleted taskCompleted = (TaskCompleted) messageList.get(0);

        assertEquals(TASK_ID, taskCompleted.getId());
    }

    @Test
    public void update_current_state_task_description_after_dispatch_command() {
        final String newDescription = "new description.";
        final CreateBasicTask createBasicTask = createTaskInstance();
        aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(TASK_ID, DESCRIPTION, newDescription);
        aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
        final Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(newDescription, state.getDescription());
    }

    @Test
    public void update_current_state_task_due_date_after_dispatch_command() {
        final Timestamp updatedDueDate = Timestamps.getCurrentTime();
        final CreateBasicTask createBasicTask = createTaskInstance();
        aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

        final UpdateTaskDueDate updateTaskDueDateCmd =
                updateTaskDueDateInstance(TASK_ID, Timestamp.getDefaultInstance(), updatedDueDate);
        aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);
        final Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(updatedDueDate, state.getDueDate());
    }

    @Test
    public void update_current_state_task_priority_after_dispatch_command() {
        final TaskPriority updatedPriority = TaskPriority.HIGH;
        final CreateBasicTask createBasicTask = createTaskInstance();
        aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

        final UpdateTaskPriority updateTaskPriorityCmd =
                updateTaskPriorityInstance(TASK_ID, TaskPriority.TP_UNDEFINED, updatedPriority);
        aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);
        final Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(updatedPriority, state.getPriority());
    }

    @Test
    public void change_task_status_state_when_task_is_completed() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        final CompleteTask completeTaskCmd = completeTaskInstance();
        aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
        final Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(COMPLETED, state.getTaskStatus());
    }

    @Test
    public void change_task_status_state_when_task_is_deleted() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        final DeleteTask deleteTaskCmd = deleteTaskInstance();
        aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
        final Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(DELETED, state.getTaskStatus());
    }

    @Test
    public void record_modification_timestamp() throws InterruptedException {
        CreateBasicTask createTaskCmd = createTaskInstance();
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        Task state = aggregate.getState();
        final Timestamp firstStateCreationTime = state.getCreated();

        assertEquals(TASK_ID, state.getId());

        Thread.sleep(1000);

        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        state = aggregate.getState();
        final Timestamp secondStateCreationTime = state.getCreated();

        assertEquals(TASK_ID, state.getId());
        assertTrue(Timestamps.isLaterThan(secondStateCreationTime, firstStateCreationTime));
    }

    @Test
    public void emit_task_created_and_task_deleted_events_upon_create_basic_task_and_delete_task_commands() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        assertNotEquals(DELETED, aggregate.getState()
                                          .getTaskStatus());

        final DeleteTask deleteTaskCmd = deleteTaskInstance();
        aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

        assertEquals(DELETED, aggregate.getState()
                                       .getTaskStatus());
    }

    @Test
    public void emit_deleted_task_restored_event_upon_restore_deleted_task_command() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        final DeleteTask deleteTaskCmd = deleteTaskInstance();
        aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

        final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
        aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);

        final Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    public void catch_exception_when_handle_restore_task_command_when_task_is_completed() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
            aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(COMPLETED, DRAFT), cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_restore_deleted_task_command_when_task_is_created() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
            aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(FINALIZED, OPEN), cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_restore_deleted_task_command_when_task_in_draft_state() {
        try {
            final CreateDraft createDraftCmd = createDraftInstance();
            aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

            final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
            aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(DRAFT, OPEN), cause.getMessage());
        }
    }

    @Test
    public void emit_task_draft_finalized_event_upon_finalize_draft_command() {
        final CreateDraft createDraftCmd = createDraftInstance();
        aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

        final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance();
        Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(DRAFT, state.getTaskStatus());

        aggregate.dispatchForTest(finalizeDraftCmd, COMMAND_CONTEXT);
        state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(FINALIZED, state.getTaskStatus());
    }

    @Test
    public void emit_task_draft_created_event_upon_create_draft_task_command() {
        final CreateDraft createDraftCmd = createDraftInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskDraftCreated.class, messageList.get(0)
                                                        .getClass());
        final TaskDraftCreated taskDraftCreated = (TaskDraftCreated) messageList.get(0);

        assertEquals(TASK_ID, taskDraftCreated.getId());
    }

    @Test
    public void catch_exception_when_handle_finalized_draft_command_when_task_is_deleted() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance();
            aggregate.dispatchForTest(finalizeDraftCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(DELETED, FINALIZED), cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_finalized_draft_command_when_task_not_in_the_draft_state() {
        try {
            final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance();
            aggregate.dispatchForTest(finalizeDraftCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(TS_UNDEFINED, FINALIZED), cause.getMessage());
        }
    }

    @Test
    public void emit_task_draft_created_event_upon_create_task_draft_command() {
        final CreateDraft createDraftCmd = createDraftInstance();
        aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);
        final Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(DRAFT, state.getTaskStatus());
    }

    @Test
    public void catch_exception_when_handle_complete_task_command_when_task_is_deleted() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(DELETED, COMPLETED), cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_complete_task_command_when_task_in_draft_state() {
        try {
            final CreateDraft createDraftCmd = createDraftInstance();
            aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(DRAFT, COMPLETED), cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_reopen_task_command_when_task_created() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final ReopenTask reopenTaskCmd = reopenTaskInstance();
            aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(FINALIZED, OPEN), cause.getMessage());
        }
    }

    @Test
    public void emit_task_completed_and_task_reopened_events_upon_complete_and_reopen_task_command() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        final CompleteTask completeTaskCmd = completeTaskInstance();
        aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

        Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(COMPLETED, state.getTaskStatus());

        final ReopenTask reopenTaskCmd = reopenTaskInstance();
        aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);
        state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    public void catch_exception_when_handle_reopen_task_command_when_task_is_created() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final ReopenTask reopenTaskCmd = reopenTaskInstance();
            aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(FINALIZED, OPEN), cause.getMessage());
        }
    }

    @Test
    public void emit_task_reopened_event_when_handle_reopen_task_command_when_task_is_deleted() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        final DeleteTask deleteTaskCmd = deleteTaskInstance();
        aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

        final ReopenTask reopenTaskCmd = reopenTaskInstance();
        aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);

        final Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    public void catch_exception_when_handle_reopen_task_command_when_task_in_draft_state() {
        try {
            final CreateDraft createDraftCmd = createDraftInstance();
            aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

            final ReopenTask reopenTaskCmd = reopenTaskInstance();
            aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(DRAFT, OPEN), cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_assign_label_to_task_command_when_task_is_deleted() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            final AssignLabelToTask assignLabelToTaskCmd = assignLabelToTaskInstance();
            aggregate.dispatchForTest(assignLabelToTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(DELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_assign_label_to_task_command_when_task_is_completed() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            final AssignLabelToTask assignLabelToTaskCmd = assignLabelToTaskInstance();
            aggregate.dispatchForTest(assignLabelToTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(COMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_remove_label_from_task_when_task_is_completed() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            final RemoveLabelFromTask removeLabelFromTaskCmd = removeLabelFromTaskInstance();
            aggregate.dispatchForTest(removeLabelFromTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(COMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_remove_label_from_task_when_task_is_deleted() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            final RemoveLabelFromTask removeLabelFromTaskCmd = removeLabelFromTaskInstance();
            aggregate.dispatchForTest(removeLabelFromTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(DELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void catch_exception_when_handle_delete_task_command_when_task_is_already_deleted() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals(constructExceptionMessage(DELETED, DELETED), cause.getMessage());
        }
    }

    @Test
    public void emit_task_deleted_and_deleted_task_restored_event_upon_delete_and_restore_task_command() {
        final CreateDraft createDraftCmd = createDraftInstance();
        aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

        final DeleteTask deleteTaskCmd = deleteTaskInstance();
        aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

        Task state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(DELETED, state.getTaskStatus());

        final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
        aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);

        state = aggregate.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    public void emit_task_deleted_event_upon_delete_task_command() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        final DeleteTask deleteTaskCmd = deleteTaskInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskDeleted.class, messageList.get(0)
                                                   .getClass());
        final TaskDeleted taskDeleted = (TaskDeleted) messageList.get(0);

        assertEquals(TASK_ID, taskDeleted.getId());
    }

    @Test
    public void emit_labelled_task_restored_event_upon_restore_task_command_when_task_has_label() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        final AssignLabelToTask assignLabelToTaskCmd = assignLabelToTaskInstance();
        aggregate.dispatchForTest(assignLabelToTaskCmd, COMMAND_CONTEXT);

        final DeleteTask deleteTaskCmd = deleteTaskInstance();
        aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

        final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);

        final int expectedListSize = 2;
        assertEquals(expectedListSize, messageList.size());

        final LabelledTaskRestored labelledTaskRestored = (LabelledTaskRestored) messageList.get(1);
        assertEquals(TASK_ID, labelledTaskRestored.getTaskId());
        assertEquals(LABEL_ID, labelledTaskRestored.getLabelId());
    }

    @Test
    public void emit_cannot_update_task_priority_failure_upon_update_task_priority_command() {
        final UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(TASK_ID, TaskPriority.LOW, TaskPriority.HIGH);
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(updateTaskPriority, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(CannotUpdateTaskPriority.class, messageList.get(0)
                                                                .getClass());
        final CannotUpdateTaskPriority cannotUpdateTaskPriority = (CannotUpdateTaskPriority) messageList.get(0);

        final ValueMismatch mismatch = cannotUpdateTaskPriority.getPriorityMismatch();
        assertEquals(TASK_ID, cannotUpdateTaskPriority.getTaskId());

        final TaskPriorityValue expectedValue = TaskPriorityValue.newBuilder()
                                                                 .setPriorityValue(TaskPriority.LOW)
                                                                 .build();
        final TaskPriorityValue actualValue = TaskPriorityValue.newBuilder()
                                                               .setPriorityValue(TaskPriority.TP_UNDEFINED)
                                                               .build();
        final TaskPriorityValue newValue = TaskPriorityValue.newBuilder()
                                                            .setPriorityValue(TaskPriority.HIGH)
                                                            .build();
        assertEquals(actualValue, unpack(mismatch.getActual()));
        assertEquals(expectedValue, unpack(mismatch.getExpected()));
        assertEquals(newValue, unpack(mismatch.getNewValue()));
    }

    @Test
    public void emit_cannot_update_task_description_failure_upon_update_task_description_command() {
        final CreateBasicTask createBasicTask = createTaskInstance();
        aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

        final String expectedValue = "description";
        final String newValue = "update description";
        final String actualValue = createBasicTask.getDescription();

        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(TASK_ID, expectedValue, newValue);
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(updateTaskDescription, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(CannotUpdateTaskDescription.class, messageList.get(0)
                                                                   .getClass());
        final CannotUpdateTaskDescription cannotUpdateTaskDescription = (CannotUpdateTaskDescription) messageList.get(0);

        final ValueMismatch mismatch = cannotUpdateTaskDescription.getDescriptionMismatch();
        assertEquals(TASK_ID, cannotUpdateTaskDescription.getTaskId());

        final StringValue expectedStringValue = StringValue.newBuilder()
                                                           .setValue(expectedValue)
                                                           .build();
        final StringValue actualStringValue = StringValue.newBuilder()
                                                         .setValue(actualValue)
                                                         .build();
        final StringValue newStringValue = StringValue.newBuilder()
                                                      .setValue(newValue)
                                                      .build();
        assertEquals(expectedStringValue, unpack(mismatch.getExpected()));
        assertEquals(actualStringValue, unpack(mismatch.getActual()));
        assertEquals(newStringValue, unpack(mismatch.getNewValue()));
    }

    @Test
    public void emit_cannot_update_task_due_date_failure_upon_update_task_due_date_command() {
        final CreateBasicTask createBasicTask = createTaskInstance();
        aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

        final Timestamp expectedDueDate = Timestamps.getCurrentTime();
        final Timestamp newDueDate = Timestamps.getCurrentTime();

        final UpdateTaskDueDate updateTaskDueDate =
                updateTaskDueDateInstance(TASK_ID, expectedDueDate, newDueDate);
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(updateTaskDueDate, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(CannotUpdateTaskDueDate.class, messageList.get(0)
                                                               .getClass());
        final CannotUpdateTaskDueDate cannotUpdateTaskDueDate = (CannotUpdateTaskDueDate) messageList.get(0);

        final ValueMismatch mismatch = cannotUpdateTaskDueDate.getDueDateMismatch();
        assertEquals(TASK_ID, cannotUpdateTaskDueDate.getTaskId());

        assertEquals(newDueDate, unpack(mismatch.getNewValue()));
        assertEquals(expectedDueDate, unpack(mismatch.getExpected()));

        final Timestamp actualDueDate = Timestamp.getDefaultInstance();
        assertEquals(actualDueDate, unpack(mismatch.getActual()));
    }

    private static String constructExceptionMessage(TaskStatus fromState, TaskStatus toState) {
        return String.format("Cannot make transition from: %s to: %s state",
                             fromState, toState);
    }
}
