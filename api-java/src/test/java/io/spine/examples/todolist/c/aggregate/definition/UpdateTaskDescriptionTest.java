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
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.DescriptionUpdateFailed;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskDescription;
import io.spine.examples.todolist.c.failures.Failures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.Given.newDescription;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.server.aggregate.AggregateCommandDispatcher.dispatch;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Illia Shepilov
 */
@DisplayName("UpdateTaskDescription command should be interpreted by TaskPart and")
public class UpdateTaskDescriptionTest extends TaskCommandTest<UpdateTaskDescription> {

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("produce TaskDescriptionUpdated event")
    void produceEvent() {
        dispatchCreateTaskCmd();
        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(taskId);
        final List<? extends Message> messageList = dispatch(aggregate,
                                                             envelopeOf(updateTaskDescriptionCmd));
        assertEquals(1, messageList.size());
        assertEquals(TaskDescriptionUpdated.class, messageList.get(0)
                                                              .getClass());
        final TaskDescriptionUpdated taskDescriptionUpdated =
                (TaskDescriptionUpdated) messageList.get(0);

        assertEquals(taskId, taskDescriptionUpdated.getTaskId());
        final TaskDescription newDescription = taskDescriptionUpdated.getDescriptionChange()
                                                                     .getNewDescription();
        assertEquals(DESCRIPTION, newDescription);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription failure " +
            "upon an attempt to update the description of the deleted task")
    void cannotUpdateDeletedTaskDescription() {
        dispatchCreateTaskCmd();

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        dispatch(aggregate, envelopeOf(deleteTaskCmd));

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(taskId);
        Throwable t = assertThrows(Throwable.class,
                                   () -> dispatch(aggregate, envelopeOf(updateTaskDescriptionCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskDescription.class));
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription failure " +
            "upon an attempt to update the description of the completed task")
    void cannotUpdateCompletedTaskDescription() {
        dispatchCreateTaskCmd();

        final CompleteTask completeTaskCmd = completeTaskInstance();
        dispatch(aggregate, envelopeOf(completeTaskCmd));

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(taskId);
        Throwable t = assertThrows(Throwable.class,
                                   () -> dispatch(aggregate, envelopeOf(updateTaskDescriptionCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskDescription.class));
    }

    @Test
    @DisplayName("update the task description")
    void updateDescription() {
        final TaskDescription newDescription = newDescription("new description.");
        dispatchCreateTaskCmd();

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(taskId, DESCRIPTION, newDescription);
        dispatch(aggregate, envelopeOf(updateTaskDescriptionCmd));
        final Task state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(newDescription, state.getDescription());
    }

    @Test
    @DisplayName("produce CannotUpdateTaskDescription failure")
    void produceFailure() {
        final CreateBasicTask createBasicTask = createTaskInstance(taskId, DESCRIPTION);
        dispatch(aggregate, envelopeOf(createBasicTask));

        final TaskDescription expectedValue = newDescription("expected description");
        final TaskDescription newValue = newDescription("update description");
        final TaskDescription actualValue = createBasicTask.getDescription();

        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(taskId, expectedValue, newValue);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatch(aggregate, envelopeOf(updateTaskDescription)));
        final Throwable cause = Throwables.getRootCause(t);
        assertThat(cause, instanceOf(CannotUpdateTaskDescription.class));

        @SuppressWarnings("ConstantConditions") // Instance type checked before.
        final Failures.CannotUpdateTaskDescription failure =
                ((CannotUpdateTaskDescription) cause).getFailureMessage();
        final DescriptionUpdateFailed descriptionUpdateFailed = failure.getUpdateFailed();
        final TaskId actualTaskId = descriptionUpdateFailed.getFailureDetails()
                                                           .getTaskId();
        assertEquals(taskId, actualTaskId);

        final ValueMismatch mismatch = descriptionUpdateFailed.getDescriptionMismatch();
        assertEquals(expectedValue, unpack(mismatch.getExpected()));
        assertEquals(actualValue, unpack(mismatch.getActual()));
        assertEquals(newValue, unpack(mismatch.getNewValue()));
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createBasicTask = createTaskInstance(taskId, DESCRIPTION);
        dispatch(aggregate, envelopeOf(createBasicTask));
    }
}
