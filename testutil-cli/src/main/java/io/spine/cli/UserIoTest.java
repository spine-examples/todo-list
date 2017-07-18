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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import static io.spine.cli.TestScreen.getOutput;

/**
 * Abstract base class for test suites that are dealing with user I/O of a command-line application.
 *
 * @author Dmytro Grankin
 */
public abstract class UserIoTest {

    private TestScreen screen;

    @SuppressWarnings("TestOnlyProblems") // This class should be used only for tests needs.
    @BeforeEach
    protected void setUp() {
        TestScreen.clearOutput();
        this.screen = new TestScreen();
        Application.getInstance()
                   .setScreen(this.screen);
    }

    protected void assertOutput(String expected) {
        Assertions.assertEquals(expected, getOutput());
    }

    protected void assertAllAnswersWereGiven() {
        Assertions.assertTrue(screen.getAnswers()
                                    .isEmpty());
    }

    protected void addAnswer(String answer) {
        screen.addAnswer(answer);
    }

    protected TestScreen getScreen() {
        return screen;
    }
}
