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

package io.spine.examples.todolist.client;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.spine.base.CommandMessage;
import io.spine.base.EntityState;
import io.spine.base.Identifier;
import io.spine.client.Client;
import io.spine.client.Subscription;
import io.spine.core.UserId;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.Task;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.TaskLabel;
import io.spine.examples.todolist.tasks.TaskLabels;
import io.spine.examples.todolist.tasks.view.LabelView;
import io.spine.examples.todolist.tasks.view.TaskView;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.base.Identifier.newUuid;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * An implementation of the TodoList gRPC client.
 */
final class TodoClientImpl implements SubscribingTodoClient {

    private static final int TIMEOUT = 10;

    private final Client client;
    private final UserId user;

    /**
     * Construct the client connecting to server at {@code host:port}.
     */
    TodoClientImpl(String host, int port) {
        ManagedChannel channel = initChannel(host, port);
        this.client = initClient(channel);
        this.user = userId();
    }

    private static Client initClient(ManagedChannel channel) {
        Client client = Client.usingChannel(channel)
                              .shutdownTimout(TIMEOUT, SECONDS)
                              .build();
        return client;
    }

    @SuppressWarnings("CheckReturnValue")
    // Rely on client shutdown for cancelling the subscriptions in this simple client.
    @Override
    public void postCommand(CommandMessage cmd) {
        client.onBehalfOf(user)
              .command(cmd)
              .post();
    }

    @Override
    public List<TaskView> taskViews() {
        ImmutableList<TaskView> result =
                client.onBehalfOf(user)
                      .select(TaskView.class)
                      .run();
        return result;
    }

    @Override
    public List<Task> tasks() {
        return getByType(Task.class);
    }

    @Override
    public List<TaskLabel> labels() {
        return getByType(TaskLabel.class);
    }

    @Override
    public TaskLabels labelsOf(TaskId taskId) {
        Optional<TaskLabels> labels = findById(TaskLabels.class, taskId);
        TaskLabels result = labels.orElse(TaskLabels.newBuilder()
                                                            .setTaskId(taskId)
                                                            .vBuild());
        return result;
    }

    @Override
    public Optional<LabelView> labelView(LabelId id) {
        Optional<LabelView> result = findById(LabelView.class, id);
        return result;
    }

    @Nullable
    @Override
    public TaskLabel labelOr(LabelId id, @Nullable TaskLabel other) {
        Optional<TaskLabel> found = findById(TaskLabel.class, id);
        return found.orElse(other);
    }

    @Override
    public Subscription subscribeToTasks(StreamObserver<TaskView> observer) {
        Subscription subscription =
                client.onBehalfOf(user)
                      .subscribeTo(TaskView.class)
                      .observe(observer::onNext)
                      .post();
        return subscription;
    }

    @Override
    public void unSubscribe(Subscription subscription) {
        client.cancel(subscription);
    }

    @Override
    public void shutdown() {
        client.shutdown();
    }

    /**
     * Retrieves all the entities of the given type.
     *
     * @param cls
     *         the state class of the desired entities
     * @param <S>
     *         the entity state type
     * @return all the messages of the given type present in the system
     */
    private <S extends EntityState> List<S> getByType(Class<S> cls) {
        ImmutableList<S> result =
                client.onBehalfOf(user)
                      .select(cls)
                      .run();
        return result;
    }

    private <S extends EntityState> Optional<S> findById(Class<S> messageClass, Message id) {
        ImmutableList<S> messages =
                client.onBehalfOf(user)
                      .select(messageClass)
                      .byId(id)
                      .run();
        checkState(messages.size() <= 1,
                   "Too many %s-s with ID %s:%s %s",
                   messageClass.getSimpleName(), Identifier.toString(id),
                   System.lineSeparator(), messages);

        Optional<S> result = messages.stream()
                                     .findFirst();
        return result;
    }

    private static ManagedChannel initChannel(String host, int port) {
        ManagedChannel result = ManagedChannelBuilder.forAddress(host, port)
                                                     .usePlaintext()
                                                     .build();
        return result;
    }

    private static UserId userId() {
        return UserId
                    .newBuilder()
                    .setValue(newUuid())
                    .vBuild();
    }
}
