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

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract base class for test suites that are dealing with user I/O of a command-line application.
 *
 * @author Dmytro Grankin
 */
public abstract class UserIoTest {

    private TestScreen screen;

    /**
     * Creates the new screen and
     * {@linkplain Application#setScreen(Screen) injects} it to the application.
     */
    @SuppressWarnings("TestOnlyProblems") // This class should be used only for tests needs.
    @BeforeEach
    protected void setUp() {
        screen = new TestScreen();
        Application.getInstance()
                   .setScreen(screen);
    }

    /**
     * Asserts that expected and actual {@link TestScreen#getOutput() output} are equal.
     *
     * @param expected the expected output
     */
    protected void assertOutput(String expected) {
        assertEquals(expected, screen.getOutput());
    }

    /**
     * Asserts that answers is empty.
     *
     * <p>Should be used to make sure that a user was
     * {@link Screen#promptUser(String) prompted} certain number of times.
     */
    protected void assertAllAnswersWereGiven() {
        assertTrue(!screen.hasAnswers());
    }

    /**
     * Adds the specified answer to the {@link TestScreen}.
     *
     * @param answer the answer to add
     */
    protected void addAnswer(String answer) {
        screen.addAnswer(answer);
    }

    /**
     * Obtains screen for the test.
     *
     * @return used screen
     */
    protected Screen screen() {
        return screen;
    }
}
