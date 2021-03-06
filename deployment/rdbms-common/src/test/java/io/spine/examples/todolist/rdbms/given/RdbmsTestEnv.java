/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.rdbms.given;

import io.spine.examples.todolist.rdbms.ConnectionProperties;
import io.spine.examples.todolist.rdbms.ConnectionUrl;
import io.spine.examples.todolist.rdbms.JdbcConnectionProtocol;
import io.spine.examples.todolist.rdbms.RelationalStorage;
import io.spine.examples.todolist.rdbms.RunsOnRdbms;

import static java.lang.String.format;

public final class RdbmsTestEnv {

    private RdbmsTestEnv() {
    }

    /**
     * A dummy implementation of an RDBMS-backed server for tests.
     *
     * <p>Always returns the predefined connection properties specified to the ctor.
     */
    public static final class TestServer extends RunsOnRdbms {

        private final ConnectionProperties properties;

        public TestServer(ConnectionProperties properties) {
            this.properties = properties;
        }


        @Override
        public RelationalStorage storage(ConnectionProperties properties) {
            ConnectionUrl connectionUrl = new ConnectionUrl(TestServer.this.properties) {
                @Override
                protected String stringValue(ConnectionProperties properties) {
                    JdbcConnectionProtocol protocol = properties.connectionProtocol();
                    String result = format("%s//%s", protocol.getValue(),
                                           properties.instanceName());
                    return result;
                }
            };
            return new RelationalStorage(connectionUrl, this.properties.credentials());
        }
    }
}
