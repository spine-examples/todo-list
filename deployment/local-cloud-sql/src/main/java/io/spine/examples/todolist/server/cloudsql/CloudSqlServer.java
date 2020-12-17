/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import static io.spine.examples.todolist.rdbms.ConnectionProperties.fromResourceFile;

/**
 * A To-Do list application server backed by a Google Cloud SQL-based storage.
 *
 * <p>To run the server, use {@link #start()}. The application must be launched with the necessary
 * configuration provided via named properties (e.g. {@code -Dproperty.name="value"}).
 *
 * <p>The necessary properties are:
 * <ol>
 *     <li>JDBC protocol via the {@code db.protocol} property;
 *     <li>name of the database instance via the {@code db.instance} property;
 *     <li>name of the database via the {@code db.name} property;
 *     <li>username to use to connect to the database via the {@code db.username} property;
 *     <li>user password via the {@code db.password} property;
 * </ol>
 *
 * <p>If at least one of the properties is not specified, the configuration is taken from the
 * resource file. See {@code /resources/cloud-sql.properties} of the module which launches the
 * application.
 */
public final class CloudSqlServer extends RunsOnRdbms {

    private static final String CONFIGURATION_FILE = "cloud-sql.properties";

    @Override
    protected RelationalStorage storage(ConnectionProperties properties) {
        ConnectionUrl url = new CloudSqlUrl(properties);
        return new RelationalStorage(url, properties.credentials());
    }

    /**
     * Returns the connection properties for a Google Cloud SQL database.
     *
     * <p>If the connection properties parsed from the system properties are sufficient, simply
     * returns {@code super.connectionProperties()}. If they are not sufficient, parses
     * them from {@code /resources/cloud-sql.properties}.
     *
     * <p>Refer to the class-level Javadoc for the necessary connection properties and how to pass
     * them.
     *
     * @return properties for connecting to a Google Cloud SQL database
     */
    @Override
    protected final ConnectionProperties connectionProperties() {
        ConnectionProperties properties = super.connectionProperties();
        return enoughProperties(properties)
               ? properties
               : fromResourceFile(CONFIGURATION_FILE);
    }

    private static boolean enoughProperties(ConnectionProperties properties) {
        return properties.hasInstanceName()
                && properties.hasCredentials()
                && properties.hasDbName();
    }
}
