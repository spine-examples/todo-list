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

import io.spine.examples.todolist.rdbms.ConnectionUrl;
import io.spine.examples.todolist.rdbms.DbConnectionProperties;
import io.spine.examples.todolist.rdbms.RdbmsServer;

import java.util.Optional;

public abstract class CloudSqlServer extends RdbmsServer {

    private static final DbConnectionProperties CLOUD_SQL_PROPERTIES =
            DbConnectionProperties.fromResourceFile("cloud-sql.properties");

    @Override
    public final DbConnectionProperties properties(String[] args) {
        DbConnectionProperties result = fromArgs(args).orElse(CLOUD_SQL_PROPERTIES);
        return result;
    }

    protected abstract Optional<DbConnectionProperties> fromArgs(String[] commandLineArgs);

    @Override
    public final ConnectionUrl connectionUrl(DbConnectionProperties properties) {
        CloudSqlConnectionUrl result = new CloudSqlConnectionUrl(properties);
        return result;
    }
}
