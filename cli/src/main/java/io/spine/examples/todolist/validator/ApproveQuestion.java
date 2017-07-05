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
import io.spine.examples.todolist.IoFacade;

import java.util.Optional;

import static io.spine.examples.todolist.AppConfig.getIoFacadeFactory;
import static io.spine.examples.todolist.validator.ApproveAnswerValidator.getNegativeAnswer;
import static io.spine.examples.todolist.validator.ApproveAnswerValidator.getPositiveAnswer;
import static java.lang.String.format;

/**
 * Utilities for asking an approve question.
 *
 * @author Dmytro Grankin
 */
class ApproveQuestion {

    private static final String HINT_FORMAT = "(%s/%s)";

    private final Validator<String> validator = new ApproveAnswerValidator();
    private IoFacade ioFacade = getIoFacadeFactory().newInstance();

    /**
     * Obtains an answer for the specified question.
     *
     * @param question the question to ask
     * @return {@code true} if the positive answer was given, {@code false} otherwise
     */
    boolean ask(String question) {
        final String questionWithHint = question + ' ' + getHint();
        Optional<String> answer = getValidAnswer(questionWithHint);

        while (!answer.isPresent()) {
            answer = getValidAnswer(getExtendedHint());
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
        final String answer = ioFacade.promptUser(question);
        boolean isValidAnswer = validator.validate(answer);
        return isValidAnswer
               ? Optional.of(answer)
               : Optional.empty();
    }

    private String getExtendedHint() {
        return validator.getHint();
    }

    private static String getHint() {
        return format(HINT_FORMAT, getPositiveAnswer(), getNegativeAnswer());
    }

    @VisibleForTesting
    void setIoFacade(IoFacade ioFacade) {
        this.ioFacade = ioFacade;
    }
}
