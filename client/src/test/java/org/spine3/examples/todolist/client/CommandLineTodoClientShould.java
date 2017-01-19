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

package org.spine3.examples.todolist.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.client.builder.CommandBuilder;
import org.spine3.examples.todolist.server.Server;
import org.spine3.server.storage.memory.InMemoryStorageFactory;
import org.spine3.util.Exceptions;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.LABEL_TITLE;

/**
 * @author Illia Shepilov
 */
class CommandLineTodoClientShould {

    private static final String HOST = "localhost";
    private Server server;
    TodoClient client;
    static final String UPDATED_TASK_DESCRIPTION = "New task description.";

    @BeforeEach
    public void setUp() throws InterruptedException {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        server = new Server(storageFactory);
        startServer();
        client = new CommandLineTodoClient(HOST, DEFAULT_CLIENT_SERVICE_PORT);
    }

    @AfterEach
    public void tearDown() throws Exception {
        server.shutdown();
        client.shutdown();
    }

    private void startServer() throws InterruptedException {
        final CountDownLatch serverStartLatch = new CountDownLatch(1);
        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
                serverStartLatch.countDown();
            } catch (IOException e) {
                throw Exceptions.wrappedCause(e);
            }
        });

        serverThread.start();
        serverStartLatch.await(100, TimeUnit.MILLISECONDS);
    }

    static CreateBasicLabel createBasicLabel() {
        final CreateBasicLabel result = CommandBuilder.label()
                                                      .createLabel()
                                                      .setTitle(LABEL_TITLE)
                                                      .build();
        return result;
    }

    static CreateDraft createDraft() {
        final CreateDraft result = CommandBuilder.task()
                                                 .createDraft()
                                                 .build();
        return result;
    }

    static CreateBasicTask createBasicTask() {
        final CreateBasicTask result = CommandBuilder.task()
                                                     .createTask()
                                                     .setDescription(DESCRIPTION)
                                                     .build();
        return result;
    }

    static TaskId getWrongTaskId() {
        final TaskId result = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        return result;
    }

    static TaskLabelId getWrongTaskLabelId() {
        final TaskLabelId result = TaskLabelId.newBuilder()
                                              .setValue(newUuid())
                                              .build();
        return result;
    }
}
