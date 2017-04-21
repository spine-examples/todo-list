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
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.events.TaskCompleted;
import org.spine3.examples.todolist.c.failures.CannotCompleteTask;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.spine3.examples.todolist.TaskStatus.COMPLETED;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("CompleteTask command should be interpreted by TaskDefinitionPart and")
public class CompleteTaskTest extends TaskDefinitionCommandTest<CompleteTask> {

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("produce TaskCompleted event")
    public void produceEvent() {
        dispatchCreateTaskCmd();

        final CompleteTask completeTaskCmd = completeTaskInstance(taskId);
        final List<? extends Message> messageList =
                aggregate.dispatchForTest(completeTaskCmd, commandContext);

        assertEquals(1, messageList.size());
        assertEquals(TaskCompleted.class, messageList.get(0)
                                                     .getClass());
        final TaskCompleted taskCompleted = (TaskCompleted) messageList.get(0);

        assertEquals(taskId, taskCompleted.getTaskId());
    }

    @Test
    @DisplayName("complete the task")
    public void completeTheTask() {
        dispatchCreateTaskCmd();

        dispatchCompleteTaskCmd();
        final TaskDefinition state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(COMPLETED, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotCompleteTask failure upon an attempt to complete the deleted task")
    public void cannotCompleteDeletedTask() {
        dispatchCreateTaskCmd();

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(deleteTaskCmd, commandContext);

        try {
            dispatchCompleteTaskCmd();
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotCompleteTask);
        }
    }

    @Test
    @DisplayName("throw CannotCompleteTask failure upon an attempt to complete the task in draft state")
    public void cannotCompleteDraft() {
        final CreateDraft createDraftCmd = createDraftInstance(taskId);
        aggregate.dispatchForTest(createDraftCmd, commandContext);

        try {
            dispatchCompleteTaskCmd();
            fail("CannotCompleteTask was not thrown.");
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotCompleteTask);
        }
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createTaskCmd, commandContext);
    }

    private void dispatchCompleteTaskCmd() {
        final CompleteTask completeTaskCmd = completeTaskInstance(taskId);
        aggregate.dispatchForTest(completeTaskCmd, commandContext);
    }
}
