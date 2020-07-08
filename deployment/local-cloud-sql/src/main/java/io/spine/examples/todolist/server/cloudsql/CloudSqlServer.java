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
import io.spine.examples.todolist.rdbms.ConnectionUrl;
import io.spine.examples.todolist.rdbms.RelationalStorage;
import io.spine.examples.todolist.rdbms.RunsOnRdbms;

import java.util.Optional;

/**
 * An abstract base for servers backed by a Google Cloud SQL-based storage.
 *
 * <p>By convention, Google Cloud SQL servers may be configured using a properties file
 * with a predefined name.
 */
public abstract class CloudSqlServer extends RunsOnRdbms {

    private static final ConnectionProperties CLOUD_SQL_PROPERTIES =
            ConnectionProperties.fromResourceFile("cloud-sql.properties");

    /**
     * Returns the {@code ConnectionProperties} assembled from the command line arguments.
     *
     * <p>If the command line arguments could not be assembled, returns an empty {@code Optional}.
     *
     * <p>If extenders return an empty {@code Optional}, a default configuration is used when
     * {@linkplain #storage(String[]) creating the storage}.
     *
     * @param args
     *         command line arguments specified to launch the application
     */
    protected abstract Optional<ConnectionProperties> connectionProperties(String[] args);

    @Override
    protected RelationalStorage storage(String[] args) {
        ConnectionProperties properties = connectionProperties(args).orElse(CLOUD_SQL_PROPERTIES);
        ConnectionUrl url = new CloudSqlConnectionUrl(properties);
        return new RelationalStorage(url, properties.credentials());
    }
}
