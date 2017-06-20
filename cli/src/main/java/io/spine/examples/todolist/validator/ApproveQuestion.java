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

import io.spine.examples.todolist.UserCommunicator;
import io.spine.examples.todolist.UserCommunicatorImpl;

import static io.spine.examples.todolist.validator.ApproveAnswerValidator.getNegativeAnswer;
import static io.spine.examples.todolist.validator.ApproveAnswerValidator.getPositiveAnswer;

/**
 * @author Dmytro Grankin
 */
public class ApproveQuestion {

    private static final Validator<String> validator = new ApproveAnswerValidator();
    private static final UserCommunicator communicator = new UserCommunicatorImpl();

    private ApproveQuestion() {
        // Prevent instantiation of this utility class.
    }

    public static boolean ask(String question) {
        final String questionWithHelp =
                question + " (" + getPositiveAnswer() + '/' + getNegativeAnswer() + ") ";
        String answer = communicator.askUser(questionWithHelp);
        boolean isValidAnswer = validator.validate(answer);

        while (!isValidAnswer) {
            answer = communicator.askUser(validator.getAdvice());
            isValidAnswer = validator.validate(answer);
        }

        return answer.equals(getPositiveAnswer());
    }
}
