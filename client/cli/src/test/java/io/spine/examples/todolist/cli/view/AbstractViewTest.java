/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.cli.view;

import io.spine.examples.todolist.cli.Bot;
import io.spine.examples.todolist.cli.Screen;
import io.spine.examples.todolist.cli.action.Action;
import io.spine.examples.todolist.cli.action.Shortcut;
import io.spine.examples.todolist.cli.action.TransitionAction;
import io.spine.examples.todolist.cli.action.TransitionAction.TransitionActionProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.spine.examples.todolist.cli.action.NoOpAction.noOpActionProducer;
import static io.spine.examples.todolist.cli.action.TransitionAction.transitionProducer;
import static io.spine.examples.todolist.cli.view.AbstractView.getBackShortcut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AbstractView should")
class AbstractViewTest {

    private static final String ACTION_NAME = "action";
    private static final Shortcut SHORTCUT = new Shortcut("s");

    private Bot bot;
    private final AbstractView view = new AView("abstract view");

    @BeforeEach
    void setUp() {
        bot = new Bot();
    }

    @SuppressWarnings("ConstantConditions") // Purpose of the test.
    @Test
    @DisplayName("not allow null or empty title")
    void notAllowNullOrEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> new AView(null));
        assertThrows(IllegalArgumentException.class, () -> new AView(""));
    }

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
        Set<Action> actions = view.getActions();
        int sizeBeforeAddition = actions.size();

        view.addAction(transitionProducer(ACTION_NAME, SHORTCUT, view));

        int sizeAfterAddition = sizeBeforeAddition + 1;
        assertEquals(sizeAfterAddition, actions.size());
    }

    @Test
    @DisplayName("not add null action")
    void notAddNullAction() {
        NullProducer producer = new NullProducer(ACTION_NAME, SHORTCUT, view);
        assertThrows(NullPointerException.class, () -> view.addAction(producer));
    }

    @Test
    @DisplayName("not add the action with the back shortcut")
    void notAddActionWithBackShortcut() {
        assertThrows(IllegalArgumentException.class,
                     () -> view.addAction(
                             transitionProducer(ACTION_NAME, getBackShortcut(), view)));
    }

    @Test
    @DisplayName("not add the action with an occupied shortcut")
    void notAddActionWithOccupiedShortcut() {
        TransitionActionProducer<ActionListView, View> firstActionProducer =
                transitionProducer(ACTION_NAME, SHORTCUT, view);
        view.addAction(firstActionProducer);

        String secondActionName = firstActionProducer.getName() + " difference";
        assertThrows(IllegalArgumentException.class,
                     () -> view.addAction(transitionProducer(secondActionName, SHORTCUT, view)));
    }

    @Test
    @DisplayName("ask about action selection while the shortcut is invalid")
    void askActionSelection() {
        view.addAction(noOpActionProducer(ACTION_NAME, SHORTCUT));

        bot.addAnswer("invalid answer");
        bot.addAnswer(SHORTCUT.getValue());

        bot.screen()
           .renderView(view);
        bot.assertAllAnswersWereGiven();
    }

    private static class AView extends AbstractView {

        private AView(String title) {
            super(title);
        }

        @Override
        public void renderBody(Screen screen) {
            // Do nothing.
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
