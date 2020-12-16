/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.cli.action;

import io.spine.examples.todolist.cli.view.AbstractView;
import io.spine.examples.todolist.cli.view.View;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * An {@link Action}, that does nothing.
 *
 * <p>Suits for the tests, where {@link Action} behavior does not play a role.
 */
public final class NoOpAction implements Action<AbstractView, View> {

    private static final String UNSUPPORTED_MSG = "NoOpAction does not support transitions between views";

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

    /**
     * Creates a new {@link NoOpActionProducer} instance.
     *
     * @param name
     *         the name for the action
     * @param shortcut
     *         the shortcut for the action
     * @return the new producer
     */
    public static NoOpActionProducer noOpActionProducer(String name, Shortcut shortcut) {
        return new NoOpActionProducer(name, shortcut);
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
    public AbstractView getSource() {
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

    /**
     * Producer of no-op actions.
     *
     * <p>Purpose of this producer is simplify testing of {@link ActionProducer}-based API.
     */
    public static class NoOpActionProducer
            extends AbstractActionProducer<AbstractView, View, NoOpAction> {

        private NoOpActionProducer(String name, Shortcut shortcut) {
            super(name, shortcut);
        }

        @Override
        public NoOpAction create(AbstractView source) {
            return new NoOpAction(getName(), getShortcut());
        }
    }
}
