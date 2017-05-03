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
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.c.failures.CannotFinalizeDraft;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("finalize the task")
    void finalizeTask() {
        final CreateDraft createDraftCmd = createDraftInstance(taskId);
        aggregate.dispatchForTest(envelopeOf(createDraftCmd));

        final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance(taskId);
        TaskDefinition state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(DRAFT, state.getTaskStatus());

        aggregate.dispatchForTest(envelopeOf(finalizeDraftCmd));
        state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(FINALIZED, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotFinalizeDraft failure upon an attempt to finalize the deleted task")
    void cannotFinalizeDeletedTask() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(envelopeOf(createTaskCmd));

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(envelopeOf(deleteTaskCmd));

        final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance(taskId);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> aggregate.dispatchForTest(
                                                 envelopeOf(finalizeDraftCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotFinalizeDraft.class));
    }

    @Test
    @DisplayName("throw CannotFinalizeDraft failure upon an attempt to finalize " +
            "the task which is not a draft")
    void cannotFinalizeNotDraftTask() {
        final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance(taskId);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> aggregate.dispatchForTest(
                                                 envelopeOf(finalizeDraftCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotFinalizeDraft.class));
    }
}
