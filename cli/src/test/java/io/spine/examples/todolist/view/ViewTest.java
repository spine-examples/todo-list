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
public class ViewTest {

    private final RootView rootView = new RootView();
    private final View childView = new ChildView();
    private final Action displayChild = newAction(childView);

    @Test
    @DisplayName("not set first display cause for root view")
    void notSetFirstDisplayCause() {
        final Action displayRoot = newAction(rootView);
        displayRoot.execute(childView);
        assertNull(rootView.getFirstDisplayCause());
    }

    @Test
    @DisplayName("set first display cause for NON-root")
    void setFirstDisplayCause() {
        assertNull(childView.getFirstDisplayCause());
        displayChild.execute(rootView);
        assertSame(displayChild, childView.getFirstDisplayCause());
    }

    @Test
    @DisplayName("not overwrite first display cause")
    void notOverwriteFirstDisplayCause() {
        displayChild.execute(rootView);
        assertSame(displayChild, childView.getFirstDisplayCause());

        final Action secondDisplayCause = newAction(childView);
        secondDisplayCause.execute(rootView);
        assertSame(displayChild, childView.getFirstDisplayCause());
    }

    @Test
    @DisplayName("create pseudo back action for root view")
    void createPseudoBackAction() {
        final String backName = "Back";
        final String backShortcut = "b";
        final Action back = rootView.createBackAction(backName, backShortcut);

        assertEquals(backName, back.getName());
        assertEquals(backShortcut, back.getShortcut());
        assertThat(back, instanceOf(View.PseudoAction.class));
    }

    @Test
    @DisplayName("not create back action for child view if `firstDisplayCause` is unknown")
    void notCreateBackAction() {
        assertThrows(IllegalStateException.class,
                     () -> childView.createBackAction("b", "b"));
    }

    @Test
    @DisplayName("create usual back action for child view")
    void createUsualBackAction() {
        displayChild.execute(rootView);
        final Action back = childView.createBackAction("b", "b");
        back.execute(childView);
        assertTrue(rootView.wasDisplayed);
    }

    @Test
    @DisplayName("throw ISE if first display cause is unknown for child view and `back()` was called")
    void throwIllegalStateOnBack() {
        assertThrows(IllegalStateException.class, childView::back);
    }

    @Test
    @DisplayName("display source view on `back()`")
    void displaySourceViewOnBack() {
        displayChild.execute(rootView);
        childView.back();
        assertTrue(rootView.wasDisplayed);
    }

    @Test
    @DisplayName("allow call `back()` for root view")
    void handleBackOnRootView() {
        rootView.back();
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

    private static Action newAction(View destination) {
        return new Action("a", "a", destination);
    }
}
