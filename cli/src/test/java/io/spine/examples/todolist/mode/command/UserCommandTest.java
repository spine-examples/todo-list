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

package io.spine.examples.todolist.mode.command;

import io.spine.examples.todolist.test.CreateComment;
import io.spine.examples.todolist.test.CreateCommentVBuilder;
import io.spine.examples.todolist.test.NaturalNumberVBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.examples.todolist.mode.command.UserCommand.checkNotThrowsValidationEx;
import static io.spine.examples.todolist.mode.command.UserCommand.getErrorMessage;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Dmytro Grankin
 */
@DisplayName("UserCommand should")
class UserCommandTest {

    private static final int INVALID_NATURAL_NUMBER_VALUE = -1;
    private static final int VALID_NATURAL_NUMBER_VALUE = 1;

    @Test
    @DisplayName("be successfully executed if builder was configured properly")
    void successfullyExecuteCommand() {
        final Commenting commenting = new Commenting(true);
        commenting.start();
    }

    @Test
    @DisplayName("throw ISE if builder state was not prepared properly")
    void throwIllegalStateIfBuilderStateIsInvalid() {
        final Commenting commenting = new Commenting(false);
        assertThrows(IllegalStateException.class, commenting::start);
    }

    @Test
    @DisplayName("return error message if `ValidationException` was thrown")
    void returnErrorMessage() {
        final NaturalNumberVBuilder builder = NaturalNumberVBuilder.newBuilder();

        final Optional<String> errMsg =
                getErrorMessage(() -> builder.setValue(INVALID_NATURAL_NUMBER_VALUE));

        assertTrue(errMsg.isPresent());
    }

    @Test
    @DisplayName("return empty optional if `ValidationException` was not thrown")
    void notReturnErrorMessage() {
        final NaturalNumberVBuilder builder = NaturalNumberVBuilder.newBuilder();

        final Optional<String> errMsg =
                getErrorMessage(() -> builder.setValue(VALID_NATURAL_NUMBER_VALUE));

        assertFalse(errMsg.isPresent());
    }

    @Test
    @DisplayName("throw ISE if `ValidationException` was thrown")
    void notPassChecking() {
        final NaturalNumberVBuilder builder = NaturalNumberVBuilder.newBuilder();
        assertThrows(IllegalStateException.class,
                     () -> checkNotThrowsValidationEx(
                             () -> builder.setValue(INVALID_NATURAL_NUMBER_VALUE)));
    }

    @Test
    @DisplayName("not throw ISE if `ValidationException` was not thrown")
    void passChecking() {
        try {
            checkNotThrowsValidationEx(() -> {
                throw new RuntimeException("Something.");
            });
            fail("`RuntimeException` is expected.");
        } catch (RuntimeException ignored) {
        }
    }

    private static class Commenting
            extends UserCommand<CreateComment, CreateCommentVBuilder> {

        private static final String VALID_COMMENT_TEXT = "Non-empty";

        private final boolean setupBuilderProperly;

        private Commenting(boolean setupBuilderProperly) {
            this.setupBuilderProperly = setupBuilderProperly;
        }

        @Override
        protected void prepareBuilder() {
            if (setupBuilderProperly) {
                getBuilder().setValue(VALID_COMMENT_TEXT);
            }
        }

        @Override
        protected void postCommand(CreateComment commandMessage) {
        }
    }
}
