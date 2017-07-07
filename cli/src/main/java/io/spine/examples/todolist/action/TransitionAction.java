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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@code Action} which takes the end-user from a {@linkplain #source source view}
 * to a {@linkplain #destination destination view}.
 *
 * @param <S> the type of the source view
 * @param <D> the type of the destination view
 * @author Dmytro Grankin
 */
public class TransitionAction<S extends View, D extends View> extends Action {

    /**
     * A source {@code View} of the action.
     */
    private final S source;

    /**
     * A destination {@code View} of the action.
     */
    private final D destination;

    protected TransitionAction(String name, Shortcut shortcut, S source, D destination) {
        super(name, shortcut);
        checkNotNull(source);
        checkNotNull(destination);
        this.source = source;
        this.destination = destination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        destination.display(this);
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
        return new TransitionAction<>(name, shortcut, reserveSource, reverseDestination);
    }

    protected S getSource() {
        return source;
    }

    protected D getDestination() {
        return destination;
    }

    /**
     * Creates a new instance of the {@code TransitionAction}.
     *
     * @param name the name for the action
     * @param shortcut the shortcut for the action
     * @param destination the destination for the action
     * @param <S> the type of the source view
     * @param <D> the type of the destination view
     * @return the new producer
     */
    public static <S extends View, D extends View>
    TransitionActionProducer<S, D> newProducer(String name, Shortcut shortcut, D destination) {
        return new TransitionActionProducer<>(name, shortcut, destination);
    }

    /**
     * Producer of transition actions.
     *
     * @param <S> {@inheritDoc}
     * @param <D> {@inheritDoc}
     */
    public static class TransitionActionProducer<S extends View,
                                                 D extends View>
            extends AbstractTransitionActionProducer<S, D, TransitionAction<S, D>> {

        private final D destination;

        protected TransitionActionProducer(String name, Shortcut shortcut, D destination) {
            super(name, shortcut);
            checkNotNull(destination);
            this.destination = destination;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TransitionAction<S, D> create(S source) {
            return new TransitionAction<>(getName(), getShortcut(), source, destination);
        }
    }
}
