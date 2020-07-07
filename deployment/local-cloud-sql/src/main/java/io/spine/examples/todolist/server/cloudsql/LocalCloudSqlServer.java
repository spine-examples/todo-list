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

package io.spine.examples.todolist.server.cloudsql;

import io.spine.examples.todolist.rdbms.ConnectionProperties;
import io.spine.examples.todolist.server.Server;

import java.io.IOException;
import java.util.Optional;

/**
 * A local {@link Server} using {@link io.spine.server.storage.jdbc.JdbcStorageFactory
 * JdbcStorageFactory} with {@code Cloud SQL} instance as a data source.
 *
 * <p>To run the server successfully (for the detailed explanation see {@code README.md}):
 * <ol>
 *     <li>Install {@code gcloud} tool.
 *     <li>Authenticate using {@code gcloud}. {@code Cloud SQL client} role is required.
 *     <li>Create a Cloud SQL instance.
 *     <li>Create a database.
 * </ol>
 *
 * <p>To run the server from a command-line run the command as follows:
 * {@code gradle :local-cloud-sql:runServer -Pconf=instance_connection_name,db_name,username,password}
 *
 * <p>If the parameters were not specified to a command or the server was ran directly,
 * a configuration from resources is going to be used instead, see {@code cloud-sql.properties}.
 *
 * <p>The server exposes its {@code gRPC API} at
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * @see <a href="https://cloud.google.com/sdk/gcloud/">gcloud tool</a>
 * @see <a href="https://cloud.google.com/sql/docs/mysql/quickstart">Cloud SQL instance
 *         creation</a>
 */
public class LocalCloudSqlServer extends CloudSqlServer {

    public static void main(String[] args) throws IOException {
        LocalCloudSqlServer server = new LocalCloudSqlServer();
        server.start(args);
    }

    /**
     * {@inheritDoc}
     *
     * <p>To launch a local Cloud SQL server, arguments must specify the following:
     * <ol>
     *     <li>name of the instance to connect to;
     *     <li>name of the database to connect to;
     *     <li>name of the database user;
     *     <li>a user password;
     * </ol>
     *
     * @param args command line arguments specified to launch the application
     * @return
     */
    @Override
    protected Optional<ConnectionProperties> connectionProperties(String[] args) {
        if (args.length == 4) {
            ConnectionProperties result = ConnectionProperties.newBuilder()
                                                              .setInstanceName(args[0])
                                                              .setDbName(args[1])
                                                              .setUsername(args[2])
                                                              .setPassword(args[3])
                                                              .build();
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }
}
