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

package io.spine.examples.todolist.cli;

import com.google.common.annotations.VisibleForTesting;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.lang.String.format;

/**
 * This class serves for asking for a confirmation.
 *
 * <p>Valid confirmation values are `y` and `n`.
 */
public final class Confirmation {

    @VisibleForTesting
    static final String POSITIVE_ANSWER = "y";

    @VisibleForTesting
    static final String NEGATIVE_ANSWER = "n";

    private static final String DETAILED_HINT = format(
            "Valid values: `%s` or `%s`.", POSITIVE_ANSWER, NEGATIVE_ANSWER
    );
    private static final String MINOR_HINT = format(
            "(%s/%s)", POSITIVE_ANSWER, NEGATIVE_ANSWER
    );

    /** Prevents instantiation of this utility class. */
    private Confirmation() {
    }

    /**
     * Obtains a confirmation value for the specified question.
     *
     * @param screen
     *         the screen to use
     * @param question
     *         the question to ask
     * @return {@code true} if the positive answer was given, {@code false} otherwise
     */
    public static boolean ask(Screen screen, String question) {
        checkNotNull(screen);
        checkNotEmptyOrBlank(question);
        String questionWithHint = question + ' ' + MINOR_HINT;
        Optional<String> answer = getValidAnswer(screen, questionWithHint);

        while (!answer.isPresent()) {
            answer = getValidAnswer(screen, DETAILED_HINT);
        }

        return POSITIVE_ANSWER.equals(answer.get());
    }

    /**
     * Obtains valid value of the answer for the specified question.
     *
     * @param screen
     *         the screen to use
     * @param question
     *         the question to ask
     * @return a valid answer value
     *         or {@code Optional.empty()} if the answer is not valid
     */
    private static Optional<String> getValidAnswer(Screen screen, String question) {
        String answer = screen.promptUser(question);
        boolean isValidAnswer = NEGATIVE_ANSWER.equals(answer) || POSITIVE_ANSWER.equals(answer);
        return isValidAnswer
               ? Optional.of(answer)
               : Optional.empty();
    }
}
