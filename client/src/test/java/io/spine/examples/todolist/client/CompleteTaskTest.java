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
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.TaskStatus.COMPLETED;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@DisplayName("After execution of CompleteTask command a list of task views should")
class CompleteTaskTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("contain a labelled completed task")
    void obtainLabelledViewWithCompletedTask() {
        TaskView view = obtainViewWhenHandledCommandCompleteTask(true);
        assertEquals(COMPLETED, view.getStatus());
    }

    @Test
    @DisplayName("contain a labelled uncompleted task if the command had an incorrect ID")
    void obtainLabelledViewWithUncompletedTask() {
        TaskView view = obtainViewWhenHandledCommandCompleteTask(false);
        assertNotSame(COMPLETED, view.getStatus());
    }

    private TaskView obtainViewWhenHandledCommandCompleteTask(boolean isCorrectId) {
        CreateBasicTask createTask = createTask();
        TaskId createdTaskId = createTask.getId();

        CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);

        TaskId taskId = createTask.getId();
        LabelId labelId = createLabel.getLabelId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        completeTask(isCorrectId, createdTaskId);

        List<TaskView> labelledTaskViews = client
                .taskViews()
                .stream()
                .filter(view -> !view.getLabelIdsList()
                                     .getIdsList()
                                     .isEmpty())
                .collect(toList());
        int expectedListSize = 1;
        assertEquals(expectedListSize, labelledTaskViews.size());
        TaskView view = labelledTaskViews.get(0);
        assertEquals(taskId, view.getId());
        return view;
    }

    private void completeTask(boolean isCorrectId, TaskId idOfCreatedTask) {
        TaskId idOfCompletedTask = isCorrectId ? idOfCreatedTask : createWrongTaskId();
        CompleteTask completeTask = completeTaskInstance(idOfCompletedTask);
        client.postCommand(completeTask);
    }
}
