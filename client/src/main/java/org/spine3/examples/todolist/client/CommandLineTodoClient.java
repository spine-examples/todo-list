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

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.base.Command;
import org.spine3.client.ActorRequestFactory;
import org.spine3.client.Query;
import org.spine3.client.QueryResponse;
import org.spine3.client.grpc.CommandServiceGrpc;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.commands.ReopenTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.q.projection.DraftTasksView;
import org.spine3.examples.todolist.q.projection.LabelledTasksView;
import org.spine3.examples.todolist.q.projection.MyListView;
import org.spine3.server.BoundedContext;
import org.spine3.server.QueryService;
import org.spine3.time.ZoneOffsets;
import org.spine3.users.UserId;
import org.spine3.util.Exceptions;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.spine3.base.Identifiers.newUuid;

/**
 * Implementation of the command line gRPC client.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("OverlyCoupledClass")
public class CommandLineTodoClient implements TodoClient {

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
            throw Exceptions.wrappedCause(e);
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
            throw Exceptions.wrappedCause(e);
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
            throw Exceptions.wrappedCause(e);
        }
    }

    @Override
    public void shutdown() {
        try {
            channel.shutdown()
                   .awaitTermination(TIMEOUT, SECONDS);
        } catch (InterruptedException e) {
            throw Exceptions.wrappedCause(e);
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
