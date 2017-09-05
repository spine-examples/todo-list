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
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.examples.todolist.server.Server;
import io.spine.server.BoundedContext;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.client.CommandLineTodoClient.HOST;

/**
 * Entry point to the command-line application.
 *
 * <p>To run application from command-line, use the following command:
 * <pre>{@code gradle runTodoList}</pre>
 *
 * @author Illia Shepilov
 */
public class CliEntryPoint {

    private CliEntryPoint() {
        // Prevent instantiation of this class.
    }

    public static void main(String[] args) throws Exception {
        final int port = DEFAULT_CLIENT_SERVICE_PORT;
        final BoundedContext boundedContext = BoundedContexts.create();
        final Server server = new Server(port, boundedContext);
        final TodoClient client = new CommandLineTodoClient(HOST, port, boundedContext);
        final TodoList todoList = new TodoList(server, client);
        todoList.run();
    }
}
