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
import io.spine.server.BoundedContext;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.io.PrintStream;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

/**
 * @author Dmytro Grankin
 */
public class AppConfig {

    @SuppressWarnings("UseOfSystemOutOrSystemErr" /* OK for command-line app. */)
    private static final PrintStream PRINT_STREAM = System.out;

    private static final TodoClient CLIENT =
            new CommandLineTodoClient("localhost",
                                      DEFAULT_CLIENT_SERVICE_PORT,
                                      TodoListBoundedContext.getInstance());

    private AppConfig() {
        // Prevent instantiation of this utility class.
    }

    public static LineReader newLineReader() {
        return LineReaderBuilder.builder()
                                .build();
    }

    public static PrintStream getPrintStream() {
        return PRINT_STREAM;
    }

    public static TodoClient getClient() {
        return CLIENT;
    }

    public static BoundedContext getBoundedContext() {
        return TodoListBoundedContext.getInstance();
    }
}
