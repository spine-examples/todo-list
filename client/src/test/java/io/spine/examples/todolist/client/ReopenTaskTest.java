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
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.spine.examples.todolist.TaskStatus.COMPLETED;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@DisplayName("After execution of ReopenTask command task views should")
class ReopenTaskTest extends TodoClientTest {

    private TodoClient client;

    private static boolean hasLabel(TaskView view) {
        return !view.getLabelIdsList()
                    .getIdsList()
                    .isEmpty();
    }

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("contain a view of an uncompleted labelled task")
    void containViewWithUncompletedTask() {
        TaskView view = obtainViewWhenHandledCommandReopenTask(true);
        assertNotSame(COMPLETED, view.getStatus());
    }

    @Test
    @DisplayName("contain a view of a completed labelled task if the command had a wrong ID")
    void containViewWithCompletedTask() {
        TaskView view = obtainViewWhenHandledCommandReopenTask(false);
        assertEquals(COMPLETED, view.getStatus());
    }

    private TaskView obtainViewWhenHandledCommandReopenTask(boolean isCorrectId) {
        CreateBasicTask createTask = createTask();
        TaskId createdTaskId = createTask.getId();

        CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);

        TaskId taskId = createTask.getId();
        LabelId labelId = createLabel.getLabelId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        completeAndReopenTask(isCorrectId, createdTaskId);

        List<TaskView> labelledTasksView = client
                .taskViews()
                .stream()
                .filter(ReopenTaskTest::hasLabel)
                .collect(Collectors.toList());
        assertEquals(1, labelledTasksView.size());
        return checkAndObtainView(taskId, labelledTasksView);
    }

    private static TaskView checkAndObtainView(TaskId idOfCreatedTask, List<TaskView> taskViews) {
        assertEquals(1, taskViews.size());

        TaskView view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());
        return view;
    }

    private void completeAndReopenTask(boolean isCorrectId, TaskId createdTaskId) {
        CompleteTask completeTask = completeTaskInstance(createdTaskId);
        client.postCommand(completeTask);

        TaskId reopenedTaskId = isCorrectId ? createdTaskId : createWrongTaskId();
        ReopenTask reopenTask = reopenTaskInstance(reopenedTaskId);
        client.postCommand(reopenTask);
    }
}
