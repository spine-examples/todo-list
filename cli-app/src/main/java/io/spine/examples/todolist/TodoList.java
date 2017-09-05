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

import com.google.common.annotations.VisibleForTesting;
import io.spine.cli.Application;
import io.spine.cli.Screen;
import io.spine.cli.view.View;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.server.Server;
import io.spine.examples.todolist.view.MainMenu;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * A facade for the application execution.
 *
 * @author Dmytro Grankin
 */
public final class TodoList {

    private final Server server;
    private final TodoClient client;
    private final Screen screen;

    /**
     * Creates a new instance.
     *
     * @param server the not started {@link Server}
     * @param client the {@link TodoClient} to use
     */
    public TodoList(Server server, TodoClient client) {
        this.server = checkNotNull(server);
        this.client = checkNotNull(client);
        this.screen = new TerminalScreen();
    }

    /**
     * Runs the {@code TodoList}.
     */
    public void run() {
        startServer();
        initCli();

        final View entryPoint = MainMenu.create(client);
        screen.renderView(entryPoint);

        client.shutdown();
        server.shutdown();
    }

    @VisibleForTesting
    void initCli() {
        final Application application = Application.getInstance();
        application.init(screen);
    }

    @VisibleForTesting
    void startServer() {
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
