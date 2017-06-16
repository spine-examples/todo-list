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

package io.spine.examples.todolist.mode.action;

import io.spine.base.FieldPath;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ValidationException;

/**
 * @author Dmytro Grankin
 */
class ValidationExceptionFormatter {

    private ValidationExceptionFormatter() {
        // Prevent instantiation of this utility class.
    }

    static String format(ValidationException e) {
        final ConstraintViolation violation = e.getConstraintViolations()
                                               .get(0);
        final FieldPath fieldPath = violation.getFieldPath();
        final int fieldPathSize = fieldPath.getFieldNameCount();
        final String unqualifiedName = fieldPath.getFieldName(fieldPathSize - 1);
        return String.format("Invalid field `%s`.", unqualifiedName);
    }
}
