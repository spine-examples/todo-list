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

import com.google.common.base.Throwables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.base.Commands;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.c.failures.CannotRestoreDeletedTask;
import org.spine3.examples.todolist.context.TodoListBoundedContext;
import org.spine3.examples.todolist.testdata.TestResponseObserver;
import org.spine3.server.command.CommandBus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.TaskStatus.DELETED;
import static org.spine3.examples.todolist.TaskStatus.OPEN;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.TASK_ID;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("RestoreDeletedTask command")
public class RestoreDeletedTaskTest {

    private static final CommandContext COMMAND_CONTEXT = createCommandContext();
    private TestResponseObserver responseObserver;
    private CommandBus commandBus;
    private TaskLabelsPart taskLabelsPart;
    private TaskDefinitionPart taskDefinitionPart;

    private static final TaskId ID = TaskId.newBuilder()
                                           .setValue(newUuid())
                                           .build();

    @BeforeEach
    public void setUp() {
        commandBus = TodoListBoundedContext.getCommandBus();
        responseObserver = new TestResponseObserver();
        taskDefinitionPart = createTaskDefinitionPart(TASK_ID);
        taskLabelsPart = createTaskLabelsPart(TASK_ID);
    }

    private static TaskDefinitionPart createTaskDefinitionPart(TaskId taskId) {
        return new TaskDefinitionPart(taskId);
    }

    private static TaskLabelsPart createTaskLabelsPart(TaskId taskId) {
        return new TaskLabelsPart(taskId);
    }

    @Test
    @DisplayName("produces LabelledTaskRestored event")
    public void emit_labelled_task_restored_event_upon_restore_task_command_when_task_has_label() {
        final CreateBasicTask createTask = createTaskInstance();
        final Command createTaskCmd = Commands.create(createTask, COMMAND_CONTEXT);
        commandBus.post(createTaskCmd, responseObserver);

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance();
        final Command assignLabelToTaskCmd = Commands.create(assignLabelToTask, COMMAND_CONTEXT);
        commandBus.post(assignLabelToTaskCmd, responseObserver);

        final DeleteTask deleteTask = deleteTaskInstance();
        final Command deleteTaskCmd = Commands.create(deleteTask, COMMAND_CONTEXT);
        commandBus.post(deleteTaskCmd, responseObserver);

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance();
        final Command restoreDeletedTaskCmd = Commands.create(restoreDeletedTask, COMMAND_CONTEXT);
        commandBus.post(restoreDeletedTaskCmd, responseObserver);

        final int expectedListSize = 2;
        //assertEquals(expectedListSize, messageList.size());
        //
        //final LabelledTaskRestored labelledTaskRestored = (LabelledTaskRestored) messageList.get(1);
        //assertEquals(TASK_ID, labelledTaskRestored.getTaskId());
        //assertEquals(LABEL_ID, labelledTaskRestored.getLabelId());
    }

    @Test
    @DisplayName("restores deleted task")
    public void emit_deleted_task_restored_event_upon_restore_deleted_task_command() {
        final CreateBasicTask createTaskCmd = createTaskInstance();
        taskDefinitionPart.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        final DeleteTask deleteTaskCmd = deleteTaskInstance();
        taskDefinitionPart.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

        final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
        taskDefinitionPart.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);

        final TaskDefinition state = taskDefinitionPart.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    @DisplayName("cannot restore task when task is completed")
    public void catch_exception_when_handle_restore_task_command_when_task_is_completed() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            taskDefinitionPart.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            taskDefinitionPart.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
            taskDefinitionPart.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotRestoreDeletedTask);
        }
    }

    @Test
    @DisplayName("cannot restore task when task is finalized")
    public void catch_exception_when_handle_restore_deleted_task_command_when_task_is_created() {
        try {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            taskDefinitionPart.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
            taskDefinitionPart.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotRestoreDeletedTask);
        }
    }

    @Test
    @DisplayName("cannot restore task when task in draft state")
    public void catch_exception_when_handle_restore_deleted_task_command_when_task_in_draft_state() {
        try {
            final CreateDraft createDraftCmd = createDraftInstance();
            taskDefinitionPart.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

            final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
            taskDefinitionPart.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotRestoreDeletedTask);
        }
    }

    @Test
    public void emit_task_deleted_and_deleted_task_restored_event_upon_delete_and_restore_task_command() {
        final CreateDraft createDraftCmd = createDraftInstance();
        taskDefinitionPart.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

        final DeleteTask deleteTaskCmd = deleteTaskInstance();
        taskDefinitionPart.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

        TaskDefinition state = taskDefinitionPart.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(DELETED, state.getTaskStatus());

        final RestoreDeletedTask restoreDeletedTaskCmd = restoreDeletedTaskInstance();
        taskDefinitionPart.dispatchForTest(restoreDeletedTaskCmd, COMMAND_CONTEXT);

        state = taskDefinitionPart.getState();

        assertEquals(TASK_ID, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }
}
