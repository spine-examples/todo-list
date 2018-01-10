/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.removeLabelFromTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution of RemoveLabelFromTask command")
class RemoveLabelFromTaskTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class RemoveLabelFromTaskFromLabelledTasksView {

        @Test
        @DisplayName("be empty")
        void containEmptyView() {
            final CreateBasicTask createTask = createTask();
            final CreateBasicLabel createLabel = createLabel();

            final TaskId taskId = createTask.getId();
            final LabelId labelId = createLabel.getLabelId();

            final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.postCommand(assignLabelToTask);

            final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskId,
                                                                                        labelId);
            client.postCommand(removeLabelFromTask);

            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            assertEquals(1, tasksViewList.size());

            final List<TaskItem> taskViews = tasksViewList.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
            assertTrue(taskViews.isEmpty());
        }

        @Test
        @DisplayName("contain the task view")
        void containView() {
            final CreateBasicTask createTask = createTask();
            final CreateBasicLabel createLabel = createLabel();

            final TaskId taskId = createTask.getId();
            final LabelId labelId = createLabel.getLabelId();

            final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.postCommand(assignLabelToTask);

            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            assertEquals(1, tasksViewList.size());

            final LabelledTasksView labelledTasksView = getLabelledTasksView(tasksViewList);
            final List<TaskItem> taskViews = labelledTasksView.getLabelledTasks()
                                                              .getItemsList();
            assertEquals(1, taskViews.size());

            final TaskItem view = taskViews.get(0);
            assertEquals(taskId, view.getId());
            assertFalse(taskViews.isEmpty());
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class RemoveLabelFromTaskFromDraftTasksView {

        @Test
        @DisplayName("contain the task view without label")
        void containViewWithoutLabels() {
            final CreateBasicLabel createBasicLabel = createLabel();
            final LabelId labelId = createBasicLabel.getLabelId();

            final TaskItem view = obtainTaskItemWhenHandledRemoveLabeledFromTask(labelId, true);
            assertNotEquals(labelId, view.getLabelId());
        }

        @Test
        @DisplayName("contain the task view with labels when command has wrong task ID")
        void containLabelledViewWhenTaskIdIsWrong() {
            final CreateBasicLabel createBasicLabel = createLabel();
            final LabelId labelId = createBasicLabel.getLabelId();

            final TaskItem view = obtainTaskItemWhenHandledRemoveLabeledFromTask(labelId,
                                                                                 false);
            assertEquals(labelId, view.getLabelId());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class RemoveLabelFromTaskFromMyListView {

        @Test
        @DisplayName("contain the task view without label")
        void containViewWithLabels() {
            final CreateBasicLabel createLabel = createLabel();
            final LabelId labelId = createLabel.getLabelId();

            final TaskItem view = obtainTaskItemWhenHandledRemoveLabelFromTask(labelId,
                                                                               true);
            assertNotEquals(labelId, view.getLabelId());
        }

        @Test
        @DisplayName("contain the task view with labels when command has wrong task ID")
        void containViewsWithoutLabels() {
            final CreateBasicLabel createLabel = createLabel();
            final LabelId labelId = createLabel.getLabelId();

            final TaskItem view = obtainTaskItemWhenHandledRemoveLabelFromTask(labelId,
                                                                               false);
            assertEquals(labelId, view.getLabelId());
        }
    }

    private TaskItem obtainTaskItemWhenHandledRemoveLabelFromTask(LabelId labelId,
            boolean isCorrectId) {
        final CreateBasicTask createTask = createTask();
        final TaskId taskId = createTask.getId();

        assignAndRemoveLabel(labelId, isCorrectId, taskId);

        final List<TaskItem> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        return checkAndObtainView(taskId, taskViews);
    }

    private TaskItem obtainTaskItemWhenHandledRemoveLabeledFromTask(LabelId labelId,
            boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);
        final TaskId taskId = createDraft.getId();

        assignAndRemoveLabel(labelId, isCorrectId, taskId);

        final List<TaskItem> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        return checkAndObtainView(taskId, taskViews);
    }

    private void assignAndRemoveLabel(LabelId labelId, boolean isCorrectId, TaskId taskId) {
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        final TaskId idOfUpdatedTask = isCorrectId ? taskId : createWrongTaskId();
        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(idOfUpdatedTask,
                                                                                    labelId);
        client.postCommand(removeLabelFromTask);
    }

    private static TaskItem checkAndObtainView(TaskId taskId, List<TaskItem> taskViews) {
        assertEquals(1, taskViews.size());

        final TaskItem view = taskViews.get(0);
        assertEquals(taskId, view.getId());
        return view;
    }
}
