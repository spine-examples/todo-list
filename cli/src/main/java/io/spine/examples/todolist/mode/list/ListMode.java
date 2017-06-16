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

import com.google.protobuf.Message;
import io.spine.examples.todolist.mode.Mode;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.UserIO.println;
import static java.lang.Character.LINE_SEPARATOR;

/**
 * @author Dmytro Grankin
 */
public abstract class ListMode<S extends Message> extends Mode {

    private List<S> state;

    @Override
    public void start() {
        display();
    }

    protected void display() {
        updateState();
        final String view = getView();
        println(view);
    }

    private void updateState() {
        final List<S> newState = receiveRecentState();
        setState(newState);
    }

    protected abstract List<S> receiveRecentState();

    private String getView() {
        return state.isEmpty()
               ? getEmptyView()
               : getNonEmptyView();
    }

    protected abstract String getEmptyView();

    protected String getNonEmptyView() {
        final StringBuilder builder = new StringBuilder();

        for (S item : state) {
            final String itemView = getItemView(item);
            builder.append(itemView)
                   .append(LINE_SEPARATOR);
        }

        return builder.toString();
    }

    protected abstract String getItemView(S item);

    protected void setState(List<S> state) {
        checkNotNull(state);
        this.state = state;
    }
}
