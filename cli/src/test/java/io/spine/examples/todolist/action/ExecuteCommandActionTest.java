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

package io.spine.examples.todolist.action;

import com.google.protobuf.StringValue;
import io.spine.examples.todolist.view.command.CommandView;
import io.spine.validate.StringValueVBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("ExecuteCommandAction should")
class ExecuteCommandActionTest {

    @Test
    @DisplayName("execute command and display a source view")
    void executeCommandAndDisplaySource() {
        final UpdateStringValueView view = new UpdateStringValueView();
        final UpdateStringValueAction action = new UpdateStringValueAction(view);

        action.execute();

        assertTrue(action.isUpdated());
        assertTrue(view.isDisplayed());
    }

    private static class UpdateStringValueAction
            extends ExecuteCommandAction<StringValue, StringValueVBuilder> {

        private boolean updated;

        private UpdateStringValueAction(CommandView<StringValue, StringValueVBuilder> source) {
            super(source);
        }

        @Override
        protected void executeCommand(StringValue commandMessage) {
            updated = true;
        }

        private boolean isUpdated() {
            return updated;
        }
    }

    private static class UpdateStringValueView extends CommandView<StringValue, StringValueVBuilder> {

        private boolean displayed;

        private UpdateStringValueView() {
            super(true, emptySet());
        }

        @Override
        protected void display() {
            displayed = true;
        }

        private boolean isDisplayed() {
            return displayed;
        }
    }
}
