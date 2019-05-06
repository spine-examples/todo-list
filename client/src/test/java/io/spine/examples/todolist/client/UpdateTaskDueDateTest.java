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

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.base.Time.currentTime;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("After execution of UpdateTaskDueDate command, a list of task views should")
class UpdateTaskDueDateTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("contain an updated draft")
    void containUpdatedDraft() {
        Timestamp newDueDate = currentTime();
        TaskView view = getUpdatedDraft(newDueDate, true);

        assertEquals(newDueDate, view.getDueDate());
    }

    @Test
    @DisplayName("contain an unchanged draft if the command had an incorrect ID")
    void containUnchangedDraft() {
        Timestamp newDueDate = currentTime();
        TaskView view = getUpdatedDraft(newDueDate, false);

        assertNotEquals(newDueDate, view.getDueDate());
    }

    private TaskView getUpdatedDraft(Timestamp newDueDate, boolean isCorrectId) {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);

        TaskId createdTaskId = createDraft.getId();

        updateDueDate(newDueDate, isCorrectId, createdTaskId);

        List<TaskView> drafts = client
                .getTaskViews()
                .stream()
                .filter(view -> view.getStatus() == TaskStatus.DRAFT)
                .collect(toList());
        assertEquals(1, drafts.size());

        TaskView view = drafts.get(0);
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    @Test
    @DisplayName("contain an updated labelled task")
    void containUpdatedLabelledTask() {
        Timestamp newDueDate = currentTime();
        TaskView view = getUpdatedLabelledTask(newDueDate, true);
        assertEquals(newDueDate, view.getDueDate());
    }

    @Test
    @DisplayName("contain an unchanged labelled task if the command had an incorrect ID")
    void containUnchangedLabelledTask() {
        Timestamp newDueDate = currentTime();
        TaskView view = getUpdatedLabelledTask(newDueDate, false);
        assertNotEquals(newDueDate, view.getDueDate());
    }

    private TaskView getUpdatedLabelledTask(Timestamp newDueDate, boolean isCorrectId) {
        CreateBasicTask createTask = createBasicTask();
        TaskId createdTaskId = createTask.getId();
        client.postCommand(createTask);

        UpdateTaskPriority updateTaskPriority = setInitialTaskPriority(createdTaskId);
        client.postCommand(updateTaskPriority);

        CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);

        TaskId taskId = createTask.getId();
        LabelId labelId = createLabel.getLabelId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        updateDueDate(newDueDate, isCorrectId, createdTaskId);

        List<TaskView> labelledTasks = client
                .getTaskViews()
                .stream()
                .filter(view -> !view.getLabelIdsList()
                                     .getIdsList()
                                     .isEmpty())
                .collect(toList());
        int expectedListSize = 1;
        assertEquals(expectedListSize, labelledTasks.size());

        TaskView view = labelledTasks.get(0);

        assertEquals(labelId, view.getLabelIdsList()
                                  .getIds(0));
        assertEquals(taskId, view.getId());

        return view;
    }

    private void updateDueDate(Timestamp newDueDate, boolean isCorrectId, TaskId idOfCreatedTask) {
        TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : createWrongTaskId();
        Timestamp previousDueDate = Timestamp.getDefaultInstance();
        UpdateTaskDueDate updateTaskDueDate =
                updateTaskDueDateInstance(idOfUpdatedTask, previousDueDate, newDueDate);
        client.postCommand(updateTaskDueDate);
    }
}
