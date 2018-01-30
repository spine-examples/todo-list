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

package io.spine.test;

import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.client.builder.CommandBuilder;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.examples.todolist.server.Server;
import io.spine.server.BoundedContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.client.TodoClient.HOST;
import static io.spine.examples.todolist.server.Server.newServer;
import static io.spine.examples.todolist.testdata.Given.newDescription;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.util.Arrays.stream;

/**
 * Base class for integration and performance tests. Encapsulates server, clients setup logic.
 *
 * <p>Contains {@link #getClients() method getClients} to access clients.
 *
 * <p>Use {@link #asyncPerformanceTest(ToDoCommand, Integer) asyncPerformanceTest method} to execute
 * operation in multithreaded environment.
 *
 * @author Dmitry Ganzha
 */
public abstract class AbstractIntegrationTest {

    private static final int PORT = DEFAULT_CLIENT_SERVICE_PORT;
    private static final int NUMBER_OF_CLIENTS = 20;

    private final TodoClient[] clients = new TodoClient[NUMBER_OF_CLIENTS];
    private Server server;
    private TodoClient client;

    protected static CreateBasicTask createBasicTask() {
        return CommandBuilder.task()
                             .createTask()
                             .setDescription(newDescription(DESCRIPTION))
                             .build();
    }

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

    @BeforeEach
    protected void setUp() throws InterruptedException {
        final BoundedContext boundedContextInMemory = createBoundedContext();
        server = newServer(PORT, boundedContextInMemory);
        startServer();
        client = TodoClient.instance(HOST, PORT);
        for (int i = 0; i < NUMBER_OF_CLIENTS; i++) {
            clients[i] = TodoClient.instance(HOST, PORT);
        }
    }

    @AfterEach
    protected void tearDown() {
        server.shutdown();
        getClient().shutdown();
        stream(getClients()).forEach(TodoClient::shutdown);
    }

    private void startServer() throws InterruptedException {
        final CountDownLatch serverStartLatch = new CountDownLatch(1);
        final Thread serverThread = new Thread(() -> {
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

    private static BoundedContext createBoundedContext() {
        final BoundedContext boundedContext = BoundedContexts.create();
        return boundedContext;
    }

    protected TodoClient getClient() {
        return client;
    }

    protected TodoClient[] getClients() {
        return clients;
    }

    protected void asyncPerformanceTest(ToDoCommand command, Integer numberOfRequests)
            throws InterruptedException {
        final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime()
                                                                         .availableProcessors() *
                                                                          2);
        final CountDownLatch latch = new CountDownLatch(numberOfRequests);
        for (int i = 0; i < numberOfRequests; i++) {
            final int iterationIndex = i;
            pool.submit(() -> {
                command.execute(iterationIndex);
                latch.countDown();
            });
        }
        try {
            latch.await();
        } finally {
            pool.shutdownNow();
        }
    }

    protected interface ToDoCommand {
        void execute(int iterationIndex);
    }
}
