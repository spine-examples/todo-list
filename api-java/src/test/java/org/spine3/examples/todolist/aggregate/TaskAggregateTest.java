//
// Copyright 2016, TeamDev Ltd. All rights reserved.
//
// Redistribution and use in source and/or binary forms, with or without
// modification, must retain the above copyright notice and the following
// disclaimer.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
package org.spine3.examples.todolist.aggregate;

import org.junit.Before;
import org.junit.Test;
import org.spine3.base.CommandContext;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaslInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;

public class TaskAggregateTest {

    private static final CommandContext COMMAND_CONTEXT = createCommandContext();
    private TaskAggregate aggregate;
    private CreateBasicTask createTaskCmd;
    private UpdateTaskDescription updateTaskDescriptionCmd;
    private UpdateTaskDueDate updateTaskDueDate;
    private UpdateTaskPriority updateTaskPriority;
    private ReopenTask reopenTask;
    private DeleteTask deleteTask;
    private RestoreDeletedTask restoreDeletedTask;
    private CompleteTask completeTask;

    private static final TaskId ID = TaskId.newBuilder()
                                           .setValue(newUuid())
                                           .build();

    @Before
    public void setUp() throws Exception {
        aggregate = new TaskAggregate(ID);
        createTaskCmd = createTaskInstance();
        updateTaskDescriptionCmd = updateTaskDescriptionInstance();
        updateTaskDueDate = updateTaskDueDateInstance();
        updateTaskPriority = updateTaskPriorityInstance();
        reopenTask = reopenTaskInstance();
        deleteTask = deleteTaslInstance();
        restoreDeletedTask = restoreDeletedTaskInstance();
        completeTask = completeTaskInstance();
    }

    @Test
    public void accept_Message_id_to_constructor() {
        try {
            final TaskAggregate aggregate = new TaskAggregate(ID);
            assertEquals(ID, aggregate.getId());
        } catch (Throwable e) {
            fail();
        }
    }

    @Test
    public void handle_create_task_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

        assertNotNull(aggregate.getState()
                               .getCreated());
        assertNotNull(aggregate.getId());
        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskCreated.class, messageList.get(0)
                                                   .getClass());
    }

    @Test
    public void handle_update_task_description_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskDescriptionUpdated.class, messageList.get(0)
                                                              .getClass());
    }

    @Test(expected = RuntimeException.class)
    public void handle_update_task_description_when_description_contains_one_symbol() {
        updateTaskDescriptionCmd = updateTaskDescriptionInstance(".");
        aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
    }

    @Test
    public void handle_update_task_due_date_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(updateTaskDueDate, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskDueDateUpdated.class, messageList.get(0)
                                                          .getClass());
    }

    @Test
    public void handle_update_task_priority_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(updateTaskPriority, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskPriorityUpdated.class, messageList.get(0)
                                                           .getClass());
    }

    @Test(expected = RuntimeException.class)
    public void handle_reopen_task_command_when_task_not_completed() {
        final int expectedListSize = 1;
        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(reopenTask, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskReopened.class, messageList.get(0)
                                                    .getClass());
    }

    @Test
    public void handle_delete_task_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(deleteTask, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskDeleted.class, messageList.get(0)
                                                   .getClass());
    }

    @Test(expected = RuntimeException.class)
    public void handle_restore_deleted_task_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(restoreDeletedTask, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(DeletedTaskRestored.class, messageList.get(0)
                                                           .getClass());
    }

    @Test
    public void handle_complete_task_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(completeTask, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(TaskCompleted.class, messageList.get(0)
                                                     .getClass());
    }

}