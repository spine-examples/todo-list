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

import io.spine.examples.todolist.DbCredentials;
import io.spine.examples.todolist.rdbms.DbConnectionProperties;
import io.spine.examples.todolist.rdbms.DbUrlPrefix;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`CloudSqlServers` utility class should")
final class CloudSqlServersTest extends UtilityClassTest<CloudSqlServers> {

    CloudSqlServersTest() {
        super(CloudSqlServers.class);
    }

    @Test
    @DisplayName("return an h2 connection URL prefix")
    void localH2Prefix() {
        DbConnectionProperties props = DbConnectionProperties.newBuilder()
                                                             .setDbName("database")
                                                             .setUsername("admin")
                                                             .setPassword("admin")
                                                             .build();

        DbUrlPrefix prefix = CloudSqlServers.prefix(props);
        assertThat(prefix.toString()).isEqualTo(DbUrlPrefix.LOCAL_H2);
    }

    @Test
    @DisplayName("read the properties from resources")
    void readFromResources() {
        // All values in the test properties file are prefixed with `_test'.
        String testPrefix = "test_";
        DbConnectionProperties properties = CloudSqlServers.propertiesFromResourceFile();
        assertThat(properties.dbName()).isEqualTo(testPrefix + "db");
        assertThat(properties.instanceName()).isEqualTo(testPrefix + "instance");
        assertThat(properties.connectionUrlPrefix()).startsWith(testPrefix + "prefix");

        DbCredentials credentials = properties.credentials();
        assertThat(credentials.getUsername()).isEqualTo(testPrefix + "user");
        assertThat(credentials.getPassword()).isEqualTo(testPrefix + "password");
    }
}
