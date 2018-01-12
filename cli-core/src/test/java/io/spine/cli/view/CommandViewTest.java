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

package io.spine.cli.view;

import com.google.protobuf.StringValue;
import io.spine.cli.Bot;
import io.spine.cli.Screen;
import io.spine.cli.action.Action;
import io.spine.cli.action.Shortcut;
import io.spine.validate.StringValueVBuilder;
import io.spine.validate.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.cli.NoOpAction.noOpActionProducer;
import static io.spine.cli.view.CommandView.GenericParameter.COMMAND_MESSAGE;
import static io.spine.cli.view.CommandView.GenericParameter.STATE_BUILDER;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("CommandView should")
class CommandViewTest {

    private static final String ACTION_NAME = "quit";
    private static final Shortcut QUIT_SHORTCUT = new Shortcut("q");

    private Bot bot;
    private final TestCommandView view = new TestCommandView();

    @BeforeEach
    void setUp() {
        bot = new Bot();
    }

    @Test
    @DisplayName("render state representation")
    void renderStateRepresentation() {
        view.renderBody(bot.screen());
        final String expectedBody = view.renderState(StringValueVBuilder.newBuilder()) +
                lineSeparator();
        bot.assertOutput(expectedBody);
    }

    @Test
    @DisplayName("wrap ValidationException and re-render itself")
    void displayViewOnValidationException() {
        final Action throwVException = new ThrowValidationExceptionAction();
        assertThrows(ValidationException.class, throwVException::execute);
        assertFalse(view.wasRendered);

        view.addAction(noOpActionProducer(ACTION_NAME, QUIT_SHORTCUT));
        bot.addAnswer(QUIT_SHORTCUT.getValue());
        view.executeAction(new ThrowValidationExceptionAction());

        assertTrue(view.wasRendered);
    }

    @Nested
    @DisplayName("GenericParameter should")
    class GenericParameterTest {

        @Test
        @DisplayName("obtain classes for the generic parameters")
        void obtainClassesForGenericParams() {
            assertEquals(StringValue.class,
                         COMMAND_MESSAGE.getArgumentIn(TestCommandView.class));
            assertEquals(StringValueVBuilder.class,
                         STATE_BUILDER.getArgumentIn(TestCommandView.class));
        }
    }

    @Nested
    @DisplayName("TypeInfo should")
    class TypeInfoTest {

        @Test
        @DisplayName("have the private constructor")
        void havePrivateCtor() {
            assertHasPrivateParameterlessCtor(CommandView.TypeInfo.class);
        }
    }

    private static class TestCommandView extends CommandView<StringValue, StringValueVBuilder> {

        private boolean wasRendered = false;

        private TestCommandView() {
            super("View title");
        }

        @Override
        protected void renderBody(Screen screen) {
            wasRendered = true;
            super.renderBody(screen);
        }

        @Override
        protected String renderState(StringValueVBuilder state) {
            return "state";
        }
    }

    private static class ThrowValidationExceptionAction implements Action {

        private static final String UNSUPPORTED_MSG =
                "This class should not be used for this goal.";

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

        @Override
        public View getSource() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }

        @Override
        public View getDestination() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
    }
}
