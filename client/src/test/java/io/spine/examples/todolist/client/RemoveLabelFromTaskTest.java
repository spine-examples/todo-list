/*
 * Copyright 2019, TeamDev. All rights reserved.
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
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.removeLabelFromTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("After execution of RemoveLabelFromTask command the list of task views should")
class RemoveLabelFromTaskTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("be empty")
    void containEmptyView() {
        CreateBasicTask createTask = createTask();
        CreateBasicLabel createLabel = createLabel();

        TaskId taskId = createTask.getId();
        LabelId labelId = createLabel.getLabelId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskId, labelId);
        client.postCommand(removeLabelFromTask);

        List<TaskView> tasksViewList = client.taskViews();
        assertEquals(1, tasksViewList.size());

        List<LabelId> taskViews = tasksViewList.get(0)
                                               .getLabelIdsList()
                                               .getIdsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    @DisplayName("contain the task view")
    void containView() {
        CreateBasicTask createTask = createTask();
        CreateBasicLabel createLabel = createLabel();

        TaskId taskId = createTask.getId();
        LabelId labelId = createLabel.getLabelId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        List<TaskView> taskViews = client.taskViews();
        assertEquals(1, taskViews.size());

        List<LabelId> labelIds = taskViews.get(0)
                                          .getLabelIdsList()
                                          .getIdsList();
        assertEquals(1, labelIds.size());
        assertEquals(labelId, labelIds.get(0));
    }

    @Test
    @DisplayName("contain an unlabelled draft")
    void containViewWithoutLabels() {
        CreateBasicLabel createBasicLabel = createLabel();
        LabelId labelId = createBasicLabel.getLabelId();

        TaskView view = obtainViewWithRemovedLabel(labelId, true);
        assertTrue(view.getLabelIdsList()
                       .getIdsList()
                       .isEmpty());
    }

    @Test
    @DisplayName("contain a labelled draft if the command had an incorrect ID")
    void containLabelledViewWhenTaskIdIsWrong() {
        CreateBasicLabel createBasicLabel = createLabel();
        LabelId labelId = createBasicLabel.getLabelId();

        TaskView view = obtainViewWithRemovedLabel(labelId, false);
        assertEquals(labelId, view.getLabelIdsList()
                                  .getIds(0));
    }

    @Test
    @DisplayName("contain an unlabelled task view")
    void containViewWithLabels() {
        CreateBasicLabel createLabel = createLabel();
        LabelId labelId = createLabel.getLabelId();

        TaskView view = obtainViewWithRemovedLabel(labelId, true);
        List<LabelId> labels = view.getLabelIdsList()
                                   .getIdsList();
        assertTrue(labels.isEmpty());
    }

    @Test
    @DisplayName("contain the task view with labels when command has wrong task ID")
    void containViewsWithoutLabels() {
        CreateBasicLabel createLabel = createLabel();
        LabelId labelId = createLabel.getLabelId();

        TaskView view = obtainViewWithRemovedLabel(labelId, false);
        assertEquals(labelId, view.getLabelIdsList()
                                  .getIds(0));
    }

    private void assignAndRemoveLabel(LabelId labelId, boolean isCorrectId, TaskId taskId) {
        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        TaskId idOfUpdatedTask = isCorrectId ? taskId : createWrongTaskId();
        RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(idOfUpdatedTask,
                                                                              labelId);
        client.postCommand(removeLabelFromTask);
    }

    private static TaskView checkAndObtainView(TaskId taskId, List<TaskView> taskViews) {
        assertEquals(1, taskViews.size());

        TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());
        return view;
    }

    private TaskView obtainViewWithRemovedLabel(LabelId labelId, boolean isCorrectId) {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);
        UpdateTaskPriority updateTaskPriority = setInitialTaskPriority(createDraft.getId());
        client.postCommand(updateTaskPriority);
        TaskId taskId = createDraft.getId();

        assignAndRemoveLabel(labelId, isCorrectId, taskId);

        List<TaskView> taskViews = client
                .taskViews()
                .stream()
                .filter(view -> view.getStatus() == TaskStatus.DRAFT)
                .collect(Collectors.toList());
        return checkAndObtainView(taskId, taskViews);
    }
}
