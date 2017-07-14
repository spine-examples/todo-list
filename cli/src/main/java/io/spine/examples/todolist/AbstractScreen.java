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

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction;
import io.spine.examples.todolist.view.View;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Optional;

/**
 * Abstract base class for {@link Screen}.
 *
 * <p>Implements common operation for all screens.
 *
 * @author Dmytro Grankin
 */
abstract class AbstractScreen implements Screen {

    private final Deque<View> history = new ArrayDeque<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void renderView(View view) {
        if (isNewView(view)) {
            history.push(view);
        }
        view.render(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TransitionAction<View, View>> createBackAction(String name, Shortcut shortcut) {
        final boolean canGoBack = history.size() > 1;

        if (!canGoBack) {
            return Optional.empty();
        }

        final View current = history.pop();
        final View previous = history.pop();
        final TransitionAction<View, View> back = new TransitionAction<>(name, shortcut,
                                                                         current, previous);
        return Optional.of(back);
    }

    private boolean isNewView(View view) {
        final View currentView = history.peek();
        return currentView == null || !currentView.equals(view);
    }

    @VisibleForTesting
    Collection<View> getHistory() {
        return Collections.unmodifiableCollection(history);
    }
}
