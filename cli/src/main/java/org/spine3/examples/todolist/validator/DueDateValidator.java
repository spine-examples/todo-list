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

import org.spine3.examples.todolist.DateHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.spine3.examples.todolist.DateHelper.DATE_FORMAT;
import static org.spine3.examples.todolist.DateHelper.getDateFormat;
import static org.spine3.examples.todolist.validator.ValidatorHelper.isEmpty;
import static org.spine3.examples.todolist.validator.ValidatorHelper.isNull;

/**
 * Serves as validator class for the task due date input.
 *
 * <p>Validation will be passed when:
 *     <li>input is not {@code null};
 *     <li>input is not empty;
 *     <li>input has correct format according to the {@link DateHelper#getDateFormat()}.
 * <p>
 * @author Illia Shepilov
 */
public class DueDateValidator implements Validator {

    private static final String DUE_DATE_IS_NULL = "The due date cannot be null.";
    private static final String DUE_DATE_IS_EMPTY = "The due date cannot be empty.";
    private static final String INCORRECT_DUE_DATE = "Incorrect due date format. Correct format: " + DATE_FORMAT + '.';
    private String message;

    @Override
    public boolean validate(String input) {
        final boolean isNull = isNull(input);

        if (isNull) {
            message = DUE_DATE_IS_NULL;
            return false;
        }

        final boolean empty = isEmpty(input);
        if (empty) {
            message = DUE_DATE_IS_EMPTY;
            return false;
        }

        final boolean result = isCorrectFormat(input);
        return result;
    }

    private boolean isCorrectFormat(String input) {
        try {
            final SimpleDateFormat simpleDateFormat = getDateFormat();
            simpleDateFormat.parse(input);
        } catch (ParseException ignored) {
            message = INCORRECT_DUE_DATE;
            return false;
        }
        return true;
    }

    /**
     * Returns the message is indicated incorrect input by user.
     *
     * @return the warning message
     */
    public String getMessage() {
        return message;
    }
}
