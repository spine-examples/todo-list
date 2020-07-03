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

package io.spine.examples.todolist.server;

import com.google.common.annotations.VisibleForTesting;
import io.spine.base.Production;
import io.spine.examples.todolist.rdbms.CloudSqlServers;
import io.spine.examples.todolist.rdbms.DbProperties;
import io.spine.examples.todolist.rdbms.RdbmsStorageFactorySupplier;
import io.spine.examples.todolist.server.tasks.TasksContextFactory;
import io.spine.server.BoundedContext;
import io.spine.server.ServerEnvironment;
import io.spine.server.storage.StorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.server.Server.newServer;

/**
 * A local {@link Server} using {@link io.spine.server.storage.jdbc.JdbcStorageFactory
 * JdbcStorageFactory} with {@code Cloud SQL} instance as a data source.
 *
 * <p>To run the server successfully (for the detailed explanation see {@code README.md}):
 * <ol>
 * <li>Install {@code gcloud} tool.</li>
 * <li>Authenticate using {@code gcloud}. {@code Cloud SQL client} role is required.</li>
 * <li>Create a Cloud SQL instance.</li>
 * <li>Create a database.</li>
 * </ol>
 *
 * <p>To run the server from a command-line run the command as follows:
 * {@code gradle :local-cloud-sql:runServer -Pconf=instance_connection_name,db_name,username,password}
 *
 * <p>If the parameters were not specified to a command or the server was ran directly,
 * {@linkplain #defaultArguments() default arguments} will be used.
 * The arguments are stored in the properties file {@code cloud-sql.properties}.
 *
 * <p>The server exposes its {@code gRPC API} at
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * @see <a href="https://cloud.google.com/sdk/gcloud/">gcloud tool</a>
 * @see <a href="https://cloud.google.com/sql/docs/mysql/quickstart">Cloud SQL instance
 *         creation</a>
 */
public class LocalCloudSqlServer {

    private static final DbProperties DB_PROPERTIES = CloudSqlServers.propertiesFromResourceFile();

    /** Prevents instantiation of this class. */
    private LocalCloudSqlServer() {
    }

    public static void main(String[] args) throws IOException {
        DbProperties properties = properties(args);

        ServerEnvironment serverEnvironment = ServerEnvironment.instance();
        serverEnvironment.use(createStorageFactory(properties), Production.class)
                         .use(InMemoryTransportFactory.newInstance(), Production.class);

        BoundedContext context = createContext();
        Server server = newServer(DEFAULT_CLIENT_SERVICE_PORT, context);
        server.start();
    }

    @VisibleForTesting
    static DbProperties properties(String[] args) {
        if (args.length == 4) {
            DbProperties result = DbProperties.newBuilder()
                                              .setInstanceName(args[0])
                                              .setDbName(args[1])
                                              .setUsername(args[2])
                                              .setPassword(args[3])
                                              .build();
            return result;
        } else {
            return DB_PROPERTIES;
        }
    }

    private static StorageFactory createStorageFactory(DbProperties props) {
        String dbUrl = CloudSqlServers.dbUrl(props);
        RdbmsStorageFactorySupplier supplier =
                new RdbmsStorageFactorySupplier(dbUrl, props.username(), props.password());
        return supplier.get();
    }

    @VisibleForTesting
    static BoundedContext createContext() {
        return TasksContextFactory.create();
    }
}
