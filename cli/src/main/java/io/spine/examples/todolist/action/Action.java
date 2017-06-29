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

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.view.View;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * An {@code Action} encapsulates navigation between {@linkplain View views}.
 *
 * @param <S> the type of source {@link View} for the action
 * @author Dmytro Grankin
 * @see #execute(View)
 */
public abstract class Action<S extends View> {

    private static final String SHORTCUT_FORMAT = "(%s)";
    private static final String SHORTCUT_NAME_SEPARATOR = " ";

    private final String name;
    private final Shortcut shortcut;

    protected Action(String name, Shortcut shortcut) {
        checkArgument(!isNullOrEmpty(name));
        this.name = name;
        this.shortcut = shortcut;
    }

    /**
     * Executes the action.
     *
     * @param source the source view of the action
     */
    public abstract void execute(S source);

    public String getName() {
        return name;
    }

    public Shortcut getShortcut() {
        return shortcut;
    }

    public static String getShortcutFormat() {
        return SHORTCUT_FORMAT;
    }

    @VisibleForTesting
    static String getShortcutNameSeparator() {
        return SHORTCUT_NAME_SEPARATOR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Action other = (Action) o;
        return Objects.equals(shortcut, other.shortcut);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(shortcut);
    }

    @Override
    public String toString() {
        final String formattedShortcut = format(SHORTCUT_FORMAT, shortcut);
        return formattedShortcut + SHORTCUT_NAME_SEPARATOR + name;
    }
}
