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

import io.spine.examples.todolist.client.builder.CommandBuilder;
import io.spine.examples.todolist.server.Server;
import io.spine.examples.todolist.server.tasks.TasksContextFactory;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.command.AssignLabelToTask;
import io.spine.examples.todolist.tasks.command.CreateBasicLabel;
import io.spine.examples.todolist.tasks.command.CreateBasicTask;
import io.spine.examples.todolist.tasks.command.CreateDraft;
import io.spine.examples.todolist.tasks.command.FinalizeDraft;
import io.spine.server.BoundedContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.client.TodoClientImpl.HOST;
import static io.spine.examples.todolist.server.Server.newServer;
import static io.spine.examples.todolist.testdata.Given.newDescription;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

abstract class TodoClientTest {

    private static final int PORT = DEFAULT_CLIENT_SERVICE_PORT;

    private Server server;
    private SubscribingTodoClient client;

    @BeforeEach
    void setUp() throws InterruptedException {
        BoundedContext boundedContext = TasksContextFactory.create();
        server = newServer(PORT, boundedContext);
        startServer();
        client = SubscribingTodoClient.instance(HOST, PORT);
    }

    @AfterEach
    public void tearDown() {
        server.shutdown();
        client().shutdown();
    }

    private void startServer() throws InterruptedException {
        CountDownLatch serverStartLatch = new CountDownLatch(1);
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
                serverStartLatch.countDown();
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        });

        serverThread.start();
        serverStartLatch.await(100, TimeUnit.MILLISECONDS);
    }

    static CreateBasicLabel createBasicLabel() {
        return CommandBuilder.label()
                             .createLabel()
                             .setTitle(LABEL_TITLE)
                             .build();
    }

    static AssignLabelToTask assignLabelToTask(TaskId taskId, LabelId labelId) {
        AssignLabelToTask result = AssignLabelToTask
                .newBuilder()
                .setId(taskId)
                .setLabelId(labelId)
                .vBuild();
        return result;
    }

    static CreateDraft createDraft() {
        return CommandBuilder
                .task()
                .createDraft()
                .build();
    }

    static FinalizeDraft finalizeDraft(TaskId taskId) {
        FinalizeDraft result = FinalizeDraft
                .newBuilder()
                .setId(taskId)
                .vBuild();
        return result;
    }

    static CreateBasicTask createBasicTask() {
        return CommandBuilder
                .task()
                .createTask()
                .setDescription(newDescription(DESCRIPTION))
                .build();
    }

    public SubscribingTodoClient client() {
        return client;
    }
}
