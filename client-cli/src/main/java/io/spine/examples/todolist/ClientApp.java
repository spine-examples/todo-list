/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
import io.spine.examples.todolist.view.MainMenu;
import org.slf4j.Logger;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.AppConfig.getClient;
import static io.spine.examples.todolist.client.TodoClient.HOST;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A command-line client application, which connects to a server using the host and port.
 *
 * <p>To run the application from a command-line, use the following command:
 * <pre>{@code gradle runTodoClient -Pconf=hostname,port}</pre>
 *
 * <p>If the parameters were not specified to a command or the server was ran directly,
 * default {@linkplain TodoClient#HOST host}
 * and {@link io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT port} will be used.
 *
 * @author Illia Shepilov
 */
public class ClientApp {

    private static final int ARGUMENTS_AMOUNT = 2;
    private static final String DEFAULT_HOST = HOST;
    private static final int DEFAULT_PORT = DEFAULT_CLIENT_SERVICE_PORT;

    /**
     * The {@code private} constructor prevents instantiation.
     */
    private ClientApp() {
    }

    public static void main(String[] args) {
        final Screen screen = new TerminalScreen();
        initCli(screen);
        final TodoClient client = createClient(args);
        AppConfig.init(client);

        final View entryPoint = MainMenu.create();
        screen.renderView(entryPoint);

        getClient().shutdown();
    }

    private static TodoClient createClient(String[] arguments) {
        final String hostname;
        final int port;
        if (arguments.length != ARGUMENTS_AMOUNT) {
            log().info("Expected arguments amount is {}. " +
                               "Default arguments will be used, hostname: {} and port: {}.",
                       ARGUMENTS_AMOUNT, DEFAULT_HOST, DEFAULT_PORT);
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
        Application.getInstance()
                   .init(screen);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = getLogger(ClientApp.class);
    }
}
