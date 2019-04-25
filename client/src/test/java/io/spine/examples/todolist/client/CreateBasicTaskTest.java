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

import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.spine.examples.todolist.TaskStatus.FINALIZED;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("After execution of CreateBasicTask command")
class CreateBasicTaskTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("task views should be unlabelled")
    void obtainEmptyLabelledTasksView() {
        CreateBasicTask createBasicTask = createBasicTask();
        client.postCommand(createBasicTask);

        boolean noLabelsPresent = client
                .getTaskViews()
                .stream()
                .map(view -> view.getLabelIdsList()
                                 .getIdsList())
                .allMatch(List::isEmpty);
        assertTrue(noLabelsPresent);
    }

    @Test
    @DisplayName("no drafts should be present")
    void obtainEmptyDraftViewList() {
        CreateBasicTask createBasicTask = createBasicTask();
        client.postCommand(createBasicTask);

        boolean noDrafts = client
                .getTaskViews()
                .stream()
                .allMatch(view -> view.getStatus() != TaskStatus.DRAFT);

        assertTrue(noDrafts);
    }

    @Test
    @DisplayName("List of task views should contain the created task")
    void obtainMyListView() {
        CreateBasicTask createFirstTask = createBasicTask();
        client.postCommand(createFirstTask);

        CreateBasicTask createSecondTask = createBasicTask();
        client.postCommand(createSecondTask);

        List<TaskView> taskViews = client.getTaskViews();
        assertEquals(2, taskViews.size());

        List<TaskId> taskIds = new ArrayList<>(2);
        TaskView firstView = taskViews.get(0);
        TaskView secondView = taskViews.get(1);
        taskIds.add(firstView.getId());
        taskIds.add(secondView.getId());

        assertTrue(taskIds.contains(createFirstTask.getId()));
        assertTrue(taskIds.contains(createSecondTask.getId()));
        assertEquals(DESCRIPTION, firstView.getDescription()
                                           .getValue());
        assertEquals(DESCRIPTION, secondView.getDescription()
                                            .getValue());
    }

    @Test
    @DisplayName("the task should be found")
    void obtainAllTasks() {
        CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);

        List<Task> allTasks = client.getTasks();

        assertEquals(1, allTasks.size());
        Task singleTask = allTasks.get(0);

        // Set fields
        assertEquals(createTask.getId(), singleTask.getId());
        assertEquals(createTask.getDescription(), singleTask.getDescription());

        // Default fields
        assertEquals(FINALIZED, singleTask.getTaskStatus());
    }
}
