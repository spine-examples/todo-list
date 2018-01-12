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

package io.spine.cli.action;

import io.spine.cli.view.View;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Abstract base class for the {@link ActionProducer}.
 *
 * @param <S> {@inheritDoc}
 * @param <D> {@inheritDoc}
 * @param <T> {@inheritDoc}
 */
public abstract class AbstractActionProducer<S extends View,
                                             D extends View,
                                             T extends Action<S, D>> implements ActionProducer<S, D, T> {

    private final String name;
    private final Shortcut shortcut;

    protected AbstractActionProducer(String name, Shortcut shortcut) {
        checkArgument(!isNullOrEmpty(name));
        checkNotNull(shortcut);
        this.name = name;
        this.shortcut = shortcut;
    }

    /**
     * Obtains the name to be used for an action creation.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Obtains the shortcut to be used for an action creation.
     *
     * @return the shortcut
     */
    public Shortcut getShortcut() {
        return shortcut;
    }
}
