/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import io.spine.cli.Application;
import io.spine.cli.view.View;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base implementation of {@link Action}.
 *
 * <p>Executes transition from a {@link #source} to a {@link #destination}.
 *
 * @param <S> {@inheritDoc}
 * @param <D> {@inheritDoc}
 * @author Dmytro Grankin
 */
public class TransitionAction<S extends View, D extends View> extends AbstractAction<S, D> {

    public TransitionAction(String name, Shortcut shortcut, S source, D destination) {
        super(name, shortcut, source, destination);
    }

    /**
     * Makes transition to a {@link #destination} view
     * using the {@linkplain Application#screen() screen}.
     */
    @Override
    public void execute() {
        Application.getInstance()
                   .screen()
                   .renderView(getDestination());
    }

    /**
     * Creates a new instance of a {@code TransitionActionProducer}.
     *
     * @param name        the name for the action
     * @param shortcut    the shortcut for the action
     * @param destination the destination for the action
     * @param <S>         the type of the source view
     * @param <D>         the type of the destination view
     * @return the new producer
     */
    public static <S extends View, D extends View>
    TransitionActionProducer<S, D> transitionProducer(String name, Shortcut shortcut,
                                                      D destination) {
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
            extends AbstractActionProducer<S, D, TransitionAction<S, D>> {

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
