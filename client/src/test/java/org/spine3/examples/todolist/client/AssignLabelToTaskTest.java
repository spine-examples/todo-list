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
import org.spine3.examples.todolist.q.projection.LabelledTasksView;
import org.spine3.examples.todolist.q.projection.TaskView;
import org.spine3.examples.todolist.testdata.TestTaskLabelsCommandFactory;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution of AssignLabelToTask command")
public class AssignLabelToTaskTest extends CommandLineTodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class AssignLabelToTaskToLabelledTasksView {

        @Test
        @DisplayName("contain the empty TaskView list")
        public void obtainEmptyView() {
            createTask();
            createLabel();

            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            assertTrue(tasksViewList.isEmpty());
        }

        @Test
        @DisplayName("contain TaskView with label")
        public void obtainViewsWithLabel() {
            final CreateBasicTask createTask = createTask();
            final CreateBasicLabel createLabel = createLabel();

            final TaskId taskId = createTask.getId();
            final LabelId labelId = createLabel.getLabelId();

            final AssignLabelToTask assignLabelToTask = TestTaskLabelsCommandFactory.assignLabelToTaskInstance(taskId, labelId);
            client.assignLabel(assignLabelToTask);

            final int expectedListSize = 1;
            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            assertEquals(expectedListSize, tasksViewList.size());

            final List<TaskView> taskViews = tasksViewList.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
            assertEquals(expectedListSize, taskViews.size());

            final TaskView view = taskViews.get(0);

            assertEquals(taskId, view.getId());
            assertEquals(labelId, view.getLabelId());
        }

        @Test
        @DisplayName("contain two task view with labels")
        public void obtainViewsWithLabels() {
            final CreateBasicTask createTask = createTask();
            final CreateBasicLabel createLabel = createLabel();
            final CreateBasicLabel createSecondLabel = createLabel();

            final LabelId firstLabelId = createLabel.getLabelId();
            final LabelId secondLabelId = createSecondLabel.getLabelId();
            final TaskId taskId = createTask.getId();

            AssignLabelToTask assignLabelToTask = TestTaskLabelsCommandFactory.assignLabelToTaskInstance(taskId, firstLabelId);
            client.assignLabel(assignLabelToTask);

            assignLabelToTask = TestTaskLabelsCommandFactory.assignLabelToTaskInstance(taskId, secondLabelId);
            client.assignLabel(assignLabelToTask);

            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            assertEquals(2, tasksViewList.size());

            final List<TaskView> taskViews = tasksViewList.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
            final Collection labelIds = newHashSet(firstLabelId, secondLabelId);
            for (TaskView view : taskViews) {
                assertEquals(taskId, view.getId());
                assertTrue(labelIds.contains(view.getLabelId()));
            }
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class AssignLabelToTaskToDraftTaskView {

        @Test
        @DisplayName("contain the task view without label when command has wrong task ID")
        public void obtainDraftViewWithoutLabels() {
            final CreateBasicLabel createBasicLabel = createLabel();
            final LabelId labelId = createBasicLabel.getLabelId();

            final TaskView view = obtainViewWhenHandledAssignLabelToTask(labelId, false);
            assertNotEquals(labelId, view.getLabelId());
        }

        @Test
        @DisplayName("contain task view with label")
        public void obtainDraftViewWithLabels() {
            final CreateBasicLabel createBasicLabel = createLabel();
            final LabelId labelId = createBasicLabel.getLabelId();
            final TaskView view = obtainViewWhenHandledAssignLabelToTask(labelId, true);

            assertEquals(labelId, view.getLabelId());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class AssignLabelToTaskToMyListView {

        @Test
        @DisplayName("task view with label")
        public void obtainMyListViewWithLabels() {
            final CreateBasicLabel createLabel = createLabel();
            final LabelId labelId = createLabel.getLabelId();

            final TaskView view = obtainTaskViewWhenHandledAssignLabelToTask(labelId, true);

            assertEquals(labelId, view.getLabelId());
        }

        @Test
        @DisplayName("contain task view without label when command has wrong task ID")
        public void obtainMyListViewWithoutLabels() {
            final CreateBasicLabel createLabel = createLabel();

            final LabelId labelId = createLabel.getLabelId();
            final TaskView view = obtainTaskViewWhenHandledAssignLabelToTask(labelId, false);

            assertNotEquals(labelId, view.getLabelId());
        }
    }

    private TaskView obtainTaskViewWhenHandledAssignLabelToTask(LabelId labelId, boolean isCorrectId) {
        final CreateBasicTask createTask = createTask();

        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : createWrongTaskId();

        final AssignLabelToTask assignLabelToTask = TestTaskLabelsCommandFactory.assignLabelToTaskInstance(idOfUpdatedTask, labelId);
        client.assignLabel(assignLabelToTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledAssignLabelToTask(LabelId labelId, boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final TaskId createTaskId = createDraft.getId();
        final TaskId taskIdToAssign = isCorrectId ? createTaskId : createWrongTaskId();

        final AssignLabelToTask assignLabelToTask = TestTaskLabelsCommandFactory.assignLabelToTaskInstance(taskIdToAssign, labelId);
        client.assignLabel(assignLabelToTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createTaskId, view.getId());

        return view;
    }
}
