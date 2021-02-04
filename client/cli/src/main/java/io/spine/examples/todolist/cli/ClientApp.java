/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.flogger.FluentLogger;
import io.spine.examples.todolist.cli.view.MainMenu;
import io.spine.examples.todolist.cli.view.View;
import io.spine.examples.todolist.client.TodoClient;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.cli.AppConfig.getClient;
import static io.spine.examples.todolist.client.TodoClient.HOST;

/**
 * A command-line client application, which connects to a server using the host and port.
 *
 * <p>To run the application from a command-line, use the following command:
 * <pre>{@code gradle runTodoClient -Pconf=hostname,port}</pre>
 *
 * <p>If the parameters were not specified to a command or the server was ran directly,
 * default {@linkplain TodoClient#HOST host}
 * and {@link io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT port} will be used.
 */
public final class ClientApp {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static final int ARGUMENT_NUMBER = 2;
    private static final String DEFAULT_HOST = HOST;
    private static final int DEFAULT_PORT = DEFAULT_CLIENT_SERVICE_PORT;

    /** Prevent instantiation of this class. */
    private ClientApp() {
    }

    public static void main(String[] args) {
        Screen screen = new TerminalScreen();
        initCli(screen);
        TodoClient client = createClient(args);
        AppConfig.init(client);

        View entryPoint = MainMenu.create();
        screen.renderView(entryPoint);

        getClient().shutdown();
    }

    private static TodoClient createClient(String[] arguments) {
        String hostname;
        int port;
        if (arguments.length != ARGUMENT_NUMBER) {
            logger.atInfo()
                  .log("Expected number of arguments is: %d. " +
                       "Default arguments will be used, hostname: %s and port: %d.",
                       ARGUMENT_NUMBER, DEFAULT_HOST, DEFAULT_PORT);
            hostname = DEFAULT_HOST;
            port = DEFAULT_PORT;
        } else {
            hostname = arguments[0];
            port = Integer.parseInt(arguments[1]);
        }

        return TodoClient.instance(hostname, port);
    }

    @VisibleForTesting
    static void initCli(Screen screen) {
        Application.instance()
                   .init(screen);
    }
}
