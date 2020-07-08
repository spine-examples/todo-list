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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.todolist.rdbms.ConnectionUrl.LOCAL_H2;
import static io.spine.examples.todolist.rdbms.given.RdbmsServerTestEnv.TEST_PROPERTIES;

@DisplayName("Servers that run on relational databases should")
class RdbmsServerTest {

    private static final String[] EMPTY_ARGS = {};

    @AfterAll
    static void afterAll() {
        Environment.instance()
                   .reset();
    }

    @Test
    @DisplayName("use the specified prefix for the production environment")
    void useSpecifiedForProd() {
        Environment.instance()
                   .setTo(Production.class);

        TestRdbmsServer server = new TestRdbmsServer();
        ConnectionUrl connectionUrl = connectionUrl(server);
        String stringValue = connectionUrl.toString();
        assertThat(stringValue).startsWith(TEST_PROPERTIES.connectionUrlPrefix()
                                                          .getValue());
    }

    @Test
    @DisplayName("use a predefined prefix for the testing environment")
    void usePredefinedForTests() {
        Environment.instance()
                   .setTo(Tests.class);

        TestRdbmsServer server = new TestRdbmsServer();
        ConnectionUrl connectionUrl = connectionUrl(server);
        String stringValue = connectionUrl.toString();
        assertThat(stringValue).startsWith(LOCAL_H2);
    }

    private static ConnectionUrl connectionUrl(RunsOnRdbms server) {
        ConnectionProperties props = server.properties(EMPTY_ARGS);
        return server.connectionUrl(props);
    }
}
