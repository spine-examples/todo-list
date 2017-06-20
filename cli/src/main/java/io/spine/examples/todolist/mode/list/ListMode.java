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

package io.spine.examples.todolist.mode.list;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;
import io.spine.examples.todolist.mode.Mode;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.mode.DisplayHelper.getLineSeparator;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * @author Dmytro Grankin
 */
public abstract class ListMode<E extends Message> extends Mode {

    private List<E> state;

    @Override
    public void start() {
        display();
    }

    protected void display() {
        updateState();
        final String view = getView();
        println(view);
    }

    @VisibleForTesting
    void updateState() {
        final List<E> newState = receiveRecentState();
        setState(newState);
    }

    protected abstract List<E> receiveRecentState();

    @VisibleForTesting
    String getView() {
        return state.isEmpty()
               ? getEmptyView()
               : getNonEmptyView();
    }

    protected abstract String getEmptyView();

    protected String getNonEmptyView() {
        final StringBuilder builder = new StringBuilder();

        for (E item : state) {
            final String itemView = getItemView(item);
            builder.append(itemView)
                   .append(getLineSeparator());
        }

        return builder.toString();
    }

    protected abstract String getItemView(E item);

    protected void setState(List<E> state) {
        checkNotNull(state);
        this.state = state;
    }

    @VisibleForTesting
    List<E> getState() {
        return state == null
               ? emptyList()
               : unmodifiableList(state);
    }
}
