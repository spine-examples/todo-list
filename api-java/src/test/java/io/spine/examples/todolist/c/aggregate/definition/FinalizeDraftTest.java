/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.c.aggregate.definition;

import com.google.common.base.Throwables;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.rejection.CannotFinalizeDraft;
import io.spine.testing.server.ShardingReset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.spine.examples.todolist.TaskStatus.DRAFT;
import static io.spine.examples.todolist.TaskStatus.FINALIZED;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(ShardingReset.class)
@DisplayName("FinalizeDraft command should be interpreted by TaskPart and")
public class FinalizeDraftTest extends TaskCommandTest<FinalizeDraft> {

    FinalizeDraftTest() {
        super(finalizeDraftInstance());
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("finalize the task")
    void finalizeTask() {
        final CreateDraft createDraftCmd = createDraftInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(createDraftCmd));

        Task state = aggregate.getState();

        assertEquals(entityId(), state.getId());
        assertEquals(DRAFT, state.getTaskStatus());

        final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(finalizeDraftCmd));
        state = aggregate.getState();

        assertEquals(entityId(), state.getId());
        assertEquals(FINALIZED, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotFinalizeDraft rejection upon an attempt to finalize the deleted task")
    void cannotFinalizeDeletedTask() {
        final CreateBasicTask createTaskCmd = createTaskInstance(entityId(), DESCRIPTION);
        dispatchCommand(aggregate, envelopeOf(createTaskCmd));

        final DeleteTask deleteTaskCmd = deleteTaskInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(deleteTaskCmd));

        final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance(entityId());
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatchCommand(aggregate, envelopeOf(finalizeDraftCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotFinalizeDraft.class));
    }

    @Test
    @DisplayName("throw CannotFinalizeDraft rejection upon an attempt to finalize " +
            "the task which is not a draft")
    void cannotFinalizeNotDraftTask() {
        final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance(entityId());
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatchCommand(aggregate, envelopeOf(finalizeDraftCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotFinalizeDraft.class));
    }
}
