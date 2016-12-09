//
// Copyright 2016, TeamDev Ltd. All rights reserved.
//
// Redistribution and use in source and/or binary forms, with or without
// modification, must retain the above copyright notice and the following
// disclaimer.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
package org.spine3.examples.todolist.aggregate;

import com.google.common.base.Throwables;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.FinalizeDraft;
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
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;
import org.spine3.protobuf.Timestamps;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;

public class TaskAggregateTest {

    private static final CommandContext COMMAND_CONTEXT = createCommandContext();
    private static final String COMPLETED_TASK_EXCEPTION_MESSAGE = "Command cannot be applied on completed task.";
    private static final String DELETED_TASK_EXCEPTION_MESSAGE = "Command cannot be applied on deleted task.";
    private static final String UNCOMPLETED_TASK_EXCEPTION_MESSAGE = "Command cannot be applied on not completed task.";
    private static final String UNDELETED_TASK_EXCEPTION_MESSAGE = "Command cannot be applied on not deleted task.";
    private static final String INAPPROPRIATE_TASK_DESCRIPTION = "Description should contains at least 3 alphanumeric symbols.";
    private static final String DELETED_DRAFT_EXCEPTION_MESSAGE = "Command cannot be applied to the deleted draft.";
    private static final String TASK_IS_NOT_DRAFT_EXCEPTION_MESSAGE = "Command applicable to the draft tasks only.";
    private static final String TASK_IN_DRAFT_STATE_EXCEPTION_MESSAGE = "Command cannot be applied on task in draft state.";
    private TaskAggregate aggregate;
    private CreateBasicTask createTaskCmd;
    private UpdateTaskDescription updateTaskDescriptionCmd;
    private UpdateTaskDueDate updateTaskDueDateCmd;
    private UpdateTaskPriority updateTaskPriorityCmd;
    private ReopenTask reopenTaskCmd;
    private DeleteTask deleteTaskCmd;
    private RestoreDeletedTask restoreDeletedTaskCmd;
    private CompleteTask completeTaskCmd;
    private FinalizeDraft finalizeDraftCmd;
    private CreateDraft createDraftCmd;

    private static final TaskId ID = TaskId.newBuilder()
                                           .setValue(newUuid())
                                           .build();

    @BeforeEach
    public void setUp() throws Exception {
        aggregate = new TaskAggregate(ID);
        createTaskCmd = createTaskInstance();
        updateTaskDescriptionCmd = updateTaskDescriptionInstance();
        updateTaskDueDateCmd = updateTaskDueDateInstance();
        updateTaskPriorityCmd = updateTaskPriorityInstance();
        reopenTaskCmd = reopenTaskInstance();
        deleteTaskCmd = deleteTaskInstance();
        restoreDeletedTaskCmd = restoreDeletedTaskInstance();
        completeTaskCmd = completeTaskInstance();
        finalizeDraftCmd = finalizeDraftInstance();
        createDraftCmd = createDraftInstance();
    }

    @Test
    public void accept_Message_id_to_constructor() {
        try {
            final TaskAggregate aggregate = new TaskAggregate(ID);
            assertEquals(ID, aggregate.getId());
        } catch (Throwable e) {
            fail();
        }
    }

