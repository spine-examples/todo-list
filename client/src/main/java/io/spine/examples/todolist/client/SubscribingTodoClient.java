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

import io.grpc.stub.StreamObserver;
import io.spine.client.Subscription;
import io.spine.examples.todolist.q.projection.TaskView;

/**
 * A TodoList gRPC client able to make calls to the {@code SubscriptionService}.
 */
public interface SubscribingTodoClient extends TodoClient {

    /**
     * Subscribes the given {@code observer} onto the updates of the {@link TaskView} entity.
     *
     * @param observer
     *         the result observer
     * @return the new {@link Subscription}
     */
    Subscription subscribeToTasks(StreamObserver<TaskView> observer);

    /**
     * Cancels the given {@code subscription}.
     *
     * @param subscription
     *         the subscription to cancel
     */
    void unSubscribe(Subscription subscription);

    /**
     * Creates a new instance of {@code SubscribingTodoClient}.
     *
     * @param host
     *         the host of the server to connect to
     * @param port
     *         the port of the server to connect to
     * @return new subscribing TodoList client
     */
    static SubscribingTodoClient instance(String host, int port) {
        return new TodoClientImpl(host, port);
    }
}
