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
 * A client interface.
 *
 * <p> Provides methods to communicate with server.
 *
 * @author Illia Shepilov
 */
public interface TodoClient {

    /**
     * Creates task according to the command data.
     *
     * @param cmd the {@link CreateBasicTask} command
     */
    void create(CreateBasicTask cmd);

    /**
     * Creates label according to the command data.
     *
     * @param cmd the {@link CreateBasicLabel} command
     */
    void create(CreateBasicLabel cmd);

    /**
     * Creates draft according to the command data.
     *
     * @param cmd the {@link CreateDraft} command
     */
    void create(CreateDraft cmd);

    /**
     * Updates task description according to the command data.
     *
     * @param cmd the {@link UpdateTaskDescription} command
     */
    void update(UpdateTaskDescription cmd);

    /**
     * Updates task due date according to the command data.
     *
     * @param cmd the {@link UpdateTaskDueDate} command
     */
    void update(UpdateTaskDueDate cmd);

    /**
     * Updates task priority according to the command data.
     *
     * @param cmd the {@link UpdateTaskPriority} command
     */
    void update(UpdateTaskPriority cmd);

    /**
     * Updates label details according to the command data.
     *
     * @param cmd the {@link UpdateLabelDetails} command
     */
    void update(UpdateLabelDetails cmd);

    /**
     * Deletes task according to the command data.
     *
     * @param cmd the {@link DeleteTask} command
     */
    void delete(DeleteTask cmd);

    /**
     * Removes label from task according to the command data.
     *
     * @param cmd the {@link RemoveLabelFromTask} command
     */
    void removeLabel(RemoveLabelFromTask cmd);

    /**
     * Assigns label to task according to the command data.
     *
     * @param cmd the {@link AssignLabelToTask} command
     */
    void assignLabel(AssignLabelToTask cmd);

    /**
     * Reopens task according to the command data.
     *
     * @param cmd the {@link ReopenTask} command
     */
    void reopen(ReopenTask cmd);

    /**
     * Restores deleted task according to the command data.
     *
     * @param cmd the {@link RestoreDeletedTask} command
     */
    void restore(RestoreDeletedTask cmd);

    /**
     * Completes task according to the command data.
     *
     * @param cmd the {@link CompleteTask} command
     */
    void complete(CompleteTask cmd);

    /**
     * Finalizes draft acording to the command data.
     *
     * @param cmd the {@link FinalizeDraft} command
     */
    void finalize(FinalizeDraft cmd);

    /**
     * Obtains the single {@link MyListView}.
     *
     * @return the {@code MyListView}
     */
    MyListView getMyListView();

    /**
     * Obtains the list of the {@link LabelledTasksView}.
     *
     * @return the list of the {@code LabelledTasksView}
     */
    List<LabelledTasksView> getLabelledTasksView();

    /**
     * Obtains the single {@link DraftTasksView}.
     *
     * @return the {@code DraftTasksView}
     */
    DraftTasksView getDraftTasksView();

    /**
     * Shutdown the connection channel.
     */
    void shutdown();
}
