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
import com.google.protobuf.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.DescriptionUpdateFailed;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDescription;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskWithInappropriateDescription;
import org.spine3.examples.todolist.c.failures.Failures;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.spine3.protobuf.AnyPacker.unpack;

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
    public void produceEvent() {
        dispatchCreateTaskCmd();
        final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance(taskId);
        final List<? extends Message> messageList =
                aggregate.dispatchForTest(updateTaskDescriptionCmd, commandContext);

        assertEquals(1, messageList.size());
        assertEquals(TaskDescriptionUpdated.class, messageList.get(0)
                                                              .getClass());
        final TaskDescriptionUpdated taskDescriptionUpdated = (TaskDescriptionUpdated) messageList.get(0);

        assertEquals(taskId, taskDescriptionUpdated.getTaskId());
        final String newDescription = taskDescriptionUpdated.getDescriptionChange()
                                                            .getNewValue();
        assertEquals(DESCRIPTION, newDescription);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskWithInappropriateDescription failure " +
            "upon an attempt to update the task by too short description")
    public void cannotUpdateTaskDescription() {
        try {
            final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance(taskId, "", ".");
            aggregate.dispatchForTest(updateTaskDescriptionCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotUpdateTaskWithInappropriateDescription);
        }
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription failure " +
            "upon an attempt to update the description of the deleted task")
    public void cannotUpdateDeletedTaskDescription() {
        dispatchCreateTaskCmd();

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(deleteTaskCmd, commandContext);

        try {
            final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance(taskId);
            assertThrows(CannotUpdateTaskDescription.class, () ->
                    aggregate.dispatchForTest(updateTaskDescriptionCmd, commandContext));
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotUpdateTaskDescription);
        }
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription failure " +
            "upon an attempt to update the description of the completed task")
    public void cannotUpdateCompletedTaskDescription() {
        dispatchCreateTaskCmd();

        final CompleteTask completeTaskCmd = completeTaskInstance();
        aggregate.dispatchForTest(completeTaskCmd, commandContext);

        try {
            final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance(taskId);
            aggregate.dispatchForTest(updateTaskDescriptionCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotUpdateTaskDescription);
        }
    }

    @Test
    @DisplayName("update the task description")
    public void updateDescription() {
        final String newDescription = "new description.";
        dispatchCreateTaskCmd();

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(taskId, DESCRIPTION, newDescription);
        aggregate.dispatchForTest(updateTaskDescriptionCmd, commandContext);
        final TaskDefinition state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(newDescription, state.getDescription());
    }

    @Test
    @DisplayName("produce CannotUpdateTaskDescription failure")
    public void produceFailure() {
        final CreateBasicTask createBasicTask = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createBasicTask, commandContext);

        final String expectedValue = "expected description";
        final String newValue = "update description";
        final String actualValue = createBasicTask.getDescription();

        try {
            final UpdateTaskDescription updateTaskDescription =
                    updateTaskDescriptionInstance(taskId, expectedValue, newValue);
            aggregate.dispatchForTest(updateTaskDescription, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotUpdateTaskDescription);

            @SuppressWarnings("ConstantConditions")
            final Failures.CannotUpdateTaskDescription failure =
                    ((CannotUpdateTaskDescription) cause).getFailureMessage();
            final DescriptionUpdateFailed descriptionUpdateFailed = failure.getUpdateFailed();
            final TaskId actualTaskId = descriptionUpdateFailed.getFailureDetails()
                                                               .getTaskId();
            assertEquals(taskId, actualTaskId);

            final StringValue expectedStringValue = StringValue.newBuilder()
                                                               .setValue(expectedValue)
                                                               .build();
            final StringValue actualStringValue = StringValue.newBuilder()
                                                             .setValue(actualValue)
                                                             .build();
            final StringValue newStringValue = StringValue.newBuilder()
                                                          .setValue(newValue)
                                                          .build();

            final ValueMismatch mismatch = descriptionUpdateFailed.getDescriptionMismatch();
            assertEquals(expectedStringValue, unpack(mismatch.getExpected()));
            assertEquals(actualStringValue, unpack(mismatch.getActual()));
            assertEquals(newStringValue, unpack(mismatch.getNewValue()));
        }
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createBasicTask = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createBasicTask, commandContext);
    }
}
