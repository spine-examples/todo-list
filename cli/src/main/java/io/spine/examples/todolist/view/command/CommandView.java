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

package io.spine.examples.todolist.view.command;

import com.google.protobuf.Message;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.view.ActionListView;
import io.spine.validate.ValidatingBuilder;
import io.spine.validate.ValidationException;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.view.command.ValidationExceptionFormatter.toErrorMessages;
import static java.util.Collections.emptySet;

/**
 * @author Dmytro Grankin
 */
public abstract class CommandView<M extends Message,
                                  B extends ValidatingBuilder<M, ? extends Message.Builder>>
        extends ActionListView {

    private final B state;

    protected CommandView(boolean rootView, B state) {
        super(rootView, emptySet());
        checkNotNull(state);
        this.state = state;
    }

    @Override
    protected void executeAction(Action action) {
        try {
            super.executeAction(action);
        } catch (ValidationException ex) {
            handleValidationException(ex);
        }
    }

    private void handleValidationException(ValidationException ex) {
        final List<String> errorMessages = toErrorMessages(ex);
        for (String message : errorMessages) {
            println(message);
        }
        display();
    }

    protected B getState() {
        return state;
    }
}
