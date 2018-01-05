/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.examples.todolist.view

import com.google.protobuf.Timestamp
import com.google.protobuf.util.Timestamps
import java.text.SimpleDateFormat
import java.util.*

/**
 * The formatter for the date/time/date-time data types.
 */
object TimeFormatter {

    /**
     * The date format in which the due date is displayed on the UI.
     */
    private val FORMAT = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    /**
     * Formats the given [Timestamp].
     *
     * If the input timestamp is [default][io.spine.validate.Validate.isDefault] returns an empty
     * string.
     *
     * @param timestamp [Timestamp] to format
     * @return the formatted timestamp as a string
     */
    fun format(timestamp: Timestamp): String {
        if (timestamp.seconds == 0L) {
            return ""
        } else {
            val date = Date(Timestamps.toMillis(timestamp))
            val result = FORMAT.format(date)
            return result
        }
    }

}
