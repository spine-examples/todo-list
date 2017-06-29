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

import io.spine.examples.todolist.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("TransitionAction should")
abstract class AbstractTransitionActionTestSuite<T extends TransitionAction<
        AbstractTransitionActionTestSuite.DisplayCounterView>> {

    static final String ACTION_NAME = "static transition action";
    static final String SHORTCUT = "s";

    private final T action;

    protected AbstractTransitionActionTestSuite(T action) {
        checkArgument(action.getDestination().displayedTimes == 0);
        this.action = action;
    }

    static DisplayCounterView newDisplayCounterView() {
        return new DisplayCounterView();
    }

    static DisplayCounterView newDisplayCounterView(int displayedTimes) {
        return new DisplayCounterView(displayedTimes);
    }

    @Test
    @DisplayName("not accept null view to `execute()`")
    void notAcceptNullToExecute() {
        assertThrows(NullPointerException.class, () -> action.execute(null));
    }

    @Test
    @DisplayName("update source view")
    void updateSource() {
        assertNull(action.getSource());
        final DisplayCounterView expectedView = newDisplayCounterView();

        action.execute(expectedView);

        assertSame(expectedView, action.getSource());
    }

    @Test
    @DisplayName("display a destination view")
    void displayDestination() {
        assertEquals(0, action.getDestination()
                              .getDisplayedTimes());
        action.execute(newDisplayCounterView());
        assertEquals(1, action.getDestination()
                              .getDisplayedTimes());
    }

    @Test
    @DisplayName("not create reverse action if source view for the action is unknown")
    void notCreateReverseAction() {
        assertThrows(IllegalStateException.class,
                     () -> action.createReverseAction("r", "r"));
    }

    @Test
    @DisplayName("create reverse action")
    void createReverseAction() {
        final String reverseName = "Reverse";
        final String reverseShortcut = "r";
        final DisplayCounterView sourceOfAction = newDisplayCounterView();
        action.execute(sourceOfAction);

        final TransitionAction reverse = action.createReverseAction(reverseName, reverseShortcut);

        assertEquals(reverseName, reverse.getName());
        assertEquals(reverseShortcut, reverse.getShortcut());
        assertSame(action.getSource(), reverse.getDestination());
    }

    T getAction() {
        return action;
    }

    static class DisplayCounterView extends View {

        private int displayedTimes = 0;

        private DisplayCounterView() {
            super(true);
        }

        private DisplayCounterView(int displayedTimes) {
            this();
            this.displayedTimes = displayedTimes;
        }

        @Override
        public void display() {
            displayedTimes++;
        }

        public int getDisplayedTimes() {
            return displayedTimes;
        }
    }
}
