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

import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.NoOpAction;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("View should")
class ViewTest {

    private static final Shortcut SHORTCUT = new Shortcut("b");

    private final RootView rootView = new RootView();
    private final View childView = new ChildView();
    private final TransitionAction<View, View> displayChild = newAction(rootView, childView);

    @Test
    @DisplayName("not set first display cause for root view")
    void notSetFirstDisplayCause() {
        final TransitionAction<View, View> displayRoot = newAction(childView, rootView);
        displayRoot.execute();
        assertNull(rootView.getOriginAction());
    }

    @Test
    @DisplayName("set first display cause for NON-root")
    void setFirstDisplayCause() {
        assertNull(childView.getOriginAction());
        displayChild.execute();
        assertSame(displayChild, childView.getOriginAction());
    }

    @Test
    @DisplayName("not overwrite first display cause")
    void notOverwriteFirstDisplayCause() {
        displayChild.execute();
        assertSame(displayChild, childView.getOriginAction());

        final TransitionAction<View, View> secondDisplayCause = newAction(rootView, childView);
        secondDisplayCause.execute();
        assertSame(displayChild, childView.getOriginAction());
    }

    @Test
    @DisplayName("create NoOpAction for return from a root view")
    void createPseudoBackAction() {
        final String backName = "Back";
        final Action back = rootView.createBackAction(backName, SHORTCUT);

        assertEquals(backName, back.getName());
        assertEquals(SHORTCUT, back.getShortcut());
        assertThat(back, instanceOf(NoOpAction.class));
    }

    @Test
    @DisplayName("not create back action for child view if `firstDisplayCause` is unknown")
    void notCreateBackAction() {
        assertThrows(IllegalStateException.class,
                     () -> childView.createBackAction("b", SHORTCUT));
    }

    @Test
    @DisplayName("create usual back action for child view")
    void createUsualBackAction() {
        displayChild.execute();
        final Action back = childView.createBackAction("b", SHORTCUT);
        back.execute();
        assertTrue(rootView.wasDisplayed);
    }

    private static class RootView extends View {

        private boolean wasDisplayed = false;

        private RootView() {
            super(true);
        }

        @Override
        public void display() {
            wasDisplayed = true;
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

    private static TransitionAction<View, View> newAction(View source, View destination) {
        final Shortcut shortcut = new Shortcut("a");
        return TransitionAction.newProducer("a name", shortcut, destination)
                               .create(source);
    }
}
