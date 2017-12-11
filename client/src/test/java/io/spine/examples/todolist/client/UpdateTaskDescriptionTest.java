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

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution of UpdateTaskDescription command")
class UpdateTaskDescriptionTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class UpdateTaskDescriptionInLabelledTasksView {

        @Test
        @DisplayName("contain the task view with updated task description")
        void containUpdatedView() {
            final TaskItem view = obtainViewWhenHandledCommandUpdateTaskDescription(
                    UPDATED_TASK_DESCRIPTION, true);
            assertEquals(UPDATED_TASK_DESCRIPTION, view.getDescription()
                                                       .getValue());
        }

        @Test
        @DisplayName("contain the task view with non-updated task description " +
                "when command ID does not match the aggregate")
        void containNonUpdatedView() {
            final TaskItem view = obtainViewWhenHandledCommandUpdateTaskDescription(
                    UPDATED_TASK_DESCRIPTION, false);
            final TaskDescription actualDescription = view.getDescription();
            assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
            assertEquals(DESCRIPTION, actualDescription.getValue());
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class UpdateTaskDescriptionInDraftTasksView {

        @Test
        @DisplayName("contain the task view with non-updated task description " +
                "when command ID does not match the aggregate")
        void containNonUpdatedView() {
            final TaskItem view = obtainViewWhenHandledUpdateTaskDescription(
                    UPDATED_TASK_DESCRIPTION, false);
            assertNotEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
        }

        @Test
        @DisplayName("contain the task view with updated task description")
        void containUpdatedView() {
            final TaskItem view = obtainViewWhenHandledUpdateTaskDescription(
                    UPDATED_TASK_DESCRIPTION, true);
            assertEquals(UPDATED_TASK_DESCRIPTION, view.getDescription()
                                                       .getValue());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class UpdateTaskDescriptionInMyListView {

        @Test
        @DisplayName("contain the task view with updated task description")
        void containUpdatedView() {
            final TaskItem view = obtainTaskItemWhenHandledUpdateTaskDescriptionCommand(
                    UPDATED_TASK_DESCRIPTION, true);
            final TaskDescription actualDescription = view.getDescription();
            assertEquals(UPDATED_TASK_DESCRIPTION, actualDescription.getValue());
        }

        @Test
        @DisplayName("contain the task view with non-updated task description " +
                "when command ID does not match the aggregate")
        void containNonUpdatedView() {
            final TaskItem view = obtainTaskItemWhenHandledUpdateTaskDescriptionCommand(
                    UPDATED_TASK_DESCRIPTION, false);
            final TaskDescription actualDescription = view.getDescription();
            assertEquals(DESCRIPTION, actualDescription.getValue());
            assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
        }
    }

    private TaskItem obtainTaskItemWhenHandledUpdateTaskDescriptionCommand(String newDescription,
                                                                           boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);

        updateDescription(newDescription, isCorrectId, createTask);

        final List<TaskItem> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskItem view = taskViews.get(0);
        assertEquals(createTask.getId(), view.getId());

        return view;
    }

    private TaskItem obtainViewWhenHandledCommandUpdateTaskDescription(String newDescription,
                                                                       boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);
        final LabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        updateDescription(newDescription, isCorrectId, createTask);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(1, tasksViewList.get(0)
                                     .getLabelledTasks()
                                     .getItemsCount());
        final TaskItem view = tasksViewList.get(0)
                                           .getLabelledTasks()
                                           .getItems(0);
        assertEquals(labelId, view.getLabelId());
        assertEquals(taskId, view.getId());

        return view;
    }

    private TaskItem obtainViewWhenHandledUpdateTaskDescription(String newDescription,
                                                                boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);
        final TaskId createdTaskId = createDraft.getId();

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : createWrongTaskId();
        final String previousDescription = client.getDraftTasksView()
                                                 .getDraftTasks()
                                                 .getItemsList()
                                                 .get(0)
                                                 .getDescription()
                                                 .getValue();
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId, previousDescription, newDescription);
        client.postCommand(updateTaskDescription);

        final List<TaskItem> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskItem view = taskViews.get(0);
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    private void updateDescription(String newDescription, boolean isCorrectId,
                                   CreateBasicTask createTask) {
        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId updatedTaskId = isCorrectId ? idOfCreatedTask : createWrongTaskId();
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId, createTask.getDescription()
                                                                       .getValue(),
                                              newDescription);
        client.postCommand(updateTaskDescription);
    }
}
