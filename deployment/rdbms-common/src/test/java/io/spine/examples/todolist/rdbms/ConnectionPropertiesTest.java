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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`ConnectionProperties` should")
class ConnectionPropertiesTest {

    private static final String RESOURCE_FILE_NAME = "test.properties";

    @Test
    @DisplayName("throw if no resource file exists")
    void throwIfNoFile() {
        String badName = "non_existing_" + RESOURCE_FILE_NAME;
        assertThrows(IllegalStateException.class,
                     () -> ConnectionProperties.fromResourceFile(badName));
    }

    @Test
    @DisplayName("initialize from a resource file")
    void initializeOk() {
        ConnectionProperties properties = ConnectionProperties.fromResourceFile(RESOURCE_FILE_NAME);
        String testPrefix = "test_";
        assertThat(properties.dbName()).isEqualTo(testPrefix + "db");
        assertThat(properties.instanceName()).isEqualTo(testPrefix + "instance");
        assertThat(properties.connectionUrlPrefix()
                             .toString()).isEqualTo(testPrefix + "prefix");
        DbCredentials credentials = properties.credentials();
        assertThat(credentials.getUsername()).isEqualTo(testPrefix + "user");
        assertThat(credentials.getPassword()).startsWith(testPrefix + "password");
    }
}
