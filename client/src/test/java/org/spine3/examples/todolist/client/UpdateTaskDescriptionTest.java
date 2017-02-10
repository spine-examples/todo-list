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
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution UpdateTaskDescription command")
public class UpdateTaskDescriptionTest extends CommandLineTodoClientTest {

    @Nested
    @DisplayName("LabelledTasksView should contain")
    class UpdateTaskDescriptionInLabelledTasksView {

        @Test
        public void obtain_updated_task_description_from_labelled_tasks_view_when_handled_command_update_task_description() {
            final TaskView view = obtainViewWhenHandledCommandUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, true);
            assertEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
        }

        @Test
        public void obtain_labelled_tasks_view_when_handled_command_update_task_description_with_wrong_task_id() {
            final TaskView view = obtainViewWhenHandledCommandUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, false);
            final String actualDescription = view.getDescription();
            assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
            assertEquals(DESCRIPTION, actualDescription);
        }
    }

    @Nested
    @DisplayName("DraftTasksView should contain")
    class UpdateTaskDescriptionInDraftTasksView {

        @Test
        @DisplayName("task view with not updated task description when command has wrong task ID")
        public void obtainNotUpdatedView() {
            final TaskView view = obtainViewWhenHandledUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, false);
            assertNotEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
        }

        @Test
        @DisplayName("task view with updated task description")
        public void obtainUpdatedView() {
            final TaskView view = obtainViewWhenHandledUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, true);
            assertEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
        }
    }

    @Nested
    @DisplayName("MyListView should contain")
    class UpdateTaskDescriptionInMyListView {

        @Test
        @DisplayName("task view with updated task description")
        public void obtainUpdatedView() {
            final TaskView view = obtainTaskViewWhenHandledUpdateTaskDescriptionCommand(UPDATED_TASK_DESCRIPTION, true);
            final String actualDescription = view.getDescription();
            assertEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
        }

        @Test
        @DisplayName("task view with not updated task description when command has wrong task ID")
        public void obtainNotUpdatedView() {
            final TaskView view = obtainTaskViewWhenHandledUpdateTaskDescriptionCommand(UPDATED_TASK_DESCRIPTION, false);
            final String actualDescription = view.getDescription();
            assertEquals(DESCRIPTION, actualDescription);
            assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
        }
    }

    private TaskView obtainTaskViewWhenHandledUpdateTaskDescriptionCommand(String newDescription, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);
        final TaskId idOfCreatedTask = createTask.getId();

        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(idOfUpdatedTask, createTask.getDescription(), newDescription);
        client.update(updateTaskDescription);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createTask.getId(), view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledCommandUpdateTaskDescription(String newDescription, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final LabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId, createTask.getDescription(), newDescription);
        client.update(updateTaskDescription);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, tasksViewList.get(0)
                                                    .getLabelledTasks()
                                                    .getItemsCount());
        final TaskView view = tasksViewList.get(0)
                                           .getLabelledTasks()
                                           .getItems(0);
        assertEquals(labelId, view.getLabelId());
        assertEquals(taskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledUpdateTaskDescription(String newDescription,
                                                                boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);
        final TaskId createdTaskId = createDraft.getId();

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final String previousDescription = "";
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId, previousDescription, newDescription);
        client.update(updateTaskDescription);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createdTaskId, view.getId());

        return view;
    }
}
