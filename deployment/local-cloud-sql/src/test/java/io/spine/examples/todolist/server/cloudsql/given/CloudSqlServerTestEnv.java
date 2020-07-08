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

package io.spine.examples.todolist.server.cloudsql.given;

import io.spine.examples.todolist.rdbms.ConnectionProperties;
import io.spine.examples.todolist.server.cloudsql.CloudSqlServer;

import java.util.Optional;

public final class CloudSqlServerTestEnv {

    /*
     * Database properties from the `cloud-sql.properties` resource file.
     */

    public static final String JDBC_PROTOCOL = "local-sql:";
    public static final String INSTANCE_NAME = "instance_1";
    public static final String DB_NAME = "tests";
    public static final String USERNAME = "test_user";
    public static final String PASSWORD = "test_password";

    private CloudSqlServerTestEnv() {
    }

    public static class TestCloudSqlServer extends CloudSqlServer {

        @Override
        protected Optional<ConnectionProperties> connectionProperties(String[] args) {
            if (args.length == 3) {
                ConnectionProperties result = ConnectionProperties
                        .newBuilder()
                        .setDbName(args[0])
                        .setUsername(args[1])
                        .setPassword(args[2])
                        .build();
                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        }
    }
}
