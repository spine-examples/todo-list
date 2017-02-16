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
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.c.failures.CannotFinalizeDraft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.TaskStatus.DRAFT;
import static org.spine3.examples.todolist.TaskStatus.FINALIZED;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("FinalizeDraft command should be interpreted by TaskDefinitionPart and")
public class FinalizeDraftTest extends TaskDefinitionCommandTest<FinalizeDraft> {

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
    @DisplayName("finalize the task")
    public void finalizeTask() {
        final CreateDraft createDraftCmd = createDraftInstance(taskId);
        aggregate.dispatchForTest(createDraftCmd, commandContext);

        final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance(taskId);
        TaskDefinition state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(DRAFT, state.getTaskStatus());

        aggregate.dispatchForTest(finalizeDraftCmd, commandContext);
        state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(FINALIZED, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotFinalizeDraft failure when it is trying to finalize the deleted task")
    public void cannotFinalizeDeletedTask() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createTaskCmd, commandContext);

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(deleteTaskCmd, commandContext);

        try {
            final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance(taskId);
            aggregate.dispatchForTest(finalizeDraftCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotFinalizeDraft);
        }
    }

    @Test
    @DisplayName("throw CannotFinalizeDraft failure when it is trying to finalize the task which is not a draft")
    public void cannotFinalizeNotDraftTask() {
        try {
            final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance(taskId);
            aggregate.dispatchForTest(finalizeDraftCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotFinalizeDraft);
        }
    }
}
