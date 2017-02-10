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

package org.spine3.examples.todolist.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.TaskListView;
import org.spine3.examples.todolist.q.projections.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution RestoreDeletedTask command")
public class RestoreDeletedTaskTest extends CommandLineTodoClientTest {

    @Test
    @DisplayName("LabelledTasksView contains restored task")
    public void obtainView() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final LabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        client.delete(deleteTask);

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        client.restore(restoreDeletedTask);

        final int expectedListSize = 1;
        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());

        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(taskId, view.getId());
        assertEquals(labelId, view.getLabelId());
    }

    @Test
    @DisplayName("LabelledTasksView does not contain restored task when command has wrong task ID")
    public void obtainEmptyView() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final LabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        client.delete(deleteTask);

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(getWrongTaskId());
        client.restore(restoreDeletedTask);

        final int expectedListSize = 1;
        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());

        final TaskListView taskListView = tasksViewList.get(0)
                                                       .getLabelledTasks();
        final List<TaskView> taskViews = taskListView.getItemsList();

        assertTrue(taskViews.isEmpty());
    }
}
