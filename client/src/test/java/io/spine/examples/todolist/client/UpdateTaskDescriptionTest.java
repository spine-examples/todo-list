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
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("After execution of UpdateTaskDescription command task views should")
class UpdateTaskDescriptionTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("contain an updated labelled task")
    void containUpdatedView() {
        TaskView view = obtainLabelledViewWhenHandledCommandUpdateTaskDescription(
                UPDATED_TASK_DESCRIPTION, true);
        assertEquals(UPDATED_TASK_DESCRIPTION, view.getDescription()
                                                   .getValue());
    }

    @Test
    @DisplayName("not contain an updated labelled task if the command had an incorrect ID")
    void containNonUpdatedView() {
        TaskView view = obtainLabelledViewWhenHandledCommandUpdateTaskDescription(
                UPDATED_TASK_DESCRIPTION, false);
        TaskDescription actualDescription = view.getDescription();
        assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
        assertEquals(DESCRIPTION, actualDescription.getValue());
    }

    //TODO:2019-04-25:serhii.lekariev:rename me and those like me
    private TaskView obtainLabelledViewWhenHandledCommandUpdateTaskDescription(
            String newDescription,
            boolean isCorrectId) {
        CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);

        UpdateTaskPriority updateTaskPriority = setInitialTaskPriority(createTask.getId());
        client.postCommand(updateTaskPriority);

        CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);
        LabelId labelId = createLabel.getLabelId();
        TaskId taskId = createTask.getId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        updateDescription(newDescription, isCorrectId, createTask);

        List<TaskView> labelledTasks = client
                .getTaskViews()
                .stream()
                .filter(view -> !view.getLabelIdsList()
                                     .getIdsList()
                                     .isEmpty())
                .collect(toList());
        assertEquals(1, labelledTasks.size());
        TaskView view = labelledTasks.get(0);
        assertEquals(labelId, view.getLabelIdsList()
                                  .getIds(0));
        assertEquals(taskId, view.getId());

        return view;
    }

    @Test
    @DisplayName("contain a draft with and unchanged ID if the command had an incorrect ID")
    void containNonUpdatedDraft() {
        TaskView view = obtainDraftWhenHandledUpdateTaskDescription(
                UPDATED_TASK_DESCRIPTION, false);
        assertNotEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
    }

    @Test
    @DisplayName("contain a draft with updated task description")
    void containUpdatedDraft() {
        TaskView view = obtainDraftWhenHandledUpdateTaskDescription(
                UPDATED_TASK_DESCRIPTION, true);
        assertEquals(UPDATED_TASK_DESCRIPTION, view.getDescription()
                                                   .getValue());
    }

    private TaskView obtainDraftWhenHandledUpdateTaskDescription(String newDescription,
                                                                 boolean isCorrectId) {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);
        TaskId createdTaskId = createDraft.getId();

        TaskId updatedTaskId = isCorrectId ? createdTaskId : createWrongTaskId();

        List<TaskView> drafts = fetchDrafts();

        String previousDescription = drafts
                .stream()
                .map(TaskView::getDescription)
                .map(TaskDescription::getValue)
                .findFirst()
                .orElseThrow(() -> newIllegalStateException("No draft found"));
        UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId,
                                              previousDescription,
                                              newDescription);
        client.postCommand(updateTaskDescription);
        assertEquals(1, drafts.size());

        TaskView view = fetchDrafts().get(0);
        assertEquals(createdTaskId, view.getId());
        return view;
    }

    /** Obtains all the task views that are in the {@code DRAFT} state. */
    private List<TaskView> fetchDrafts() {
        return client
                .getTaskViews()
                .stream()
                .filter(view -> view.getStatus() == TaskStatus.DRAFT)
                .collect(toList());
    }

    private void updateDescription(String newDescription, boolean isCorrectId,
                                   CreateBasicTask createTask) {
        TaskId idOfCreatedTask = createTask.getId();
        TaskId updatedTaskId = isCorrectId ? idOfCreatedTask : createWrongTaskId();
        UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId, createTask.getDescription()
                                                                       .getValue(),
                                              newDescription);
        client.postCommand(updateTaskDescription);
    }
}
