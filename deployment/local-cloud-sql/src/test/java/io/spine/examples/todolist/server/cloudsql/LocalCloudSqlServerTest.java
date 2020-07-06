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

import com.google.common.truth.StringSubject;
import com.google.common.truth.Truth;
import io.spine.examples.todolist.rdbms.DbConnectionProperties;
import io.spine.server.BoundedContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.todolist.server.cloudsql.LocalCloudSqlServer.createContext;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.Assert.assertFalse;

@DisplayName("LocalCloudSqlServer should")
class LocalCloudSqlServerTest {

    @Test
    @DisplayName("have the private constructor")
    void havePrivateCtor() {
        assertHasPrivateParameterlessCtor(LocalCloudSqlServer.class);
    }

    @Test
    @DisplayName("assemble DB properties from resources if command line args are insufficient")
    void assemblePropsFromResources() {
        String[] insufficientCmdArgs = {"dbName", "username"};

        DbConnectionProperties properties = LocalCloudSqlServer.properties(insufficientCmdArgs);
        Stream.of(properties.dbName(),
                  properties.credentials()
                            .getUsername(),
                  properties.credentials()
                            .getPassword(),
                  properties.instanceName())
              .map(Truth::assertThat)
              .forEach(StringSubject::isNotEmpty);
    }

    @Test
    @DisplayName("use the DB properties specified as command line args")
    void useCmdArgsAsDbProps() {
        final String dbInstance = "instance_1";
        final String dbName = "myDb";
        final String username = "admin";
        final String password = username + "123";

        String[] customProperties = {dbInstance, dbName, username, password};

        DbConnectionProperties properties = LocalCloudSqlServer.properties(customProperties);
        assertThat(properties.dbName()).isEqualTo(dbName);
        assertThat(properties.credentials()
                             .getUsername()).isEqualTo(username);
        assertThat(properties.credentials()
                             .getPassword()).isEqualTo(password);
        assertThat(properties.instanceName()).isEqualTo(dbInstance);
    }

    @Test
    @DisplayName("create signletenant BoundedContext")
    void createSingletenantBoundedContext() {
        BoundedContext context = createContext();
        assertFalse(context.isMultitenant());
    }
}
