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

package io.spine.cli.view;

import io.spine.cli.Screen;
import io.spine.cli.UserIoTest;
import io.spine.cli.action.Action;
import io.spine.cli.action.Shortcut;
import io.spine.cli.action.TransitionAction;
import io.spine.cli.action.TransitionAction.TransitionActionProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.spine.cli.NoOpAction.noOpActionProducer;
import static io.spine.cli.action.TransitionAction.transitionProducer;
import static io.spine.cli.view.AbstractView.getBackShortcut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("AbstractView should")
class AbstractViewTest extends UserIoTest {

    private static final String ACTION_NAME = "action";
    private static final Shortcut SHORTCUT = new Shortcut("s");

    private final AbstractView view = new AView();

    @Test
    @DisplayName("clear all actions")
    void clearAllActions() {
        view.addAction(transitionProducer(ACTION_NAME, SHORTCUT, view));
        view.addAction(transitionProducer(ACTION_NAME, new Shortcut("s2"), view));
        view.clearActions();
        assertTrue(view.getActions()
                       .isEmpty());
    }

    @Test
    @DisplayName("add an action")
    void addAction() {
        final Set<Action> actions = view.getActions();
        final int sizeBeforeAddition = actions.size();

        view.addAction(transitionProducer(ACTION_NAME, SHORTCUT, view));

        final int sizeAfterAddition = sizeBeforeAddition + 1;
        assertEquals(sizeAfterAddition, actions.size());
    }

    @Test
    @DisplayName("not add null action")
    void notAddNullAction() {
        final NullProducer producer = new NullProducer(ACTION_NAME, SHORTCUT, view);
        assertThrows(NullPointerException.class, () -> view.addAction(producer));
    }

    @Test
    @DisplayName("not add the action with the back shortcut")
    void notAddActionWithBackShortcut() {
        assertThrows(IllegalArgumentException.class,
                     () -> view.addAction(transitionProducer(ACTION_NAME, getBackShortcut(), view)));
    }

    @Test
    @DisplayName("not add the action with an occupied shortcut")
    void notAddActionWithOccupiedShortcut() {
        final TransitionActionProducer<ActionListView, View> firstActionProducer =
                transitionProducer(ACTION_NAME, SHORTCUT, view);
        view.addAction(firstActionProducer);

        final String secondActionName = firstActionProducer.getName() + " difference";
        assertThrows(IllegalArgumentException.class,
                     () -> view.addAction(transitionProducer(secondActionName, SHORTCUT, view)));
    }

    @Test
    @DisplayName("ask about action selection while the shortcut is invalid")
    void askActionSelection() {
        view.addAction(noOpActionProducer(ACTION_NAME, SHORTCUT));

        addAnswer("invalid answer");
        addAnswer(SHORTCUT.getValue());

        view.render(getScreen());

        assertAllAnswersWereGiven();
    }

    private static class AView extends AbstractView {

        private AView() {
            super("View title");
        }

        @Override
        public void renderBody(Screen screen) {
        }
    }

    private static class NullProducer extends TransitionActionProducer<AbstractView, View> {

        private NullProducer(String name, Shortcut shortcut, View destination) {
            super(name, shortcut, destination);
        }

        @SuppressWarnings("ReturnOfNull") // Purpose of this class.
        @Override
        public TransitionAction<AbstractView, View> create(AbstractView source) {
            return null;
        }
    }
}
