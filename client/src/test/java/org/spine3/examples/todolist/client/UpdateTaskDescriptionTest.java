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

import org.junit.jupiter.api.BeforeEach;
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
import org.spine3.examples.todolist.q.projection.LabelledTasksView;
import org.spine3.examples.todolist.q.projection.TaskView;
import org.spine3.examples.todolist.testdata.TestTaskLabelsCommandFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution UpdateTaskDescription command")
public class UpdateTaskDescriptionTest extends CommandLineTodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class UpdateTaskDescriptionInLabelledTasksView {

        @Test
        @DisplayName("contain the task view with updated task description")
        public void containUpdatedView() {
            final TaskView view = obtainViewWhenHandledCommandUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, true);
            assertEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
        }

        @Test
        @DisplayName("contain the task view with not updated task description when command has wrong ID")
        public void containNotUpdatedView() {
            final TaskView view = obtainViewWhenHandledCommandUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, false);
            final String actualDescription = view.getDescription();
            assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
            assertEquals(DESCRIPTION, actualDescription);
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class UpdateTaskDescriptionInDraftTasksView {

        @Test
        @DisplayName("contain the task view with not updated task description when command has wrong ID")
        public void containNotUpdatedView() {
            final TaskView view = obtainViewWhenHandledUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, false);
            assertNotEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
        }

        @Test
        @DisplayName("contain the task view with updated task description")
        public void containUpdatedView() {
            final TaskView view = obtainViewWhenHandledUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, true);
            assertEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class UpdateTaskDescriptionInMyListView {

        @Test
        @DisplayName("contain the task view with updated task description")
        public void containUpdatedView() {
            final TaskView view = obtainTaskViewWhenHandledUpdateTaskDescriptionCommand(UPDATED_TASK_DESCRIPTION, true);
            final String actualDescription = view.getDescription();
            assertEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
        }

        @Test
        @DisplayName("contain the task view with not updated task description when command has wrong ID")
        public void containNotUpdatedView() {
            final TaskView view = obtainTaskViewWhenHandledUpdateTaskDescriptionCommand(UPDATED_TASK_DESCRIPTION, false);
            final String actualDescription = view.getDescription();
            assertEquals(DESCRIPTION, actualDescription);
            assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
        }
    }

    private TaskView obtainTaskViewWhenHandledUpdateTaskDescriptionCommand(String newDescription, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        updateDescription(newDescription, isCorrectId, createTask);

        final List<TaskView> taskViews = client.getMyListView()
                                                    .getMyList()
                                                    .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createTask.getId(), view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledCommandUpdateTaskDescription(String newDescription, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);
        final LabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = TestTaskLabelsCommandFactory.assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);


       updateDescription(newDescription, isCorrectId, createTask);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(1, tasksViewList.get(0)
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

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : createWrongTaskId();
        final String previousDescription = "";
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId, previousDescription, newDescription);
        client.update(updateTaskDescription);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                                    .getDraftTasks()
                                                    .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    private void updateDescription(String newDescription, boolean isCorrectId, CreateBasicTask createTask) {
        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId updatedTaskId = isCorrectId ? idOfCreatedTask : createWrongTaskId();
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId, createTask.getDescription(), newDescription);
        client.update(updateTaskDescription);
    }
}
