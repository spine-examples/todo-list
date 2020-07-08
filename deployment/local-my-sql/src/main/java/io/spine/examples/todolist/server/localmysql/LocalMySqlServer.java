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

package io.spine.examples.todolist.server.localmysql;

import io.spine.examples.todolist.rdbms.ConnectionProperties;
import io.spine.examples.todolist.rdbms.ConnectionUrl;
import io.spine.examples.todolist.rdbms.RunsOnRdbms;
import io.spine.examples.todolist.server.Server;

import java.io.IOException;

/**
 * A local {@link Server} using {@link io.spine.server.storage.jdbc.JdbcStorageFactory
 * JdbcStorageFactory}, {@code MySQL} in particular.
 *
 * <p>To run the server from a command-line run the command as follows:
 * {@code gradle :local-my-sql:runServer -Pconf=db_name,username,password}
 *
 * <p>If the parameters were not specified to a command or the server was ran directly,
 * a configuration file from resources is going to be used instead, see
 * {@code jdbc-storage.properties}.
 *
 * <p>As the server uses {@code MySQL}, the database with the specified name should be created
 * and the username and password should be correct.
 *
 * <p>The server exposes its {@code gRPC API} at
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 */
public final class LocalMySqlServer extends RunsOnRdbms {

    private static final ConnectionProperties DB_PROPERTIES =
            ConnectionProperties.fromResourceFile("jdbc-storage.properties");

    public static void main(String[] args) throws IOException {
        LocalMySqlServer server = new LocalMySqlServer();
        server.start(args);
    }

    @Override
    protected ConnectionProperties properties(String[] args) {
        if (args.length == 3) {
            ConnectionProperties result = ConnectionProperties.newBuilder()
                                                              .setDbName(args[0])
                                                              .setUsername(args[1])
                                                              .setPassword(args[2])
                                                              .build();
            return result;
        } else {
            return DB_PROPERTIES;
        }
    }

    @Override
    protected ConnectionUrl connectionUrl(ConnectionProperties properties) {
        LocalMySqlConnectionUrl result = new LocalMySqlConnectionUrl(properties);
        return result;
    }
}
