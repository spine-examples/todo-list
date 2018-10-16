/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import io.spine.base.CommandMessage;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.TaskLabels;
import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.MyListView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A client interface.
 *
 * <p> Provides methods to communicate with server.
 */
public interface TodoClient {

    String HOST = "localhost";

    /**
     * Posts the given command to the {@code CommandService}.
     *
     * @param commandMessage the command to post
     */
    void postCommand(CommandMessage commandMessage);

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
     * Obtains a single {@link Task} by its ID.
     *
     * <p>If the system contains no task with such ID, the {@code other} value is returned.
     *
     * <p>Returns {@code null} iff the task is not found by ID and the {@code other} value is
     * {@code null}.
     *
     * @param id    the task ID to search by
     * @param other the default value of the task
     * @return the task with the requested ID or {@code other} if the task is not found
     */
    @Nullable
    Task getTaskOr(TaskId id, @Nullable Task other);

    /**
     * Obtains all {@linkplain TaskLabel labels} in the system.
     *
     * @return the list of the {@code TaskLabel}
     */
    List<TaskLabel> getLabels();

    /**
     * Obtains the labels assigned to the task with the given ID.
     *
     * @param taskId the task ID to search by
     * @return the labels of the specified task
     */
    TaskLabels getLabels(TaskId taskId);

    /**
     * Obtains a single {@link TaskLabel} by its ID.
     *
     * <p>If the system contains no label with such ID, the {@code other} value is returned.
     *
     * <p>Returns {@code null} iff the label is not found by ID and the {@code other} value is
     * {@code null}.
     *
     * @param id    the label ID to search by
     * @param other the default value of the label
     * @return the label with the requested ID or {@code other} if the label is not found
     */
    @Nullable
    TaskLabel getLabelOr(LabelId id, @Nullable TaskLabel other);

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
