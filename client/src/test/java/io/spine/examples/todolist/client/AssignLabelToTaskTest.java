/*
 * Copyright 2018, TeamDev. All rights reserved.
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
import io.spine.examples.todolist.TaskLabels;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution of AssignLabelToTask command")
class AssignLabelToTaskTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class AssignLabelToTaskToLabelledTasksView {

        @Test
        @DisplayName("contain the empty TaskView list")
        void obtainEmptyView() {
            createTask();
            createLabel();

            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            assertTrue(tasksViewList.isEmpty());
        }

        @Test
        @DisplayName("contain TaskItem with label")
        void obtainViewsWithLabel() {
            final CreateBasicTask createTask = createTask();
            final CreateBasicLabel createLabel = createLabel();

            final TaskId taskId = createTask.getId();
            final LabelId labelId = createLabel.getLabelId();

            final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.postCommand(assignLabelToTask);

            final int expectedListSize = 1;
            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            assertEquals(expectedListSize, tasksViewList.size());

            final List<TaskItem> taskViews = tasksViewList.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
            assertEquals(expectedListSize, taskViews.size());

            final TaskItem view = taskViews.get(0);

            assertEquals(taskId, view.getId());
            assertEquals(labelId, view.getLabelId());
        }

        @Test
        @DisplayName("contain two task view with labels")
        void obtainViewsWithLabels() {
            final CreateBasicTask createTask = createTask();
            final CreateBasicLabel createLabel = createLabel();
            final CreateBasicLabel createSecondLabel = createLabel();

            final LabelId firstLabelId = createLabel.getLabelId();
            final LabelId secondLabelId = createSecondLabel.getLabelId();
            final TaskId taskId = createTask.getId();

            AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, firstLabelId);
            client.postCommand(assignLabelToTask);

            assignLabelToTask = assignLabelToTaskInstance(taskId, secondLabelId);
            client.postCommand(assignLabelToTask);

            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            assertEquals(2, tasksViewList.size());

            final List<TaskItem> taskViews = tasksViewList.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
            final Collection labelIds = newHashSet(firstLabelId, secondLabelId);
            for (TaskItem view : taskViews) {
                assertEquals(taskId, view.getId());
                assertTrue(labelIds.contains(view.getLabelId()));
            }
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class AssignLabelToTaskToDraftTaskItem {

        @Test
        @DisplayName("contain the task view without label when command has wrong task ID")
        void obtainDraftViewWithoutLabels() {
            final CreateBasicLabel createBasicLabel = createLabel();
            final LabelId labelId = createBasicLabel.getLabelId();

            final TaskItem view = obtainViewWhenHandledAssignLabelToTask(labelId, false);
            assertNotEquals(labelId, view.getLabelId());
        }

        @Test
        @DisplayName("contain task view with label")
        void obtainDraftViewWithLabels() {
            final CreateBasicLabel createBasicLabel = createLabel();
            final LabelId labelId = createBasicLabel.getLabelId();
            final TaskItem view = obtainViewWhenHandledAssignLabelToTask(labelId, true);

            assertEquals(labelId, view.getLabelId());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class AssignLabelToTaskToMyListView {

        @Test
        @DisplayName("contain task view with label")
        void obtainMyListViewWithLabels() {
            final CreateBasicLabel createLabel = createLabel();
            final LabelId labelId = createLabel.getLabelId();

            final TaskItem view = obtainTaskItemWhenHandledAssignLabelToTask(labelId, true);

            assertEquals(labelId, view.getLabelId());
        }

        @Test
        @DisplayName("contain task view without label when command has wrong task ID")
        void obtainMyListViewWithoutLabels() {
            final CreateBasicLabel createLabel = createLabel();

            final LabelId labelId = createLabel.getLabelId();
            final TaskItem view = obtainTaskItemWhenHandledAssignLabelToTask(labelId, false);

            assertNotEquals(labelId, view.getLabelId());
        }
    }

    @Nested
    @DisplayName("TaskLabels part should")
    class AssignLabelToTaskToTasklabels {

        @Test
        @DisplayName("contain label ID")
        void testContains() {
            final CreateBasicLabel createLabel = createLabel();
            final LabelId labelId = createLabel.getLabelId();

            final TaskLabels labels = obtainTaskLabelsWhenHandledAssignLabelToTask(labelId);
            final List<LabelId> labelIds = labels.getLabelIdsList()
                                                 .getIdsList();
            assertEquals(1, labelIds.size());
            assertEquals(labelId, labelIds.get(0));
        }
    }

    private TaskItem obtainTaskItemWhenHandledAssignLabelToTask(LabelId labelId,
                                                                boolean isCorrectId) {
        final CreateBasicTask createTask = createTask();

        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : createWrongTaskId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(idOfUpdatedTask,
                                                                              labelId);
        client.postCommand(assignLabelToTask);

        final List<TaskItem> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskItem view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private TaskItem obtainViewWhenHandledAssignLabelToTask(LabelId labelId, boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);

        final TaskId createTaskId = createDraft.getId();
        final TaskId taskIdToAssign = isCorrectId ? createTaskId : createWrongTaskId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskIdToAssign,
                                                                              labelId);
        client.postCommand(assignLabelToTask);

        final List<TaskItem> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskItem view = taskViews.get(0);
        assertEquals(createTaskId, view.getId());

        return view;
    }

    private TaskLabels obtainTaskLabelsWhenHandledAssignLabelToTask(LabelId labelId) {
        final CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);
        final TaskId taskId = createDraft.getId();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);
        final TaskLabels labels = client.getLabels(taskId);
        return labels;
    }
}
