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
import io.spine.examples.todolist.Edit;
import io.spine.examples.todolist.Screen;
import io.spine.examples.todolist.UserIoTest;
import io.spine.examples.todolist.view.CommandView;
import io.spine.validate.StringValueVBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.action.EditCommandAction.newProducer;
import static io.spine.examples.todolist.action.EditCommandActionTest.AnEdit.VALUE_AFTER_UPDATE;
import static io.spine.examples.todolist.view.ActionListView.getBackShortcut;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("EditCommandAction should")
class EditCommandActionTest extends UserIoTest {

    private static final String ACTION_NAME = "action";
    private static final Shortcut SHORTCUT = new Shortcut("s");

    private final ACommandView view = new ACommandView();
    private final EditCommandAction<StringValue, StringValueVBuilder> action =
            newProducer(ACTION_NAME, SHORTCUT, singleton(new AnEdit())).create(view);

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
        view.setScreen(getScreen());
    }

    @Test
    @DisplayName("not allow empty edits")
    void notAllowEmptyEdits() {
        assertThrows(IllegalArgumentException.class,
                     () -> newProducer(ACTION_NAME, SHORTCUT, emptyList()));
    }

    @Test
    @DisplayName("have same source and destination view")
    void haveSameSourceAndDestination() {
        assertSame(action.getSource(), action.getDestination());
    }

    @Test
    @DisplayName("render destination view")
    void renderDestinationView() {
        addAnswer(getBackShortcut().getValue());
        action.execute();
        assertTrue(view.wasRendered);
    }

    @Test
    @DisplayName("update state of a view")
    void updateViewState() {
        addAnswer(getBackShortcut().getValue());
        action.execute();
        assertEquals(VALUE_AFTER_UPDATE, view.getState()
                                             .build()
                                             .getValue());
    }

    private static class ACommandView extends CommandView<StringValue, StringValueVBuilder> {

        private boolean wasRendered;

        private ACommandView() {
            super("View title");
        }

        @Override
        public void render(Screen screen) {
            wasRendered = true;
            super.render(screen);
        }

        @Override
        protected String renderState(StringValueVBuilder state) {
            return "";
        }
    }

    static class AnEdit implements Edit<StringValue, StringValueVBuilder> {

        static final String VALUE_AFTER_UPDATE = "updated";

        @Override
        public void start(Screen screen, StringValueVBuilder state) {
            state.setValue(VALUE_AFTER_UPDATE);
        }
    }
}
