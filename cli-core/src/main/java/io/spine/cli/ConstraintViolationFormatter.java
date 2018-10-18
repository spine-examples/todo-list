/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import io.spine.base.FieldPath;
import io.spine.validate.ConstraintViolation;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * Utilities for {@code ConstraintViolation} formatting.
 */
public class ConstraintViolationFormatter {

    @VisibleForTesting
    static final String ERROR_MSG_FORMAT = "Invalid `%s`.";

    private ConstraintViolationFormatter() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Formats the specified constraint violations into a user-friendly representation.
     *
     * @param violations the violations to format
     * @return formatted error messages
     */
    public static List<String> format(Iterable<ConstraintViolation> violations) {
        final List<String> messages = newLinkedList();
        for (ConstraintViolation violation : violations) {
            final FieldPath fieldPath = violation.getFieldPath();
            final int fieldPathSize = fieldPath.getFieldNameCount();
            final String unqualifiedName = fieldPath.getFieldName(fieldPathSize - 1);
            final String errorMessage = String.format(ERROR_MSG_FORMAT, unqualifiedName);
            messages.add(errorMessage);
        }
        return messages;
    }
}
