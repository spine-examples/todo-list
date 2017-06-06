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
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.PriorityUpdateFailed;
import io.spine.examples.todolist.TaskDefinition;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.TaskPriorityValue;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskPriority;
import io.spine.examples.todolist.c.failures.Failures;

import java.util.List;

import static io.spine.server.aggregate.AggregateCommandDispatcher.dispatch;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static io.spine.examples.todolist.TaskPriority.HIGH;
import static io.spine.examples.todolist.TaskPriority.LOW;
import static io.spine.examples.todolist.TaskPriority.TP_UNDEFINED;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static io.spine.protobuf.AnyPacker.unpack;

/**
 * @author Illia Shepilov
 */

@DisplayName("UpdateTaskPriority command should be interpreted by TaskDefinitionPart and")
public class UpdateTaskPriorityTest extends TaskDefinitionCommandTest<UpdateTaskPriority> {

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("throw CannotUpdateTaskPriority failure upon an attempt to " +
            "update the priority of the deleted task")
    void cannotUpdateDeletedTaskPriority() {
        dispatchCreateTaskCmd();

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        dispatch(aggregate, envelopeOf(deleteTaskCmd));

        final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance(taskId);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatch(aggregate, envelopeOf(updateTaskPriorityCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskPriority.class));

    }

    @Test
    @DisplayName("throw CannotUpdateTaskPriority failure " +
            "upon an attempt to update the priority of the completed task")
    void cannotUpdateCompletedTaskPriority() {
        dispatchCreateTaskCmd();

        final CompleteTask completeTaskCmd = completeTaskInstance(taskId);
        dispatch(aggregate, envelopeOf(completeTaskCmd));

        final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance(taskId);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatch(aggregate, envelopeOf(updateTaskPriorityCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskPriority.class));
    }

    @Test
    @DisplayName("produce TaskPriorityUpdated event")
    void produceEvent() {
        dispatchCreateTaskCmd();

        final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance(taskId);
        final List<? extends Message> messageList = dispatch(aggregate,
                                                             envelopeOf(updateTaskPriorityCmd));
        assertEquals(1, messageList.size());
        assertEquals(TaskPriorityUpdated.class, messageList.get(0)
                                                           .getClass());
        final TaskPriorityUpdated taskPriorityUpdated = (TaskPriorityUpdated) messageList.get(0);

        assertEquals(taskId, taskPriorityUpdated.getTaskId());
        final TaskPriority newPriority = taskPriorityUpdated.getPriorityChange()
                                                            .getNewValue();
        assertEquals(HIGH, newPriority);
    }

    @Test
    @DisplayName("update the task priority")
    void updatePriority() {
        final TaskPriority updatedPriority = HIGH;
        dispatchCreateTaskCmd();

        final UpdateTaskPriority updateTaskPriorityCmd =
                updateTaskPriorityInstance(taskId, TP_UNDEFINED, updatedPriority);
        dispatch(aggregate, envelopeOf(updateTaskPriorityCmd));
        final TaskDefinition state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(updatedPriority, state.getPriority());
    }

    @Test
    @DisplayName("produce CannotUpdateTaskPriority failure")
    void produceFailure() {
        final UpdateTaskPriority updateTaskPriority = updateTaskPriorityInstance(taskId, LOW, HIGH);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatch(aggregate, envelopeOf(updateTaskPriority)));
        final Throwable cause = Throwables.getRootCause(t);
        assertThat(cause, instanceOf(CannotUpdateTaskPriority.class));

        @SuppressWarnings("ConstantConditions") // Instance type checked before.
        final Failures.CannotUpdateTaskPriority cannotUpdateTaskPriority =
                ((CannotUpdateTaskPriority) cause).getFailureMessage();
        final PriorityUpdateFailed priorityUpdateFailed =
                cannotUpdateTaskPriority.getUpdateFailed();
        final TaskId actualTaskId = priorityUpdateFailed.getFailureDetails()
                                                        .getTaskId();
        assertEquals(taskId, actualTaskId);

        final PriorityChange priorityChange = updateTaskPriority.getPriorityChange();
        final ValueMismatch mismatch = priorityUpdateFailed.getPriorityMismatch();
        final TaskPriorityValue expectedValue = priorityValueOf(priorityChange.getPreviousValue());
        final TaskPriorityValue actualValue = priorityValueOf(TP_UNDEFINED);
        final TaskPriorityValue newValue = priorityValueOf(priorityChange.getNewValue());
        assertEquals(actualValue, unpack(mismatch.getActual()));
        assertEquals(expectedValue, unpack(mismatch.getExpected()));
        assertEquals(newValue, unpack(mismatch.getNewValue()));
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        dispatch(aggregate, envelopeOf(createTaskCmd));
    }

    private TaskPriorityValue priorityValueOf(TaskPriority taskPriority) {
        return TaskPriorityValue.newBuilder()
                                .setPriorityValue(taskPriority)
                                .build();
    }
}
