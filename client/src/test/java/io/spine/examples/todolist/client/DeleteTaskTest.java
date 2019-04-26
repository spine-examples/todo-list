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
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("After execution of DeleteTask command task views should")
class DeleteTaskTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("contain an open task if the command was sent with a wrong ID")
    void obtainView() {
        CreateBasicTask createTask = createTask();

        CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);

        LabelId labelId = createLabel.getLabelId();
        TaskId taskId = createTask.getId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        DeleteTask deleteTask = deleteTaskInstance(createWrongTaskId());
        client.postCommand(deleteTask);

        List<TaskView> tasks = client.getTaskViews();
        assertEquals(1, tasks.size());

        TaskView task = tasks.get(0);
        assertEquals(createTask.getId(), task.getId());
        assertEquals(TaskStatus.OPEN, task.getStatus());
    }

    @Test
    @DisplayName("not contain a deleted draft")
    void obtainNoView() {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);

        DeleteTask deleteTask = deleteTaskInstance(createDraft.getId());
        client.postCommand(deleteTask);

        List<TaskView> views = client.getTaskViews();
        assertEquals(0, views.size());
    }

    @Test
    @DisplayName("contain a task with `Deleted` status")
    void obtainDeletedView() {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);

        TaskId id = createDraft.getId();

        FinalizeDraft finalizeDraft = finalizeDraftInstance(id);
        client.postCommand(finalizeDraft);

        DeleteTask deleteTask = deleteTaskInstance(id);
        client.postCommand(deleteTask);

        List<TaskView> views = client.getTaskViews();
        assertEquals(1, views.size());
        TaskView taskView = views.get(0);
        assertEquals(TaskStatus.DELETED, taskView.getStatus());
    }
}

