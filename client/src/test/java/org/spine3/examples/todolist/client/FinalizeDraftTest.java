/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.q.projections.DraftTasksView;
import org.spine3.examples.todolist.q.projections.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;

/**
 * @author Illia Shepilov
 */
public class FinalizeDraftTest extends CommandLineTodoClientTest {

    @Test
    @DisplayName("obtain empty view list when handled FinalizeDraft command")
    public void obtainEmptyViewsWhenDraftIsFinalized() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        DraftTasksView draftTasksView = client.getDraftTasksView();

        List<TaskView> taskViewList = draftTasksView.getDraftTasks()
                                                    .getItemsList();
        final int expectedListSize = 1;

        assertEquals(expectedListSize, taskViewList.size());
        assertEquals(createDraft.getId(), taskViewList.get(0)
                                                      .getId());
        final FinalizeDraft finalizeDraft = finalizeDraftInstance(createDraft.getId());
        client.finalize(finalizeDraft);

        draftTasksView = client.getDraftTasksView();
        taskViewList = draftTasksView.getDraftTasks()
                                     .getItemsList();
        assertTrue(taskViewList.isEmpty());
    }

    @Test
    @DisplayName("obtain view when handled FinalizeDraft command with wrong task ID")
    public void obtainViewWhenFinalizedWrongDraft() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);
        final TaskId taskId = createDraft.getId();

        final FinalizeDraft finalizeDraft = finalizeDraftInstance(getWrongTaskId());
        client.finalize(finalizeDraft);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());
    }

    @Test
    public void obtain_task_view_when_handled_command_finalize_draft() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        List<TaskView> views = client.getMyListView()
                                     .getMyList()
                                     .getItemsList();
        int expectedListSize = 0;
        assertEquals(expectedListSize, views.size());

        final TaskId taskId = createDraft.getId();
        final FinalizeDraft finalizeDraft = finalizeDraftInstance(taskId);
        client.finalize(finalizeDraft);

        expectedListSize = 1;
        views = client.getMyListView()
                      .getMyList()
                      .getItemsList();
        assertEquals(expectedListSize, views.size());

        final TaskView view = views.get(0);
        assertEquals(taskId, view.getId());
    }
}
