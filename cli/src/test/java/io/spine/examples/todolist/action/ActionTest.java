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

import static io.spine.examples.todolist.action.Action.getShortcutFormat;
import static io.spine.examples.todolist.action.Action.getShortcutNameSeparator;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("Action should")
class ActionTest {

    private static final String ACTION_NAME = "action";
    private static final String SHORTCUT = "s";
    private static final Action action = new ActionImpl(ACTION_NAME, SHORTCUT);

    @Test
    @DisplayName("not allow null or empty name or shortcut")
    void notAllowEmptyStrings() {
        final String invalid = "";
        final String valid = "s";
        assertThrows(IllegalArgumentException.class, () -> new ActionImpl(invalid, valid));
        assertThrows(IllegalArgumentException.class, () -> new ActionImpl(valid, invalid));
    }

    @Test
    @DisplayName("consider an action with same shortcut equal")
    void overrideEqualsAndHashCode() {
        final Action firstAction = action;
        final String differentName = action.getName() + "difference";
        final Action secondAction = new ActionImpl(differentName, action.getShortcut());
        assertEquals(firstAction, secondAction);
        assertEquals(firstAction.hashCode(), secondAction.hashCode());
    }

    @Test
    @DisplayName("override `toString`")
    void overrideToString() {
        final String formattedShortcut = format(getShortcutFormat(), SHORTCUT);
        final String expectedString = formattedShortcut + getShortcutNameSeparator() + ACTION_NAME;
        assertEquals(expectedString, action.toString());
    }

    private static class ActionImpl extends Action {

        private ActionImpl(String name, String shortcut) {
            super(name, shortcut);
        }

        @Override
        public void execute(View source) {
        }
    }
}
