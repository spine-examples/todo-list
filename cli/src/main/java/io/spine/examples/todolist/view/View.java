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
import io.spine.examples.todolist.action.PseudoAction;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Abstract base class for views.
 *
 * @author Dmytro Grankin
 */
public abstract class View {

    private final boolean rootView;

    /**
     * An {@link TransitionAction}, that <b>initially</b> led to the {@code View}.
     *
     * <p>If the {@code View} is {@link #rootView}, this field is always {@code null}.
     */
    @Nullable
    private TransitionAction originAction;
    private UserCommunicator userCommunicator = new UserCommunicatorImpl();

    /**
     * Creates a new instance.
     *
     * @param rootView whether the view is root
     */
    protected View(boolean rootView) {
        this.rootView = rootView;
    }

    public void display(@Nullable Action displayCause) {
        setOriginAction(displayCause);
        display();
    }

    private void setOriginAction(@Nullable Action displayCause) {
        if (!rootView) {
            checkNotNull(displayCause);
            final boolean notDisplayedBefore = originAction == null;
            if (notDisplayedBefore) {
                originAction = (TransitionAction) displayCause;
            }
        }
    }

    protected abstract void display();

    protected Action createBackAction(String name, Shortcut shortcut) {
        if (rootView) {
            return new PseudoAction(name, shortcut);
        }

        if (originAction == null) {
            throw newIllegalStateException(
                    "An action, that caused this view, should be known to create back action.");
        }

        return originAction.createReverseAction(name, shortcut);
    }

    protected String promptUser(String question) {
        return userCommunicator.promptUser(question);
    }

    protected void println(String message) {
        userCommunicator.println(message);
    }

    public void setUserCommunicator(UserCommunicator userCommunicator) {
        this.userCommunicator = userCommunicator;
    }

    @VisibleForTesting
    public boolean isRootView() {
        return rootView;
    }

    @VisibleForTesting
    @Nullable
    Action getOriginAction() {
        return originAction;
    }
}
