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

import com.google.common.annotations.VisibleForTesting;
import io.spine.base.Production;
import io.spine.examples.todolist.rdbms.ConnectionUrl;
import io.spine.examples.todolist.rdbms.DbCredentials;
import io.spine.examples.todolist.rdbms.DbConnectionProperties;
import io.spine.examples.todolist.rdbms.DbUrlPrefix;
import io.spine.examples.todolist.rdbms.RdbmsServer;
import io.spine.examples.todolist.rdbms.RdbmsStorageFactorySupplier;
import io.spine.examples.todolist.server.Server;
import io.spine.examples.todolist.server.tasks.TasksContextFactory;
import io.spine.server.BoundedContext;
import io.spine.server.ServerEnvironment;
import io.spine.server.storage.StorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.server.Server.newServer;
import static java.lang.String.format;

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
public final class LocalMySqlServer extends RdbmsServer {

    private static final DbConnectionProperties DB_PROPERTIES =
            DbConnectionProperties.fromResourceFile("jdbc-storage.properties");

    /** Prevents instantiation of this class. */
    private LocalMySqlServer() {
    }

    @VisibleForTesting
    static BoundedContext createContext() {
        return TasksContextFactory.create();
    }


    @Override
    protected DbConnectionProperties properties(String[] args) {
        if (args.length == 3) {
            DbConnectionProperties result = DbConnectionProperties.newBuilder()
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
    protected ConnectionUrl connectionUrl(DbConnectionProperties properties) {
        LocalMySqlConnectionUrl result = new LocalMySqlConnectionUrl(properties);
        return result;
    }
}
