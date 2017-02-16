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

package org.spine3.examples.todolist.c.aggregate.definition;

import com.google.common.base.Throwables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.aggregate.TaskDefinitionPart;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.ReopenTask;
import org.spine3.examples.todolist.c.failures.CannotReopenTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.TaskStatus.COMPLETED;
import static org.spine3.examples.todolist.TaskStatus.OPEN;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("ReopenTask command should be interpreted by TaskDefinitionPart and")
public class ReopenTaskCommandTest extends TaskDefinitionCommandTest<ReopenTask> {

    private final CommandContext commandContext = createCommandContext();
    private TaskDefinitionPart aggregate;
    private TaskId taskId;

    @Override
    @BeforeEach
    protected void setUp() {
        taskId = createTaskId();
        aggregate = createTaskDefinitionPart(taskId);
    }

    @Test
    @DisplayName("throw CannotReopenTask failure when it is trying to reopen not completed task")
    public void cannotReopenNotCompletedTask() {
        dispatchCreateTaskCmd();
        try {
            final ReopenTask reopenTaskCmd = reopenTaskInstance(taskId);
            aggregate.dispatchForTest(reopenTaskCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotReopenTask);
        }
    }

    @Test
    @DisplayName("reopen completed task")
    public void reopenTask() {
        dispatchCreateTaskCmd();
        final CompleteTask completeTaskCmd = completeTaskInstance(taskId);
        aggregate.dispatchForTest(completeTaskCmd, commandContext);

        TaskDefinition state = aggregate.getState();
        assertEquals(taskId, state.getId());
        assertEquals(COMPLETED, state.getTaskStatus());

        final ReopenTask reopenTaskCmd = reopenTaskInstance(taskId);
        aggregate.dispatchForTest(reopenTaskCmd, commandContext);

        state = aggregate.getState();
        assertEquals(taskId, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotReopenTask when it is trying to reopen the deleted task")
    public void cannotReopenDeletedTask() {
        dispatchCreateTaskCmd();
        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(deleteTaskCmd, commandContext);

        try {
            final ReopenTask reopenTaskCmd = reopenTaskInstance(taskId);
            aggregate.dispatchForTest(reopenTaskCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotReopenTask);
        }
    }

    @Test
    @DisplayName("throw CannotReopenTask when it is trying to reopen the task in draft state")
    public void cannotReopenDraft() {
        final CreateDraft createDraftCmd = createDraftInstance(taskId);
        aggregate.dispatchForTest(createDraftCmd, commandContext);

        try {
            final ReopenTask reopenTaskCmd = reopenTaskInstance(taskId);
            aggregate.dispatchForTest(reopenTaskCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotReopenTask);
        }
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createTaskCmd, commandContext);
    }
}
