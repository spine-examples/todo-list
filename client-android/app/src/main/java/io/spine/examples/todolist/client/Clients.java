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

import io.spine.client.ConnectionConstants;

/**
 * A factory of the TodoList clients.
 */
public final class Clients {

    /**
     * The default host of a gRPC server to connect to.
     *
     * <p>In debug mode this value may be equal to {@code 10.0.2.2} which is the loopback IP for
     * an Android emulator pointing to the machine localhost.
     *
     * <p>See
     * <a href="https://developer.android.com/studio/run/emulator-networking.html">the official doc
     * </a> for more info.
     */
    private static final String HOST = "10.0.2.2";

    /**
     * The default port of a gRPC server to connect to.
     *
     * <p>In debug mode may be equal to {@link ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT}.
     */
    private static final int PORT = ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

    // Prevent instantiation.
    private Clients() {}

    /**
     * Retrieves an instance of {@link SubscribingTodoClient} connecting to the gRPC server at
     * {@link #HOST}{@code :}{@link #PORT}.
     *
     * <p>Do not call
     * {@link io.spine.examples.todolist.client.TodoClient#shutdown() shutdown()} on the instances
     * returned by this method.
     *
     * @return an instance of subscribing TodoList client
     */
    public static SubscribingTodoClient subscribingInstance() {
        return ClientSingleton.INSTANCE.value;
    }

    /**
     * The application-wide singleton of the {@link SubscribingTodoClient}.
     *
     * <p>The client connects to the server at {@link #HOST}{@code :}{@link #PORT}.
     */
    private enum ClientSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final SubscribingTodoClient value = SubscribingTodoClient.instance(HOST, PORT);
    }
}
