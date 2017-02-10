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
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;

/**
 * @author Illia Shepilov
 */
public class UpdateTaskPriorityTest extends CommandLineTodoClientTest {

    private static final String CONTAIN_TASK_VIEW_WITH_UPDATED_TASK_PRIORITY =
            "contain task view with updated task priority";
    private static final String CONTAIN_TASK_VIEW_WITH_NOT_UPDATED_TASK_PRIORITY_WHEN_COMMAND_HAS_WRONG_TASK_ID =
            "contain task view with not updated task priority when command has wrong task ID";

    @Nested
    @DisplayName(LABELLED_TASK_VIEW_SHOULD)
    class UpdateTaskPriorityInLabelledTasksView {

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WITH_UPDATED_TASK_PRIORITY)
        public void containUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainViewWhenHandledCommandUpdateTaskPriority(newPriority, true);
            assertEquals(newPriority, view.getPriority());
        }

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WITH_NOT_UPDATED_TASK_PRIORITY_WHEN_COMMAND_HAS_WRONG_TASK_ID)
        public void containNotUpdated() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainViewWhenHandledCommandUpdateTaskPriority(newPriority, false);
            assertNotEquals(newPriority, view.getPriority());
        }
    }

    @Nested
    @DisplayName(DRAFT_TASKS_VIEW_SHOULD)
    class UpdateTaskPriorityInDraftTasksView {

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WITH_UPDATED_TASK_PRIORITY)
        public void containUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainViewWhenHandledUpdateTaskPriorityCommand(newPriority, true);

            assertEquals(newPriority, view.getPriority());
        }

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WITH_NOT_UPDATED_TASK_PRIORITY_WHEN_COMMAND_HAS_WRONG_TASK_ID)
        public void containNotUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainViewWhenHandledUpdateTaskPriorityCommand(newPriority, false);

            assertNotEquals(newPriority, view.getPriority());
        }
    }

    @Nested
    @DisplayName(MY_LIST_VIEW_SHOULD)
    class UpdateTaskPriorityInMyListView {

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WITH_UPDATED_TASK_PRIORITY)
        public void containUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskView view = obtainTaskViewWhenHandledUpdateTaskPriority(newPriority, true);

            assertEquals(newPriority, view.getPriority());
        }

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WITH_NOT_UPDATED_TASK_PRIORITY_WHEN_COMMAND_HAS_WRONG_TASK_ID)
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
        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();

        final UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(idOfUpdatedTask, TaskPriority.TP_UNDEFINED, newPriority);
        client.update(updateTaskPriority);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

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
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(createdTaskId, createLabel.getLabelId());
        client.assignLabel(assignLabelToTask);

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(updatedTaskId, TaskPriority.TP_UNDEFINED, priority);
        client.update(updateTaskPriority);

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

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(updatedTaskId, TaskPriority.TP_UNDEFINED, newPriority);
        client.update(updateTaskPriority);

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
