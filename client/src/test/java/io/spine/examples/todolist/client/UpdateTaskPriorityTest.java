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

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("After execution of UpdateTaskPriority command task views should")
class UpdateTaskPriorityTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("contain an updated labelled task")
    void containUpdatedLabelledTask() {
        TaskPriority newPriority = TaskPriority.LOW;
        TaskView view = obtainViewWhenHandledCommandUpdateTaskPriority(newPriority, true);
        assertEquals(newPriority, view.getPriority());
    }

    @Test
    @DisplayName("contain an unchanged labelled task if the command had an incorrect ID")
    void containUnchangedLabelledTask() {
        TaskPriority newPriority = TaskPriority.LOW;
        TaskView view = obtainViewWhenHandledCommandUpdateTaskPriority(newPriority, false);
        assertNotEquals(newPriority, view.getPriority());
    }

    private TaskView obtainViewWhenHandledCommandUpdateTaskPriority(TaskPriority priority,
                                                                    boolean isCorrectId) {
        CreateBasicTask createTask = createBasicTask();
        TaskId createdTaskId = createTask.getId();
        client.postCommand(createTask);

        UpdateTaskPriority updateTaskPriority = setInitialTaskPriority(createdTaskId);
        client.postCommand(updateTaskPriority);

        updatePriority(priority, isCorrectId, createdTaskId);

        CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);

        int expectedListSize = 1;
        AssignLabelToTask assignLabelToTask =
                assignLabelToTaskInstance(createdTaskId, createLabel.getLabelId());
        client.postCommand(assignLabelToTask);

        List<TaskView> labelledTasks = client
                .taskViews()
                .stream()
                .filter(view -> !view.getLabelIdsList()
                                     .getIdsList()
                                     .isEmpty())
                .collect(toList());
        assertEquals(expectedListSize, labelledTasks.size());

        TaskView view = labelledTasks.get(0);
        assertEquals(createLabel.getLabelId(), view.getLabelIdsList()
                                                   .getIds(0));
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    @Test
    @DisplayName("contain a draft with updated task priority")
    void containUpdatedDraft() {
        TaskPriority newPriority = TaskPriority.LOW;
        TaskView view = obtainViewWhenHandledUpdateTaskPriorityCommand(newPriority, true);

        assertEquals(newPriority, view.getPriority());
    }

    @Test
    @DisplayName("contain an unchanged draft if the command had an incorrect ID")
    void containUnchangedDraft() {
        TaskPriority newPriority = TaskPriority.LOW;
        TaskView view = obtainViewWhenHandledUpdateTaskPriorityCommand(newPriority, false);

        assertNotEquals(newPriority, view.getPriority());
    }

    private TaskView obtainViewWhenHandledUpdateTaskPriorityCommand(TaskPriority newPriority,
                                                                    boolean isCorrectId) {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);
        TaskId createdTaskId = createDraft.getId();

        UpdateTaskPriority updateTaskPriority = setInitialTaskPriority(createdTaskId);
        client.postCommand(updateTaskPriority);

        updatePriority(newPriority, isCorrectId, createdTaskId);

        List<TaskView> drafts = client
                .taskViews()
                .stream()
                .filter(view -> view.getStatus() == TaskStatus.DRAFT)
                .collect(toList());
        assertEquals(1, drafts.size());

        TaskView result = drafts.get(0);
        assertEquals(createdTaskId, result.getId());

        return result;
    }

    private void
    updatePriority(TaskPriority newPriority, boolean isCorrectId, TaskId createdTaskId) {
        TaskId updatedTaskId = isCorrectId
                               ? createdTaskId
                               : createWrongTaskId();
        TaskPriority previousPriority = isCorrectId
                                        ? TaskPriority.HIGH
                                        : TaskPriority.TP_UNDEFINED;
        UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(updatedTaskId, previousPriority, newPriority);
        client.postCommand(updateTaskPriority);
    }
}
