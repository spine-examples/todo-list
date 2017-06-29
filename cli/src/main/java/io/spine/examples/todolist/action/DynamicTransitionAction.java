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

import io.spine.examples.todolist.AppConfig;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.view.View;

/**
 * A {@link TransitionAction} without a pre-defined
 * {@linkplain TransitionAction#destination destination}.
 *
 * <p>Actions are specified at compile time and there are data, that vary during runtime.
 * So, if there is a {@link View} creation of which depends on a vary data,
 * use {@code DynamicTransitionAction} to specify transition to such a view.
 *
 * <p>The destination is {@linkplain #refreshDestination() refreshed}
 * during every {@linkplain #execute(View) action execution}.
 *
 * @param <V> {@inheritDoc}
 * @author Dmytro Grankin
 */
public abstract class DynamicTransitionAction<V extends View> extends TransitionAction<V> {

    private final TodoClient client = AppConfig.getClient();

    protected DynamicTransitionAction(String name, String shortcut) {
        super(name, shortcut);
    }

    /**
     * Displays a {@linkplain #refreshDestination() refreshed destination view}.
     *
     * @param source {@inheritDoc}
     */
    @Override
    public void execute(V source) {
        setSource(source);
        refreshDestination();
        getDestination().display(this);
    }

    private void refreshDestination() {
        final V destination = createDestination();
        setDestination(destination);
    }

    /**
     * Creates an instance of the destination view.
     *
     * @return the destination view
     */
    protected abstract V createDestination();

    protected TodoClient getClient() {
        return client;
    }
}
