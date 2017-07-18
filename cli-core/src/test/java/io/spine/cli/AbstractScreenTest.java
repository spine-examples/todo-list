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

package io.spine.cli;

import io.spine.cli.action.Shortcut;
import io.spine.cli.action.TransitionAction;
import io.spine.cli.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("AbstractScreen should")
class AbstractScreenTest {

    private static final String BACK_NAME = "back";
    private static final Shortcut BACK_SHORTCUT = new Shortcut("b");

    private final AbstractScreen screen = new AnAbstractScreen();

    @Test
    @DisplayName("render a view")
    void renderView() {
        final NoOpView view = new NoOpView();
        screen.renderView(view);
        assertTrue(view.wasRendered());
    }

    @Test
    @DisplayName("not modify history on a current view rendering")
    void notModifyHistoryOnCurrentViewRendering() {
        final View view = new NoOpView();

        screen.renderView(view);
        final List<View> expectedHistory = screen.getHistory();

        screen.renderView(view);
        assertEquals(expectedHistory, screen.getHistory());
    }

    @Test
    @DisplayName("make previous view current on its rendering")
    void makePreviousViewCurrent() {
        final View first = new NoOpView();
        final View second = new NoOpView();

        screen.renderView(first);
        screen.renderView(second);

        final List<View> history = screen.getHistory();
        final List<View> historyWithoutCurrentView =
                new ArrayList<>(history.subList(0, history.size() - 1));

        screen.renderView(first);

        assertEquals(historyWithoutCurrentView, screen.getHistory());
    }

    @Test
    @DisplayName("not create back action if there is not back destination")
    void notCreateBackAction() {
        final View view = new NoOpView();
        screen.renderView(view);
        final Optional<TransitionAction<View, View>> back = screen.createBackAction(BACK_NAME,
                                                                                    BACK_SHORTCUT);
        assertFalse(back.isPresent());
    }

    @Test
    @DisplayName("create back action if there is back destination")
    void createBackAction() {
        final View first = new NoOpView();
        final View second = new NoOpView();
        final View third = new NoOpView();

        screen.renderView(first);
        screen.renderView(second);
        screen.renderView(third);

        final TransitionAction<View, View> back = screen.createBackAction(BACK_NAME, BACK_SHORTCUT)
                                                        .get();
        assertEquals(BACK_NAME, back.getName());
        assertEquals(BACK_SHORTCUT, back.getShortcut());
        assertEquals(third, back.getSource());
        assertEquals(second, back.getDestination());
    }

    @Test
    @DisplayName("create back action and does not modify view history")
    void createBackAndNotModifyHistory() {
        final View first = new NoOpView();
        final View second = new NoOpView();
        screen.renderView(first);
        screen.renderView(second);

        final List<View> expectedHistory = screen.getHistory();
        screen.createBackAction(BACK_NAME, BACK_SHORTCUT);
        assertEquals(expectedHistory, screen.getHistory());
    }

    private static class AnAbstractScreen extends AbstractScreen {

        private static final String UNSUPPORTED_MSG = "Not implemented in AbstractScreen.";

        @Override
        public String promptUser(String prompt) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }

        @Override
        public void println(String message) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
    }
}
