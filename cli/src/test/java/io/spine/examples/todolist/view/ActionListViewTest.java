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
import io.spine.examples.todolist.action.PseudoAction;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.StaticTransitionAction;
import io.spine.examples.todolist.action.StaticTransitionAction.StaticTransitionActionProducer;
import io.spine.examples.todolist.action.TransitionAction.TransitionActionProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.view.ActionListView.getBackShortcut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("ActionListView should")
class ActionListViewTest extends UserIoTest {

    private static final String ACTION_NAME = "action";
    private static final Shortcut SHORTCUT = new Shortcut("a");

    private final ActionListView view = new ActionListView(true);

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        view.setUserCommunicator(getCommunicator());
    }

    @Test
    @DisplayName("not add null action")
    void notAddNullAction() {
        final NullProducer producer = new NullProducer(ACTION_NAME, SHORTCUT);
        assertThrows(NullPointerException.class, () -> view.addAction(producer));
    }

    @Test
    @DisplayName("not add the action with the back shortcut")
    void notAddActionWithBackShortcut() {
        assertThrows(IllegalArgumentException.class,
                     () -> view.addAction(newProducer(ACTION_NAME, getBackShortcut())));
    }

    @Test
    @DisplayName("not add the action with an occupied shortcut")
    void notAddActionWithOccupiedShortcut() {
        final Action firstAction = new PseudoAction(ACTION_NAME, SHORTCUT);
        view.addAction(firstAction);

        final String secondActionName = firstAction.getName() + " difference";
        assertThrows(IllegalArgumentException.class,
                     () -> view.addAction(newProducer(secondActionName, SHORTCUT)));
    }

    @Test
    @DisplayName("add back action without duplications")
    void addBackAction() {
        final int actionCountBefore = view.getActions()
                                          .size();
        final int expectedCount = actionCountBefore + 1;

        view.addBackAction();
        view.addBackAction(); // To check that there are no duplications after second call.

        assertEquals(expectedCount, view.getActions()
                                        .size());
    }

    @Test
    @DisplayName("ask about action selection while the shortcut is invalid")
    void askActionSelection() {
        final String validAnswer = getBackShortcut().getValue();
        addAnswer("invalid answer");
        addAnswer(validAnswer);

        view.display();

        assertAllAnswersWereGiven();
    }

    private StaticTransitionActionProducer<ActionListView, View> newProducer(String name,
                                                                             Shortcut shortcut) {
        return new StaticTransitionActionProducer<>(name, shortcut, view);
    }

    private static class NullProducer
            extends TransitionActionProducer<ActionListView,
                                             View,
                                             StaticTransitionAction<ActionListView, View>> {

        private NullProducer(String name, Shortcut shortcut) {
            super(name, shortcut);
        }

        @SuppressWarnings("ReturnOfNull") // Purpose of this class.
        @Override
        public StaticTransitionAction<ActionListView, View> create(ActionListView source) {
            return null;
        }
    }
}
