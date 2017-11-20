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

package io.spine.examples.todolist.client;

import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.MyListView;

import java.util.List;

/**
 * A client interface.
 *
 * <p> Provides methods to communicate with server.
 *
 * @author Illia Shepilov
 */
public interface TodoClient {

    String HOST = "localhost";

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
     * Finalizes draft according to the command data.
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
     * Obtains all {@linkplain Task tasks} in the system.
     *
     * @return the list of the {@code Task}
     */
    List<Task> getTasks();

    /**
     * Shutdown the connection channel.
     */
    void shutdown();

    /**
     * Creates a new instance of {@code TodoClient}.
     *
     * <p>The resulting {@code TodoClient} connects to the server at {@code host:port}.
     *
     * @param host the host of the server to connect to
     * @param port the port of the server to connect to
     * @return new TodoList client
     */
    static TodoClient instance(String host, int port) {
        return new TodoClientImpl(host, port);
    }
}
