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
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.q.projection.LabelledTasksView;
import org.spine3.examples.todolist.q.projection.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution DeleteTask command")
public class DeleteTaskTest extends CommandLineTodoClientTest {

    private static final String CONTAIN_TASK_VIEW_WHEN_COMMAND_HAS_WRONG_ID =
            "contain task view when command has wrong ID";

    private TodoClient client;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName(LABELLED_TASK_VIEW_SHOULD)
    class DeleteTaskFromLabelledTasksView {

        @Test
        @DisplayName(BE_EMPTY)
        public void obtainEmptyView() {
            final CreateBasicTask createTask = createTask();

            final CreateBasicLabel createLabel = createBasicLabel();
            client.create(createLabel);

            final LabelId labelId = createLabel.getLabelId();
            final TaskId taskId = createTask.getId();

            final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.assignLabel(assignLabelToTask);

            final DeleteTask deleteTask = deleteTaskInstance(taskId);
            client.delete(deleteTask);

            final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
            final int expectedListSize = 1;
            assertEquals(expectedListSize, labelledTasksView.size());

            final List<TaskView> taskViews = labelledTasksView.get(0)
                                                              .getLabelledTasks()
                                                              .getItemsList();
            assertTrue(taskViews.isEmpty());
        }

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WHEN_COMMAND_HAS_WRONG_ID)
        public void obtainView() {
            final CreateBasicTask createTask = createTask();

            final CreateBasicLabel createLabel = createBasicLabel();
            client.create(createLabel);

            final LabelId labelId = createLabel.getLabelId();
            final TaskId taskId = createTask.getId();

            final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.assignLabel(assignLabelToTask);

            final DeleteTask deleteTask = deleteTaskInstance(createWrongTaskId());
            client.delete(deleteTask);

            final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
            final int expectedListSize = 1;
            assertEquals(expectedListSize, labelledTasksView.size());

            final List<TaskView> taskViews = labelledTasksView.get(0)
                                                              .getLabelledTasks()
                                                              .getItemsList();
            assertEquals(expectedListSize, taskViews.size());

            final TaskView view = taskViews.get(0);
            assertEquals(taskId, view.getId());
        }
    }

    @Nested
    @DisplayName(DRAFT_TASKS_VIEW_SHOULD)
    class DeleteTaskFromDraftTasksView {

        @Test
        @DisplayName(BE_EMPTY)
        public void obtainEmptyView() {
            final CreateDraft createDraft = createDraft();
            client.create(createDraft);

            final DeleteTask deleteTask = deleteTaskInstance(createDraft.getId());
            client.delete(deleteTask);

            final List<TaskView> taskViews = client.getDraftTasksView()
                                                   .getDraftTasks()
                                                   .getItemsList();
            assertTrue(taskViews.isEmpty());
        }

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WHEN_COMMAND_HAS_WRONG_ID)
        public void obtainView() {
            final CreateDraft createDraft = createDraft();
            client.create(createDraft);

            final TaskId taskId = createDraft.getId();
            final DeleteTask deleteTask = deleteTaskInstance(createWrongTaskId());
            client.delete(deleteTask);

            final List<TaskView> taskViews = client.getDraftTasksView()
                                                   .getDraftTasks()
                                                   .getItemsList();
            final int expectedListSize = 1;
            assertEquals(expectedListSize, taskViews.size());

            final TaskView view = taskViews.get(0);
            assertEquals(taskId, view.getId());
        }
    }

    @Nested
    @DisplayName(MY_LIST_VIEW_SHOULD)
    class DeleteTaskFromMyListView {

        @Test
        @DisplayName(BE_EMPTY)
        public void obtainEmptyView() {
            final CreateBasicTask createTask = createTask();

            final TaskId idOfCreatedTask = createTask.getId();
            final List<TaskView> taskViews = obtainTaskViewListWhenHandledDeleteTask(idOfCreatedTask, true);

            assertTrue(taskViews.isEmpty());
        }

        @Test
        @DisplayName(CONTAIN_TASK_VIEW_WHEN_COMMAND_HAS_WRONG_ID)
        public void obtainView() {
            final CreateBasicTask createTask = createTask();

            final TaskId idOfCreatedTask = createTask.getId();
            final List<TaskView> taskViews = obtainTaskViewListWhenHandledDeleteTask(idOfCreatedTask, false);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, taskViews.size());

            final TaskView view = taskViews.get(0);

            final TaskId taskId = createTask.getId();
            assertEquals(taskId, view.getId());
        }
    }

    private List<TaskView> obtainTaskViewListWhenHandledDeleteTask(TaskId idOfCreatedTask, boolean isCorrectId) {
        final TaskId idOfDeletedTask = isCorrectId ? idOfCreatedTask : createWrongTaskId();

        final DeleteTask deleteTask = deleteTaskInstance(idOfDeletedTask);
        client.delete(deleteTask);

        final List<TaskView> result = client.getMyListView()
                                            .getMyList()
                                            .getItemsList();
        return result;
    }
}
