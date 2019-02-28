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
import com.google.protobuf.Timestamp;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskDueDateUpdateRejected;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDueDate;
import io.spine.examples.todolist.c.rejection.Rejections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.base.Time.getCurrentTime;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DUE_DATE;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UpdateTaskDueDate command should be interpreted by TaskPart and")
class UpdateTaskDueDateTest extends TaskCommandTest<UpdateTaskDueDate> {

    UpdateTaskDueDateTest() {
        super(updateTaskDueDateInstance());
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        dispatchCreateTaskCmd();
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDueDate rejection " +
            "upon an attempt to update the due date of the completed task")
    void cannotUpdateCompletedTaskDueDate() {
        final CompleteTask completeTaskCmd = completeTaskInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(completeTaskCmd));

        final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance(entityId());
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatchCommand(aggregate,
                                                               envelopeOf(updateTaskDueDateCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskDueDate.class));
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDueDate rejection " +
            "upon an attempt to update the due date of the deleted task")
    void cannotUpdateDeletedTaskDueDate() {
        final DeleteTask deleteTaskCmd = deleteTaskInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(deleteTaskCmd));

        final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance(entityId());
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatchCommand(aggregate,
                                                               envelopeOf(updateTaskDueDateCmd)));
        assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskDueDate.class));
    }

    @Test
    @DisplayName("produce TaskDueDateUpdated event")
    void produceEvent() {
        final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance(entityId());
        final List<? extends Message> messageList =
                dispatchCommand(aggregate, envelopeOf(updateTaskDueDateCmd));
        assertEquals(1, messageList.size());
        assertEquals(TaskDueDateUpdated.class, messageList.get(0)
                                                          .getClass());

        final TaskDueDateUpdated taskDueDateUpdated = (TaskDueDateUpdated) messageList.get(0);
        assertEquals(entityId(), taskDueDateUpdated.getTaskId());
        final Timestamp newDueDate = taskDueDateUpdated.getDueDateChange()
                                                       .getNewValue();
        assertEquals(DUE_DATE, newDueDate);
    }

    @Test
    @DisplayName("update the task due date")
    void updateDueDate() {
        final Timestamp updatedDueDate = getCurrentTime();
        final UpdateTaskDueDate updateTaskDueDateCmd =
                updateTaskDueDateInstance(entityId(), Timestamp.getDefaultInstance(),
                                          updatedDueDate);
        dispatchCommand(aggregate, envelopeOf(updateTaskDueDateCmd));
        final Task state = aggregate.state();

        assertEquals(entityId(), state.getId());
        assertEquals(updatedDueDate, state.getDueDate());
    }

    @Test
    @DisplayName("produce CannotUpdateTaskDueDate rejection")
    void produceRejection() {
        final Timestamp expectedDueDate = getCurrentTime();
        final Timestamp newDueDate = getCurrentTime();

        final UpdateTaskDueDate updateTaskDueDate =
                updateTaskDueDateInstance(entityId(), expectedDueDate, newDueDate);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> dispatchCommand(aggregate,
                                                               envelopeOf(updateTaskDueDate)));
        final Throwable cause = Throwables.getRootCause(t);
        assertThat(cause, instanceOf(CannotUpdateTaskDueDate.class));

        final Rejections.CannotUpdateTaskDueDate cannotUpdateTaskDueDate =
                ((CannotUpdateTaskDueDate) cause).getMessageThrown();

        final TaskDueDateUpdateRejected details = cannotUpdateTaskDueDate.getRejectionDetails();
        final TaskId actualTaskId = details.getCommandDetails()
                                           .getTaskId();
        assertEquals(entityId(), actualTaskId);

        final ValueMismatch mismatch = details.getDueDateMismatch();

        assertEquals(newDueDate, unpack(mismatch.getNewValue()));
        assertEquals(expectedDueDate, unpack(mismatch.getExpected()));

        final Timestamp actualDueDate = Timestamp.getDefaultInstance();
        assertEquals(actualDueDate, unpack(mismatch.getActual()));
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createTaskCmd = createTaskInstance(entityId(), DESCRIPTION);
        dispatchCommand(aggregate, envelopeOf(createTaskCmd));
    }
}
