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
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("After execution of CompleteTask command")
class CompleteTaskTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class CompleteTaskFromLabelledTasksView {

        @Test
        @DisplayName("contain task view marked as completed")
        void obtainLabelledViewWithCompletedTask() {
            TaskItem view = obtainViewWhenHandledCommandCompleteTask(true);
            assertTrue(view.getCompleted());
        }

        @Test
        @DisplayName("contain task view which does not marked as completed " +
                "when command has wrong task ID")
        void obtainLabelledViewWithUncompletedTask() {
            TaskItem view = obtainViewWhenHandledCommandCompleteTask(false);
            assertFalse(view.getCompleted());
        }

        private TaskItem obtainViewWhenHandledCommandCompleteTask(boolean isCorrectId) {
            CreateBasicTask createTask = createTask();
            TaskId createdTaskId = createTask.getId();

            CreateBasicLabel createLabel = createBasicLabel();
            client.postCommand(createLabel);

            TaskId taskId = createTask.getId();
            LabelId labelId = createLabel.getLabelId();

            AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.postCommand(assignLabelToTask);

            completeTask(isCorrectId, createdTaskId);

            List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
            int expectedListSize = 1;
            assertEquals(expectedListSize, labelledTasksView.size());

            List<TaskItem> taskViews = labelledTasksView.get(0)
                                                        .getLabelledTasks()
                                                        .getItemsList();
            assertEquals(expectedListSize, taskViews.size());

            TaskItem view = taskViews.get(0);
            assertEquals(taskId, view.getId());

            return view;
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class CompleteTaskFromMyListView {

        @Test
        @DisplayName("contain task view marked as completed")
        void obtainMyListViewWithCompletedTask() {
            TaskItem view = obtainTaskItemWhenHandledCompleteTask(true);
            assertTrue(view.getCompleted());
        }

        @Test
        @DisplayName("task view which does not marked as completed when command has wrong task ID")
        void obtainMyListViewWithoutCompletedTask() {
            TaskItem view = obtainTaskItemWhenHandledCompleteTask(false);
            assertFalse(view.getCompleted());
        }

        private TaskItem obtainTaskItemWhenHandledCompleteTask(boolean isCorrectId) {
            CreateBasicTask createTask = createTask();
            TaskId idOfCreatedTask = createTask.getId();

            completeTask(isCorrectId, idOfCreatedTask);

            List<TaskItem> taskViews = client.getMyListView()
                                             .getMyList()
                                             .getItemsList();
            assertEquals(1, taskViews.size());

            TaskItem result = taskViews.get(0);
            assertEquals(idOfCreatedTask, result.getId());

            return result;
        }
    }

    private void completeTask(boolean isCorrectId, TaskId idOfCreatedTask) {
        TaskId idOfCompletedTask = isCorrectId ? idOfCreatedTask : createWrongTaskId();
        CompleteTask completeTask = completeTaskInstance(idOfCompletedTask);
        client.postCommand(completeTask);
    }
}
