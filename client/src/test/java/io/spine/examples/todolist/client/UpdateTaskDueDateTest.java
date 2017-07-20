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

package io.spine.examples.todolist.client;

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static io.spine.time.Time.getCurrentTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution of UpdateTaskDueDate command")
class UpdateTaskDueDateTest extends CommandLineTodoClientTest {

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
            final Timestamp newDueDate = getCurrentTime();
            final TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, true);

            assertEquals(newDueDate, view.getDueDate());
        }

        @Test
        @DisplayName("contain the task view with non-updated due date " +
                "when command has wrong task ID")
        void containNonUpdatedView() {
            final Timestamp newDueDate = getCurrentTime();
            final TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, false);

            assertNotEquals(newDueDate, view.getDueDate());
        }

        private TaskItem getViewAfterUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
            final CreateDraft createDraft = createDraft();
            client.create(createDraft);

            final TaskId createdTaskId = createDraft.getId();

            updateDueDate(newDueDate, isCorrectId, createdTaskId);

            final List<TaskItem> taskViews = client.getDraftTasksView()
                                                   .getDraftTasks()
                                                   .getItemsList();
            assertEquals(1, taskViews.size());

            final TaskItem view = taskViews.get(0);
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
            final Timestamp newDueDate = getCurrentTime();
            final TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, true);
            assertEquals(newDueDate, view.getDueDate());
        }

        @Test
        @DisplayName("contain task view with non-updated due date when command has wrong task ID")
        void containNonUpdatedView() {
            final Timestamp newDueDate = getCurrentTime();
            final TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, false);
            assertNotEquals(newDueDate, view.getDueDate());
        }

        private TaskItem getViewAfterUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
            final CreateBasicTask createTask = createBasicTask();
            client.create(createTask);

            final TaskId idOfCreatedTask = createTask.getId();

            updateDueDate(newDueDate, isCorrectId, idOfCreatedTask);
            final List<TaskItem> taskViews = client.getMyListView()
                                                   .getMyList()
                                                   .getItemsList();
            assertEquals(1, taskViews.size());
            final TaskItem view = taskViews.get(0);

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
            final Timestamp newDueDate = getCurrentTime();
            final TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, true);
            assertEquals(newDueDate, view.getDueDate());
        }

        @Test
        @DisplayName("contain the task view with non-updated due date " +
                "when command has wrong task ID")
        void containNonUpdatedView() {
            final Timestamp newDueDate = getCurrentTime();
            final TaskItem view = getViewAfterUpdateTaskDueDate(newDueDate, false);
            assertNotEquals(newDueDate, view.getDueDate());
        }

        private TaskItem getViewAfterUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
            final CreateBasicTask createTask = createBasicTask();
            final TaskId createdTaskId = createTask.getId();
            client.create(createTask);

            final CreateBasicLabel createLabel = createBasicLabel();
            client.create(createLabel);

            final TaskId taskId = createTask.getId();
            final LabelId labelId = createLabel.getLabelId();

            final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.assignLabel(assignLabelToTask);

            updateDueDate(newDueDate, isCorrectId, createdTaskId);

            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            final int expectedListSize = 1;
            assertEquals(expectedListSize, tasksViewList.size());

            final List<TaskItem> taskViews = tasksViewList.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
            assertEquals(expectedListSize, taskViews.size());

            final TaskItem view = taskViews.get(0);

            assertEquals(labelId, view.getLabelId());
            assertEquals(taskId, view.getId());

            return view;
        }
    }

    private void updateDueDate(Timestamp newDueDate, boolean isCorrectId, TaskId idOfCreatedTask) {
        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : createWrongTaskId();
        final Timestamp previousDueDate = Timestamp.getDefaultInstance();
        final UpdateTaskDueDate updateTaskDueDate =
                updateTaskDueDateInstance(idOfUpdatedTask, previousDueDate, newDueDate);
        client.update(updateTaskDueDate);
    }
}
