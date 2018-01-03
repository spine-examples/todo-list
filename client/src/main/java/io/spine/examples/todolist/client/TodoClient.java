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

import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.c.commands.TodoCommand;
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
     * Posts the given command to the {@code CommandService}.
     *
     * @param commandMessage the command to post
     */
    void postCommand(TodoCommand commandMessage);

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
     * Obtains all {@linkplain TaskLabel labels} in the system.
     *
     * @return the list of the {@code TaskLabel}
     */
    List<TaskLabel> getLabels();

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
