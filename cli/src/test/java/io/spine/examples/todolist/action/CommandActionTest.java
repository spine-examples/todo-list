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
import io.spine.examples.todolist.UserIoTest;
import io.spine.examples.todolist.view.ActionListView;
import io.spine.examples.todolist.view.command.CommandView;
import io.spine.validate.StringValueVBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.action.CommandActionTest.ACommandAction.VALUE_AFTER_UPDATE;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("CommandAction should")
class CommandActionTest extends UserIoTest {

    private final CommandView<StringValue, StringValueVBuilder> view = new ACommandView();
    private final ACommandAction action = new ACommandAction();

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
        view.setUserCommunicator(getCommunicator());
    }

    @Test
    @DisplayName("not allow null source")
    void notAllowNullSource() {
        assertThrows(NullPointerException.class, () -> action.execute(null));
    }

    @Test
    @DisplayName("update state of a view")
    void updateViewState() {
        addAnswer(ActionListView.getBackShortcut()
                                .getValue());
        action.execute(view);
        assertEquals(VALUE_AFTER_UPDATE, view.getState()
                                             .build()
                                             .getValue());
    }

    static class ACommandAction extends CommandAction<StringValue, StringValueVBuilder> {

        static final String VALUE_AFTER_UPDATE = "updated";

        private ACommandAction() {
            super("name", new Shortcut("s"));
        }

        @Override
        protected void updateState(StringValueVBuilder state) {
            state.setValue(VALUE_AFTER_UPDATE);
        }
    }

    private static class ACommandView extends CommandView<StringValue, StringValueVBuilder> {

        private ACommandView() {
            super(true, emptySet());
        }
    }
}
