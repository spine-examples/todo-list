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
import com.google.protobuf.Message;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.rejection.CannotCompleteTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.TaskStatus.COMPLETED;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CompleteTask command should be interpreted by TaskPart and")
class CompleteTaskTest extends TaskCommandTest<CompleteTask> {

    CompleteTaskTest() {
        super(completeTaskInstance());
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("produce TaskCompleted event")
    void produceEvent() {
        dispatchCreateTaskCmd();

        List<? extends Message> messageList = dispatchCompleteTaskCmd();

        assertEquals(1, messageList.size());
        assertEquals(TaskCompleted.class, messageList.get(0)
                                                     .getClass());
        TaskCompleted taskCompleted = (TaskCompleted) messageList.get(0);

        assertEquals(entityId(), taskCompleted.getTaskId());
    }

    @Test
    @DisplayName("complete the task")
    void completeTheTask() {
        dispatchCreateTaskCmd();

        dispatchCompleteTaskCmd();
        Task state = aggregate.state();

        assertEquals(entityId(), state.getId());
        assertEquals(COMPLETED, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotCompleteTask rejection upon an attempt to complete the deleted task")
    void cannotCompleteDeletedTask() {
        dispatchCreateTaskCmd();

        DeleteTask deleteTaskCmd = deleteTaskInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(deleteTaskCmd));

        Throwable t = assertThrows(Throwable.class, this::dispatchCompleteTaskCmd);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotCompleteTask.class));
    }

    @Test
    @DisplayName("throw CannotCompleteTask rejection upon " +
            "an attempt to complete the task in draft state")
    void cannotCompleteDraft() {
        CreateDraft createDraftCmd = createDraftInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(createDraftCmd));

        Throwable t = assertThrows(Throwable.class, this::dispatchCompleteTaskCmd);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotCompleteTask.class));
    }

    private void dispatchCreateTaskCmd() {
        CreateBasicTask createTaskCmd = createTaskInstance(entityId(), DESCRIPTION);
        dispatchCommand(aggregate, envelopeOf(createTaskCmd));
    }

    private List<? extends Message> dispatchCompleteTaskCmd() {
        CompleteTask completeTaskCmd = completeTaskInstance(entityId());
        return dispatchCommand(aggregate, envelopeOf(completeTaskCmd));
    }
}
