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

package org.spine3.examples.todolist.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
@DisplayName("ApproveValidator should")
class ApproveValidatorTest {

    private final Validator validator = new ApproveValidator();

    @Test
    @DisplayName("pass the validation when input is `n`")
    public void passValidationWhenNegativeAnswer() {
        final boolean passed = validator.validate("n");
        assertTrue(passed);
    }

    @Test
    @DisplayName("pass the validation when input is `y`")
    public void passValidationWhenPositiveAnswer() {
        final boolean passed = validator.validate("y");
        assertTrue(passed);
    }

    @Test
    @DisplayName("not pass the validation when input is null")
    public void notPassValidationWhenInputIsNull() {
        final boolean passed = validator.validate(null);
        assertFalse(passed);
    }

    @Test
    @DisplayName("not pass the validation when input is empty")
    public void notPassTheValidationWhenInputIsEmpty() {
        final boolean passed = validator.validate("");
        assertFalse(passed);
    }

    @Test
    @DisplayName("not pass the validation when input neither positive neither negative")
    public void notPassTheValidationWhenIsputIsIncorrect() {
        final boolean passed = validator.validate("O");
        assertFalse(passed);
    }
}
