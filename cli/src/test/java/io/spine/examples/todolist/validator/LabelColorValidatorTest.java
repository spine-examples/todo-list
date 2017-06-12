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

import io.spine.examples.todolist.LabelColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
@DisplayName("LabelColorValidator should")
class LabelColorValidatorTest {

    private static final String CORRECT_LABEL_COLOR_KEY = "1";
    private static final String INCORRECT_LABEL_COLOR_KEY = "0";

    private Validator validator;

    @BeforeEach
    void setUp() {
        final Map<String, LabelColor> colorMap = newHashMap();
        colorMap.put(CORRECT_LABEL_COLOR_KEY, LabelColor.RED);
        validator = new LabelColorValidator(colorMap);
    }

    @Test
    @DisplayName("pass the validation")
    void passValidation() {
        final boolean passed = validator.validate(CORRECT_LABEL_COLOR_KEY);
        assertTrue(passed);
    }

    @Test
    @DisplayName("not pass the validation when color map does not contain value by specified key")
    void notPassValidation() {
        final boolean passed = validator.validate(INCORRECT_LABEL_COLOR_KEY);
        assertFalse(passed);
    }
}
