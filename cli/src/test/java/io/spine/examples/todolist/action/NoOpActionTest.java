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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.action.ActionFormatter.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("NoOpAction should")
class NoOpActionTest {

    private static final String ACTION_NAME = "action";
    private static final Shortcut SHORTCUT = new Shortcut("s");
    private static final Action action = new NoOpAction(ACTION_NAME, SHORTCUT);

    @Test
    @DisplayName("not allow null or empty name")
    void notAllowEmptyStrings() {
        final String emptyString = "";
        assertThrows(IllegalArgumentException.class, () -> new NoOpAction(emptyString, SHORTCUT));
        assertThrows(IllegalArgumentException.class, () -> new NoOpAction(null, SHORTCUT));
    }

    @Test
    @DisplayName("consider an action with same shortcut equal")
    void overrideEqualsAndHashCode() {
        final String differentName = action.getName() + "difference";
        final Action secondAction = new NoOpAction(differentName, action.getShortcut());
        assertEquals(action, secondAction);
        assertEquals(action.hashCode(), secondAction.hashCode());
    }

    @Test
    @DisplayName("override `toString`")
    void overrideToString() {
        assertEquals(format(action), action.toString());
    }
}
