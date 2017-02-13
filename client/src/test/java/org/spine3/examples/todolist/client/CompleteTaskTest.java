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
import org.junit.jupiter.api.Nested;
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
@DisplayName("After execution CompleteTask command")
public class CompleteTaskTest extends CommandLineTodoClientTest {

    private static final String CONTAIN_TASK_VIEW_MARKED_AS_COMPLETED = "contain task view marked as completed";
    private static final String TASK_VIEW_WHICH_DOES_NOT_MARKED_AS_COMPLETED_WHEN_COMMAND_HAS_WRONG_TASK_ID =
            "task view which does not marked as completed when command has wrong task ID";

    @Nested
    @DisplayName(LABELLED_TASK_VIEW_SHOULD)
    class CompleteTaskFromLabelledTaskView {

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_MARKED_AS_COMPLETED)
        public void obtainLabelledViewWithCompletedTask() {
            final TaskView view = obtainViewWhenHandledCommandCompleteTask(true);
            assertTrue(view.getCompleted());
        }

        @Test
        @DisplayName(TASK_VIEW_WHICH_DOES_NOT_MARKED_AS_COMPLETED_WHEN_COMMAND_HAS_WRONG_TASK_ID)
        public void obtainLabelledViewWithUncompletedTask() {
            final TaskView view = obtainViewWhenHandledCommandCompleteTask(false);
            assertFalse(view.getCompleted());
        }
    }

    @Nested
    @DisplayName(MY_LIST_VIEW_SHOULD)
    class CompleteTaskFromMyListView {

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_MARKED_AS_COMPLETED)
        public void obtainMyListViewWithCompletedTask() {
            final TaskView view = obtainTaskViewWhenHandledCompleteTask(true);
            assertTrue(view.getCompleted());
        }

        @Test
        @DisplayName(TASK_VIEW_WHICH_DOES_NOT_MARKED_AS_COMPLETED_WHEN_COMMAND_HAS_WRONG_TASK_ID)
        public void obtainMyListViewWithoutCompletedTask() {
            final TaskView view = obtainTaskViewWhenHandledCompleteTask(false);
            assertFalse(view.getCompleted());
        }
    }

    private TaskView obtainViewWhenHandledCommandCompleteTask(boolean isCorrectId) {
        final CreateBasicTask createTask = createTask();
        final TaskId createdTaskId = createTask.getId();

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final LabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        completeTask(isCorrectId, createdTaskId);

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
        final CreateBasicTask createTask = createTask();
        final TaskId idOfCreatedTask = createTask.getId();

        completeTask(isCorrectId, idOfCreatedTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView result = taskViews.get(0);
        assertEquals(idOfCreatedTask, result.getId());

        return result;
    }

    private void completeTask(boolean isCorrectId, TaskId idOfCreatedTask) {
        final TaskId idOfCompletedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();
        final CompleteTask completeTask = completeTaskInstance(idOfCompletedTask);
        client.complete(completeTask);
    }
}
