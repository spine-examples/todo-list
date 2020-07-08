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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static io.spine.examples.todolist.server.cloudsql.given.CloudSqlServerTestEnv.TestCloudSqlServer;

@DisplayName("`CloudSqlServer` should")
class CloudSqlServerTest {

    @Test
    @DisplayName("use the value from the properties file if command line args are insufficient")
    void fallbackToProperties() {
        TestCloudSqlServer server = new TestCloudSqlServer();
        String[] args = {"name", "password"};
        Optional<ConnectionProperties> properties = server.connectionProperties(args);
        assertThat(properties).isEmpty();
    }

    @Test
    @DisplayName("use the values specified to the command line")
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void useArgs() {
        String dbName = "test_database";
        String username = "test-user";
        String password = "test-password";

        ConnectionProperties expectedProperties =
                ConnectionProperties
                        .newBuilder()
                        .setDbName(dbName)
                        .setUsername(username)
                        .setPassword(password)
                        .build();

        String[] args = {dbName, username, password};
        TestCloudSqlServer server = new TestCloudSqlServer();
        Optional<ConnectionProperties> properties = server.connectionProperties(args);
        assertThat(properties.get()).isEqualTo(expectedProperties);
    }
}
