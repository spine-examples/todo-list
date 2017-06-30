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

package io.spine.examples.todolist.view;

import com.google.protobuf.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.google.protobuf.util.Timestamps.toMillis;

/**
 * Formats the data into a user-friendly representation.
 *
 * @author Illia Shepilov
 */
class DisplayFormatter {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_VALUE = "default";

    static final String LABEL_ID_VALUE = "Label id: ";
    static final String TASK_ID_VALUE = "Task id: ";
    static final String DESCRIPTION_VALUE = "Description: ";
    static final String PRIORITY_VALUE = "Priority: ";
    static final String DUE_DATE_VALUE = "Due date: ";

    private DisplayFormatter() {
        // Prevent instantiation of this utility class.
    }

    static String format(Timestamp timestamp) {
        final long millis = toMillis(timestamp);
        return millis == 0
               ? DEFAULT_VALUE
               : getDateFormat().format(new Date(millis));
    }

    private static SimpleDateFormat getDateFormat() {
        final SimpleDateFormat result = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return result;
    }
}