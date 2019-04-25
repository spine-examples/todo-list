/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.client;

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import io.spine.examples.todolist.q.projection.TaskListView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("After execution of RestoreDeletedTask command")
class RestoreDeletedTaskTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("LabelledTasksView should contain restored task")
    void containRestoredTask() {
        CreateBasicTask createTask = createTask();
        CreateBasicLabel createLabel = createLabel();

        LabelId labelId = createLabel.getLabelId();
        TaskId taskId = createTask.getId();

        assignAndDeleteTask(labelId, taskId);

        RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        client.postCommand(restoreDeletedTask);

        int expectedListSize = 1;
        List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());

        List<TaskItem> taskViews = tasksViewList.get(0)
                                                .getLabelledTasks()
                                                .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        TaskItem view = taskViews.get(0);

        assertEquals(taskId, view.getId());
        assertEquals(labelId, view.getLabelId());
    }

    @Test
    @DisplayName("LabelledTasksView should not contain restored task " +
            "when command has wrong task ID")
    void containEmptyView() {
        CreateBasicTask createTask = createTask();
        CreateBasicLabel createLabel = createLabel();

        LabelId labelId = createLabel.getLabelId();
        TaskId taskId = createTask.getId();

        assignAndDeleteTask(labelId, taskId);

        RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(createWrongTaskId());
        client.postCommand(restoreDeletedTask);

        List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(1, tasksViewList.size());

        TaskListView taskListView = tasksViewList.get(0)
                                                 .getLabelledTasks();
        List<TaskItem> taskViews = taskListView.getItemsList();

        assertTrue(taskViews.isEmpty());
    }

    private void assignAndDeleteTask(LabelId labelId, TaskId taskId) {
        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        DeleteTask deleteTask = deleteTaskInstance(taskId);
        client.postCommand(deleteTask);
    }
}