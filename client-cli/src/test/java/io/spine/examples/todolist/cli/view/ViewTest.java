/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.cli.view;

import io.spine.examples.todolist.cli.AppConfig;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.server.Server;
import io.spine.examples.todolist.server.TasksContextFactory;
import io.spine.server.BoundedContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.client.TodoClient.HOST;
import static io.spine.examples.todolist.server.Server.newServer;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

abstract class ViewTest {

    private static final int PORT = DEFAULT_CLIENT_SERVICE_PORT;

    private Server server;

    @BeforeEach
    void setUp() throws InterruptedException {
        BoundedContext boundedContext = TasksContextFactory.create();
        server = newServer(PORT, boundedContext);
        startServer();

        TodoClient client = TodoClient.instance(HOST, PORT);
        AppConfig.setClient(client);
    }

    @AfterEach
    public void tearDown() {
        server.shutdown();
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
}
