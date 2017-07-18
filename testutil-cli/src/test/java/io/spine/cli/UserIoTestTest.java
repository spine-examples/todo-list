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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("UserIoTest should")
class UserIoTestTest {

    private static final String MESSAGE = "a message";

    private final ATest test = new ATest();

    @BeforeEach
    void setUp() {
        test.setUp();
    }

    @Test
    @DisplayName("pass if expected output is match actual")
    void passOnEqualExpectedAndActual() {
        test.screen()
            .println(MESSAGE);
        test.assertOutput(MESSAGE + lineSeparator());
    }

    @Test
    @DisplayName("throw if expected output does not match actual")
    void throwOnUnexpectedOutput() {
        test.screen()
            .println(MESSAGE);
        assertThrows(AssertionError.class, () -> test.assertOutput(MESSAGE + "wrong part"));
    }

    @Test
    @DisplayName("pass if all answers were given")
    void passIfAnswersWereGiven() {
        test.assertAllAnswersWereGiven();
    }

    @Test
    @DisplayName("throw if there are remaining answers")
    void throwIfAnswersNotEmpty() {
        test.addAnswer(MESSAGE);
        assertThrows(AssertionError.class, test::assertAllAnswersWereGiven);
    }

    private static class ATest extends UserIoTest {
    }
}
