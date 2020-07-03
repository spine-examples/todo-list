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

import com.google.common.flogger.FluentLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.spine.examples.todolist.DbCredentials;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.jdbc.JdbcStorageFactory;

import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * A supplier of the storage factory backed by a relational database.
 */
public final class RdbmsStorageFactorySupplier implements Supplier<StorageFactory> {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private final String dbUrl;
    private final DbCredentials dbCredentials;

    public RdbmsStorageFactorySupplier(String dbConnectionUrl,
                                       DbCredentials dbCredentials) {
        this.dbUrl = dbConnectionUrl;
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
        FluentLogger.Api info = logger.atInfo();
        HikariConfig config = new HikariConfig();

        info.log("Start `DataSource` creation. The following parameters will be used:");
        config.setJdbcUrl(dbUrl);
        info.log("JDBC URL: %s", dbUrl);

        config.setUsername(dbCredentials.getUsername());
        info.log("Username: %s", dbCredentials.getUsername());

        config.setPassword(dbCredentials.getPassword());
        info.log("Password: %s", dbCredentials.getPassword());

        DataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }
}
