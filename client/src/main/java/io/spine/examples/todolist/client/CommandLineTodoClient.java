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

package io.spine.examples.todolist.client;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.spine.client.ActorRequestFactory;
import io.spine.client.Query;
import io.spine.client.grpc.CommandServiceGrpc;
import io.spine.client.grpc.QueryServiceGrpc;
import io.spine.core.Command;
import io.spine.core.UserId;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.time.ZoneOffsets;

import java.util.List;

import static io.spine.Identifier.newUuid;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

/**
 * Implementation of the command line gRPC client.
 *
 * @author Illia Shepilov
 * @author Dmitry Ganzha
 */
@SuppressWarnings("OverlyCoupledClass")
public class CommandLineTodoClient implements TodoClient {

    public static final String HOST = "localhost";
    private static final int TIMEOUT = 10;

    private final ManagedChannel channel;
    private final QueryServiceGrpc.QueryServiceBlockingStub queryService;
    private final CommandServiceGrpc.CommandServiceBlockingStub commandService;
    private final ActorRequestFactory requestFactory;

    /**
     * Construct the client connecting to server at {@code host:port}.
     */
    public CommandLineTodoClient(String host, int port) {
        this.requestFactory = actorRequestFactoryInstance();
        this.channel = initChannel(host, port);
        this.commandService = CommandServiceGrpc.newBlockingStub(channel);
        this.queryService = QueryServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void create(CreateBasicTask cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void create(CreateBasicLabel cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void create(CreateDraft cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void update(UpdateTaskDescription cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void update(UpdateTaskDueDate cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void update(UpdateTaskPriority cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void update(UpdateLabelDetails cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void delete(DeleteTask cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void removeLabel(RemoveLabelFromTask cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void assignLabel(AssignLabelToTask cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void reopen(ReopenTask cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void restore(RestoreDeletedTask cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void complete(CompleteTask cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void finalize(FinalizeDraft cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public MyListView getMyListView() {
        final Query query = requestFactory.query()
                                          .all(MyListView.class);
        final List<Any> messages = queryService.read(query)
                                               .getMessagesList();
        return messages.isEmpty()
               ? MyListView.getDefaultInstance()
               : convertAnyToMessage(messages.get(0), MyListView.class);
    }

    @Override
    public List<LabelledTasksView> getLabelledTasksView() {
        final Query query = requestFactory.query()
                                          .all(LabelledTasksView.class);
        final List<Any> messages = queryService.read(query)
                                               .getMessagesList();
        final List<LabelledTasksView> result = messages
                .stream()
                .map(any -> convertAnyToMessage(any, LabelledTasksView.class))
                .collect(toList());

        return result;
    }

    @Override
    public DraftTasksView getDraftTasksView() {
        final Query query = requestFactory.query()
                                          .all(DraftTasksView.class);
        final List<Any> messages = queryService.read(query)
                                               .getMessagesList();
        return messages.isEmpty()
               ? DraftTasksView.getDefaultInstance()
               : convertAnyToMessage(messages.get(0), DraftTasksView.class);
    }

    @Override
    public List<Task> getTasks() {
        final Query query = requestFactory.query()
                                          .all(Task.class);
        final List<Any> messages = queryService.read(query)
                                               .getMessagesList();
        final List<Task> result = messages
                .stream()
                .map(any -> convertAnyToMessage(any, Task.class))
                .collect(toList());

        return result;
    }

    @Override
    public void shutdown() {
        try {
            channel.shutdown()
                   .awaitTermination(TIMEOUT, SECONDS);
        } catch (InterruptedException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static ManagedChannel initChannel(String host, int port) {
        final ManagedChannel result = ManagedChannelBuilder.forAddress(host, port)
                                                           .usePlaintext(true)
                                                           .build();
        return result;
    }

    private static ActorRequestFactory actorRequestFactoryInstance() {
        final UserId userId = UserId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        final ActorRequestFactory result = ActorRequestFactory.newBuilder()
                                                              .setActor(userId)
                                                              .setZoneOffset(ZoneOffsets.UTC)
                                                              .build();
        return result;
    }

    private <M extends Message> M convertAnyToMessage(Any any, Class<M> messageClass) {
        try {
            return any.unpack(messageClass);
        } catch (InvalidProtocolBufferException e) {
            throw illegalStateWithCauseOf(e);
        }
    }
}
