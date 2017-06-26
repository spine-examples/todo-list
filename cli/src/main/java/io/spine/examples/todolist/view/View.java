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

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.UserCommunicator;
import io.spine.examples.todolist.UserCommunicatorImpl;
import io.spine.examples.todolist.action.Action;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Abstract base class for views.
 *
 * @author Dmytro Grankin
 */
public abstract class View {

    private final boolean rootView;

    /**
     * An {@link Action}, that caused first {@link #display()} of the {@code View}.
     *
     * <p>If the {@code View} is {@link #rootView}, this field is always {@code null}.
     */
    @Nullable
    private Action firstDisplayCause;
    private UserCommunicator userCommunicator = new UserCommunicatorImpl();

    protected View(boolean rootView) {
        this.rootView = rootView;
    }

    public void display(@Nullable Action displayCause) {
        setFirstDisplayCause(displayCause);
        display();
    }

    private void setFirstDisplayCause(@Nullable Action displayCause) {
        if (!rootView) {
            checkNotNull(displayCause);
            final boolean notDisplayedBefore = firstDisplayCause == null;
            if (notDisplayedBefore) {
                firstDisplayCause = displayCause;
            }
        }
    }

    protected abstract void display();

    protected Action createBackAction(String name, String shortcut) {
        if (rootView) {
            return new PseudoAction(name, shortcut, this);
        }

        if (firstDisplayCause == null) {
            throw newIllegalStateException(
                    "An action, that caused this view, should be known to create back action.");
        }

        return firstDisplayCause.createReverseAction(name, shortcut);
    }

    protected String promptUser(String question) {
        checkArgument(!isNullOrEmpty(question));
        return userCommunicator.promptUser(question);
    }

    protected void println(String message) {
        checkArgument(!isNullOrEmpty(message));
        userCommunicator.println(message);
    }

    public void setUserCommunicator(UserCommunicator userCommunicator) {
        this.userCommunicator = userCommunicator;
    }

    @VisibleForTesting
    @Nullable
    Action getFirstDisplayCause() {
        return firstDisplayCause;
    }

    /**
     * Special kind of {@link Action}, that does nothing.
     *
     * <p>Should be used for boundary conditions, e.g. exit from root view.
     */
    @VisibleForTesting
    static class PseudoAction extends Action {

        private PseudoAction(String name, String shortcut, View destination) {
            super(name, shortcut, destination);
        }

        @Override
        public void execute(View source) {
            // Do nothing.
        }
    }
}
