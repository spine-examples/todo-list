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

package io.spine.examples.todolist.view;

import com.google.protobuf.Message;
import io.spine.examples.todolist.AppConfig;
import io.spine.examples.todolist.DataSource;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.lineSeparator;

/**
 * Detailed view of a {@link #state}.
 *
 * @param <I> the type of ID for the state
 * @param <S> the type of the displayed state
 * @author Dmytro Grankin
 */
public abstract class DetailsView<I extends Message, S extends Message> extends ActionListView {

    private final I id;
    private S state;

    private final DataSource dataSource = AppConfig.getDataSource();

    protected DetailsView(I id) {
        super(false);
        checkNotNull(id);
        this.id = id;
    }

    @Override
    protected void display() {
        state = getRecentState(id);
        super.display();
    }

    /**
     * Obtains recent state by the specified ID.
     *
     * @param id the ID of the state
     * @return recent state
     */
    protected abstract S getRecentState(I id);

    /**
     * Obtains a string view of the specified state.
     *
     * @param state the state to obtain the view
     * @return view of the state
     */
    protected abstract String viewOf(S state);

    protected DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public String toString() {
        return viewOf(state) + lineSeparator() + super.toString();
    }
}
