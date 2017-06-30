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

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.UserCommunicator;
import io.spine.examples.todolist.UserCommunicatorImpl;

import java.util.Optional;

import static io.spine.examples.todolist.validator.ApproveAnswerValidator.getNegativeAnswer;
import static io.spine.examples.todolist.validator.ApproveAnswerValidator.getPositiveAnswer;
import static java.lang.String.format;

/**
 * Utilities for asking an approve question.
 *
 * @author Dmytro Grankin
 */
public class ApproveQuestion {

    private static final String TIP_FORMAT = "(%s/%s)";

    private final Validator<String> validator = new ApproveAnswerValidator();
    private UserCommunicator communicator = new UserCommunicatorImpl();

    /**
     * Obtains an answer for the specified question.
     *
     * @param question the question to ask
     * @return {@code true} if the positive answer was given, {@code false} otherwise
     */
    public boolean ask(String question) {
        final String questionWithTip = question + getTip();
        Optional<String> answer = getValidAnswer(questionWithTip);

        while (!answer.isPresent()) {
            answer = getValidAnswer(getExtendedTip());
        }

        return answer.get()
                     .equals(getPositiveAnswer());
    }

    /**
     * Obtains valid value of the answer for the specified question.
     *
     * @param question the question to ask
     * @return a valid answer value
     *         or {@code Optional.empty()} if the answer is not valid
     */
    private Optional<String> getValidAnswer(String question) {
        final String answer = communicator.promptUser(question);
        boolean isValidAnswer = validator.validate(answer);
        return isValidAnswer
               ? Optional.of(answer)
               : Optional.empty();
    }

    private String getExtendedTip() {
        return validator.getAdvice();
    }

    private static String getTip() {
        return format(TIP_FORMAT, getPositiveAnswer(), getNegativeAnswer());
    }

    @VisibleForTesting
    void setCommunicator(UserCommunicator communicator) {
        this.communicator = communicator;
    }
}
