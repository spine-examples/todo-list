/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.view.DraftTasksView;
import org.spine3.examples.todolist.view.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;

/**
 * @author Illia Shepilov
 */
public class DraftTasksViewClientShould extends BasicTodoClientShould{

    @Test
    public void obtain_task_draft_when_handled_create_draft_command() {
        final CreateDraft createDraft = createDraftInstance();
        client.create(createDraft);

        final DraftTasksView draftTasksView = client.getDraftTasksView();
        final List<TaskView> taskViewList = draftTasksView.getDraftTasks()
                                                          .getItemsList();
        final int expectedListSize = 1;

        assertEquals(expectedListSize, taskViewList.size());
        assertEquals(createDraft.getId(), taskViewList.get(0)
                                                      .getId());
    }

    @Test
    public void obtain_empty_tasks_draft_view_when_handled_command_delete_task() {
        final CreateDraft createDraft = createDraftInstance();
        client.create(createDraft);

        final DeleteTask deleteTask = deleteTaskInstance(createDraft.getId());
        client.delete(deleteTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_empty_tasks_draft_view_when_handled_command_finalize_draft() {
        final CreateDraft createDraft = createDraftInstance();
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
}
