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
import io.spine.reflect.GenericTypeIndex;
import io.spine.validate.ValidatingBuilder;
import io.spine.validate.ValidatingBuilders;
import io.spine.validate.ValidationException;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.mode.action.UserCommand.GenericParameter.STATE_BUILDER;
import static io.spine.examples.todolist.mode.action.ValidationExceptionFormatter.format;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * @author Dmytro Grankin
 */
abstract class UserCommand<M extends Message,
                           B extends ValidatingBuilder<M, ? extends Message.Builder>>
        extends Mode {

    private final B builder = newBuilder();

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

    private B newBuilder() {
        @SuppressWarnings("unchecked")   // it's safe, as we rely on the definition of this class.
        final Class<? extends UserCommand<M, B>> aClass =
                (Class<? extends UserCommand<M, B>>) getClass();
        final Class<B> builderClass = TypeInfo.getBuilderClass(aClass);
        return ValidatingBuilders.newInstance(builderClass);
    }

    /**
     * Enumeration of generic type parameters of this class.
     */
    enum GenericParameter implements GenericTypeIndex<UserCommand> {

        /** The index of the generic type {@code <M>}. */
        STATE(0),

        /** The index of the generic type {@code <B>}. */
        STATE_BUILDER(1);

        private final int index;

        GenericParameter(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return this.index;
        }

        @Override
        public Class<?> getArgumentIn(Class<? extends UserCommand> cls) {
            return Default.getArgument(this, cls);
        }
    }

    /**
     * Provides type information on classes extending {@code UserCommand}.
     */
    private static class TypeInfo {

        private TypeInfo() {
            // Prevent instantiation of this utility class.
        }

        /**
         * Obtains the class of the {@linkplain ValidatingBuilder} for the given
         * {@code EventPlayingEntity} descendant class {@code entityClass}.
         */
        private static <M extends Message,
                B extends ValidatingBuilder<M, ? extends Message.Builder>>
        Class<B> getBuilderClass(Class<? extends UserCommand<M, B>> entityClass) {
            checkNotNull(entityClass);
            @SuppressWarnings("unchecked") // The type is ensured by this class declaration.
            final Class<B> builderClass = (Class<B>) STATE_BUILDER.getArgumentIn(entityClass);
            return builderClass;
        }
    }
}
