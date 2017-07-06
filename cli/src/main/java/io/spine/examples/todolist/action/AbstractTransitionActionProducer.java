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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Abstract producer of a {@link TransitionAction}.
 *
 * <p>Encapsulates creation of the action.
 *
 * @param <S> the type of the source view
 * @param <D> the type of the destination view
 * @param <T> the type of the action to be created
 */
public abstract class AbstractTransitionActionProducer<S extends View,
                                                       D extends View,
                                                       T extends TransitionAction<S, D>> {

    private final String name;
    private final Shortcut shortcut;


    protected AbstractTransitionActionProducer(String name, Shortcut shortcut) {
        checkArgument(!isNullOrEmpty(name));
        checkNotNull(shortcut);
        this.name = name;
        this.shortcut = shortcut;
    }

    public abstract T create(S source);

    @VisibleForTesting
    public String getName() {
        return name;
    }

    @VisibleForTesting
    public Shortcut getShortcut() {
        return shortcut;
    }
}
