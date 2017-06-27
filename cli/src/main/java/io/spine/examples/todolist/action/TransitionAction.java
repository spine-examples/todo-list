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

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * An {@code TransitionAction} represents transition from a {@linkplain #source source view}
 * to a {@linkplain #destination destination view}.
 *
 * @author Dmytro Grankin
 */
public class TransitionAction extends Action {

    /**
     * A source {@code View} of the action.
     *
     * <p>The source is unknown until the action was not {@linkplain #execute(View) executed}.
     */
    @Nullable
    private View source;

    /**
     * A destination {@code View} of the action.
     */
    private final View destination;

    public TransitionAction(String name, String shortcut, View destination) {
        super(name, shortcut);
        checkNotNull(destination);
        this.destination = destination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(View source) {
        checkNotNull(source);
        this.source = source;
        destination.display(this);
    }

    public TransitionAction createReverseAction(String name, String shortcut) {
        if (source == null) {
            throw newIllegalStateException("There is no source view for the action, " +
                                                   "cannot create reverse action.");
        }

        final View destination = source;
        return new TransitionAction(name, shortcut, destination);
    }

    @VisibleForTesting
    @Nullable
    View getSource() {
        return source;
    }

    @VisibleForTesting
    View getDestination() {
        return destination;
    }
}
