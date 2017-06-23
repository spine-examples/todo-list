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

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("Action should")
class ActionTest {

    private static final String ACTION_NAME = "action";
    private static final String SHORTCUT = "s";

    private final DisplayCounterView view = new DisplayCounterView();
    private final Action action = new Action(ACTION_NAME, SHORTCUT, view);

    @Test
    @DisplayName("validate parameters are passed to the constructor")
    void validateCtorParams() {
        final String invalid = "";
        final String valid = "s";
        assertThrows(IllegalArgumentException.class, () -> new Action(invalid, valid, view));
        assertThrows(IllegalArgumentException.class, () -> new Action(valid, invalid, view));
        assertThrows(NullPointerException.class, () -> new Action(valid, valid, null));
    }

    @Test
    @DisplayName("update source view")
    void updateSource() {
        assertNull(action.getSource());
        action.execute(view);
        assertSame(view, action.getSource());
    }

    @Test
    @DisplayName("display a destination view")
    void displayDestination() {
        assertEquals(0, view.count);
        action.execute(view);
        assertEquals(1, view.count);
    }

    @Test
    @DisplayName("throw ISE on `back()` if source is null")
    void throwIllegalStateIfSourceNull() {
        assertNull(action.getSource());
        assertThrows(IllegalStateException.class, action::back);
    }

    @Test
    @DisplayName("display source view on `back()`")
    void displaySourceView() {
        assertEquals(0, view.count);
        action.execute(view);
        assertEquals(1, view.count);
        action.back();
        assertEquals(2, view.count);
    }

    @Test
    @DisplayName("consider an action with same shortcut equal")
    void overrideEqualsAndHashCode() {
        final Action firstAction = new Action(ACTION_NAME, SHORTCUT, view);
        final Action secondAction = new Action(ACTION_NAME + " fast", SHORTCUT,
                                               new DisplayCounterView());
        assertEquals(firstAction, secondAction);
        assertEquals(firstAction.hashCode(), secondAction.hashCode());
    }

    @Test
    @DisplayName("override `toString`")
    void overrideToString() {
        final String expectedString = '(' + SHORTCUT + ") " + ACTION_NAME;
        assertEquals(expectedString, action.toString());
    }

    private static class DisplayCounterView extends View {

        private int count = 0;

        private DisplayCounterView() {
            super(null);
        }

        @Override
        public void display() {
            count++;
        }
    }
}
