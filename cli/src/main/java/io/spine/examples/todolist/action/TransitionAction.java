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

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * An {@code TransitionAction} represents transition from a {@linkplain #source source view}
 * to a {@linkplain #destination destination view}.
 *
 * @param <V> the type of the source and the destination view
 * @author Dmytro Grankin
 */
public abstract class TransitionAction<V extends View> extends Action<V> {

    /**
     * A source {@code View} of the action.
     *
     * <p>The source is unknown until the action was not {@linkplain #execute(View) executed}.
     */
    @Nullable
    private V source;

    /**
     * A destination {@code View} of the action.
     */
    private V destination;

    protected TransitionAction(String name, String shortcut) {
        super(name, shortcut);
    }

    /**
     * Creates reverse action for the action with the specified name and the shortcut.
     *
     * @param name     the name of the reverse action
     * @param shortcut the shortcut of the reverse action
     * @return the reverse action
     */
    public TransitionAction<V> createReverseAction(String name, String shortcut) {
        if (getSource() == null) {
            throw newIllegalStateException("There is no source view for the action, " +
                                                   "cannot create reverse action.");
        }

        final V destination = getSource();
        return new StaticTransitionAction<>(name, shortcut, destination);
    }

    public void setSource(V source) {
        checkNotNull(source);
        this.source = source;
    }

    public void setDestination(V destination) {
        checkNotNull(destination);
        this.destination = destination;
    }

    @Nullable
    protected V getSource() {
        return source;
    }

    protected V getDestination() {
        return destination;
    }
}
