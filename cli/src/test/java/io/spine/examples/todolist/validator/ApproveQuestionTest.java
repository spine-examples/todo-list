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

import io.spine.examples.todolist.UserIoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.validator.ApproveAnswerValidator.getNegativeAnswer;
import static io.spine.examples.todolist.validator.ApproveAnswerValidator.getPositiveAnswer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("ApproveQuestion should")
class ApproveQuestionTest extends UserIoTest {

    private static final String QUESTION = "?";

    private final ApproveQuestion approveQuestion = new ApproveQuestion();

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        approveQuestion.setIoFacade(getIoFacade());
    }

    @Test
    @DisplayName("return true for a positive answer")
    void returnTrueForPositiveAnswer() {
        addAnswer(getPositiveAnswer());
        final boolean result = approveQuestion.ask(QUESTION);
        assertTrue(result);
    }

    @Test
    @DisplayName("return false for a negative answer")
    void returnFalseForNegativeAnswer() {
        addAnswer(getNegativeAnswer());
        final boolean result = approveQuestion.ask(QUESTION);
        assertFalse(result);
    }

    @Test
    @DisplayName("ask a user again while question is not valid")
    void askAgainWhileAnswerIsNotValid() {
        final String invalidAnswer = "";
        addAnswer(invalidAnswer);
        addAnswer(getPositiveAnswer());

        approveQuestion.ask(QUESTION);
        assertAllAnswersWereGiven();
    }
}
