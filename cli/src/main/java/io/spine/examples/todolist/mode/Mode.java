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

package io.spine.examples.todolist.mode;

import io.spine.examples.todolist.AppConfig;
import io.spine.examples.todolist.client.TodoClient;
import org.jline.reader.LineReader;

import java.io.PrintStream;

/**
 * @author Dmytro Grankin
 */
public abstract class Mode {

    @SuppressWarnings("UseOfSystemOutOrSystemErr" /* OK for command-line app. */)
    private final PrintStream printStream = System.out;
    private final LineReader reader = AppConfig.newLineReader();
    private final TodoClient client = AppConfig.getClient();

    public abstract void start();

    protected void println(String message) {
        printStream.println(message);
    }

    protected void println() {
        printStream.println();
    }

    protected String askUser(String question) {
        println(question);
        final String answer = readLine();
        return answer;
    }

    protected String readLine() {
        return reader.readLine();
    }

    protected TodoClient getClient() {
        return client;
    }
}