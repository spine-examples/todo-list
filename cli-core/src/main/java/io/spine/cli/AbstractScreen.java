/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.cli;

import com.google.common.annotations.VisibleForTesting;
import io.spine.cli.action.Shortcut;
import io.spine.cli.action.TransitionAction;
import io.spine.cli.view.View;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

/**
 * An implementation base for {@linkplain Screen screens}.
 *
 * <p>Implements all methods except the I/O methods,
 * that are specific for a concrete implementation.
 *
 * @author Dmytro Grankin
 */
public abstract class AbstractScreen implements Screen {

    /**
     * History of rendered views
     *
     * <p>Does not include next views for the current view,
     * i.e. last rendered view is the end of the history.
     */
    private final List<View> history = newArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    public void renderView(View view) {
        if (isPreviousView(view)) {
            history.remove(getCurrentViewIndex());
        } else if (!isCurrentView(view)) {
            history.add(view);
        }
        view.render(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TransitionAction<View, View>> createBackAction(String name, Shortcut shortcut) {
        final int currentIndex = getCurrentViewIndex();
        final int previousIndex = getPreviousViewIndex();

        if (currentIndex == -1 || previousIndex == -1) {
            return Optional.empty();
        }
        final View current = history.get(currentIndex);
        final View previous = history.get(previousIndex);
        final TransitionAction<View, View> back = new TransitionAction<>(name, shortcut,
                                                                         current, previous);
        return Optional.of(back);
    }

    private boolean isCurrentView(View view) {
        final int currIndex = getCurrentViewIndex();
        return currIndex != -1 && currIndex == history.indexOf(view);
    }

    private boolean isPreviousView(View view) {
        final int prevIndex = getPreviousViewIndex();
        return prevIndex != -1 && prevIndex == history.indexOf(view);
    }

    private int getCurrentViewIndex() {
        return history.size() - 1;
    }

    private int getPreviousViewIndex() {
        final int currentIndex = getCurrentViewIndex();
        return currentIndex == -1
               ? -1
               : currentIndex - 1;
    }

    @VisibleForTesting
    List<View> getHistory() {
        return unmodifiableList(history);
    }
}
