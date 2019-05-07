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
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("After execution of FinalizeDraft command the list of task views should")
class FinalizeDraftTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("contain the task view")
    void obtainView() {
        CreateDraft createDraft = createDraftTask();

        CreateBasicLabel createBasicLabel = createBasicLabel();
        client.postCommand(createBasicLabel);

        UpdateTaskPriority updateTaskPriority = setInitialTaskPriority(createDraft.getId());
        client.postCommand(updateTaskPriority);

        TaskId taskId = createDraft.getId();
        LabelId labelId = createBasicLabel.getLabelId();
        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        FinalizeDraft finalizeDraft = finalizeDraftInstance(taskId);
        client.postCommand(finalizeDraft);

        List<TaskView> views = client.taskViews();
        assertEquals(1, views.size());
        assertEquals(taskId, views.get(0)
                                  .getId());
    }

    @Test
    @DisplayName("contain no drafts")
    void obtainEmptyViewsWhenDraftIsFinalized() {
        CreateDraft createDraft = createDraftTask();

        List<TaskView> taskViewList = client
                .taskViews()
                .stream()
                .filter(view -> view.getStatus() == TaskStatus.DRAFT)
                .collect(toList());
        assertEquals(1, taskViewList.size());
        assertEquals(createDraft.getId(), taskViewList.get(0)
                                                      .getId());
        FinalizeDraft finalizeDraft = finalizeDraftInstance(createDraft.getId());
        client.postCommand(finalizeDraft);

        taskViewList = client
                .taskViews()
                .stream()
                .filter(view -> view.getStatus() == TaskStatus.DRAFT)
                .collect(toList());
        assertTrue(taskViewList.isEmpty());
    }

    @Test
    @DisplayName("contain a task draft when command has wrong task ID")
    void obtainViewWhenFinalizedWrongDraft() {
        CreateDraft createDraft = createDraftTask();
        TaskId taskId = createDraft.getId();

        FinalizeDraft finalizeDraft = finalizeDraftInstance(createWrongTaskId());
        client.postCommand(finalizeDraft);

        List<TaskView> drafts = client
                .taskViews()
                .stream()
                .filter(view -> view.getStatus() == TaskStatus.DRAFT)
                .collect(toList());
        assertEquals(1, drafts.size());

        TaskView view = drafts.get(0);
        assertEquals(taskId, view.getId());
    }

    private CreateDraft createDraftTask() {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);
        return createDraft;
    }
}
