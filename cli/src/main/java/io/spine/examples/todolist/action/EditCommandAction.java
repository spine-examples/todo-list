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

package io.spine.examples.todolist.action;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;
import io.spine.examples.todolist.Screen;
import io.spine.examples.todolist.view.command.CommandView;
import io.spine.validate.ValidatingBuilder;

/**
 * Abstract base class for editing command actions.
 *
 * <p>Represents a user action, that updates
 * {@linkplain CommandView#state state of the command view}.
 *
 * @param <M> the type of the command message
 * @param <B> the validating builder type for the command message
 * @author Dmytro Grankin
 */
public abstract class EditCommandAction<M extends Message,
                                        B extends ValidatingBuilder<M, ? extends Message.Builder>>
        extends AbstractAction<CommandView<M, B>, CommandView<M, B>> {

    private Screen screen;

    protected EditCommandAction(String name, Shortcut shortcut, CommandView<M, B> source) {
        super(name, shortcut, source, source);
    }

    /**
     * Edits a state of the source view and then renders destination view.
     */
    @Override
    public void execute() {
        screen = getSource().getScreen();
        edit();
        screen.renderView(getDestination());
    }

    /**
     * Edits {@link #getBuilder() state of a the source view}.
     */
    protected abstract void edit();

    /**
     * Obtains state of the source view.
     *
     * @return source view state
     */
    protected B getBuilder() {
        return getSource().getState();
    }

    protected Screen getScreen() {
        return screen;
    }

    @VisibleForTesting
    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    /**
     * {@inheritDoc}
     *
     * @param <M> the type of the command message
     * @param <B> the validating builder type for the command message
     * @param <T> {@inheritDoc}
     */
    public abstract static class AbstractCommandActionProducer<M extends Message,
                                                               B extends ValidatingBuilder<M, ? extends Message.Builder>,
                                                               T extends EditCommandAction<M, B>>
            extends AbstractActionProducer<CommandView<M, B>, CommandView<M, B>, T> {

        protected AbstractCommandActionProducer(String name, Shortcut shortcut) {
            super(name, shortcut);
        }
    }
}
