/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import io.spine.examples.todolist.cli.view.View;

/**
 * <p>An {@code Action} takes the end-user from a {@linkplain #getSource() source view}
 * to a {@linkplain #getDestination() destination view}.
 *
 * <p>Actions with same {@link Shortcut} are considered equal.
 *
 * @param <S>
 *         the type of the source view
 * @param <D>
 *         the type of the destination view
 */
public interface Action<S extends View, D extends View> {

    /**
     * Executes the action.
     */
    void execute();

    /**
     * Obtains name of the action.
     *
     * @return action name
     */
    String getName();

    /**
     * Obtains {@link Shortcut} of the action.
     *
     * @return action shortcut
     */
    Shortcut getShortcut();

    /**
     * A source {@code View} of the action.
     *
     * @return a source view
     */
    S getSource();

    /**
     * A destination {@code View} of the action.
     *
     * @return a destination view
     */
    D getDestination();
}
