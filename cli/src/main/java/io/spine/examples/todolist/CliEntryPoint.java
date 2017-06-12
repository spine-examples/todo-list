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

import io.spine.examples.todolist.mode.Mode;
import io.spine.server.BoundedContext;
import io.spine.util.Exceptions;
import jline.console.ConsoleReader;
import io.spine.examples.todolist.client.CommandLineTodoClient;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.context.TodoListBoundedContext;
import io.spine.examples.todolist.mode.GeneralMode;
import io.spine.examples.todolist.server.Server;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * @author Illia Shepilov
 */
public class CliEntryPoint {

    private CliEntryPoint() {
    }

    public static void main(String[] args) throws Exception {
        final BoundedContext boundedContext = TodoListBoundedContext.getInstance();
        final Server server = new Server(boundedContext);
        startServer(server);
        final TodoClient client = new CommandLineTodoClient("localhost", DEFAULT_CLIENT_SERVICE_PORT, boundedContext);
        final ConsoleReader reader = new ConsoleReader();
        final Mode entryPoint = new GeneralMode(client, reader);
        entryPoint.start();
        reader.close();
        client.shutdown();
        server.shutdown();
    }

    private static void startServer(Server server) throws InterruptedException {
        final CountDownLatch serverStartLatch = new CountDownLatch(1);
        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
                serverStartLatch.countDown();
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        });

        serverThread.start();
        serverStartLatch.await(1500, TimeUnit.MILLISECONDS);
    }
}
