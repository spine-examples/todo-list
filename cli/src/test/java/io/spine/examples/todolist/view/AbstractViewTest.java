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

import io.spine.examples.todolist.UserIoTest;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.NoOpAction;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Dmytro Grankin
 */
@DisplayName("AbstractView should")
class AbstractViewTest extends UserIoTest {

    private static final String BACK_ACTION_NAME = "back";
    private static final Shortcut SHORTCUT = new Shortcut("b");

    private final AbstractView view = new AView();

    @Test
    @DisplayName("print formatted title")
    void printFormattedTitle() {
        new AView().render(getScreen());
        assertOutput(view.formatTitle() + lineSeparator());
    }

    @Test
    @DisplayName("create NoOpAction as a back action for a root view")
    void createNoOpBackAction() {
        view.setScreen(getScreen());
        final Action back = view.createBackAction(BACK_ACTION_NAME, SHORTCUT);

        assertEquals(BACK_ACTION_NAME, back.getName());
        assertEquals(SHORTCUT, back.getShortcut());
        assertThat(back, instanceOf(NoOpAction.class));
    }

    @Test
    @DisplayName("create usual back action for a child view")
    void createUsualBackAction() {
        final View rootView = new AView();
        final AbstractView childView = new AView();
        getScreen().renderView(rootView);
        getScreen().renderView(childView);

        final TransitionAction back =
                (TransitionAction) childView.createBackAction(BACK_ACTION_NAME, SHORTCUT);

        assertEquals(BACK_ACTION_NAME, back.getName());
        assertEquals(SHORTCUT, back.getShortcut());
        assertSame(rootView, back.getDestination());
        assertSame(childView, back.getSource());
    }

    private static class AView extends AbstractView {

        private AView() {
            super("View title");
        }

        @Override
        public void render() {
        }
    }
}
