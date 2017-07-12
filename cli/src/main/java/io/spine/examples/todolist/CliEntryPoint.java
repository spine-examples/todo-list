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

import io.spine.examples.todolist.server.Server;
import io.spine.examples.todolist.view.MainMenu;
import io.spine.examples.todolist.view.View;

import java.io.IOException;

import static io.spine.examples.todolist.AppConfig.getClient;
import static io.spine.examples.todolist.AppConfig.getServer;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

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
        final Server server = getServer();
        startServer(server);

        final View entryPoint = MainMenu.create();
        final Screen screen = new CommandLineScreen();
        screen.renderView(entryPoint);

        getClient().shutdown();
        server.shutdown();
    }

    private static void startServer(Server server) throws InterruptedException {
        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        });

        serverThread.start();
    }
}
