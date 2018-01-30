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

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution of FinalizeDraft command")
class FinalizeDraftTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("MyListView should")
    class FinalizeDraftFromMyListView {

        @Test
        @DisplayName("contain the task view")
        void obtainView() {
            final CreateDraft createDraft = createDraftTask();

            List<TaskItem> views = client.getMyListView()
                                         .getMyList()
                                         .getItemsList();
            int expectedListSize = 0;
            assertEquals(expectedListSize, views.size());

            final TaskId taskId = createDraft.getId();
            final FinalizeDraft finalizeDraft = finalizeDraftInstance(taskId);
            client.postCommand(finalizeDraft);

            expectedListSize = 1;
            views = client.getMyListView()
                          .getMyList()
                          .getItemsList();
            assertEquals(expectedListSize, views.size());

            final TaskItem view = views.get(0);
            assertEquals(taskId, view.getId());
        }
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class FinalizeDraftFromLabelledTasksView {

        @Test
        @DisplayName("contain the task view")
        void obtainView() {
            final CreateDraft createDraft = createDraftTask();

            final CreateBasicLabel createBasicLabel = createBasicLabel();
            client.postCommand(createBasicLabel);

            final TaskId taskId = createDraft.getId();
            final LabelId labelId = createBasicLabel.getLabelId();
            final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
            client.postCommand(assignLabelToTask);

            final FinalizeDraft finalizeDraft = finalizeDraftInstance(taskId);
            client.postCommand(finalizeDraft);

            final List<LabelledTasksView> labelledViews = client.getLabelledTasksView();
            assertEquals(1, labelledViews.size());

            final LabelledTasksView labelledView = labelledViews.get(0);
            final List<TaskItem> taskViews = labelledView.getLabelledTasks()
                                                         .getItemsList();
            assertEquals(1, taskViews.size());

            final TaskItem taskView = taskViews.get(0);
            assertEquals(taskId, taskView.getId());
            assertEquals(labelId, taskView.getLabelId());
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class FinalizeDraftFromDraftTasksView {

        @Test
        @DisplayName("be empty")
        void obtainEmptyViewsWhenDraftIsFinalized() {
            final CreateDraft createDraft = createDraftTask();

            DraftTasksView draftTasksView = client.getDraftTasksView();

            List<TaskItem> taskViewList = draftTasksView.getDraftTasks()
                                                        .getItemsList();
            assertEquals(1, taskViewList.size());
            assertEquals(createDraft.getId(), taskViewList.get(0)
                                                          .getId());
            final FinalizeDraft finalizeDraft = finalizeDraftInstance(createDraft.getId());
            client.postCommand(finalizeDraft);

            draftTasksView = client.getDraftTasksView();
            taskViewList = draftTasksView.getDraftTasks()
                                         .getItemsList();
            assertTrue(taskViewList.isEmpty());
        }

        @Test
        @DisplayName("not be empty when command has wrong task ID")
        void obtainViewWhenFinalizedWrongDraft() {
            final CreateDraft createDraft = createDraftTask();
            final TaskId taskId = createDraft.getId();

            final FinalizeDraft finalizeDraft = finalizeDraftInstance(createWrongTaskId());
            client.postCommand(finalizeDraft);

            final List<TaskItem> taskViews = client.getDraftTasksView()
                                                   .getDraftTasks()
                                                   .getItemsList();
            assertEquals(1, taskViews.size());

            final TaskItem view = taskViews.get(0);
            assertEquals(taskId, view.getId());
        }
    }

    private CreateDraft createDraftTask() {
        final CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);
        return createDraft;
    }
}
