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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
@DisplayName("DescriptionValidator should")
class DescriptionValidatorTest {

    private DescriptionValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new DescriptionValidator();
    }

    @Test
    @DisplayName("pass the validation")
    public void passValidation() {
        final boolean passed = validator.validate("Description");
        assertTrue(passed);
    }

    @Test
    @DisplayName("not pass the validation when passed input is null")
    public void notPassValidationWhenInputIsNull() {
        final boolean passed = validator.validate(null);
        assertFalse(passed);
    }

    @Test
    @DisplayName("not pass the validation when passed input is empty")
    public void notPassValidationWhenInputIsEmpty() {
        final boolean passed = validator.validate("");
        assertFalse(passed);
    }

    @Test
    @DisplayName("not pass the validation when passed input has less then three symbols")
    public void notPassValidationWhenInputHasLessThenThreeSymbols() {
        final boolean passed = validator.validate("D");
        assertFalse(passed);
    }

    @Test
    @DisplayName("retrun non null message when validation is failed")
    public void getMessage() {
        validator.validate(null);
        assertNotNull(validator.getMessage());
    }
}
