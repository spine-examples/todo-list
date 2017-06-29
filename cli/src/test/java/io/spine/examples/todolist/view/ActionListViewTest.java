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
import io.spine.examples.todolist.action.StaticTransitionAction;
import io.spine.examples.todolist.action.TransitionAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static io.spine.examples.todolist.view.ActionListView.getBackShortcut;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("ActionListView should")
class ActionListViewTest extends UserIoTest {

    private final ActionListView mainView = new MainMenu(emptySet());
    private final View childView = new ChildView();

    @BeforeEach
    void setUp() {
        mainView.setUserCommunicator(getCommunicator());
    }

    @Test
    @DisplayName("not allow actions with back shortcut")
    void notAllowActionsWithBackShortcut() {
        final String backShortcut = getBackShortcut();
        final TransitionAction action = new StaticTransitionAction<>("action", backShortcut, childView);
        final Set<Action> actions = singleton(action);
        assertThrows(IllegalArgumentException.class, () -> new MainMenu(actions));
    }

    @Test
    @DisplayName("not allow addition of action with occupied shortcut")
    void notAllowActionWithOccupiedShortcut() {
        final String shortcut = "s";
        final Action firstAction = new StaticTransitionAction<>("to child", shortcut, childView);
        final Action secondAction = new StaticTransitionAction<>("to second child", shortcut,
                                                         new ChildView());
        mainView.addAction(firstAction);

        assertThrows(IllegalArgumentException.class, () -> mainView.addAction(secondAction));
    }

    @Test
    @DisplayName("add back action without duplications")
    void addBackAction() {
        final int actionCountBefore = mainView.getActions()
                                              .size();
        final int expectedCount = actionCountBefore + 1;

        mainView.addBackAction();
        mainView.addBackAction(); // To check that there are no duplications after second call.

        assertEquals(expectedCount, mainView.getActions()
                                            .size());
    }

    @Test
    @DisplayName("ask about action selection while the shortcut is invalid")
    void askActionSelection() {
        final String validAnswer = getBackShortcut();
        addAnswer("invalid answer");
        addAnswer(validAnswer);

        mainView.display();

        assertAllAnswersWereGiven();
    }

    private static class MainMenu extends ActionListView {

        private MainMenu(Collection<Action> actions) {
            super(true, actions);
        }
    }

    private static class ChildView extends View {

        private ChildView() {
            super(false);
        }

        @Override
        public void display() {
        }
    }
}
