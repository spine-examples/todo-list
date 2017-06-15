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

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static org.jline.reader.LineReader.Option.AUTO_FRESH_LINE;

/**
 * Utilities for working with command-line input/output.
 *
 * @author Dmytro Grankin
 */
public class UserIO {

    @SuppressWarnings("UseOfSystemOutOrSystemErr" /* OK for command-line app. */)
    private static final PrintStream PRINT_STREAM = System.out;

    private static final LineReader READER = newLineReader();

    private UserIO() {
        // Prevent instantiation of this utility class.
    }

    public static void println(String message) {
        PRINT_STREAM.println(message);
    }

    public static String askUser(String question) {
        checkArgument(!isNullOrEmpty(question));
        final String answer = READER.readLine(question);
        return answer;
    }

    private static LineReader newLineReader() {
        final LineReader reader = LineReaderBuilder.builder()
                                                   .terminal(newTerminal())
                                                   .build();
        reader.setOpt(AUTO_FRESH_LINE);
        return reader;
    }

    private static Terminal newTerminal() {
        try {
            return TerminalBuilder.builder()
                                  .dumb(true)
                                  .build();
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }
}
