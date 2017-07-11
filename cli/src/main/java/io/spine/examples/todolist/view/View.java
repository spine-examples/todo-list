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

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.Screen;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.NoOpAction;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.repeat;
import static java.lang.System.lineSeparator;

/**
 * Abstract base class for views.
 *
 * @author Dmytro Grankin
 */
public abstract class View {

    private final String title;
    private Screen screen;

    protected View(String title) {
        checkArgument(!isNullOrEmpty(title));
        this.title = title;
    }

    /**
     * Renders the view.
     *
     * @param screen the screen
     */
    public final void render(Screen screen) {
        setScreen(screen);
        screen.println(getFormattedTitle());
        render();
    }

    protected abstract void render();

    /**
     * Obtains the action leading to the {@linkplain Screen#getPreviousView(View) previous view}.
     *
     * @param name     the name for the action
     * @param shortcut the shortcut for the action
     * @return the back action
     */
    protected Action createBackAction(String name, Shortcut shortcut) {
        final Optional<View> previousView = screen.getPreviousView(this);
        return previousView
                .<Action>map(view -> new TransitionAction<>(name, shortcut, this, view))
                .orElseGet(() -> new NoOpAction(name, shortcut));
    }

    public void setScreen(Screen screen) {
        checkNotNull(screen);
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    @VisibleForTesting
    String getFormattedTitle() {
        final String underline = repeat("-", title.length());
        return title + lineSeparator() + underline;
    }
}
