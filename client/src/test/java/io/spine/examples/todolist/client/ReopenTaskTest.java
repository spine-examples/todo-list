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

package io.spine.examples.todolist.client;

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("After execution of ReopenTask command")
class ReopenTaskTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class ReopenTaskFromLabelledTasksView {

        @Test
        @DisplayName("contain the task view with uncompleted task")
        void containViewWithUncompletedTask() {
            TaskItem view = obtainViewWhenHandledCommandReopenTask(true);
            assertFalse(view.getCompleted());
        }

        @Test
        @DisplayName("contain the task view with completed task when command has wrong ID")
        void containViewWithCompletedTask() {
            TaskItem view = obtainViewWhenHandledCommandReopenTask(false);
            assertTrue(view.getCompleted());
        }

        private TaskItem obtainViewWhenHandledCommandReopenTask(boolean isCorrectId) {
            CreateBasicTask createTask = createTask();
            TaskId createdTaskId = createTask.getId();

            CreateBasicLabel createLabel = createBasicLabel();
            client.postCommand(createLabel);

            TaskId taskId = createTask.getId();
            LabelId labelId = createLabel.getLabelId();

            AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.postCommand(assignLabelToTask);

            completeAndReopenTask(isCorrectId, createdTaskId);

            List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
            assertEquals(1, labelledTasksView.size());

            List<TaskItem> taskViews = labelledTasksView.get(0)
                                                        .getLabelledTasks()
                                                        .getItemsList();
            return checkAndObtainView(taskId, taskViews);
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class ReopenTaskFromMyListView {

        @Test
        @DisplayName("contain the task view with uncompleted task")
        void containViewWithUncompletedTask() {
            TaskItem view = obtainTaskItemWhenHandledReopenTask(true);
            assertFalse(view.getCompleted());
        }

        @Test
        @DisplayName("contain the task view with completed task when command has wrong ID")
        void containViewWithCompletedTask() {
            TaskItem view = obtainTaskItemWhenHandledReopenTask(false);
            assertTrue(view.getCompleted());
        }

        private TaskItem obtainTaskItemWhenHandledReopenTask(boolean isCorrectId) {
            CreateBasicTask createTask = createTask();
            TaskId idOfCreatedTask = createTask.getId();

            completeAndReopenTask(isCorrectId, idOfCreatedTask);

            List<TaskItem> taskViews = client.getMyListView()
                                             .getMyList()
                                             .getItemsList();
            return checkAndObtainView(idOfCreatedTask, taskViews);
        }
    }

    private static TaskItem checkAndObtainView(TaskId idOfCreatedTask, List<TaskItem> taskViews) {
        assertEquals(1, taskViews.size());

        TaskItem view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());
        return view;
    }

    private void completeAndReopenTask(boolean isCorrectId, TaskId createdTaskId) {
        CompleteTask completeTask = completeTaskInstance(createdTaskId);
        client.postCommand(completeTask);

        TaskId reopenedTaskId = isCorrectId ? createdTaskId : createWrongTaskId();
        ReopenTask reopenTask = reopenTaskInstance(reopenedTaskId);
        client.postCommand(reopenTask);
    }
}
