/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.cli.view;

import com.google.protobuf.Message;
import io.spine.examples.todolist.cli.Application;
import io.spine.examples.todolist.cli.Screen;
import io.spine.examples.todolist.cli.action.Action;
import io.spine.examples.todolist.cli.action.CommandAction;
import io.spine.examples.todolist.cli.action.EditCommandAction;
import io.spine.protobuf.Messages;
import io.spine.protobuf.ValidatingBuilder;
import io.spine.reflect.GenericTypeIndex;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ValidationException;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.examples.todolist.cli.ConstraintViolationFormatter.format;
import static io.spine.examples.todolist.cli.view.CommandView.GenericParameter.COMMAND_MESSAGE;

/**
 * A {@code CommandView} is a view where the end-user prepares and sends a command to a server.
 *
 * <p>{@link EditCommandAction} and {@link CommandAction} are well-suited for usage in this view.
 *
 * <p>List of actions in a {@code CommandView} may looks like this:
 * <ol>
 * <li>prepare a command (EditCommandAction)</li>
 * <li>send a command to a server (CommandAction)</li>
 * <li>move back (TransitionAction)</li>
 * </ol>
 *
 * @param <M>
 *         the type of the command message
 * @param <B>
 *         the validating builder type for the command message
 */
public abstract class CommandView<M extends Message, B extends ValidatingBuilder<M>>
        extends AbstractView {

    private final B state;
    private final Collection<ConstraintViolation> recentViolations = newLinkedList();

    protected CommandView(String title) {
        super(title);
        this.state = newBuilderInstance();
    }

    /**
     * Renders {@link #recentViolations} and the {@link #state} of the command message.
     */
    @Override
    protected void renderBody(Screen screen) {
        renderRecentViolations(screen);
        String renderedState = renderState(state);
        screen.println(renderedState);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Handles a {@link ValidationException} in the case of occurrence.
     */
    @Override
    protected void executeAction(Action<?, ?> action) {
        try {
            super.executeAction(action);
        } catch (ValidationException ex) {
            handleValidationException(ex);
        }
    }

    /**
     * Renders the specified state.
     *
     * @param state
     *         the command state
     * @return the string representation
     */
    protected abstract String renderState(B state);

    private void handleValidationException(ValidationException ex) {
        recentViolations.addAll(ex.getConstraintViolations());
        Application.instance()
                   .screen()
                   .renderView(this);
    }

    private void renderRecentViolations(Screen screen) {
        List<String> errorMessages = format(recentViolations);
        errorMessages.forEach(screen::println);
        recentViolations.clear();
    }

    public B getState() {
        return state;
    }

    @SuppressWarnings("unchecked") // Logically checked.
    private B newBuilderInstance() {
        Class<? extends CommandView<M, B>> viewClass =
                (Class<? extends CommandView<M, B>>) getClass();
        Class<M> builderClass = TypeInfo.messageClass(viewClass);
        B builder = (B) Messages.builderFor(builderClass);
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
        public int index() {
            return this.index;
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
        private static <M extends Message>
        Class<M> messageClass(Class<? extends CommandView<M, ?>> viewClass) {
            checkNotNull(viewClass);
            @SuppressWarnings("unchecked") // The type is ensured by this class declaration.
            Class<M> builderClass = (Class<M>) COMMAND_MESSAGE.argumentIn(viewClass);
            return builderClass;
        }


    }
}
