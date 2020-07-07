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

import io.spine.base.Environment;
import io.spine.base.Production;
import io.spine.base.Tests;
import io.spine.examples.todolist.server.Server;
import io.spine.examples.todolist.server.tasks.TasksContextFactory;
import io.spine.server.BoundedContext;
import io.spine.server.ServerEnvironment;
import io.spine.server.storage.StorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.server.Server.newServer;

public abstract class RdbmsServer {

    private static final String LOCAL_H2 = "jdbc:h2:mem:";

    public void run(String[] argus) throws IOException {
        ServerEnvironment environment = ServerEnvironment.instance();
        DbConnectionProperties connectionProperties = properties(argus);
        configureServerEnvironment(environment, connectionProperties);
    }

    private void
    configureServerEnvironment(ServerEnvironment serverEnvironment,
                               DbConnectionProperties connectionProperties) throws IOException {
        ConnectionUrl connectionUrl = composeUrl(connectionProperties);
        RdbmsStorageFactorySupplier supplier =
                new RdbmsStorageFactorySupplier(connectionUrl, connectionProperties.credentials());

        StorageFactory factory = supplier.get();
        serverEnvironment.use(factory, Production.class)
                         .use(InMemoryTransportFactory.newInstance(), Production.class);
        BoundedContext context = TasksContextFactory.create();
        Server server = newServer(DEFAULT_CLIENT_SERVICE_PORT, context);
        server.start();
    }

    protected abstract DbConnectionProperties properties(String[] commandLineArguments);

    protected abstract ConnectionUrl connectionUrl(DbConnectionProperties properties);

    private ConnectionUrl composeUrl(DbConnectionProperties properties) {
        Environment environment = Environment.instance();
        DbConnectionProperties props = environment.is(Tests.class)
                                       ? properties
                                       : properties.toBuilder()
                                                   .setUrlPrefix(LOCAL_H2)
                                                   .build();
        ConnectionUrl result = connectionUrl(props);
        return result;
    }
}
