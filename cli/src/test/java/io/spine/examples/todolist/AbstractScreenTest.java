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

package io.spine.examples.todolist;

import io.spine.examples.todolist.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.examples.todolist.Given.newNoOpView;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Dmytro Grankin
 */
@DisplayName("AbstractScreen should")
class AbstractScreenTest {

    private final Screen screen = new AnAbstractScreen();

    @Test
    @DisplayName("not return previous view for a root view")
    void notReturnPreviousViewForRootView() {
        final View view = newNoOpView();
        screen.renderView(view);
        final Optional<View> previousView = screen.getPreviousView(view);
        assertFalse(previousView.isPresent());
    }

    @Test
    @DisplayName("return previous view for a child view")
    void returnPreviousViewForChildView() {
        final View rootView = newNoOpView();
        final View childView = newNoOpView();

        screen.renderView(rootView);
        screen.renderView(childView);

        final View previousView = screen.getPreviousView(childView)
                                        .get();
        assertSame(rootView, previousView);
    }

    @Test
    @DisplayName("not overwrite a previous view")
    void notOverwritePreviousView() {
        final View rootView = newNoOpView();
        final View childView = newNoOpView();

        screen.renderView(rootView);
        screen.renderView(childView);
        assertSame(rootView, screen.getPreviousView(childView)
                                   .get());

        screen.renderView(childView);
        assertSame(rootView, screen.getPreviousView(childView)
                                   .get());
    }

    private static class AnAbstractScreen extends AbstractScreen {

        private static final String UNSUPPORTED_MSG = "Should not be called in this test.";

        @Override
        public String promptUser(String prompt) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }

        @Override
        public void println(String message) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
    }
}
