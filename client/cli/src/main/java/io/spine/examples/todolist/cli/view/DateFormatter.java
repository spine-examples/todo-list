/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.cli.view;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Timestamp;
import io.spine.time.Temporal;
import io.spine.time.Temporals;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.google.protobuf.util.Timestamps.toMillis;

/**
 * Formats a date into a user-friendly representation.
 */
final class DateFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @VisibleForTesting
    static final String DEFAULT_TIMESTAMP_VALUE = "default";

    /**
     * Prevents instantiation of this utility class.
     */
    private DateFormatter() {
    }

    /**
     * Formats the {@code timestamp} into a date string of the {@code yyyy-MM-dd} format.
     *
     * <p>If the {@code timestamp} milliseconds is equal to {@code 0} returns a constant
     * {@code default} string.
     */
    static String format(Timestamp timestamp) {
        long millis = toMillis(timestamp);
        return millis == 0
               ? DEFAULT_TIMESTAMP_VALUE
               : formatTimestamp(timestamp);
    }

    @SuppressWarnings("FromTemporalAccessor") // we're sure that Timestamp has required fields
    private static String formatTimestamp(Timestamp timestamp) {
        Temporal<?> temporal = Temporals.from(timestamp);
        LocalDateTime localDate = LocalDateTime.ofInstant(temporal.toInstant(), ZoneOffset.UTC);
        return FORMATTER.format(localDate);
    }
}
