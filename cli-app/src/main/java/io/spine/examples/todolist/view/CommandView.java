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
import io.spine.examples.todolist.Application;
import io.spine.examples.todolist.Screen;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.CommandAction;
import io.spine.examples.todolist.action.EditCommandAction;
import io.spine.reflect.GenericTypeIndex;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ValidatingBuilder;
import io.spine.validate.ValidatingBuilders;
import io.spine.validate.ValidationException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.ConstraintViolationFormatter.format;
import static io.spine.examples.todolist.view.CommandView.GenericParameter.STATE_BUILDER;

/**
 * A {@code CommandView} is a view where the end-user prepares and sends a command to a server.
 *
 * <p>{@link EditCommandAction} and {@link CommandAction} are well-suited for usage in this view.
 *
 * <p>List of actions in a {@code CommandView} may looks like this:
 * <ol>
 *     <li>prepare a command (EditCommandAction)</li>
 *     <li>send a command to a server (CommandAction)</li>
 *     <li>move back (TransitionAction)</li>
 * </ol>
 *
 * @param <M> the type of the command message
 * @param <B> the validating builder type for the command message
 * @author Dmytro Grankin
 */
public abstract class CommandView<M extends Message,
                                  B extends ValidatingBuilder<M, ? extends Message.Builder>>
        extends AbstractView {

    private final B state;
    private final Collection<ConstraintViolation> recentViolations = new LinkedList<>();

    protected CommandView(String title) {
        super(title);
        this.state = newBuilderInstance();
    }

    /**
     * Renders {@link #recentViolations} and the {@link #state} of the command message.
     *
     * @param screen {@inheritDoc}
     */
    @Override
    protected void renderBody(Screen screen) {
        renderRecentViolations(screen);
        final String renderedState = renderState(state);
        screen.println(renderedState);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Handles a {@link ValidationException} in the case of occurrence.
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

    /**
     * Renders the specified state.
     *
     * @param state the command state
     * @return the string representation
     */
    protected abstract String renderState(B state);

    private void handleValidationException(ValidationException ex) {
        recentViolations.addAll(ex.getConstraintViolations());
        Application.getInstance()
                   .screen()
                   .renderView(this);
    }

    private void renderRecentViolations(Screen screen) {
        final List<String> errorMessages = format(recentViolations);
        errorMessages.forEach(screen::println);
        recentViolations.clear();
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
