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
import io.spine.reflect.GenericTypeIndex;
import io.spine.validate.ValidatingBuilder;
import io.spine.validate.ValidatingBuilders;
import io.spine.validate.ValidationException;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.view.command.CommandView.GenericParameter.STATE_BUILDER;
import static io.spine.examples.todolist.view.command.ValidationExceptionFormatter.toErrorMessages;

/**
 * A {@code CommandView} is a abstract base for views where a user of the command-line application
 * prepares and sends a command to a server.
 *
 * <p>Compared to the {@link ActionListView} provides handling of {@link ValidationException}.
 *
 * <p>The view must predominantly consist of
 * {@linkplain io.spine.examples.todolist.action.CommandAction command actions} and
 * an {@link io.spine.examples.todolist.action.ExecuteCommandAction ExecuteCommandAction}.
 *
 * @author Dmytro Grankin
 */
public class CommandView<M extends Message,
                         B extends ValidatingBuilder<M, ? extends Message.Builder>>
        extends ActionListView {

    private final B state;

    protected CommandView(boolean rootView) {
        super(rootView);
        this.state = newBuilderInstance();
    }

    /**
     * {@inheritDoc}
     *
     * <p>If {@link ValidationException} is occurred, prints errors and displays the view.
     *
     * @param action {@inheritDoc}
     */
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

    public B getState() {
        return state;
    }

    private B newBuilderInstance() {
        @SuppressWarnings("unchecked")   // It's safe, as we rely on the definition of this class.
        final Class<? extends CommandView<M, B>> aClass =
                (Class<? extends CommandView<M, B>>) getClass();
        final Class<B> builderClass = TypeInfo.getBuilderClass(aClass);
        final B builder = ValidatingBuilders.newInstance(builderClass);
        return builder;
    }

    /**
     * Enumeration of generic type parameters of this class.
     */
    enum GenericParameter implements GenericTypeIndex<CommandView> {

        /** The index of the generic type {@code <M>}. */
        COMMAND_MESSAGE(0),

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
        public Class<?> getArgumentIn(Class<? extends CommandView> cls) {
            return Default.getArgument(this, cls);
        }
    }

    /**
     * Provides type information on {@link CommandView} descendants.
     */
    static class TypeInfo {

        private TypeInfo() {
            // Prevent instantiation of this utility class.
        }

        /**
         * Obtains the class of the {@linkplain ValidatingBuilder} for the given
         * {@link CommandView} descendant class.
         */
        private static <M extends Message,
                B extends ValidatingBuilder<M, ? extends Message.Builder>>
        Class<B> getBuilderClass(Class<? extends CommandView<M, B>> entityClass) {
            checkNotNull(entityClass);
            @SuppressWarnings("unchecked") // The type is ensured by this class declaration.
            final Class<B> builderClass = (Class<B>) STATE_BUILDER.getArgumentIn(entityClass);
            return builderClass;
        }
    }
}
