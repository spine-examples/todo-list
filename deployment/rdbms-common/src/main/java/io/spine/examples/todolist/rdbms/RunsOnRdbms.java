/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.rdbms;

import io.spine.base.Production;
import io.spine.examples.todolist.server.Server;
import io.spine.examples.todolist.server.tasks.TasksContextFactory;
import io.spine.server.BoundedContext;
import io.spine.server.ServerEnvironment;
import io.spine.server.transport.memory.InMemoryTransportFactory;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.server.Server.newServer;

/**
 * An abstract base for To-Do List application servers that are backed by a relational storage.
 *
 * <p>To specify the relational storage, override {@link #storage(ConnectionProperties)}.
 * {@code RelationalStorage} is configured using {@code ConnectionProperties}. By default,
 * {@code ConnectionProperties} are {@linkplain #connectionProperties() parsed from system
 * properties}. This behavior can be changed by overriding {@code connectionProperties()} to,
 * for example, parse the configuration from a local file.
 *
 * <p>To run the server, use {@link #start()}.
 */
public abstract class RunsOnRdbms {

    /**
     * Launches the To-Do List application server.
     */
    public final void start() throws IOException {
        ConnectionProperties properties = connectionProperties();
        RelationalStorage storage = storage(properties);
        ServerEnvironment
                .when(Production.class)
                .use(storage.storageFactory())
                .use(InMemoryTransportFactory.newInstance());
        BoundedContext context = TasksContextFactory.create();
        Server server = newServer(DEFAULT_CLIENT_SERVICE_PORT, context);
        server.start();
    }

    /**
     * Creates an instance of a {@code RelationalStorage} with the specified connection properties.
     */
    protected abstract RelationalStorage storage(ConnectionProperties properties);

    /**
     * Parses the connection properties using {@code System.getProperty()}
     * for each connection property.
     *
     * @return connection properties
     */
    protected ConnectionProperties connectionProperties() {
        ConnectionProperties result = ConnectionProperties.fromSystemProperties();
        return result;
    }
}
