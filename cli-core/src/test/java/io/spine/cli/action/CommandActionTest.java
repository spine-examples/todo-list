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

package io.spine.cli.action;

import com.google.protobuf.StringValue;
import io.spine.cli.Screen;
import io.spine.cli.UserIoTest;
import io.spine.cli.view.CommandView;
import io.spine.validate.StringValueVBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.cli.NoOpAction.noOpActionProducer;
import static io.spine.validate.Validate.isDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("CommandAction should")
class CommandActionTest extends UserIoTest {

    private static final String ACTION_NAME = "quit";
    private static final Shortcut QUIT = new Shortcut("q");

    private final UpdateStringValueView view = new UpdateStringValueView();
    private final UpdateStringValueAction action = new UpdateStringValueAction(view);

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
        view.addAction(noOpActionProducer(ACTION_NAME, QUIT));
        addAnswer(QUIT.getValue());
    }

    @Test
    @DisplayName("have same source and destination view")
    void haveSameSourceAndDestination() {
        assertSame(action.getSource(), action.getDestination());
    }

    @Test
    @DisplayName("execute command and render a source view")
    void executeCommandAndRenderSource() {
        final String expectedString = "A string value.";
        final StringValueVBuilder viewState = view.getState();
        viewState.setValue(expectedString);
        action.execute();

        assertEquals(expectedString, action.getCommandMessageBeforeExecution()
                                           .getValue());
        assertTrue(view.wasRendered());
    }

    @Test
    @DisplayName("clear state of source view after successful execution")
    void clearSourceState() {
        final StringValueVBuilder viewState = view.getState();
        viewState.setValue("Non-default value");
        action.execute();
        assertTrue(isDefault(viewState.internalBuild()));
    }

    private static class UpdateStringValueAction extends CommandAction<StringValue,
                                                                           StringValueVBuilder> {

        private StringValue commandMessageBeforeExecution;

        private UpdateStringValueAction(CommandView<StringValue, StringValueVBuilder> source) {
            super(source);
        }

        @Override
        protected void post(StringValue commandMessage) {
            commandMessageBeforeExecution = commandMessage;
        }

        private StringValue getCommandMessageBeforeExecution() {
            return commandMessageBeforeExecution;
        }
    }

    private static class UpdateStringValueView extends CommandView<StringValue, StringValueVBuilder> {

        private boolean rendered;

        private UpdateStringValueView() {
            super("View title");
        }

        @Override
        protected void renderBody(Screen screen) {
            rendered = true;
        }

        @Override
        protected String renderState(StringValueVBuilder state) {
            return String.valueOf(rendered);
        }

        private boolean wasRendered() {
            return rendered;
        }
    }
}
