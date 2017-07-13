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

package io.spine.examples.todolist.view;

import com.google.protobuf.StringValue;
import io.spine.examples.todolist.UserIoTest;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.validate.StringValueVBuilder;
import io.spine.validate.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.view.ActionListView.getBackShortcut;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("CommandView should")
class CommandViewTest extends UserIoTest {

    private static final Shortcut BACK_SHORTCUT = getBackShortcut();

    private final ACommandView view = new ACommandView();

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        view.setScreen(getScreen());
    }

    @Test
    @DisplayName("render state representation")
    void renderStateRepresentation() {
        view.renderBody();
        final String expectedBody = view.renderState(StringValueVBuilder.newBuilder()) + lineSeparator();
        assertOutput(expectedBody);
    }

    @Test
    @DisplayName("wrap ValidationException and re-render itself")
    void displayViewOnValidationException() {
        final Action throwVException = new ThrowValidationExceptionAction();
        assertThrows(ValidationException.class, throwVException::execute);
        assertFalse(view.wasDisplayed);

        addAnswer(BACK_SHORTCUT.getValue());
        view.executeAction(new ThrowValidationExceptionAction());

        assertTrue(view.wasDisplayed);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    @Nested
    @DisplayName("TypeInfo should")
    class TypeInfoTest {

        @Test
        @DisplayName("have the private constructor")
        void havePrivateCtor() {
            assertHasPrivateParameterlessCtor(CommandView.TypeInfo.class);
        }
    }

    private static class ACommandView extends CommandView<StringValue, StringValueVBuilder> {

        private boolean wasDisplayed = false;

        private ACommandView() {
            super("View title");
        }

        @Override
        protected void renderBody() {
            wasDisplayed = true;
            super.renderBody();
        }

        @Override
        protected String renderState(StringValueVBuilder state) {
            return "state";
        }
    }

    private static class ThrowValidationExceptionAction implements Action {

        @Override
        public void execute() {
            throw new ValidationException(emptyList());
        }

        @Override
        public String getName() {
            return "throws ValidationException";
        }

        @Override
        public Shortcut getShortcut() {
            return new Shortcut("t");
        }
    }
}
