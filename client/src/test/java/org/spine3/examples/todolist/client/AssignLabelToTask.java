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
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;

/**
 * @author Illia Shepilov
 */
public class AssignLabelToTask extends CommandLineTodoClientTest {

    @Test
    @DisplayName("obtain task view without labels when handled AssignLabelToTask command with wrong task ID")
    public void obtainViewWithoutLabelsWhenTaskIdIsWrong() {
        final CreateBasicLabel createBasicLabel = createBasicLabel();
        client.create(createBasicLabel);
        final LabelId labelId = createBasicLabel.getLabelId();

        final TaskView view = obtainViewWhenHandledAssignLabelToTask(labelId, false);
        assertNotEquals(labelId, view.getLabelId());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_assign_label_to_task_with_different_task_ids() {
        final CreateBasicTask firstTask = createBasicTask();
        client.create(firstTask);

        final CreateBasicTask secondTask = createBasicTask();
        client.create(secondTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final LabelId labelId = createLabel.getLabelId();
        final TaskId firstTaskId = firstTask.getId();
        final TaskId secondTaskId = secondTask.getId();

        final org.spine3.examples.todolist.c.commands.AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(secondTaskId, labelId);
        client.assignLabel(assignLabelToTask);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, tasksViewList.size());

        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertNotEquals(firstTaskId, taskView.getId());
        assertEquals(secondTaskId, taskView.getId());
        assertEquals(labelId, taskView.getLabelId());
    }

    @Test
    public void obtain_tasks_with_assigned_labels_from_labelled_tasks_view() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final LabelId labelId = createLabel.getLabelId();

        final org.spine3.examples.todolist.c.commands.AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

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
    @DisplayName("obtain task view with labels when handled AssignLabelToTask command")
    public void obtainLabelledView() {
        final CreateBasicLabel createBasicLabel = createBasicLabel();
        client.create(createBasicLabel);

        final LabelId labelId = createBasicLabel.getLabelId();
        final TaskView view = obtainViewWhenHandledAssignLabelToTask(labelId, true);

        assertEquals(labelId, view.getLabelId());
    }

    @Test
    public void obtain_my_list_view_when_handled_command_assign_label_to_task() {
        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final LabelId labelId = createLabel.getLabelId();
        final TaskView view = obtainTaskViewWhenHandledAssignLabelToTask(labelId, true);

        assertEquals(labelId, view.getLabelId());
    }

    @Test
    public void obtain_my_list_view_when_handled_command_assign_label_to_task_with_wrong_task_id() {
        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final LabelId labelId = createLabel.getLabelId();
        final TaskView view = obtainTaskViewWhenHandledAssignLabelToTask(labelId, false);

        assertNotEquals(labelId, view.getLabelId());
    }

    private TaskView obtainTaskViewWhenHandledAssignLabelToTask(LabelId labelId, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();

        final org.spine3.examples.todolist.c.commands.AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(idOfUpdatedTask, labelId);
        client.assignLabel(assignLabelToTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledAssignLabelToTask(LabelId labelId, boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final TaskId createTaskId = createDraft.getId();
        final TaskId taskIdToAssign = isCorrectId ? createTaskId : getWrongTaskId();

        final org.spine3.examples.todolist.c.commands.AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskIdToAssign, labelId);
        client.assignLabel(assignLabelToTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createTaskId, view.getId());

        return view;
    }
}
