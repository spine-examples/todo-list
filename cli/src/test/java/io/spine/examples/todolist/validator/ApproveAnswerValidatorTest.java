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

package io.spine.examples.todolist.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.validator.ApproveAnswerValidator.getNegativeAnswer;
import static io.spine.examples.todolist.validator.ApproveAnswerValidator.getPositiveAnswer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
@DisplayName("ApproveAnswerValidator should")
class ApproveAnswerValidatorTest {

    private static final String INVALID_INPUT = getPositiveAnswer() + getNegativeAnswer();

    private ApproveAnswerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ApproveAnswerValidator();
    }

    @Test
    @DisplayName("pass the validation when input is negative answer")
    void passValidationWhenNegativeAnswer() {
        final boolean passed = validator.validate(getNegativeAnswer());
        assertTrue(passed);
    }

    @Test
    @DisplayName("pass the validation when input is positive answer")
    void passValidationWhenPositiveAnswer() {
        final boolean passed = validator.validate(getPositiveAnswer());
        assertTrue(passed);
    }

    @Test
    @DisplayName("not pass the validation when input is null")
    void notPassValidationWhenInputIsNull() {
        final boolean passed = validator.validate(null);
        assertFalse(passed);
    }

    @Test
    @DisplayName("not pass the validation when input is empty")
    void notPassTheValidationWhenInputIsEmpty() {
        final boolean passed = validator.validate("");
        assertFalse(passed);
    }

    @Test
    @DisplayName("not pass the validation when input neither positive neither negative")
    void notPassTheValidationWhenInputIsIncorrect() {
        final boolean passed = validator.validate(INVALID_INPUT);
        assertFalse(passed);
    }
}
