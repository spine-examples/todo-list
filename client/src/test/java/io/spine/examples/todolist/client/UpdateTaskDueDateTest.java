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

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.base.Time.getCurrentTime;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("After execution of UpdateTaskDueDate command")
class UpdateTaskDueDateTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class UpdateTaskDueDateInDraftTasksView {

        @Test
        @DisplayName("contain the task view with updated due date")
        void containUpdatedView() {
            Timestamp newDueDate = getCurrentTime();
            TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, true);

            assertEquals(newDueDate, view.getDueDate());
        }

        @Test
        @DisplayName("contain the task view with non-updated due date " +
                "when command has wrong task ID")
        void containNonUpdatedView() {
            Timestamp newDueDate = getCurrentTime();
            TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, false);

            assertNotEquals(newDueDate, view.getDueDate());
        }

        private TaskItem getViewAfterUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
            CreateDraft createDraft = createDraft();
            client.postCommand(createDraft);

            TaskId createdTaskId = createDraft.getId();

            updateDueDate(newDueDate, isCorrectId, createdTaskId);

            List<TaskItem> taskViews = client.getDraftTasksView()
                                             .getDraftTasks()
                                             .getItemsList();
            assertEquals(1, taskViews.size());

            TaskItem view = taskViews.get(0);
            assertEquals(createdTaskId, view.getId());

            return view;
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class UpdateTaskDueDateInMyListView {

        @Test
        @DisplayName("contain the task view with updated due date")
        void containUpdatedView() {
            Timestamp newDueDate = getCurrentTime();
            TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, true);
            assertEquals(newDueDate, view.getDueDate());
        }

        @Test
        @DisplayName("contain task view with non-updated due date when command has wrong task ID")
        void containNonUpdatedView() {
            Timestamp newDueDate = getCurrentTime();
            TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, false);
            assertNotEquals(newDueDate, view.getDueDate());
        }

        private TaskItem getViewAfterUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
            CreateBasicTask createTask = createBasicTask();
            client.postCommand(createTask);

            TaskId idOfCreatedTask = createTask.getId();

            updateDueDate(newDueDate, isCorrectId, idOfCreatedTask);
            List<TaskItem> taskViews = client.getMyListView()
                                             .getMyList()
                                             .getItemsList();
            assertEquals(1, taskViews.size());
            TaskItem view = taskViews.get(0);

            assertEquals(idOfCreatedTask, view.getId());
            return view;
        }
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class UpdateTaskDueDateInLabelledTasksView {

        @Test
        @DisplayName("contain the task view with updated due date")
        void containUpdatedView() {
            Timestamp newDueDate = getCurrentTime();
            TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, true);
            assertEquals(newDueDate, view.getDueDate());
        }

        @Test
        @DisplayName("contain the task view with non-updated due date " +
                "when command has wrong task ID")
        void containNonUpdatedView() {
            Timestamp newDueDate = getCurrentTime();
            TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, false);
            assertNotEquals(newDueDate, view.getDueDate());
        }

        private TaskItem getViewAfterUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
            CreateBasicTask createTask = createBasicTask();
            TaskId createdTaskId = createTask.getId();
            client.postCommand(createTask);

            UpdateTaskPriority updateTaskPriority = setInitialTaskPriority(createdTaskId);
            client.postCommand(updateTaskPriority);

            CreateBasicLabel createLabel = createBasicLabel();
            client.postCommand(createLabel);

            TaskId taskId = createTask.getId();
            LabelId labelId = createLabel.getLabelId();

            AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.postCommand(assignLabelToTask);

            updateDueDate(newDueDate, isCorrectId, createdTaskId);

            List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            int expectedListSize = 1;
            assertEquals(expectedListSize, tasksViewList.size());

            List<TaskItem> taskViews = tasksViewList.get(0)
                                                    .getLabelledTasks()
                                                    .getItemsList();
            assertEquals(expectedListSize, taskViews.size());

            TaskItem view = taskViews.get(0);

            assertEquals(labelId, view.getLabelId());
            assertEquals(taskId, view.getId());

            return view;
        }
    }

    private void updateDueDate(Timestamp newDueDate, boolean isCorrectId, TaskId idOfCreatedTask) {
        TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : createWrongTaskId();
        Timestamp previousDueDate = Timestamp.getDefaultInstance();
        UpdateTaskDueDate updateTaskDueDate =
                updateTaskDueDateInstance(idOfUpdatedTask, previousDueDate, newDueDate);
        client.postCommand(updateTaskDueDate);
    }
}
