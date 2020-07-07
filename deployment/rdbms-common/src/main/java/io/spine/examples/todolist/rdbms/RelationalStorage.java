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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.spine.logging.Logging;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.jdbc.JdbcStorageFactory;

import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * A supplier of the storage factory backed by a relational database.
 */
public final class RelationalStorage implements Supplier<StorageFactory>, Logging {

    private final ConnectionUrl connectionUrl;
    private final DbCredentials dbCredentials;

    /**
     * Creates a new storage factory supplier using the specified connection URL and the specified
     * database credentials.
     */
    RelationalStorage(ConnectionUrl connectionUrl, DbCredentials dbCredentials) {
        this.connectionUrl = connectionUrl;
        this.dbCredentials = dbCredentials;
    }

    @Override
    public StorageFactory get() {
        return JdbcStorageFactory
                .newBuilder()
                .setDataSource(datasource())
                .build();
    }

    private DataSource datasource() {
        HikariConfig config = new HikariConfig();

        _debug().log("Connecting to the database. URL: `%s`", connectionUrl);
        config.setJdbcUrl(connectionUrl.toString());
        config.setUsername(dbCredentials.getUsername());
        config.setPassword(dbCredentials.getPassword());
        DataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }
}
