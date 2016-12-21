/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.base.Response;
import org.spine3.client.CommandFactory;
import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;
import org.spine3.examples.todolist.client.builder.CommandsBuilder;
import org.spine3.examples.todolist.server.Server;
import org.spine3.examples.todolist.view.MyListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.server.storage.memory.InMemoryStorageFactory;
import org.spine3.time.ZoneOffsets;
import org.spine3.util.Exceptions;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.removeLabelFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static org.spine3.test.Tests.newUserId;

/**
 * @author Illia Shepilov
 */
class ClientShould {

    private static final String HOST = "localhost";
    private static TodoClient client;
    private static Server server;
    private DeleteTask deleteTask;
    private CreateBasicTask createTask;
    private UpdateTaskDescription updateTaskDescription;
    private CommandFactory commandFactory;

    @BeforeAll
    public static void initAll() throws InterruptedException {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        server = new Server(storageFactory);
        startServer();
        client = new BasicTodoClient(HOST, DEFAULT_CLIENT_SERVICE_PORT);
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        commandFactory = commandFactoryInstance();
        final UpdateTaskPriority updateTaskPriority = updateTaskPriorityInstance();
        final UpdateTaskDueDate updateTaskDueDate = updateTaskDueDateInstance();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance();
        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance();
        final CompleteTask completeTask = completeTaskInstance();
        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance();
        final ReopenTask reopenTask = reopenTaskInstance();
        final FinalizeDraft finalizeDraft = finalizeDraftInstance();
        deleteTask = deleteTaskInstance();
        createTask = CommandsBuilder.task()
                                    .createTask()
                                    .setDescription(DESCRIPTION)
                                    .build();
        updateTaskDescription = updateTaskDescriptionInstance();
    }

    @Test
    public void create_and_obtain_two_tasks() {
        final int expectedMessagesCount = 2;
        client.create(createTask);
        client.create(createTask);

        final List<TaskView> taskViews = client.getListView()
                                               .getMyList()
                                               .getItemsList();

        assertEquals(expectedMessagesCount, taskViews.size());
        assertEquals(DESCRIPTION, taskViews.get(0).getDescription());
        assertEquals(DESCRIPTION, taskViews.get(1).getDescription());
    }

    @Test
    public void create_and_update_task() {
        final int expectedListSize = 1;
        final String newDescription = "New task description.";
        client.create(createTask);
        updateTaskDescription = updateTaskDescriptionInstance(createTask.getId(), newDescription);
        client.update(updateTaskDescription);

        final MyListView view = client.getListView();
        assertEquals(expectedListSize, view.getMyList()
                                           .getItemsCount());
        assertEquals(newDescription, view.getMyList()
                                         .getItems(0)
                                         .getDescription());

    }

    private static void startServer() throws InterruptedException {
        final CountDownLatch serverStartLatch = new CountDownLatch(1);
        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
                server.awaitTermination();
                serverStartLatch.countDown();
            } catch (IOException e) {
                throw Exceptions.wrapped(e);
            }
        });

        serverThread.start();
        serverStartLatch.await(100, TimeUnit.MILLISECONDS);
    }

    private static void chekOkResponse(Response response) {
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    private static void checkOkTaskView(TaskId id, TaskView view) {
        assertEquals(id, view.getId());
        assertEquals(DESCRIPTION, view.getDescription());
    }

    private static CommandFactory commandFactoryInstance() {
        final CommandFactory result = CommandFactory.newBuilder()
                                                    .setActor(newUserId(newUuid()))
                                                    .setZoneOffset(ZoneOffsets.UTC)
                                                    .build();
        return result;
    }
}
