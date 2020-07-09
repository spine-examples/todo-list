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
import io.spine.examples.todolist.rdbms.DbCredentials;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.todolist.rdbms.ConnectionProperties.NAME;
import static io.spine.examples.todolist.server.cloudsql.given.CloudSqlServerTestEnv.databaseName;
import static io.spine.examples.todolist.server.cloudsql.given.CloudSqlServerTestEnv.instance;
import static io.spine.examples.todolist.server.cloudsql.given.CloudSqlServerTestEnv.password;
import static io.spine.examples.todolist.server.cloudsql.given.CloudSqlServerTestEnv.protocol;
import static io.spine.examples.todolist.server.cloudsql.given.CloudSqlServerTestEnv.username;

@DisplayName("`CloudSqlServer` should")
class CloudSqlServerTest {

    @SuppressWarnings("AccessOfSystemProperties")
    @Test
    @DisplayName("use the value from the properties file if system properties are insufficient")
    void fallbackToProperties() {
        CloudSqlServer server = new CloudSqlServer();
        System.setProperty(NAME, "name");
        ConnectionProperties properties = server.connectionProperties();
        assertMatchesResourceFile(properties);
    }

    @Test
    @DisplayName("use the values specified to the system properties")
    @SuppressWarnings("AccessOfSystemProperties")
    void useSystemProps() {
        String dbName = "test_database";
        String username = "test-user";
        String password = "test-password";
        String instanceName = "testInstance";

        ConnectionProperties expectedProperties =
                ConnectionProperties
                        .newBuilder()
                        .setDbName(dbName)
                        .setUsername(username)
                        .setPassword(password)
                        .setInstanceName(instanceName)
                        .build();

        System.setProperty(ConnectionProperties.NAME, dbName);
        System.setProperty(ConnectionProperties.USERNAME, username);
        System.setProperty(ConnectionProperties.PASSWORD, password);
        System.setProperty(ConnectionProperties.INSTANCE, instanceName);

        CloudSqlServer server = new CloudSqlServer();
        ConnectionProperties properties = server.connectionProperties();
        assertThat(properties).isEqualTo(expectedProperties);
    }

    private static void assertMatchesResourceFile(ConnectionProperties properties) {
        assertThat(properties.dbName()).isEqualTo(databaseName());
        assertThat(properties.instanceName()).isEqualTo(instance());
        assertThat(properties.connectionProtocol()
                             .getValue()).isEqualTo(protocol());

        DbCredentials credentials = properties.credentials();
        assertThat(credentials.getUsername()).isEqualTo(username());
        assertThat(credentials.getPassword()).isEqualTo(password());
    }
}
