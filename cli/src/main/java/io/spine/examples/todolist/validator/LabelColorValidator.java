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

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Serves as validator class for the label color input.
 *
 * <p>Validation will be passed if {@code colorMap} contains the specified color.
 *
 * @author Illia Shepilov
 */
public class LabelColorValidator implements Validator<String> {

    private static final String INCORRECT_INPUT = "Incorrect label color";

    private final Map<String, LabelColor> colorMap;
    private String message;

    public LabelColorValidator(Map<String, LabelColor> colorMap) {
        this.colorMap = colorMap;
    }

    @Override
    public boolean validate(String input) {
        final LabelColor labelColor = colorMap.get(input);
        final boolean passed = labelColor != null;
        if (!passed) {
            message = INCORRECT_INPUT;
        }
        return passed;
    }

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }

}
