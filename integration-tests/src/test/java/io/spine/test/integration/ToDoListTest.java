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

package io.spine.test.integration;

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.q.projection.TaskItem;
import io.spine.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.base.Time.getCurrentTime;
import static io.spine.examples.todolist.TaskPriority.HIGH;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.removeLabelFromTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("OverlyCoupledClass")
@DisplayName("TodoList Integration Test")
class ToDoListTest extends AbstractIntegrationTest {

    private TodoClient client;

    @BeforeEach
    @Override
    protected void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("Create task -> Create label -> Assign label -> Complete task")
    void firstFlow() {
        CreateBasicTask basicTask = createBasicTask();
        client.postCommand(basicTask);

        CreateBasicLabel basicLabel = createBasicLabel();
        client.postCommand(basicLabel);

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(
                basicTask.getId(), basicLabel.getLabelId());
        client.postCommand(assignLabelToTask);

        CompleteTask completeTask = completeTaskInstance(basicTask.getId());
        client.postCommand(completeTask);

        List<TaskItem> taskItems = client.getMyListView()
                                         .getMyList()
                                         .getItemsList();

        assertEquals(1, taskItems.size());

        TaskItem taskItem = taskItems.get(0);
        assertTrue(taskItem.getCompleted());
    }

    @Test
    @DisplayName("Create task -> Create label -> Assign label -> Complete task -> Reopen task " +
            "-> Remove label")
    void secondFlow() {
        CreateBasicTask basicTask = createBasicTask();
        client.postCommand(basicTask);

        CreateBasicLabel basicLabel = createBasicLabel();
        client.postCommand(basicLabel);

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(
                basicTask.getId(), basicLabel.getLabelId());
        client.postCommand(assignLabelToTask);

        CompleteTask completeTask = completeTaskInstance(basicTask.getId());
        client.postCommand(completeTask);

        ReopenTask reopenTask = reopenTaskInstance(basicTask.getId());
        client.postCommand(reopenTask);

        RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(
                basicTask.getId(),
                basicLabel.getLabelId());
        client.postCommand(removeLabelFromTask);

        List<TaskItem> tasks = client.getMyListView()
                                     .getMyList()
                                     .getItemsList();

        List<TaskItem> labeledTasks = client.getLabelledTasksView()
                                            .get(0)
                                            .getLabelledTasks()
                                            .getItemsList();

        assertEquals(1, tasks.size());

        TaskItem taskItem = tasks.get(0);

        assertFalse(taskItem.getCompleted());

        assertTrue(labeledTasks.isEmpty());
    }

    @Test
    @DisplayName("Create draft -> Update due date -> Update priority -> Create label " +
            "-> Assign label")
    void thirdFlow() {
        CreateDraft draftTask = createDraft();
        client.postCommand(draftTask);

        Timestamp newDueDate = getCurrentTime();
        Timestamp previousDueDate = Timestamp.getDefaultInstance();
        UpdateTaskDueDate updateTaskDueDate =
                updateTaskDueDateInstance(draftTask.getId(), previousDueDate, newDueDate);
        client.postCommand(updateTaskDueDate);

        TaskPriority newPriority = HIGH;
        UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(draftTask.getId(), TaskPriority.TP_UNDEFINED,
                                           newPriority);
        client.postCommand(updateTaskPriority);

        CreateBasicLabel basicLabel = createBasicLabel();
        client.postCommand(basicLabel);

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(
                draftTask.getId(), basicLabel.getLabelId());
        client.postCommand(assignLabelToTask);

        List<TaskItem> draftTasks = client.getDraftTasksView()
                                          .getDraftTasks()
                                          .getItemsList();

        assertEquals(1, draftTasks.size());
        assertEquals(newDueDate, draftTasks.get(0)
                                           .getDueDate());
        assertEquals(newPriority, draftTasks.get(0)
                                            .getPriority());
    }

    @Test
    @DisplayName("Create task -> Create label -> Assign label -> Update priority -> Delete task " +
            "-> Restore deleted task")
    void fourthFlow() {
        CreateBasicTask basicTask = createBasicTask();
        client.postCommand(basicTask);

        CreateBasicLabel basicLabel = createBasicLabel();
        client.postCommand(basicLabel);

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(
                basicTask.getId(), basicLabel.getLabelId());
        client.postCommand(assignLabelToTask);

        TaskPriority newPriority = HIGH;
        UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(basicTask.getId(), TaskPriority.TP_UNDEFINED,
                                           newPriority);
        client.postCommand(updateTaskPriority);

        DeleteTask deleteTask = deleteTaskInstance(basicTask.getId());
        client.postCommand(deleteTask);

        RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(basicTask.getId());
        client.postCommand(restoreDeletedTask);

        List<TaskItem> labeledTasks = client.getLabelledTasksView()
                                            .get(0)
                                            .getLabelledTasks()
                                            .getItemsList();

        assertEquals(1, labeledTasks.size());
        assertEquals(newPriority, labeledTasks.get(0)
                                              .getPriority());
    }
}
