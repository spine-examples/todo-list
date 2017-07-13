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

import io.spine.examples.todolist.view.View;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Special kind of {@link Action}, that does nothing.
 *
 * <p>Should be used for boundary conditions, e.g. exit from root view.
 *
 * <p>Also suits for usage in tests, where {@link Action} behavior does not play a role.
 */
public class NoOpAction implements Action<View, View> {

    private static final String UNSUPPORTED_MSG = "NoOpAction does not define transitions between views";

    private final String name;
    private final Shortcut shortcut;

    public NoOpAction(String name, Shortcut shortcut) {
        checkArgument(!isNullOrEmpty(name));
        checkNotNull(shortcut);
        this.name = name;
        this.shortcut = shortcut;
    }

    @Override
    public void execute() {
        // Do nothing.
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Shortcut getShortcut() {
        return shortcut;
    }

    @Override
    public View getSource() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public View getDestination() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Action)) {
            return false;
        }

        Action other = (Action) o;

        return Objects.equals(shortcut, other.getShortcut());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(shortcut);
    }

    @Override
    public String toString() {
        return ActionFormatter.format(this);
    }
}