    @Test
    public void handle_create_task_command() {
        final int expectedListSize = 1;

        final List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        assertNotNull(aggregate.getState()
                               .getCreated());
        assertNotNull(aggregate.getId());
        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskCreated.class, messageList.get(0)
                                                   .getClass());
    }

    @Test
    public void handle_update_task_description_command() {
        final int expectedListSize = 1;

        final List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskDescriptionUpdated.class, messageList.get(0)
                                                              .getClass());
    }

    @Test
    public void handle_update_task_description_when_description_contains_one_symbol() {
        try {
            updateTaskDescriptionCmd = updateTaskDescriptionInstance(".");
            aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(INAPPROPRIATE_TASK_DESCRIPTION, cause.getMessage());
        }
    }

    @Test
    public void handle_update_task_description_when_task_is_deleted() {
        try {
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(DELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_update_task_description_when_task_is_completed() {
        try {
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(COMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_update_task_due_date_when_task_is_completed() {
        try {
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(COMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_update_task_due_date_when_task_is_deleted() {
        try {
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(DELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_update_task_priority_when_task_is_deleted() {
        try {
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(DELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_update_task_priority_when_task_is_completed() {
        try {
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(COMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_update_task_due_date_command() {
        final int expectedListSize = 1;

        final List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskDueDateUpdated.class, messageList.get(0)
                                                          .getClass());
    }

    @Test
    public void handle_update_task_priority_command() {
        final int expectedListSize = 1;

        final List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskPriorityUpdated.class, messageList.get(0)
                                                           .getClass());
    }

    @Test
    public void handle_reopen_task_command_when_task_created() {
        try {
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(UNCOMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_delete_task_command() {
        final int expectedListSize = 1;

        final List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskDeleted.class, messageList.get(0)
                                                   .getClass());
    }

    @Test
    public void handle_complete_task_command() {
        final int expectedListSize = 1;

        final List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskCompleted.class, messageList.get(0)
                                                     .getClass());
    }

    @Test
    public void return_current_state_after_dispatch_when_description_is_updated() {
        final String newDescription = "new description.";
        updateTaskDescriptionCmd = updateTaskDescriptionInstance(newDescription);
        aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
        assertEquals(newDescription, aggregate.getState()
                                              .getDescription());
    }

    @Test
    public void return_current_state_when_updated_task_due_date() {
        final long updatedDueDate = System.currentTimeMillis();
        updateTaskDueDateCmd = updateTaskDueDateInstance(updatedDueDate);
        aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);
        assertEquals(updatedDueDate, aggregate.getState()
                                              .getDueDate()
                                              .getSeconds());
    }

    @Test
    public void return_current_state_when_updated_priority() {
        final TaskPriority updatedPriority = TaskPriority.HIGH;
        updateTaskPriorityCmd = updateTaskPriorityInstance(updatedPriority);
        aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);
        assertEquals(updatedPriority, aggregate.getState()
                                               .getPriority());
    }

    @Test
    public void return_current_state_when_task_is_completed() {
        aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
        assertTrue(aggregate.getState()
                            .getCompleted());
    }

    @Test
    public void return_current_state_when_task_is_deleted() {
        aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
        assertTrue(aggregate.getState()
                            .getDeleted());
    }

    @Test
    public void record_modification_timestamp() throws InterruptedException {
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);
        Timestamp firstAggregateCreationState = aggregate.getState()
                                                         .getCreated();
        Thread.sleep(1000);
        aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);
        Timestamp secondAggregateCreationState = aggregate.getState()
                                                          .getCreated();
        assertTrue(Timestamps.isLaterThan(secondAggregateCreationState, firstAggregateCreationState));
    }

    @Test
    public void handle_create_and_delete_task_command() {
        assertFalse(aggregate.getState()
                             .getDeleted());
        aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
        assertTrue(aggregate.getState()
                            .getDeleted());
    }

    @Test
    public void handle_restore_deleted_task_command() {
        aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
        aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        assertFalse(aggregate.getState()
                             .getDeleted());
    }

    @Test
    public void handle_delete_and_restore_task_command() {
        aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
        assertTrue(aggregate.getState()
                            .getDeleted());
        aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        assertFalse(aggregate.getState()
                             .getDeleted());
    }

    @Test
    public void handle_restore_task_command_when_task_is_completed() {
        try {
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(UNDELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_restore_deleted_task_command_when_task_is_created() {
        try {
            aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(UNDELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_restore_deleted_task_command_when_task_in_draft_state() {
        try {
            aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(UNDELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_complete_and_reopen_task_command() {
        aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
        assertTrue(aggregate.getState()
                            .getCompleted());
        aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);
        assertFalse(aggregate.getState()
                             .getCompleted());
    }

    @Test
    public void handle_create_and_finalize_draft_command() {
        aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);
        assertTrue(aggregate.getState()
                            .getDraft());
        aggregate.dispatchForTest(finalizeDraftCmd, COMMAND_CONTEXT);
        assertFalse(aggregate.getState()
                             .getDraft());
    }

    @Test
    public void handle_create_draft_task_command() {
        final int expectedListSize = 1;

        final List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

        assertEquals(1, messageList.size());
        assertEquals(TaskDraftCreated.class, messageList.get(0)
                                                        .getClass());
    }

    @Test
    public void handle_finalized_draft_command_when_task_is_deleted() {
        try {
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(finalizeDraftCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(DELETED_DRAFT_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_finalized_draft_command_when_task_not_in_the_draft_state() {
        try {
            aggregate.dispatchForTest(finalizeDraftCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(TASK_IS_NOT_DRAFT_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void return_current_sate_when_task_draft_is_created() {
        Task state = aggregate.getState();
        aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);
        assertFalse(state.getDraft());
        assertFalse(state.getCompleted());
        assertFalse(state.getDeleted());
    }

    @Test
    public void handle_complete_task_command_when_task_is_deleted() {
        try {
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(DELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_complete_task_command_when_task_in_draft_state() {
        try {
            aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(TASK_IN_DRAFT_STATE_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_reopen_task_command_when_task_deleted() {
        try {
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(UNCOMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_reopen_task_command_when_task_in_draft_state() {
        try {
            aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(UNCOMPLETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

    @Test
    public void handle_delete_task_command_when_task_is_already_deleted() {
        try {
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals(DELETED_TASK_EXCEPTION_MESSAGE, cause.getMessage());
        }
    }

}