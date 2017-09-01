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

package io.spine.examples.todolist.c.aggregate.definition;

import com.google.common.base.Throwables;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.failures.CannotReopenTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.TaskStatus.COMPLETED;
import static io.spine.examples.todolist.TaskStatus.OPEN;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static io.spine.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Illia Shepilov
 */
@DisplayName("ReopenTask command should be interpreted by TaskPart and")
public class ReopenTaskCommandTest extends TaskCommandTest<ReopenTask> {

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("throw CannotReopenTask failure upon an attempt to reopen not completed task")
    void cannotReopenNotCompletedTask() {
        dispatchCreateTaskCmd();

        final ReopenTask reopenTaskCmd = reopenTaskInstance(taskId);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatchCommand(aggregate, envelopeOf(reopenTaskCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotReopenTask.class));
    }

    @Test
    @DisplayName("reopen completed task")
    void reopenTask() {
        dispatchCreateTaskCmd();
        final CompleteTask completeTaskCmd = completeTaskInstance(taskId);
        dispatchCommand(aggregate, envelopeOf(completeTaskCmd));

        Task state = aggregate.getState();
        assertEquals(taskId, state.getId());
        assertEquals(COMPLETED, state.getTaskStatus());

        final ReopenTask reopenTaskCmd = reopenTaskInstance(taskId);
        dispatchCommand(aggregate, envelopeOf(reopenTaskCmd));

        state = aggregate.getState();
        assertEquals(taskId, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotReopenTask upon an attempt to reopen the deleted task")
    void cannotReopenDeletedTask() {
        dispatchCreateTaskCmd();
        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        dispatchCommand(aggregate, envelopeOf(deleteTaskCmd));

        final ReopenTask reopenTaskCmd = reopenTaskInstance(taskId);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatchCommand(aggregate, envelopeOf(reopenTaskCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotReopenTask.class));
    }

    @Test
    @DisplayName("throw CannotReopenTask upon an attempt to reopen the task in draft state")
    void cannotReopenDraft() {
        final CreateDraft createDraftCmd = createDraftInstance(taskId);
        dispatchCommand(aggregate, envelopeOf(createDraftCmd));

        final ReopenTask reopenTaskCmd = reopenTaskInstance(taskId);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatchCommand(aggregate, envelopeOf(reopenTaskCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotReopenTask.class));
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        dispatchCommand(aggregate, envelopeOf(createTaskCmd));
    }
}
