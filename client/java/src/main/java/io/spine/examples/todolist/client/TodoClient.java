/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.Task;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.TaskLabel;
import io.spine.examples.todolist.tasks.TaskLabels;
import io.spine.examples.todolist.tasks.view.LabelView;
import io.spine.examples.todolist.tasks.view.TaskView;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A client interface.
 *
 * <p>Provides methods to communicate with server.
 */
public interface TodoClient {

    String HOST = "localhost";

    /**
     * Posts the given command to the {@code CommandService}.
     *
     * @param commandMessage
     *         the command to post
     */
    void postCommand(CommandMessage commandMessage);

    /**
     * Obtains all {@linkplain TaskView task views}.
     *
     * @return all task views
     */
    List<TaskView> taskViews();

    /**
     * Obtains all {@linkplain Task tasks} in the system.
     *
     * @return the list of the {@code Task}
     */
    List<Task> tasks();

    /**
     * Obtains all {@linkplain TaskLabel labels} in the system.
     *
     * @return the list of the {@code TaskLabel}
     */
    List<TaskLabel> labels();

    /**
     * Obtains the labels assigned to the task with the given ID.
     *
     * @param taskId
     *         the task ID to search by
     * @return the labels of the specified task
     */
    TaskLabels labelsOf(TaskId taskId);

    /**
     * Obtains an {@code Optional} containing the view of the label with the specified ID.
     *
     * If the specified ID does not correspond to any label, an empty {@code Optional} is returned.
     *
     * @param id
     *         ID of the label to obtain
     * @return a view of the label with the specified ID.
     */
    Optional<LabelView> labelView(LabelId id);

    /**
     * Obtains a single {@link TaskLabel} by its ID.
     *
     * <p>If the system contains no label with such ID, the {@code other} value is returned.
     *
     * <p>Returns {@code null} iff the label is not found by ID and the {@code other} value is
     * {@code null}.
     *
     * @param id
     *         the label ID to search by
     * @param other
     *         the default value of the label
     * @return the label with the requested ID or {@code other} if the label is not found
     */
    @Nullable
    TaskLabel labelOr(LabelId id, @Nullable TaskLabel other);

    /**
     * Shutdown the connection channel.
     */
    void shutdown();

    /**
     * Creates a new instance of {@code TodoClient}.
     *
     * <p>The resulting {@code TodoClient} connects to the server at {@code host:port}.
     *
     * @param host
     *         the host of the server to connect to
     * @param port
     *         the port of the server to connect to
     * @return new TodoList client
     */
    static TodoClient instance(String host, int port) {
        return new TodoClientImpl(host, port);
    }
}
