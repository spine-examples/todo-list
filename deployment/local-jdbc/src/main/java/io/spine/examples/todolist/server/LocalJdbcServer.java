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

package io.spine.examples.todolist.server;

import com.google.common.annotations.VisibleForTesting;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.jdbc.JdbcStorageFactory;

import javax.sql.DataSource;
import java.io.IOException;

import static io.spine.Identifier.newUuid;
import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

/**
 * A local {@link Server} using
 * {@link io.spine.server.storage.jdbc.JdbcStorageFactory JdbcStorageFactory}.
 *
 * <p>The server uses the
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * @author Dmytro Grankin
 */
public class LocalJdbcServer {

    private static final String HSQL_IN_MEMORY_DB_URL_PREFIX = "jdbc:hsqldb:mem:";
    private static final String DB_NAME = "TodoListInMemoryDB";

    private LocalJdbcServer() {
        // Prevent instantiation of this class.
    }

    public static void main(String[] args) throws IOException {
        final BoundedContext boundedContext = createBoundedContext();
        final Server server = new Server(DEFAULT_CLIENT_SERVICE_PORT, boundedContext);
        server.start();
    }

    @VisibleForTesting
    static BoundedContext createBoundedContext() {
        final StorageFactory storageFactory = createStorageFactory();
        return BoundedContexts.create(storageFactory);
    }

    private static StorageFactory createStorageFactory() {
        return JdbcStorageFactory.newBuilder()
                                 .setDataSource(inMemoryDataSource())
                                 .setMultitenant(false)
                                 .build();
    }

    private static DataSource inMemoryDataSource() {
        final HikariConfig config = new HikariConfig();
        final String dbUrl = HSQL_IN_MEMORY_DB_URL_PREFIX + DB_NAME + newUuid();
        config.setJdbcUrl(dbUrl);

        // Not setting username and password is OK for in-memory database.
        final DataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }
}
