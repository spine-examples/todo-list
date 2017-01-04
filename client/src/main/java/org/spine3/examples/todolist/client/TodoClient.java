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

import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicLabel;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.UpdateLabelDetails;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;
import org.spine3.examples.todolist.view.DraftTasksView;
import org.spine3.examples.todolist.view.LabelledTasksView;
import org.spine3.examples.todolist.view.MyListView;

import java.util.List;

/**
 * @author Illia Shepilov
 */
public interface TodoClient {

    void create(CreateBasicTask cmd);

    void create(CreateBasicLabel cmd);

    void create(CreateDraft cmd);

    void update(UpdateTaskDescription cmd);

    void update(UpdateTaskDueDate cmd);

    void update(UpdateTaskPriority cmd);

    void update(UpdateLabelDetails cmd);

    void delete(DeleteTask cmd);

    void removeLabel(RemoveLabelFromTask cmd);

    void assignLabel(AssignLabelToTask cmd);

    void reopen(ReopenTask cmd);

    void restore(RestoreDeletedTask cmd);

    void complete(CompleteTask cmd);

    void finalize(FinalizeDraft cmd);

    MyListView getMyListView();

    List<LabelledTasksView> getLabelledTasksView();

    DraftTasksView getDraftTasksView();

    /**
     * Shutdown the connection channel.
     */
    void shutdown();
}
