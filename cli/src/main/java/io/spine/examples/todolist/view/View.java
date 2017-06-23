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

import io.spine.examples.todolist.UserCommunicator;
import io.spine.examples.todolist.UserCommunicatorImpl;
import io.spine.examples.todolist.action.Action;

import javax.annotation.Nullable;

/**
 * Abstract base class for views.
 *
 * @author Dmytro Grankin
 */
public abstract class View {

    /**
     * An {@link Action}, that led to the {@code View}.
     *
     * <p>Source action is {@code null} for root {@code View}.
     */
    @Nullable
    private final Action sourceAction;
    private final UserCommunicator userCommunicator = new UserCommunicatorImpl();

    protected View(@Nullable Action sourceAction) {
        this.sourceAction = sourceAction;
    }

    public abstract void display();

    protected void back() {
        if (sourceAction != null) {
            sourceAction.back();
        }
    }

    protected String promptUser(String question) {
        return userCommunicator.promptUser(question);
    }

    protected void println(String message) {
        userCommunicator.println(message);
    }
}
