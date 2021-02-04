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

package io.spine.examples.todolist.cli.action;

import io.spine.examples.todolist.cli.view.NoOpView;
import io.spine.examples.todolist.cli.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.cli.action.ActionFormatter.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AbstractAction should")
class AbstractActionTest {

    private static final String ACTION_NAME = "action";
    private static final Shortcut SHORTCUT = new Shortcut("s");
    private static final Action action = new AnAction(ACTION_NAME, SHORTCUT);

    @SuppressWarnings("ConstantConditions") // Purpose of the test.
    @Test
    @DisplayName("not allow null or empty name")
    void notAllowEmptyStrings() {
        String emptyString = "";
        assertThrows(IllegalArgumentException.class, () -> new AnAction(emptyString, SHORTCUT));
        assertThrows(IllegalArgumentException.class, () -> new AnAction(null, SHORTCUT));
    }

    @Test
    @DisplayName("have user-friendly representation")
    void haveUserFriendlyRepresentation() {
        assertEquals(format(action), action.toString());
    }

    @Test
    @DisplayName("consider an action with same shortcut equal")
    void overrideEqualsAndHashCode() {
        Action firstAction = action;
        String differentName = firstAction.getName() + "difference";
        Action secondAction = new NoOpAction(differentName, action.getShortcut());
        assertEquals(firstAction, secondAction);
        assertEquals(firstAction.hashCode(), secondAction.hashCode());
    }

    @Test
    @DisplayName("consider itself equal")
    void considerItselfEqual() {
        assertEquals(action, action);
    }

    @Test
    @DisplayName("consider classes not from Action hierarchy not equal")
    void considerNotActionNotEqual() {
        assertNotEquals(action, SHORTCUT);
    }

    private static class AnAction extends AbstractAction<View, View> {

        private AnAction(String name, Shortcut shortcut) {
            super(name, shortcut, new NoOpView(), new NoOpView());
        }

        @Override
        public void execute() {
            // Do noting.
        }
    }
}
