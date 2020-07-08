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
import io.spine.server.storage.StorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.server.Server.newServer;

/**
 * A server implementation backed by a relational database.
 *
 * <p>Run the server by passing the {@code args} from the {@code main} method to
 * {@link #start(String[])}.
 */
public abstract class RunsOnRdbms {

    /**
     * Starts the server.
     *
     * @param args
     *         command line arguments
     */
    public final void start(String[] args) throws IOException {
        ConnectionProperties connectionProperties = properties(args);
        StorageFactory factory = storageFactory(connectionProperties);

        ServerEnvironment serverEnvironment = ServerEnvironment.instance();
        serverEnvironment.use(factory, Production.class)
                         .use(InMemoryTransportFactory.newInstance(), Production.class);
        BoundedContext context = TasksContextFactory.create();
        Server server = newServer(DEFAULT_CLIENT_SERVICE_PORT, context);
        server.start();
    }

    /**
     * Extracts the connection properties from the command line arguments.
     *
     * @param commandLineArguments
     *         arguments used to launch the server
     * @return properties for connecting to a relational database
     */
    protected abstract ConnectionProperties properties(String[] commandLineArguments);

    /**
     * Constructs a DB connection URL based on the specified properties.
     */
    protected abstract ConnectionUrl connectionUrl(ConnectionProperties properties);

    private StorageFactory storageFactory(ConnectionProperties connectionProperties) {
        ConnectionUrl connectionUrl = connectionUrl(connectionProperties);
        RelationalStorage supplier =
                new RelationalStorage(connectionUrl, connectionProperties.credentials());

        return supplier.get();
    }
}
