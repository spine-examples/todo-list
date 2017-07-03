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
import org.jline.terminal.Terminal;

import java.io.PrintStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.examples.todolist.Terminals.newDumbTerminal;

/**
 * The class provides relatively high-level facilities for user I/O in a command-line.
 *
 * @author Dmytro Grankin
 */
public class UserCommunicatorImpl implements UserCommunicator {

    @SuppressWarnings("UseOfSystemOutOrSystemErr" /* OK for command-line app. */)
    private static final PrintStream PRINT_STREAM = System.out;

    private final LineReader reader = newLineReader();

    /**
     * {@inheritDoc}
     */
    @Override
    public String promptUser(String prompt) {
        checkArgument(!isNullOrEmpty(prompt));
        println(prompt);
        final String answer = reader.readLine();
        return answer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void println(String message) {
        checkArgument(!isNullOrEmpty(message));
        PRINT_STREAM.println(message);
    }

    private static LineReader newLineReader() {
        final Terminal terminal = newDumbTerminal();
        return Readers.newLineReader(terminal);
    }
}
