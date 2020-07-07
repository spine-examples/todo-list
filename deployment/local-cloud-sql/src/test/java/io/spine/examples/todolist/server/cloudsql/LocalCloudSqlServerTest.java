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

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

@DisplayName("`LocalCloudSqlServer` should")
class LocalCloudSqlServerTest {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    @DisplayName("use the properties from the command line")
    void commandLineProps() {
        LocalCloudSqlServer server = new LocalCloudSqlServer();
        String instanceName = "instance";
        String dbName = "db";

        DbCredentials credentials =
                DbCredentials.newBuilder()
                             .setUsername("user")
                             .setPassword("password")
                             .vBuild();

        String[] args = {instanceName, dbName, credentials.getUsername(), credentials.getPassword()};
        Optional<ConnectionProperties> maybeProperties = server.connectionProperties(args);
        assertThat(maybeProperties).isPresent();
        ConnectionProperties properties = maybeProperties.get();
        assertThat(properties.instanceName()).isEqualTo(instanceName);
        assertThat(properties.dbName()).isEqualTo(dbName);
        assertThat(properties.credentials()).isEqualTo(credentials);
    }

    @Test
    @DisplayName("use the values from resources if the command line arguments are insufficient")
    void fromResources() {
        CloudSqlServer server = new LocalCloudSqlServer();
        String[] args = {"instance_name", "db_name"};

        Optional<ConnectionProperties> properties = server.connectionProperties(args);
        assertThat(properties).isEmpty();
    }
}
