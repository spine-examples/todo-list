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

import io.spine.base.Environment;
import io.spine.base.Production;
import io.spine.base.Tests;
import io.spine.examples.todolist.rdbms.given.RdbmsServerTestEnv.TestRdbmsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.todolist.rdbms.ConnectionUrl.LOCAL_H2_PREFIX;

@DisplayName("Servers that run on relational databases should")
class RdbmsServerTest {

    private static final String[] EMPTY_ARGS = {};

    private final ConnectionProperties.Builder testProperties =
            ConnectionProperties.newBuilder()
                                .setDbName("db_name")
                                .setPassword("password")
                                .setUsername("test_user")
                                .setInstanceName("test_instance")
                                .setUrlPrefix("test_prefix");

    @AfterEach
    void afterEach() {
        Environment.instance()
                   .reset();
    }

    @Test
    @DisplayName("use the specified prefix for the production environment")
    void useSpecifiedForProd() {
        ConnectionProperties props = testProperties.setEnvType(Production.class)
                                                   .build();
        TestRdbmsServer server = new TestRdbmsServer(props);
        ConnectionUrl connectionUrl = connectionUrl(server);
        String stringValue = connectionUrl.toString();
        assertThat(stringValue).startsWith(props.connectionUrlPrefix()
                                                .getValue());
    }

    @Test
    @DisplayName("use a predefined prefix for the testing environment")
    void usePredefinedForTests() {
        ConnectionProperties props = testProperties.setEnvType(Tests.class)
                                                   .build();
        TestRdbmsServer server = new TestRdbmsServer(props);
        ConnectionUrl connectionUrl = connectionUrl(server);
        String stringValue = connectionUrl.toString();
        assertThat(stringValue).startsWith(LOCAL_H2_PREFIX);
    }

    private static ConnectionUrl connectionUrl(RunsOnRdbms server) {
        ConnectionProperties props = server.properties(EMPTY_ARGS);
        return server.connectionUrl(props);
    }
}
