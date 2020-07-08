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

package io.spine.examples.todolist.server.computecloudsql;

import io.spine.examples.todolist.rdbms.ConnectionProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.truth.Truth8.assertThat;

@DisplayName("`ComputeCloudSqlServer` should")
class ComputeCloudSqlServerTest {

    @DisplayName("never use the command line arguments")
    @ParameterizedTest
    @MethodSource("sampleArgs")
    void alwaysUseConfig(String[] args) {
        ComputeCloudSqlServer server = new ComputeCloudSqlServer();
        Optional<ConnectionProperties> properties = server.connectionProperties(args);
        assertThat(properties).isEmpty();
    }

    private static Stream<Arguments> sampleArgs() {
        String dbName = "sample_name";
        String protocol = "sample_protocol";
        String username = "sample_username";
        String password = "sample_password";
        String instanceName = "sample_instance";

        return Stream.of(
                stringArrArgument(dbName),
                stringArrArgument(dbName, protocol),
                stringArrArgument(dbName, protocol, username),
                stringArrArgument(dbName, protocol, username, password),
                stringArrArgument(dbName, protocol, username, password, instanceName));
    }

    private static Arguments stringArrArgument(String... args) {
        return Arguments.of((Object) args);
    }
}
