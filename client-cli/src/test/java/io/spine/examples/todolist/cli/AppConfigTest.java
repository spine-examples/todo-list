/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.cli;

import io.spine.examples.todolist.client.TodoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.cli.AppConfig.init;
import static io.spine.examples.todolist.cli.AppConfig.setClient;
import static io.spine.examples.todolist.client.TodoClient.HOST;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.testing.Tests.nullRef;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AppConfig should")
class AppConfigTest {

    @BeforeEach
    void setUp() {
        setClient(nullRef());
    }

    @Test
    @DisplayName("have the private constructor")
    void havePrivateCtor() {
        assertHasPrivateParameterlessCtor(AppConfig.class);
    }

    @Test
    @DisplayName("throw if was not initialized")
    void throwIfWasNotInitialized() {
        assertThrows(IllegalStateException.class, AppConfig::getClient);
    }

    @Test
    @DisplayName("allow initialization only once")
    void allowInitOnlyOnce() {
        TodoClient client = TodoClient.instance(HOST, DEFAULT_CLIENT_SERVICE_PORT);
        init(client);

        assertThrows(IllegalStateException.class, () -> init(client));
    }
}
