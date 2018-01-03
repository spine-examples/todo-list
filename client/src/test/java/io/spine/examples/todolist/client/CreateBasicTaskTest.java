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

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
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
    @DisplayName("LabelledTasksView should be empty")
    void obtainEmptyLabelledTasksView() {
        final CreateBasicTask createBasicTask = createBasicTask();
        client.postCommand(createBasicTask);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        assertTrue(labelledTasksView.isEmpty());
    }

    @Test
    @DisplayName("DraftTaskItem should be empty")
    void obtainEmptyDraftViewList() {
        final CreateBasicTask createBasicTask = createBasicTask();
        client.postCommand(createBasicTask);

        final List<TaskItem> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    @DisplayName("MyListView should contain the created task")
    void obtainMyListView() {
        final CreateBasicTask createFirstTask = createBasicTask();
        client.postCommand(createFirstTask);

        final CreateBasicTask createSecondTask = createBasicTask();
        client.postCommand(createSecondTask);

        final List<TaskItem> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertEquals(2, taskViews.size());

        final TaskItem firstView = taskViews.get(0);
        final TaskItem secondView = taskViews.get(1);
        final List<TaskId> taskIds = new ArrayList<>(2);
        taskIds.add(firstView.getId());
        taskIds.add(secondView.getId());

        assertTrue(taskIds.contains(createFirstTask.getId()));
        assertTrue(taskIds.contains(createSecondTask.getId()));
        assertEquals(DESCRIPTION, firstView.getDescription()
                                           .getValue());
        assertEquals(DESCRIPTION, secondView.getDescription()
                                            .getValue());
    }
}
