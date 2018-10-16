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
import com.google.protobuf.StringValue;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.DescriptionUpdateRejected;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDescription;
import io.spine.examples.todolist.c.rejection.Rejections;
import io.spine.testing.server.ShardingReset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(ShardingReset.class)
@DisplayName("UpdateTaskDescription command should be interpreted by TaskPart and")
public class UpdateTaskDescriptionTest extends TaskCommandTest<UpdateTaskDescription> {

    UpdateTaskDescriptionTest() {
        super(updateTaskDescriptionInstance());
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("produce TaskDescriptionUpdated event")
    void produceEvent() {
        dispatchCreateTaskCmd();
        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(entityId());
        final List<? extends Message> messageList =
                dispatchCommand(aggregate, envelopeOf(updateTaskDescriptionCmd));
        assertEquals(1, messageList.size());
        assertEquals(TaskDescriptionUpdated.class, messageList.get(0)
                                                              .getClass());
        final TaskDescriptionUpdated taskDescriptionUpdated =
                (TaskDescriptionUpdated) messageList.get(0);

        assertEquals(entityId(), taskDescriptionUpdated.getTaskId());
        final String newDescription = taskDescriptionUpdated.getDescriptionChange()
                                                            .getNewValue();
        assertEquals(DESCRIPTION, newDescription);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription rejection " +
            "upon an attempt to update the description of the deleted task")
    void cannotUpdateDeletedTaskDescription() {
        dispatchCreateTaskCmd();

        final DeleteTask deleteTaskCmd = deleteTaskInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(deleteTaskCmd));

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(entityId());
        Throwable t = assertThrows(Throwable.class,
                                   () -> dispatchCommand(aggregate,
                                                         envelopeOf(updateTaskDescriptionCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskDescription.class));
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription rejection " +
            "upon an attempt to update the description of the completed task")
    void cannotUpdateCompletedTaskDescription() {
        dispatchCreateTaskCmd();

        final CompleteTask completeTaskCmd = completeTaskInstance();
        dispatchCommand(aggregate, envelopeOf(completeTaskCmd));

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(entityId());
        Throwable t = assertThrows(Throwable.class,
                                   () -> dispatchCommand(aggregate,
                                                         envelopeOf(updateTaskDescriptionCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskDescription.class));
    }

    @Test
    @DisplayName("update the task description")
    void updateDescription() {
        final String newDescription = "new description.";
        dispatchCreateTaskCmd();

        final UpdateTaskDescription updateTaskDescriptionCmd =
                updateTaskDescriptionInstance(entityId(), DESCRIPTION, newDescription);
        dispatchCommand(aggregate, envelopeOf(updateTaskDescriptionCmd));
        final Task state = aggregate.getState();

        assertEquals(entityId(), state.getId());
        assertEquals(newDescription, state.getDescription()
                                          .getValue());
    }

    @Test
    @DisplayName("produce CannotUpdateTaskDescription rejection")
    void produceRejection() {
        final CreateBasicTask createBasicTask = createTaskInstance(entityId(), DESCRIPTION);
        dispatchCommand(aggregate, envelopeOf(createBasicTask));

        final String expectedValue = "expected description";
        final String newValue = "update description";
        final String actualValue = createBasicTask.getDescription()
                                                  .getValue();
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(entityId(), expectedValue, newValue);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatchCommand(aggregate,
                                                               envelopeOf(updateTaskDescription)));
        final Throwable cause = Throwables.getRootCause(t);
        assertThat(cause, instanceOf(CannotUpdateTaskDescription.class));

        @SuppressWarnings("ConstantConditions") // Instance type checked before.
        final Rejections.CannotUpdateTaskDescription rejection =
                ((CannotUpdateTaskDescription) cause).getMessageThrown();
        final DescriptionUpdateRejected rejectionDetails = rejection.getRejectionDetails();
        final TaskId actualTaskId = rejectionDetails.getCommandDetails()
                                                    .getTaskId();
        assertEquals(entityId(), actualTaskId);

        final StringValue expectedStringValue = toMessage(expectedValue);
        final StringValue actualStringValue = toMessage(actualValue);
        final StringValue newStringValue = toMessage(newValue);

        final ValueMismatch mismatch = rejectionDetails.getDescriptionMismatch();
        assertEquals(expectedStringValue, unpack(mismatch.getExpected()));
        assertEquals(actualStringValue, unpack(mismatch.getActual()));
        assertEquals(newStringValue, unpack(mismatch.getNewValue()));
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createBasicTask = createTaskInstance(entityId(), DESCRIPTION);
        dispatchCommand(aggregate, envelopeOf(createBasicTask));
    }
}
