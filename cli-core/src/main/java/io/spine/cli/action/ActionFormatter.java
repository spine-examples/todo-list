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

package io.spine.cli.action;

import com.google.common.annotations.VisibleForTesting;

/**
 * Provides formatting for {@link Action}.
 */
public final class ActionFormatter {

    @VisibleForTesting
    static final String SHORTCUT_NAME_SEPARATOR = " ";

    @VisibleForTesting
    static final String SHORTCUT_FORMAT = "(%s)";

    /** Prevents instantiation of this utility class. */
    private ActionFormatter() {
    }

    /**
     * Obtains formatted representation of the specified {@link Action}.
     *
     * @param action
     *         the action to format
     * @return the formatted representation
     */
    public static String format(Action action) {
        return format(action.getShortcut()) + SHORTCUT_NAME_SEPARATOR + action.getName();
    }

    /**
     * Obtains formatted representation of the specified {@link Shortcut}.
     *
     * @param shortcut
     *         the shortcut to format
     * @return the formatted representation
     */
    public static String format(Shortcut shortcut) {
        return String.format(SHORTCUT_FORMAT, shortcut);
    }
}
