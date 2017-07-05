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
 * A {@link TransitionAction}, that specifies
 * {@linkplain TransitionAction#destination destination view} during
 * {@linkplain #StaticTransitionAction(String, Shortcut, View, View)} object creation}.
 *
 * @param <S> {@inheritDoc}
 * @param <D> {@inheritDoc}
 * @author Dmytro Grankin
 */
public class StaticTransitionAction<S extends View, D extends View> extends TransitionAction<S, D> {

    public StaticTransitionAction(String name, Shortcut shortcut, S source, D destination) {
        super(name, shortcut, source);
        setDestination(destination);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        getDestination().display(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param <S> {@inheritDoc}
     * @param <D> {@inheritDoc}
     */
    public static class StaticTransitionActionProducer<S extends View,
                                                       D extends View>
            extends TransitionActionProducer<S, D, StaticTransitionAction<S, D>> {

        private final D destination;

        public StaticTransitionActionProducer(String name, Shortcut shortcut, D destination) {
            super(name, shortcut);
            checkNotNull(destination);
            this.destination = destination;
        }

        @Override
        public StaticTransitionAction<S, D> create(S source) {
            return new StaticTransitionAction<>(getName(), getShortcut(), source, destination);
        }
    }
}
