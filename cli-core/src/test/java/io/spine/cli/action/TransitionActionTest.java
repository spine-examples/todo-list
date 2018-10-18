/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.cli.action;

import io.spine.cli.NoOpView;
import io.spine.cli.Screen;
import io.spine.cli.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TransitionAction should")
class TransitionActionTest {

    private static final String ACTION_NAME = "static transition action";
    private static final Shortcut SHORTCUT = new Shortcut("s");

    @Test
    @DisplayName("render of a destination view")
    void renderDestinationView() {
        final View source = new NoOpView();
        final DisplayCounterView destination = new DisplayCounterView();
        final TransitionAction<View, DisplayCounterView> action =
                new TransitionAction<>(ACTION_NAME, SHORTCUT, source, destination);

        assertEquals(0, destination.displayedTimes);
        action.execute();
        assertEquals(1, destination.displayedTimes);
    }

    static class DisplayCounterView implements View {

        private int displayedTimes = 0;

        @Override
        public void render(Screen screen) {
            displayedTimes++;
        }
    }
}
