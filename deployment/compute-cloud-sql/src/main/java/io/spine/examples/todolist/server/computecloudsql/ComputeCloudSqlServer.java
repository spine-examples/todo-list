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

package io.spine.examples.todolist.server.computecloudsql;

import com.google.common.annotations.VisibleForTesting;
import io.spine.base.Production;
import io.spine.examples.todolist.DbCredentials;
import io.spine.examples.todolist.rdbms.DbConnectionProperties;
import io.spine.examples.todolist.rdbms.RdbmsStorageFactorySupplier;
import io.spine.examples.todolist.server.Server;
import io.spine.examples.todolist.server.cloudsql.CloudSqlServers;
import io.spine.examples.todolist.server.tasks.TasksContextFactory;
import io.spine.server.BoundedContext;
import io.spine.server.ServerEnvironment;
import io.spine.server.storage.StorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.server.Server.newServer;

/**
 * A Compute Engine {@link Server} using {@link io.spine.server.storage.jdbc.JdbcStorageFactory
 * JdbcStorageFactory} for working with Cloud SQL.
 *
 * <p>If you want to run this server locally, use {@code LocalCloudSqlServer} instead.
 *
 * <p>For the details, see the {@code README.md}.
 */
public class ComputeCloudSqlServer {

    /** Prevents instantiation of this class. */
    private ComputeCloudSqlServer() {
    }

    public static void main(String[] args) throws IOException {
        ServerEnvironment serverEnvironment = ServerEnvironment.instance();

        serverEnvironment.use(createStorageFactory(), Production.class)
                         .use(InMemoryTransportFactory.newInstance(), Production.class);

        BoundedContext context = createContext();
        Server server = newServer(DEFAULT_CLIENT_SERVICE_PORT, context);
        server.start();
    }

    @VisibleForTesting
    static BoundedContext createContext() {
        return TasksContextFactory.create();
    }

    private static StorageFactory createStorageFactory() {
        DbConnectionProperties properties = CloudSqlServers.propertiesFromResourceFile();
        String dbUrl = CloudSqlServers.dbUrl(properties);

        DbCredentials credentials = properties.credentials();

        RdbmsStorageFactorySupplier storageFactory = new RdbmsStorageFactorySupplier(dbUrl,
                                                                                     credentials);
        return storageFactory.get();
    }
}
