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
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.q.projection.LabelledTasksView;
import org.spine3.examples.todolist.q.projection.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.removeLabelFromTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution RemoveLabelFromTask command")
public class RemoveLabelFromTaskTest extends CommandLineTodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }
    
    @Nested
    @DisplayName("LabelledTaskView should")
    class RemoveLabelFromTaskFromLabelledTasksView {

        @Test
        @DisplayName("be empty")
        public void containEmptyView() {
            final CreateBasicTask createTask = createTask();
            final CreateBasicLabel createLabel = createLabel();

            final TaskId taskId = createTask.getId();
            final LabelId labelId = createLabel.getLabelId();

            final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.assignLabel(assignLabelToTask);

            final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskId, labelId);
            client.removeLabel(removeLabelFromTask);

            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            final int expectedListSize = 1;
            assertEquals(expectedListSize, tasksViewList.size());

            final List<TaskView> taskViews = tasksViewList.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
            assertTrue(taskViews.isEmpty());
        }

        @Test
        @DisplayName("contain task view")
        public void containView() {
            final CreateBasicTask createTask = createTask();
            final CreateBasicLabel createLabel = createLabel();

            final TaskId taskId = createTask.getId();
            final LabelId labelId = createLabel.getLabelId();

            final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.assignLabel(assignLabelToTask);

            final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskId, createWrongTaskLabelId());
            client.removeLabel(removeLabelFromTask);

            final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
            int expectedListSize = 2;
            assertEquals(expectedListSize, tasksViewList.size());

            final LabelledTasksView labelledTasksView = getLabelledTasksView(tasksViewList);
            final List<TaskView> taskViews = labelledTasksView.getLabelledTasks()
                                                              .getItemsList();

            expectedListSize = 1;
            assertEquals(expectedListSize, taskViews.size());
            final TaskView view = taskViews.get(0);

            assertEquals(taskId, view.getId());
            assertFalse(taskViews.isEmpty());
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class RemoveLabelFromTaskFromDraftTasksView {

        @Test
        @DisplayName("contain task view without label")
        public void containViewWithoutLabels() {
            final CreateBasicLabel createBasicLabel = createLabel();
            final LabelId labelId = createBasicLabel.getLabelId();

            final TaskView view = obtainTaskViewWhenHandledRemoveLabeledFromTask(labelId, true);
            assertNotEquals(labelId, view.getLabelId());
        }

        @Test
        @DisplayName("contain task view with labels when command has wrong task ID")
        public void containLabelledViewWhenTaskIdIsWrong() {
            final CreateBasicLabel createBasicLabel = createLabel();
            final LabelId labelId = createBasicLabel.getLabelId();

            final TaskView view = obtainTaskViewWhenHandledRemoveLabeledFromTask(labelId, false);
            assertEquals(labelId, view.getLabelId());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class RemoveLabelFromTaskFromMyListView {

        @Test
        @DisplayName("contain task view without label")
        public void containViewWithLabels() {
            final CreateBasicLabel createLabel = createLabel();
            final LabelId labelId = createLabel.getLabelId();

            final TaskView view = obtainTaskViewWhenHandledRemoveLabelFromTask(labelId, true);
            assertNotEquals(labelId, view.getLabelId());
        }

        @Test
        @DisplayName("contain task view with labels when command has wrong task ID")
        public void containViewsWithoutLabels() {
            final CreateBasicLabel createLabel = createLabel();
            final LabelId labelId = createLabel.getLabelId();

            final TaskView view = obtainTaskViewWhenHandledRemoveLabelFromTask(labelId, false);
            assertEquals(labelId, view.getLabelId());
        }
    }

    private TaskView obtainTaskViewWhenHandledRemoveLabelFromTask(LabelId labelId, boolean isCorrectId) {
        final CreateBasicTask createTask = createTask();
        final TaskId taskId = createTask.getId();

        assignAndRemoveLabel(labelId, isCorrectId, taskId);

        final List<TaskView> taskViews = client.getMyListView()
                                                    .getMyList()
                                                    .getItemsList();
        final TaskView view = checkAndObtainView(taskId, taskViews);
        return view;
    }

    private TaskView obtainTaskViewWhenHandledRemoveLabeledFromTask(LabelId labelId, boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);
        final TaskId taskId = createDraft.getId();

        assignAndRemoveLabel(labelId, isCorrectId, taskId);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                                    .getDraftTasks()
                                                    .getItemsList();
        final TaskView view = checkAndObtainView(taskId, taskViews);
        return view;
    }

    private void assignAndRemoveLabel(LabelId labelId, boolean isCorrectId, TaskId taskId) {
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final TaskId idOfUpdatedTask = isCorrectId ? taskId : createWrongTaskId();
        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(idOfUpdatedTask, labelId);
        client.removeLabel(removeLabelFromTask);
    }

    private static TaskView checkAndObtainView(TaskId taskId, List<TaskView> taskViews) {
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());
        return view;
    }
}
