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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * An {@code TransitionAction} represents transition from a {@linkplain #source source view}
 * to a {@linkplain #destination destination view}.
 *
 * @param <S> the type of the source view
 * @param <D> the type of the destination view
 * @author Dmytro Grankin
 */
public abstract class TransitionAction<S extends View, D extends View> extends Action {

    /**
     * A source {@code View} of the action.
     */
    private final S source;

    /**
     * A destination {@code View} of the action.
     */
    private D destination;

    protected TransitionAction(String name, Shortcut shortcut, S source) {
        super(name, shortcut);
        this.source = source;
    }

    /**
     * Creates reverse action for the action with the specified name and the shortcut.
     *
     * @param name     the name of the reverse action
     * @param shortcut the shortcut of the reverse action
     * @return the reverse action
     */
    public TransitionAction<D, S> createReverseAction(String name, Shortcut shortcut) {
        final S reverseDestination = getSource();
        final D reserveSource = getDestination();
        return new StaticTransitionAction<>(name, shortcut, reserveSource, reverseDestination);
    }

    protected void setDestination(D destination) {
        checkNotNull(destination);
        this.destination = destination;
    }

    protected S getSource() {
        return source;
    }

    protected D getDestination() {
        return destination;
    }

    /**
     * Producer of the {@link TransitionAction}.
     *
     * <p>Encapsulates creation of the action.
     *
     * @param <S> the type of the source view
     * @param <D> the type of the destination view
     * @param <T> the type of the action to be created
     */
    public abstract static class TransitionActionProducer<S extends View,
                                                          D extends View,
                                                          T extends TransitionAction<S, D>> {

        private final String name;
        private final Shortcut shortcut;

        protected TransitionActionProducer(String name, Shortcut shortcut) {
            checkArgument(!isNullOrEmpty(name));
            checkNotNull(shortcut);
            this.name = name;
            this.shortcut = shortcut;
        }

        public abstract T create(S source);

        protected String getName() {
            return name;
        }

        protected Shortcut getShortcut() {
            return shortcut;
        }
    }
}
