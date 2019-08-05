/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.cli.action.ActionFormatter.SHORTCUT_FORMAT;
import static io.spine.examples.todolist.cli.action.ActionFormatter.SHORTCUT_NAME_SEPARATOR;
import static io.spine.examples.todolist.cli.action.ActionFormatter.format;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ActionFormatter should")
class ActionFormatterTest {

    private static final Shortcut SHORTCUT = new Shortcut("s");

    @Test
    @DisplayName("have the private constructor")
    void havePrivateCtor() {
        assertHasPrivateParameterlessCtor(ActionFormatter.class);
    }

    @Test
    @DisplayName("format Shortcut")
    void formatShortcut() {
        String expected = String.format(SHORTCUT_FORMAT, SHORTCUT);
        assertEquals(expected, format(SHORTCUT));
    }

    @Test
    @DisplayName("format Action")
    void formatAction() {
        Action action = new NoOpAction("action", SHORTCUT);
        String formattedShortcut = format(action.getShortcut());
        String expected = formattedShortcut + SHORTCUT_NAME_SEPARATOR + action.getName();
        assertEquals(expected, format(action));
    }
}
