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

package io.spine.cli.view;

import com.google.protobuf.Message;
import io.spine.cli.Screen;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link View} of an entity.
 *
 * <p>This view adds rendering of an entity state.
 *
 * @param <I> the type of entity identifier
 * @param <S> the type of the entity state
 * @author Dmytro Grankin
 */
public abstract class EntityView<I extends Message, S extends Message> extends AbstractView {

    private final I id;

    protected EntityView(I id, String title) {
        super(title);
        checkNotNull(id);
        this.id = id;
    }

    /**
     * Loads the entity state and then renders it.
     *
     * @param screen {@inheritDoc}
     */
    @Override
    protected void renderBody(Screen screen) {
        final S state = load(id);
        final String renderedState = renderState(state);
        screen.println(renderedState);
    }

    /**
     * Loads entity state by the specified ID.
     *
     * @param id the ID of the entity
     * @return loaded entity state
     */
    protected abstract S load(I id);

    /**
     * Renders the specified entity state.
     *
     * @param state the entity state
     * @return the rendered state
     */
    protected abstract String renderState(S state);
}
