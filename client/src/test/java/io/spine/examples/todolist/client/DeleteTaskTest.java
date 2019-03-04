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
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("After execution of DeleteTask command")
class DeleteTaskTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class DeleteTaskFromLabelledTasksView {

        @Test
        @DisplayName("be empty")
        void obtainEmptyView() {
            CreateBasicTask createTask = createTask();

            CreateBasicLabel createLabel = createBasicLabel();
            client.postCommand(createLabel);

            LabelId labelId = createLabel.getLabelId();
            TaskId taskId = createTask.getId();

            AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.postCommand(assignLabelToTask);

            DeleteTask deleteTask = deleteTaskInstance(taskId);
            client.postCommand(deleteTask);

            List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
            assertEquals(1, labelledTasksView.size());

            List<TaskItem> taskViews = labelledTasksView.get(0)
                                                        .getLabelledTasks()
                                                        .getItemsList();
            assertTrue(taskViews.isEmpty());
        }

        @Test
        @DisplayName("contain task view when command has wrong ID")
        void obtainView() {
            CreateBasicTask createTask = createTask();

            CreateBasicLabel createLabel = createBasicLabel();
            client.postCommand(createLabel);

            LabelId labelId = createLabel.getLabelId();
            TaskId taskId = createTask.getId();

            AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.postCommand(assignLabelToTask);

            DeleteTask deleteTask = deleteTaskInstance(createWrongTaskId());
            client.postCommand(deleteTask);

            List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
            int expectedListSize = 1;
            assertEquals(expectedListSize, labelledTasksView.size());

            List<TaskItem> taskViews = labelledTasksView.get(0)
                                                        .getLabelledTasks()
                                                        .getItemsList();
            assertEquals(expectedListSize, taskViews.size());

            TaskItem view = taskViews.get(0);
            assertEquals(taskId, view.getId());
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class DeleteTaskFromDraftTasksView {

        @Test
        @DisplayName("be empty")
        void obtainEmptyView() {
            CreateDraft createDraft = createDraft();
            client.postCommand(createDraft);

            DeleteTask deleteTask = deleteTaskInstance(createDraft.getId());
            client.postCommand(deleteTask);

            List<TaskItem> taskViews = client.getDraftTasksView()
                                             .getDraftTasks()
                                             .getItemsList();
            assertTrue(taskViews.isEmpty());
        }

        @Test
        @DisplayName("contain task view when command has wrong ID")
        void obtainView() {
            CreateDraft createDraft = createDraft();
            client.postCommand(createDraft);

            TaskId taskId = createDraft.getId();
            DeleteTask deleteTask = deleteTaskInstance(createWrongTaskId());
            client.postCommand(deleteTask);

            List<TaskItem> taskViews = client.getDraftTasksView()
                                             .getDraftTasks()
                                             .getItemsList();
            assertEquals(1, taskViews.size());

            TaskItem view = taskViews.get(0);
            assertEquals(taskId, view.getId());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class DeleteTaskFromMyListView {

        @Test
        @DisplayName("be empty")
        void obtainEmptyView() {
            CreateBasicTask createTask = createTask();

            TaskId idOfCreatedTask = createTask.getId();
            List<TaskItem> taskViews =
                    obtainTaskItemListWhenHandledDeleteTask(idOfCreatedTask, true);

            assertTrue(taskViews.isEmpty());
        }

        @Test
        @DisplayName("contain task view when command has wrong ID")
        void obtainView() {
            CreateBasicTask createTask = createTask();

            TaskId idOfCreatedTask = createTask.getId();
            List<TaskItem> taskViews =
                    obtainTaskItemListWhenHandledDeleteTask(idOfCreatedTask, false);
            assertEquals(1, taskViews.size());

            TaskItem view = taskViews.get(0);
            TaskId taskId = createTask.getId();
            assertEquals(taskId, view.getId());
        }

        private List<TaskItem>
        obtainTaskItemListWhenHandledDeleteTask(TaskId idOfCreatedTask, boolean isCorrectId) {
            TaskId idOfDeletedTask = isCorrectId ? idOfCreatedTask : createWrongTaskId();

            DeleteTask deleteTask = deleteTaskInstance(idOfDeletedTask);
            client.postCommand(deleteTask);

            return client.getMyListView()
                         .getMyList()
                         .getItemsList();
        }
    }
}
