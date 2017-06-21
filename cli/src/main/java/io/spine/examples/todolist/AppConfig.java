/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.examples.todolist;

import io.spine.examples.todolist.client.CommandLineTodoClient;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.context.TodoListBoundedContext;
import io.spine.examples.todolist.server.Server;
import io.spine.server.BoundedContext;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.client.CommandLineTodoClient.LOCALHOST;

/**
 * Utilities for obtaining pre-configured {@link Server} and {@link TodoClient}.
 *
 * @author Dmytro Grankin
 */
public class AppConfig {

    private static final BoundedContext BOUNDED_CONTEXT = TodoListBoundedContext.getInstance();

    private static final Server SERVER = new Server(BOUNDED_CONTEXT);

    private static final TodoClient CLIENT =
            new CommandLineTodoClient(LOCALHOST,
                                      DEFAULT_CLIENT_SERVICE_PORT,
                                      BOUNDED_CONTEXT);

    private AppConfig() {
        // Prevent instantiation of this utility class.
    }

    static Server getServer() {
        return SERVER;
    }

    public static TodoClient getClient() {
        return CLIENT;
    }
}
