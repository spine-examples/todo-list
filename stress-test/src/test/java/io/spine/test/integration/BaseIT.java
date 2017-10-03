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

package io.spine.test.integration;

import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.client.CommandLineTodoClient;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.client.builder.CommandBuilder;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.examples.todolist.server.Server;
import io.spine.server.BoundedContext;
import io.spine.util.Exceptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.client.CommandLineTodoClient.HOST;
import static io.spine.examples.todolist.testdata.Given.newDescription;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;

public class BaseIT {
    private static final int PORT = DEFAULT_CLIENT_SERVICE_PORT;

    private Server server;
    private TodoClient client;

    protected static CreateBasicLabel createBasicLabel() {
        return CommandBuilder.label()
                             .createLabel()
                             .setTitle(LABEL_TITLE)
                             .build();
    }

    protected static CreateDraft createDraft() {
        return CommandBuilder.task()
                             .createDraft()
                             .build();
    }

    protected static CreateBasicTask createBasicTask() {
        return CommandBuilder.task()
                             .createTask()
                             .setDescription(newDescription(DESCRIPTION))
                             .build();
    }

    @BeforeEach
    protected void setUp() throws InterruptedException {
        final BoundedContext boundedContext = BoundedContexts.create();
        server = new Server(PORT, boundedContext);
        startServer();
        client = new CommandLineTodoClient(HOST, PORT);
    }

    @AfterEach
    public void tearDown() {
        server.shutdown();
        getClient().shutdown();
    }

    private void startServer() throws InterruptedException {
        final CountDownLatch serverStartLatch = new CountDownLatch(1);
        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
                serverStartLatch.countDown();
            } catch (IOException e) {
                throw Exceptions.illegalStateWithCauseOf(e);
            }
        });

        serverThread.start();
        serverStartLatch.await(100, TimeUnit.MILLISECONDS);
    }

    public TodoClient getClient() {
        return client;
    }
}