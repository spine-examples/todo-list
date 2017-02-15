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
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.q.projection.LabelledTasksView;
import org.spine3.examples.todolist.q.projection.TaskView;
import org.spine3.examples.todolist.testdata.TestTaskLabelsCommandFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.spine3.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;

/**
 * @author Illia Shepilov
 */
public class UpdateTaskPriorityTest extends CommandLineTodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class UpdateTaskPriorityInLabelledTasksView {

        @Test
        @DisplayName("contain the task view with updated task priority")
        public void containUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainViewWhenHandledCommandUpdateTaskPriority(newPriority, true);
            assertEquals(newPriority, view.getPriority());
        }

        @Test
        @DisplayName("contain the task view with not updated task priority when command has wrong task ID")
        public void containNotUpdated() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainViewWhenHandledCommandUpdateTaskPriority(newPriority, false);
            assertNotEquals(newPriority, view.getPriority());
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class UpdateTaskPriorityInDraftTasksView {

        @Test
        @DisplayName("contain the task view with updated task priority")
        public void containUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainViewWhenHandledUpdateTaskPriorityCommand(newPriority, true);

            assertEquals(newPriority, view.getPriority());
        }

        @Test
        @DisplayName("contain task view with not updated task priority when command has wrong task ID")
        public void containNotUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainViewWhenHandledUpdateTaskPriorityCommand(newPriority, false);

            assertNotEquals(newPriority, view.getPriority());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class UpdateTaskPriorityInMyListView {

        @Test
        @DisplayName("contain the task view with updated task priority")
        public void containUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainTaskViewWhenHandledUpdateTaskPriority(newPriority, true);

            assertEquals(newPriority, view.getPriority());
        }

        @Test
        @DisplayName("contain task view with not updated task priority when command has wrong task ID")
        public void containNotUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainTaskViewWhenHandledUpdateTaskPriority(newPriority, false);

            assertNotEquals(newPriority, view.getPriority());
        }
    }

    private TaskView obtainTaskViewWhenHandledUpdateTaskPriority(TaskPriority newPriority, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        updatePriority(newPriority, isCorrectId, idOfCreatedTask);

        final List<TaskView> taskViews = client.getMyListView()
                                                    .getMyList()
                                                    .getItemsList();
        assertEquals(1, taskViews.size());
        final TaskView view = taskViews.get(0);

        assertEquals(idOfCreatedTask, view.getId());
        return view;
    }

    private TaskView obtainViewWhenHandledCommandUpdateTaskPriority(TaskPriority priority, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final int expectedListSize = 1;
        final AssignLabelToTask assignLabelToTask = TestTaskLabelsCommandFactory.assignLabelToTaskInstance(createdTaskId, createLabel.getLabelId());
        client.assignLabel(assignLabelToTask);

        updatePriority(priority, isCorrectId, createdTaskId);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());
        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();

        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createLabel.getLabelId(), view.getLabelId());
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledUpdateTaskPriorityCommand(TaskPriority newPriority,
                                                                    boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);
        final TaskId createdTaskId = createDraft.getId();

        updatePriority(newPriority, isCorrectId, createdTaskId);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                                    .getDraftTasks()
                                                    .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    private void updatePriority(TaskPriority newPriority, boolean isCorrectId, TaskId createdTaskId) {
        final TaskId updatedTaskId = isCorrectId ? createdTaskId : createWrongTaskId();
        final UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(updatedTaskId, TaskPriority.TP_UNDEFINED, newPriority);
        client.update(updateTaskPriority);
    }
}
