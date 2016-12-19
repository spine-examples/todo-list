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
import org.spine3.base.Command;
import org.spine3.base.Response;
import org.spine3.client.CommandFactory;
import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.Task;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;
import org.spine3.examples.todolist.server.Server;
import org.spine3.server.storage.memory.InMemoryStorageFactory;
import org.spine3.time.ZoneOffsets;
import org.spine3.util.Exceptions;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
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
    private static Client client;
    private static Server server;
    private Command updateTaskDescriptionCmd;
    private Command updateTaskPriorityCmd;
    private Command updateTaskDueDateCmd;
    private Command assignLabelToTaskCmd;
    private Command removeLabelFromTaskCmd;
    private Command completeTaskCmd;
    private Command deleteTaskCmd;
    private Command restoreDeletedTaskCmd;
    private Command reopenTaskCmd;
    private Command finalizeDraftCmd;

    @BeforeAll
    public static void initAll() throws InterruptedException {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        server = new Server(storageFactory);
        startServer();
        client = new Client(HOST, DEFAULT_CLIENT_SERVICE_PORT);
        client.subscribe(Task.getDescriptor());
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        final CommandFactory commandFactory = commandFactoryInstance();
        final UpdateTaskDescription updateTaskDescription = updateTaskDescriptionInstance();
        final UpdateTaskPriority updateTaskPriority = updateTaskPriorityInstance();
        final UpdateTaskDueDate updateTaskDueDate = updateTaskDueDateInstance();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance();
        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance();
        final CompleteTask completeTask = completeTaskInstance();
        final DeleteTask deleteTask = deleteTaskInstance();
        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance();
        final ReopenTask reopenTask = reopenTaskInstance();
        final FinalizeDraft finalizeDraft = finalizeDraftInstance();
        updateTaskDescriptionCmd = commandFactory.create(updateTaskDescription);
        updateTaskPriorityCmd = commandFactory.create(updateTaskPriority);
        updateTaskDueDateCmd = commandFactory.create(updateTaskDueDate);
        assignLabelToTaskCmd = commandFactory.create(assignLabelToTask);
        removeLabelFromTaskCmd = commandFactory.create(removeLabelFromTask);
        completeTaskCmd = commandFactory.create(completeTask);
        deleteTaskCmd = commandFactory.create(deleteTask);
        restoreDeletedTaskCmd = commandFactory.create(restoreDeletedTask);
        reopenTaskCmd = commandFactory.create(reopenTask);
        finalizeDraftCmd = commandFactory.create(finalizeDraft);
    }

    @Test
    public void successfully_send_update_task_description_command() {
        Response response = client.execute(updateTaskDescriptionCmd);
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    @Test
    public void successfully_send_update_task_priority_command() {
        Response response = client.execute(updateTaskPriorityCmd);
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    @Test
    public void successfully_send_update_task_due_date_command() {
        Response response = client.execute(updateTaskDueDateCmd);
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    @Test
    public void successfully_send_assign_label_to_task_command() {
        Response response = client.execute(assignLabelToTaskCmd);
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    @Test
    public void successfully_send_remove_label_from_task_command() {
        Response response = client.execute(removeLabelFromTaskCmd);
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    @Test
    public void successfully_send_complete_task_command() {
        Response response = client.execute(completeTaskCmd);
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    @Test
    public void successfully_send_delete_task_command() {
        Response response = client.execute(deleteTaskCmd);
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    @Test
    public void successfully_send_restore_deleted_task_command() {
        Response response = client.execute(restoreDeletedTaskCmd);
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    @Test
    public void successfully_send_reopen_task_command() {
        Response response = client.execute(reopenTaskCmd);
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    @Test
    public void successfully_send_finalize_draft_command() {
        Response response = client.execute(finalizeDraftCmd);
        assertEquals(Response.StatusCase.OK, response.getStatusCase());
        assertEquals(0, response.getError()
                                .getAttributesCount());
        assertEquals(0, response.getFailure()
                                .getAttributesCount());
    }

    private static void startServer() throws InterruptedException {
        final CountDownLatch serverStartLatch = new CountDownLatch(3);
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

    private CommandFactory commandFactoryInstance() {
        return CommandFactory.newBuilder()
                             .setActor(newUserId(newUuid()))
                             .setZoneOffset(ZoneOffsets.UTC)
                             .build();
    }
}
