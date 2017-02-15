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
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.aggregate.TaskAggregateRoot;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.client.builder.CommandBuilder;
import org.spine3.examples.todolist.context.TodoListBoundedContext;
import org.spine3.examples.todolist.q.projection.LabelledTasksView;
import org.spine3.examples.todolist.server.Server;
import org.spine3.server.BoundedContext;
import org.spine3.util.Exceptions;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestLabelCommandFactory.LABEL_TITLE;

/**
 * @author Illia Shepilov
 */
abstract class CommandLineTodoClientTest {

    static final String UPDATED_TASK_DESCRIPTION = "New task description.";
    private static final String HOST = "localhost";
    private Server server;
    private TodoClient client;

    @BeforeEach
    public void setUp() throws InterruptedException {
        final BoundedContext boundedContext = TodoListBoundedContext.createTestInstance();
        TaskAggregateRoot.injectBoundedContext(boundedContext);

        server = new Server(boundedContext);
        startServer();
        client = new CommandLineTodoClient(HOST, DEFAULT_CLIENT_SERVICE_PORT, boundedContext);
    }

    @AfterEach
    public void tearDown() throws Exception {
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

    static TaskId createWrongTaskId() {
        final TaskId result = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        return result;
    }

    static LabelId createWrongTaskLabelId() {
        final LabelId result = LabelId.newBuilder()
                                      .setValue(newUuid())
                                      .build();
        return result;
    }

    static LabelledTasksView getLabelledTasksView(List<LabelledTasksView> tasksViewList) {
        LabelledTasksView result = LabelledTasksView.getDefaultInstance();

        for (LabelledTasksView labelledView : tasksViewList) {
            final boolean isEmpty = labelledView.getLabelId()
                                                .getValue()
                                                .isEmpty();
            if (!isEmpty) {
                result = labelledView;
            }
        }

        return result;
    }

    CreateBasicTask createTask() {
        final CreateBasicTask createTask = createBasicTask();
        getClient().create(createTask);
        return createTask;
    }

    CreateBasicLabel createLabel() {
        final CreateBasicLabel createLabel = createBasicLabel();
        getClient().create(createLabel);
        return createLabel;
    }

    public TodoClient getClient() {
        return client;
    }
}
