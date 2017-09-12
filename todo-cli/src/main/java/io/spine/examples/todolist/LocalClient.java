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
import io.spine.examples.todolist.view.MainMenu;

import static io.spine.examples.todolist.AppConfig.getClient;

/**
 * A command-line client application working with a local servers.
 *
 * <p>The client connects to a server using the
 * {@linkplain io.spine.examples.todolist.client.CommandLineTodoClient#HOST host}
 * and {@link io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * <p>To run the application from a command-line, use the following command:
 * <pre>{@code gradle runTodoClient}</pre>
 *
 * @author Illia Shepilov
 */
public class LocalClient {

    private LocalClient() {
        // Prevent instantiation of this class.
    }

    public static void main(String[] args) {
        final Screen screen = new TerminalScreen();
        initCli(screen);
        final View entryPoint = MainMenu.create();
        screen.renderView(entryPoint);

        getClient().shutdown();
    }

    @VisibleForTesting
    static void initCli(Screen screen) {
        Application.getInstance()
                   .init(screen);
    }
}
