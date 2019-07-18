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

package io.spine.examples.todolist.server;

import io.spine.server.CommandService;
import io.spine.web.firebase.subscription.FirebaseSubscriptionBridge;
import io.spine.web.query.QueryBridge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.server.Application.application;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("DuplicateStringLiteralInspection") // Test display names duplication.
@DisplayName("Application should")
class ApplicationTest {

    private final Application application = application();

    @Test
    @DisplayName("return non-null QueryBridge")
    void returnQueryBridge() {
        QueryBridge bridge = application.queryBridge();
        assertNotNull(bridge);
    }

    @Test
    @DisplayName("return non-null CommandService")
    void returnCommandService() {
        CommandService commandService = application.commandService();
        assertNotNull(commandService);
    }

    @Test
    @DisplayName("return non-null FirebaseSubscriptionBridge")
    void returnFirebaseClient() {
        final FirebaseSubscriptionBridge bridge = application.subscriptionBridge();
        assertNotNull(bridge);
    }
}
