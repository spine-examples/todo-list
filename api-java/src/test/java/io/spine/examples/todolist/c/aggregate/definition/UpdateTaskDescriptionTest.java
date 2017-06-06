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
import com.google.protobuf.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.DescriptionUpdateFailed;
import io.spine.examples.todolist.TaskDefinition;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskDescription;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskWithInappropriateDescription;
import io.spine.examples.todolist.c.failures.Failures;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.protobuf.Wrapper.forString;

/**
 * @author Illia Shepilov
 */
@DisplayName("UpdateTaskDescription command should be interpreted by TaskDefinitionPart and")
public class UpdateTaskDescriptionTest extends TaskDefinitionCommandTest<UpdateTaskDescription> {

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
        final List<? extends Message> messageList =
                aggregate.dispatchForTest(envelopeOf(updateTaskDescriptionCmd));

        assertEquals(1, messageList.size());
        assertEquals(TaskDescriptionUpdated.class, messageList.get(0)
                                                              .getClass());
        final TaskDescriptionUpdated taskDescriptionUpdated =
                (TaskDescriptionUpdated) messageList.get(0);

        assertEquals(taskId, taskDescriptionUpdated.getTaskId());
        final String newDescription = taskDescriptionUpdated.getDescriptionChange()
                                                            .getNewValue();
        assertEquals(DESCRIPTION, newDescription);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskWithInappropriateDescription failure " +
            "upon an attempt to update the task by too short description")
    void cannotUpdateTaskDescription() {
        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(taskId, "", ".");
        final Throwable t = assertThrows(Throwable.class,
                                         () -> aggregate.dispatchForTest(
                                                 envelopeOf(updateTaskDescriptionCmd)));
        assertThat(Throwables.getRootCause(t),
                   instanceOf(CannotUpdateTaskWithInappropriateDescription.class));
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription failure " +
            "upon an attempt to update the description of the deleted task")
    void cannotUpdateDeletedTaskDescription() {
        dispatchCreateTaskCmd();

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(envelopeOf(deleteTaskCmd));

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(taskId);
        Throwable t = assertThrows(Throwable.class,
                                   () -> aggregate.dispatchForTest(
                                           envelopeOf(updateTaskDescriptionCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskDescription.class));
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription failure " +
            "upon an attempt to update the description of the completed task")
    void cannotUpdateCompletedTaskDescription() {
        dispatchCreateTaskCmd();

        final CompleteTask completeTaskCmd = completeTaskInstance();
        aggregate.dispatchForTest(envelopeOf(completeTaskCmd));

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(taskId);
        Throwable t = assertThrows(Throwable.class,
                                   () -> aggregate.dispatchForTest(
                                           envelopeOf(updateTaskDescriptionCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskDescription.class));
    }

    @Test
    @DisplayName("update the task description")
    void updateDescription() {
        final String newDescription = "new description.";
        dispatchCreateTaskCmd();

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(taskId, DESCRIPTION, newDescription);
        aggregate.dispatchForTest(envelopeOf(updateTaskDescriptionCmd));
        final TaskDefinition state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(newDescription, state.getDescription());
    }

    @Test
    @DisplayName("produce CannotUpdateTaskDescription failure")
    void produceFailure() {
        final CreateBasicTask createBasicTask = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(envelopeOf(createBasicTask));

        final String expectedValue = "expected description";
        final String newValue = "update description";
        final String actualValue = createBasicTask.getDescription();

        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(taskId, expectedValue, newValue);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> aggregate.dispatchForTest(
                                                 envelopeOf(updateTaskDescription)));
        final Throwable cause = Throwables.getRootCause(t);
        assertThat(cause, instanceOf(CannotUpdateTaskDescription.class));

        @SuppressWarnings("ConstantConditions") // Instance type checked before.
        final Failures.CannotUpdateTaskDescription failure =
                ((CannotUpdateTaskDescription) cause).getFailureMessage();
        final DescriptionUpdateFailed descriptionUpdateFailed = failure.getUpdateFailed();
        final TaskId actualTaskId = descriptionUpdateFailed.getFailureDetails()
                                                           .getTaskId();
        assertEquals(taskId, actualTaskId);

        final StringValue expectedStringValue = forString(expectedValue);
        final StringValue actualStringValue = forString(actualValue);
        final StringValue newStringValue = forString(newValue);

        final ValueMismatch mismatch = descriptionUpdateFailed.getDescriptionMismatch();
        assertEquals(expectedStringValue, unpack(mismatch.getExpected()));
        assertEquals(actualStringValue, unpack(mismatch.getActual()));
        assertEquals(newStringValue, unpack(mismatch.getNewValue()));
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createBasicTask = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(envelopeOf(createBasicTask));
    }
}
