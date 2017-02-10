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
import org.spine3.examples.todolist.c.commands.ReopenTask;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution ReopenTask command")
public class ReopenTaskTest extends CommandLineTodoClientTest {

    private static final String CONTAIN_TASK_VIEW_WITH_UNCOMPLETED_TASK = "contain task view with uncompleted task";
    private static final String CONTAIN_TASK_VIEW_WITH_COMPLETED_TASK = "contain task view with completed task " +
            "when command has wrong id";

    @Nested
    @DisplayName(LABELLED_TASK_VIEW_SHOULD)
    class ReopenTaskFromLabelledTasksView {

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WITH_UNCOMPLETED_TASK)
        public void containViewWithUncompletedTask() {
            final TaskView view = obtainViewWhenHandledCommandReopenTask(true);
            assertFalse(view.getCompleted());
        }

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WITH_COMPLETED_TASK)
        public void containViewWithCompletedTask() {
            final TaskView view = obtainViewWhenHandledCommandReopenTask(false);
            assertTrue(view.getCompleted());
        }
    }

    @Nested
    @DisplayName(MY_LIST_VIEW_SHOULD)
    class ReopenTaskFromMyListView {

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WITH_UNCOMPLETED_TASK)
        public void containViewWithUncompletedTask() {
            final TaskView view = obtainTaskViewWhenHandledReopenTask(true);
            assertFalse(view.getCompleted());
        }

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WITH_COMPLETED_TASK)
        public void containViewWithCompletedTask() {
            final TaskView view = obtainTaskViewWhenHandledReopenTask(false);
            assertTrue(view.getCompleted());
        }
    }

    private TaskView obtainViewWhenHandledCommandReopenTask(boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final LabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final CompleteTask completeTask = completeTaskInstance(createdTaskId);
        client.complete(completeTask);

        final TaskId reopenedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final ReopenTask reopenTask = reopenTaskInstance(reopenedTaskId);
        client.reopen(reopenTask);

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

    private TaskView obtainTaskViewWhenHandledReopenTask(boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId idOfReopenedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();

        final CompleteTask completeTask = completeTaskInstance(idOfCreatedTask);
        client.complete(completeTask);

        final ReopenTask reopenTask = reopenTaskInstance(idOfReopenedTask);
        client.reopen(reopenTask);

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
