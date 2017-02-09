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
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;

/**
 * @author Illia Shepilov
 */
public class DeleteTaskTest extends CommandLineTodoClientTest {

    @Test
    @DisplayName("obtain empty view list when handled DeleteTask command")
    public void emptyViewsWhenDraftIsDeleted() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final DeleteTask deleteTask = deleteTaskInstance(createDraft.getId());
        client.delete(deleteTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    @DisplayName("obtain created task draft when handled DeleteTask command with wrong ID")
    public void obtainViewWhenDeletedWrongDraft() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final TaskId taskId = createDraft.getId();
        final DeleteTask deleteTask = deleteTaskInstance(getWrongTaskId());
        client.delete(deleteTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());
    }

    @Test
    public void obtain_empty_labelled_tasks_view_when_handled_command_delete_task() {
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

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, labelledTasksView.size());

        final List<TaskView> taskViews = labelledTasksView.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_delete_task_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final LabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final DeleteTask deleteTask = deleteTaskInstance(getWrongTaskId());
        client.delete(deleteTask);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, labelledTasksView.size());

        final List<TaskView> taskViews = labelledTasksView.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());
    }

    @Test
    public void obtain_empty_my_list_view_when_handled_command_delete_task() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        final List<TaskView> taskViews = obtainTaskViewListWhenHandledDeleteTask(idOfCreatedTask, true);

        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_not_empty_my_list_view_when_handled_command_delete_task_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        final List<TaskView> taskViews = obtainTaskViewListWhenHandledDeleteTask(idOfCreatedTask, false);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        final TaskId taskId = createTask.getId();
        assertEquals(taskId, view.getId());
    }

    private List<TaskView> obtainTaskViewListWhenHandledDeleteTask(TaskId idOfCreatedTask, boolean isCorrectId) {
        final TaskId idOfDeletedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();

        final DeleteTask deleteTask = deleteTaskInstance(idOfDeletedTask);
        client.delete(deleteTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        return taskViews;
    }
}
