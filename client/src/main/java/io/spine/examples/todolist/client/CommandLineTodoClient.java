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
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.spine.base.Command;
import io.spine.client.ActorRequestFactory;
import io.spine.client.Query;
import io.spine.client.QueryResponse;
import io.spine.client.grpc.CommandServiceGrpc;
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
import io.spine.server.BoundedContext;
import io.spine.server.QueryService;
import io.spine.time.ZoneOffsets;
import io.spine.users.UserId;
import io.spine.util.Exceptions;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static io.spine.base.Identifier.newUuid;

/**
 * Implementation of the command line gRPC client.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("OverlyCoupledClass")
public class CommandLineTodoClient implements TodoClient {

    public static final String LOCALHOST = "localhost";
    private static final int TIMEOUT = 10;

    private final ManagedChannel channel;
    private final QueryService queryService;
    private final CommandServiceGrpc.CommandServiceBlockingStub commandService;
    private final ActorRequestFactory requestFactory;

    /**
     * Construct the client connecting to server at {@code host:port}.
     */
    public CommandLineTodoClient(String host, int port, BoundedContext boundedContext) {
        this.requestFactory = actorRequestFactoryInstance();
        this.channel = initChannel(host, port);
        this.commandService = CommandServiceGrpc.newBlockingStub(channel);
        this.queryService = QueryService.newBuilder()
                                        .add(boundedContext)
                                        .build();
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
        try {
            final Query query = requestFactory.query()
                                              .all(MyListView.class);
            final EventStreamObserver responseObserver = new EventStreamObserver();
            queryService.read(query, responseObserver);

            final boolean isEmpty = responseObserver.queryResponses.isEmpty();
            if (isEmpty) {
                return MyListView.getDefaultInstance();
            }

            MyListView result = responseObserver.queryResponses.get(0)
                                                               .unpack(MyListView.class);
            return result;
        } catch (InvalidProtocolBufferException e) {
            throw Exceptions.illegalStateWithCauseOf(e);
        }
    }

    @Override
    public List<LabelledTasksView> getLabelledTasksView() {
        try {
            final Query query = requestFactory.query()
                                              .all(LabelledTasksView.class);
            final EventStreamObserver responseObserver = new EventStreamObserver();
            queryService.read(query, responseObserver);
            final List<LabelledTasksView> result = newArrayList();

            for (Any any : responseObserver.queryResponses) {
                final LabelledTasksView labelledView = any.unpack(LabelledTasksView.class);
                result.add(labelledView);
            }

            return result;
        } catch (InvalidProtocolBufferException e) {
            throw Exceptions.illegalStateWithCauseOf(e);
        }
    }

    @Override
    public DraftTasksView getDraftTasksView() {
        try {
            final Query query = requestFactory.query()
                                              .all(DraftTasksView.class);
            final EventStreamObserver responseObserver = new EventStreamObserver();
            queryService.read(query, responseObserver);

            final boolean isEmpty = responseObserver.queryResponses.isEmpty();
            if (isEmpty) {
                return DraftTasksView.getDefaultInstance();
            }
            final DraftTasksView result =
                    responseObserver.queryResponses.get(0)
                                                   .unpack(DraftTasksView.class);
            return result;
        } catch (InvalidProtocolBufferException e) {
            throw Exceptions.illegalStateWithCauseOf(e);
        }
    }

    @Override
    public void shutdown() {
        try {
            channel.shutdown()
                   .awaitTermination(TIMEOUT, SECONDS);
        } catch (InterruptedException e) {
            throw Exceptions.illegalStateWithCauseOf(e);
        }
    }

    private static ManagedChannel initChannel(String host, int port) {
        final ManagedChannel result = ManagedChannelBuilder
                .forAddress(host, port)
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

    private static class EventStreamObserver implements StreamObserver<QueryResponse> {

        private List<Any> queryResponses;

        @Override
        public void onNext(QueryResponse value) {
            queryResponses = value.getMessagesList();
        }

        @Override
        public void onError(Throwable t) {
            log().error("Exception is occurred during receiving the notifications.", t);
        }

        @Override
        public void onCompleted() {
            log().info("Receiving notifications is completed.");
        }

        private enum LogSingleton {
            INSTANCE;

            @SuppressWarnings("NonSerializableFieldInSerializableClass")
            private final Logger value = LoggerFactory.getLogger(EventStreamObserver.class);
        }

        private static Logger log() {
            return LogSingleton.INSTANCE.value;
        }
    }
}
