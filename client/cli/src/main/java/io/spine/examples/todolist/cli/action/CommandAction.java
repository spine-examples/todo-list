/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.cli.action;

import com.google.protobuf.Message;
import io.spine.examples.todolist.cli.Application;
import io.spine.examples.todolist.cli.view.CommandView;
import io.spine.protobuf.ValidatingBuilder;

/**
 * A {@code CommandAction} posts a command obtained from a {@link CommandView} to a server.
 *
 * @param <M>
 *         the type of the command message
 * @param <B>
 *         the validating builder type for the command message
 */
public abstract class CommandAction<M extends Message, B extends ValidatingBuilder<M>>
        extends AbstractAction<CommandView<M, B>, CommandView<M, B>> {

    private static final String ACTION_NAME = "Finish";
    private static final Shortcut ACTION_SHORTCUT = new Shortcut("f");

    protected CommandAction(CommandView<M, B> source) {
        super(ACTION_NAME, ACTION_SHORTCUT, source, source);
    }

    /**
     * Executes the obtained command from the source {@link CommandView}.
     *
     * <p>If {@link #post(Message)} is successful,
     * clears state of the source view and renders {@linkplain #getDestination() destination view}.
     */
    @Override
    public void execute() {
        B sourceState = getSource().getState();
        M commandMessage = sourceState.vBuild();
        post(commandMessage);

        sourceState.clear();
        Application.instance()
                   .screen()
                   .renderView(getDestination());
    }

    /**
     * Posts the specified command message to a server.
     *
     * @param commandMessage
     *         the command message to post
     */
    protected abstract void post(M commandMessage);

    /**
     * Producer of command actions.
     *
     * @param <M>
     *         the type of the command message
     * @param <B>
     *         the validating builder type for the command message
     * @param <T>
     *         the type of the action to be created
     */
    public abstract static class CommandActionProducer<
            M extends Message,
            B extends ValidatingBuilder<M>,
            T extends CommandAction<M, B>>
            extends AbstractActionProducer<CommandView<M, B>, CommandView<M, B>, T> {

        protected CommandActionProducer() {
            super(ACTION_NAME, ACTION_SHORTCUT);
        }
    }
}
