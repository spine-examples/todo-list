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

package io.spine.examples.todolist.rdbms.given;

import io.spine.examples.todolist.rdbms.ConnectionProperties;
import io.spine.examples.todolist.rdbms.ConnectionUrl;
import io.spine.examples.todolist.rdbms.RunsOnRdbms;

public final class RdbmsServerTestEnv {

    public static final ConnectionProperties TEST_PROPERTIES = ConnectionProperties
            .newBuilder()
            .setDbName("db_name")
            .setPassword("password")
            .setUsername("test_user")
            .setInstanceName("test_instance")
            .setUrlPrefix("test_prefix")
            .build();

    private RdbmsServerTestEnv() {
    }

    /**
     * A dummy implementation of an RDBMS-backed server for tests.
     *
     * <p>Always returns the predefined {@linkplain RdbmsServerTestEnv#TEST_PROPERTIES predefined
     * connection properties}.
     */
    public static class TestRdbmsServer extends RunsOnRdbms {

        @Override
        public ConnectionProperties properties(String[] commandLineArguments) {
            return TEST_PROPERTIES;
        }

        @Override
        public ConnectionUrl connectionUrl(ConnectionProperties properties) {
            ConnectionUrl result = new ConnectionUrl(properties) {
                @Override
                protected String stringValue(ConnectionProperties properties) {
                    return properties.toString();
                }
            };
            return result;
        }
    }
}
