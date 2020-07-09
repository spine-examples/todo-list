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
import io.spine.examples.todolist.rdbms.DbCredentials;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`LocalMySqlServer` should")
class LocalMySqlServerTest {

    private static final String PROTOCOL = "jdbc:mysql:test:";
    private static final String DB_NAME = "my_db";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    @Test
    @SuppressWarnings("AccessOfSystemProperties")
    @DisplayName("system properties as DB connection properties")
    void systemProperties() {
        String dbName = "dbName";
        String username = "username";
        String password = "password";

        System.setProperty(ConnectionProperties.NAME, dbName);
        System.setProperty(ConnectionProperties.USERNAME, username);
        System.setProperty(ConnectionProperties.PASSWORD, password);
        LocalMySqlServer server = new LocalMySqlServer();
        ConnectionProperties properties = server.connectionProperties();
        assertThat(properties.dbName()).isEqualTo(dbName);

        DbCredentials credentials = properties.credentials();
        assertThat(credentials.getUsername()).isEqualTo(username);
        assertThat(credentials.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("use the resource file if no system properties have been specified")
    void useResourceFile() {
        LocalMySqlServer server = new LocalMySqlServer();
        ConnectionProperties properties = server.connectionProperties();
        assertThat(properties.connectionProtocol()
                             .getValue()).isEqualTo(PROTOCOL);
        assertThat(properties.dbName()).isEqualTo(DB_NAME);
        DbCredentials credentials = properties.credentials();
        assertThat(credentials.getUsername()).isEqualTo(USERNAME);
        assertThat(credentials.getPassword()).isEqualTo(PASSWORD);
    }
}
