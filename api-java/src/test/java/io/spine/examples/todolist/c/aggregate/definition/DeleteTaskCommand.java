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
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.envelope.CommandEnvelope;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.failures.CannotDeleteTask;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.spine3.examples.todolist.TaskStatus.DELETED;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("DeleteTask command should be interpreted by TaskDefinitionPart and")
public class DeleteTaskCommand extends TaskDefinitionCommandTest<DeleteTask> {

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("delete the task")
    void deleteTask() {
        dispatchCreateTaskCmd();

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(envelopeOf(deleteTaskCmd));
        final TaskDefinition state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(DELETED, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotDeleteTask failure upon an attempt to " +
            "delete the already deleted task")
    void cannotDeleteAlreadyDeletedTask() {
        dispatchCreateTaskCmd();

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        final CommandEnvelope deleteTaskEnvelope = envelopeOf(deleteTaskCmd);
        aggregate.dispatchForTest(deleteTaskEnvelope);

        final Throwable t = assertThrows(Throwable.class,
                                         () -> aggregate.dispatchForTest(deleteTaskEnvelope));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotDeleteTask.class));
    }

    @Test
    @DisplayName("produce TaskDeleted event")
    void produceEvent() {
        dispatchCreateTaskCmd();
        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);

        final List<? extends Message> messageList =
                aggregate.dispatchForTest(envelopeOf(deleteTaskCmd));
        assertEquals(1, messageList.size());
        assertEquals(TaskDeleted.class, messageList.get(0)
                                                   .getClass());
        final TaskDeleted taskDeleted = (TaskDeleted) messageList.get(0);
        assertEquals(taskId, taskDeleted.getTaskId());
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(envelopeOf(createTaskCmd));
    }
}
