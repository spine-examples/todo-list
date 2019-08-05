/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TestScreen should")
class TestScreenTest {

    private static final String MESSAGE = "a message";

    private final TestScreen screen = new TestScreen();

    @Test
    @DisplayName("throw ISE if answer was not specified")
    void throwIfAnswerWasNotSpecified() {
        assertThrows(IllegalStateException.class, () -> screen.promptUser(MESSAGE));
    }

    @Test
    @DisplayName("not have answers by default")
    void notHaveAnswersByDefault() {
        assertFalse(screen.hasAnswers());
    }

    @Test
    @DisplayName("print a message and new line")
    void printMessage() {
        screen.println(MESSAGE);
        String expectedMessage = MESSAGE + lineSeparator();
        assertEquals(expectedMessage, screen.getOutput());
    }

    @Test
    @DisplayName("clear the output")
    void clearOutput() {
        screen.println(MESSAGE);
        screen.clearOutput();
        assertTrue(screen.getOutput()
                         .isEmpty());
    }

    @Test
    @DisplayName("add an answer")
    void addAnswer() {
        screen.addAnswer(MESSAGE);
        assertTrue(screen.hasAnswers());
    }

    @Test
    @DisplayName("print a prompt and remove the answer")
    void printPromptAndRemoveAnswer() {
        String prompt = "prompt";
        screen.addAnswer(MESSAGE);

        String answer = screen.promptUser(prompt);

        assertEquals(answer, MESSAGE);
        assertFalse(screen.hasAnswers());
    }
}
