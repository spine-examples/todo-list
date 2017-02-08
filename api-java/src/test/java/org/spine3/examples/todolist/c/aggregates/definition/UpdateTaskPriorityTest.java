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

package org.spine3.examples.todolist.c.aggregates.definition;

import com.google.common.base.Throwables;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.PriorityUpdateFailed;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskPriorityValue;
import org.spine3.examples.todolist.c.aggregates.TaskDefinitionPart;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskPriority;
import org.spine3.examples.todolist.c.failures.Failures;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static org.spine3.protobuf.AnyPacker.unpack;

/**
 * @author Illia Shepilov
 */

@DisplayName("UpdateTaskPriority command")
public class UpdateTaskPriorityTest extends TaskDefinitionCommandTest<UpdateTaskPriority> {

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
    @DisplayName("cannot update priority for the deleted task")
    public void cannotUpdateDeletedTaskPriority() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createTaskCmd, commandContext);

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(deleteTaskCmd, commandContext);

        try {
            final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance(taskId);
            aggregate.dispatchForTest(updateTaskPriorityCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotUpdateTaskPriority);
        }
    }

    @Test
    @DisplayName("cannot update priority for the completed task")
    public void cannotUpdateCompletedTaskPriority() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createTaskCmd, commandContext);

        final CompleteTask completeTaskCmd = completeTaskInstance(taskId);
        aggregate.dispatchForTest(completeTaskCmd, commandContext);

        try {
            final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance(taskId);
            aggregate.dispatchForTest(updateTaskPriorityCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotUpdateTaskPriority);
        }
    }

    @Test
    @DisplayName("produces TaskPriorityUpdated event")
    public void producesEvent() {
        final CreateBasicTask createBasicTask = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createBasicTask, commandContext);

        final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance(taskId);
        final List<? extends Message> messageList =
                aggregate.dispatchForTest(updateTaskPriorityCmd, commandContext);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskPriorityUpdated.class, messageList.get(0)
                                                           .getClass());
        final TaskPriorityUpdated taskPriorityUpdated = (TaskPriorityUpdated) messageList.get(0);

        assertEquals(taskId, taskPriorityUpdated.getTaskId());
        final TaskPriority newPriority = taskPriorityUpdated.getPriorityChange()
                                                            .getNewValue();
        assertEquals(TaskPriority.HIGH, newPriority);
    }

    @Test
    @DisplayName("updates task priority")
    public void updatesPriority() {
        final TaskPriority updatedPriority = TaskPriority.HIGH;
        final CreateBasicTask createBasicTask = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createBasicTask, commandContext);

        final UpdateTaskPriority updateTaskPriorityCmd =
                updateTaskPriorityInstance(taskId, TaskPriority.TP_UNDEFINED, updatedPriority);
        aggregate.dispatchForTest(updateTaskPriorityCmd, commandContext);
        final TaskDefinition state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(updatedPriority, state.getPriority());
    }

    @Test
    @DisplayName("produces throwing CannotUpdateTaskPriority failure")
    public void producesFailure() {
        try {
            final UpdateTaskPriority updateTaskPriority =
                    updateTaskPriorityInstance(taskId, TaskPriority.LOW, TaskPriority.HIGH);
            aggregate.dispatchForTest(updateTaskPriority, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotUpdateTaskPriority);

            @SuppressWarnings("ConstantConditions")
            final Failures.CannotUpdateTaskPriority cannotUpdateTaskPriority =
                    ((CannotUpdateTaskPriority) cause).getFailure();
            final PriorityUpdateFailed priorityUpdateFailed = cannotUpdateTaskPriority.getUpdateFailed();
            final TaskId actualTaskId = priorityUpdateFailed.getFailedCommand()
                                                            .getTaskId();
            assertEquals(taskId, actualTaskId);

            final ValueMismatch mismatch = priorityUpdateFailed.getPriorityMismatch();
            final TaskPriorityValue expectedValue = TaskPriorityValue.newBuilder()
                                                                     .setPriorityValue(TaskPriority.LOW)
                                                                     .build();
            final TaskPriorityValue actualValue = TaskPriorityValue.newBuilder()
                                                                   .setPriorityValue(TaskPriority.TP_UNDEFINED)
                                                                   .build();
            final TaskPriorityValue newValue = TaskPriorityValue.newBuilder()
                                                                .setPriorityValue(TaskPriority.HIGH)
                                                                .build();
            assertEquals(actualValue, unpack(mismatch.getActual()));
            assertEquals(expectedValue, unpack(mismatch.getExpected()));
            assertEquals(newValue, unpack(mismatch.getNewValue()));
        }
    }
}
