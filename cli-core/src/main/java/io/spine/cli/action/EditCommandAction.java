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

package io.spine.cli.action;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;
import io.spine.cli.Application;
import io.spine.cli.EditOperation;
import io.spine.cli.Screen;
import io.spine.cli.view.CommandView;
import io.spine.protobuf.ValidatingBuilder;
import io.spine.validate.ValidationException;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.cli.ConstraintViolationFormatter.format;

/**
 * An {@link Action}, that edits a {@linkplain CommandView#getState() state of the command view}.
 *
 * @param <M>
 *         the type of the command message
 * @param <B>
 *         the validating builder type for the command message
 */
public class EditCommandAction<M extends Message,
                               B extends ValidatingBuilder<M>>
        extends AbstractAction<CommandView<M, B>, CommandView<M, B>> {

    private final Collection<EditOperation<M, B>> editOperations;

    private EditCommandAction(String name, Shortcut shortcut,
                              CommandView<M, B> source,
                              CommandView<M, B> destination,
                              Collection<EditOperation<M, B>> editOperations) {
        super(name, shortcut, source, destination);
        checkArgument(!editOperations.isEmpty());
        this.editOperations = editOperations;
    }

    /**
     * Applies {@link #editOperations} to the {@linkplain CommandView#getState() source view state}
     * and then renders destination view.
     */
    @Override
    public void execute() {
        Screen screen = Application.instance()
                                   .screen();
        for (EditOperation<M, B> edit : editOperations) {
            start(edit, screen);
        }
        screen.renderView(getDestination());
    }

    @VisibleForTesting
    void start(EditOperation<M, B> edit, Screen screen) {
        try {
            edit.start(screen, getSource().getState());
        } catch (ValidationException e) {
            List<String> errorMessages = format(e.getConstraintViolations());
            errorMessages.forEach(screen::println);
            start(edit, screen);
        }
    }

    /**
     * Creates a new instance of a {@code EditCommandActionProducer}.
     *
     * @param name
     *         the name for the action
     * @param shortcut
     *         the shortcut for the action
     * @param edits
     *         the {@linkplain EditOperation edit operations} for the action
     * @param <M>
     *         the type of the command message
     * @param <B>
     *         the validating builder type for the command message
     * @return the new producer
     */
    public static <M extends Message, B extends ValidatingBuilder<M>>
    EditCommandActionProducer<M, B>
    editCommandActionProducer(String name,
                              Shortcut shortcut,
                              Collection<EditOperation<M, B>> edits) {
        return new EditCommandActionProducer<>(name, shortcut, edits);
    }

    /**
     * Producer of edit command actions.
     *
     * @param <M>
     *         the type of the command message
     * @param <B>
     *         the validating builder type for the command message
     */
    public static class EditCommandActionProducer<
            M extends Message,
            B extends ValidatingBuilder<M>>
            extends AbstractActionProducer<CommandView<M, B>,
                                           CommandView<M, B>,
                                           EditCommandAction<M, B>> {

        private final Collection<EditOperation<M, B>> edits;

        private EditCommandActionProducer(String name, Shortcut shortcut,
                                          Collection<EditOperation<M, B>> edits) {
            super(name, shortcut);
            checkArgument(!edits.isEmpty());
            this.edits = edits;
        }

        @Override
        public EditCommandAction<M, B> create(CommandView<M, B> source) {
            return new EditCommandAction<>(getName(), getShortcut(), source, source, edits);
        }
    }
}
