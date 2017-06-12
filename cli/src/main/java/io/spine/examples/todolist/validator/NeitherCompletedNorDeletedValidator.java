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

import javax.annotation.Nullable;

import static io.spine.examples.todolist.validator.ValidatorHelper.isEmpty;
import static io.spine.examples.todolist.validator.ValidatorHelper.isNull;

/**
 * Serves as a validator when is needed to validate the common cases.
 *
 * <p>Validation will be failed when input is empty or {@code null}.
 *
 * <p>In other cases validation will pass successfully.
 *
 * @author Illia Shepilov
 */
public class NeitherCompletedNorDeletedValidator implements Validator<String> {

    private static final String EMPTY_VALUE = "Inserted value cannot be empty.";
    private static final String VALUE_IS_NULL = "Inserted value cannot be null.";
    private String message;

    @Override
    public boolean validate(String input) {
        final boolean isNull = isNull(input);
        if (isNull) {
            message = VALUE_IS_NULL;
            return false;
        }

        final boolean empty = isEmpty(input);
        if (empty) {
            message = EMPTY_VALUE;
            return false;
        }

        return true;
    }

    @Override
    @Nullable
    public String getMessage() {
        return message;
    }
}
