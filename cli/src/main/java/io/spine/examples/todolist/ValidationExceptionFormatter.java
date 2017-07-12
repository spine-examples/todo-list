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

package io.spine.examples.todolist;

import com.google.common.annotations.VisibleForTesting;
import io.spine.base.FieldPath;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ValidationException;

import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;

/**
 * Utilities for {@code ValidationException} formatting.
 *
 * @author Dmytro Grankin
 */
public class ValidationExceptionFormatter {

    @VisibleForTesting
    static final String ERROR_MSG_FORMAT = "Invalid `%s`.";

    private ValidationExceptionFormatter() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Obtains error messages based on the specified {@code ValidationException}.
     *
     * <p>The error message tells about a name of an invalid field.
     *
     * @param e the {@code ValidationException}
     * @return error messages
     */
    public static List<String> toErrorMessages(ValidationException e) {
        final List<String> messages = new LinkedList<>();
        for (ConstraintViolation violation : e.getConstraintViolations()) {
            final FieldPath fieldPath = violation.getFieldPath();
            final int fieldPathSize = fieldPath.getFieldNameCount();
            final String unqualifiedName = fieldPath.getFieldName(fieldPathSize - 1);
            final String errorMessage = format(ERROR_MSG_FORMAT, unqualifiedName);
            messages.add(errorMessage);
        }
        return messages;
    }
}
