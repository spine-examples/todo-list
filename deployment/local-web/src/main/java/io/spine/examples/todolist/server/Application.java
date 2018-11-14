/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.server.BoundedContext;
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.web.firebase.DatabaseUrl;
import io.spine.web.firebase.FirebaseClient;

import static io.spine.web.firebase.DatabaseUrl.from;
import static io.spine.web.firebase.FirebaseClientFactory.restClient;

/**
 * The TodoList application.
 */
final class Application {

    private static final DatabaseUrl DATABASE_URL = from("https://spine-dev.firebaseio.com/");

    private final QueryService queryService;
    private final CommandService commandService;
    private final FirebaseClient firebaseClient;

    /**
     * Prevents direct instantiation.
     */
    private Application(BoundedContext boundedContext) {
        this.queryService = QueryService.newBuilder()
                                        .add(boundedContext)
                                        .build();
        this.commandService = CommandService.newBuilder()
                                            .add(boundedContext)
                                            .build();
        this.firebaseClient = restClient(DATABASE_URL);
    }

    /**
     * The query service used in this application.
     */
    QueryService queryService() {
        return queryService;
    }

    /**
     * The command service used in this application.
     */
    CommandService commandService() {
        return commandService;
    }

    /**
     * The Firebase client used in this application.
     */
    FirebaseClient firebaseClient() {
        return firebaseClient;
    }

    static Application instance() {
        return Singleton.INSTANCE.value;
    }

    private enum Singleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Application value = new Application(BoundedContexts.create());
    }
}
