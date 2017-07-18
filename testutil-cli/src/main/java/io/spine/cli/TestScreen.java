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

package io.spine.cli;

import java.util.ArrayDeque;
import java.util.Queue;

import static java.lang.System.lineSeparator;

/**
 * A {@code Screen} for the test needs.
 *
 * <p>Allows to specify an input expected from a user (answers).
 *
 * <p>Answers will be used in order of its addition.
 *
 * @author Dmytro Grankin
 */
public class TestScreen extends AbstractScreen {

    @SuppressWarnings("StringBufferField") // Used to collect output of the class.
    private final StringBuilder output = new StringBuilder();

    private final Queue<String> answers = new ArrayDeque<>();

    @Override
    public String promptUser(String prompt) {
        if (answers.isEmpty()) {
            throw new IllegalStateException("Not enough answers were specified.");
        }

        println(prompt);
        return answers.remove();
    }

    @Override
    public void println(String message) {
        output.append(message)
              .append(lineSeparator());
    }

    void addAnswer(String answer) {
        answers.add(answer);
    }

    boolean hasAnswers() {
        return !answers.isEmpty();
    }

    String getOutput() {
        return output.toString();
    }

    void clearOutput() {
        output.setLength(0);
    }
}
