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

import io.spine.examples.todolist.TestScreen;
import io.spine.examples.todolist.view.AbstractView;
import io.spine.examples.todolist.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.Given.newNoOpView;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dmytro Grankin
 */
@DisplayName("TransitionAction should")
class TransitionActionTest {

    private static final String ACTION_NAME = "static transition action";
    private static final Shortcut SHORTCUT = new Shortcut("s");

    @Test
    @DisplayName("cause render of a destination view")
    void causeDestinationViewRender() {
        final AbstractView source = newNoOpView();
        source.setScreen(new TestScreen());
        final TransitionAction<View, DisplayCounterView> action =
                new TransitionAction<>(ACTION_NAME, SHORTCUT, source, new DisplayCounterView());

        assertEquals(0, action.getDestination()
                              .getDisplayedTimes());
        action.execute();
        assertEquals(1, action.getDestination()
                              .getDisplayedTimes());
    }

    static class DisplayCounterView extends AbstractView {

        private int displayedTimes = 0;

        private DisplayCounterView() {
            super("View title");
        }

        @Override
        public void render() {
            displayedTimes++;
        }

        private int getDisplayedTimes() {
            return displayedTimes;
        }
    }
}
