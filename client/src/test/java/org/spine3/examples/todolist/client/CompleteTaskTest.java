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

import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;

/**
 * @author Illia Shepilov
 */
public class CompleteTaskTest extends CommandLineTodoClientTest {

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_complete_task() {
        final TaskView view = obtainViewWhenHandledCommandCompleteTask(true);
        assertTrue(view.getCompleted());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_complete_task_with_wrong_task_id() {
        final TaskView view = obtainViewWhenHandledCommandCompleteTask(false);
        assertFalse(view.getCompleted());
    }

    @Test
    public void obtain_my_list_view_with_completed_task_view_when_handled_complete_task_command() {
        final TaskView view = obtainTaskViewWhenHandledCompleteTask(true);
        assertTrue(view.getCompleted());
    }

    @Test
    public void obtain_my_list_view_with_uncompleted_task_view_when_handled_command_complete_task_with_wrong_task_id() {
        final TaskView view = obtainTaskViewWhenHandledCompleteTask(false);
        assertFalse(view.getCompleted());
    }

    private TaskView obtainViewWhenHandledCommandCompleteTask(boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final LabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final TaskId completedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final CompleteTask completeTask = completeTaskInstance(completedTaskId);
        client.complete(completeTask);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, labelledTasksView.size());

        final List<TaskView> taskViews = labelledTasksView.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());

        return view;
    }


    private TaskView obtainTaskViewWhenHandledCompleteTask(boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId idOfCreatedTask = createTask.getId();
        client.create(createTask);

        final TaskId idOfCompletedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();
        final CompleteTask completeTask = completeTaskInstance(idOfCompletedTask);
        client.complete(completeTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }
}
