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

package io.spine.examples.todolist.mode.action;

import com.google.protobuf.Message;
import io.spine.examples.todolist.mode.Mode;
import io.spine.validate.ValidatingBuilder;
import io.spine.validate.ValidationException;

import java.util.Optional;

import static io.spine.examples.todolist.mode.action.ValidationExceptionFormatter.format;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * @author Dmytro Grankin
 */
abstract class UserCommand<M extends Message,
                           B extends ValidatingBuilder<M, ? extends Message.Builder>>
        extends Mode {

    private final B builder;

    UserCommand(B builder) {
        this.builder = builder;
    }

    @Override
    public final void start() {
        inputCommandParams();
        final M commandMessage = buildCommandMessage();
        postCommand(commandMessage);
    }

    protected abstract void inputCommandParams();

    protected abstract void postCommand(M commandMessage);

    private M buildCommandMessage() {
        try {
            return builder.build();
        } catch (ValidationException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    protected Optional<String> trySet(Runnable runnable) {
        try {
            runnable.run();
            return Optional.empty();
        } catch (ValidationException e) {
            final String errMsg = format(e);
            return Optional.of(errMsg);
        }
    }

    protected void checkNotThrowsValidationEx(Runnable runnable) {
        try {
            runnable.run();
        } catch (ValidationException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    protected B getBuilder() {
        return builder;
    }
}
