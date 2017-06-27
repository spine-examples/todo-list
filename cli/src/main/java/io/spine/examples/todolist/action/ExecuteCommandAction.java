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

import com.google.protobuf.Message;
import io.spine.examples.todolist.AppConfig;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.view.command.CommandView;
import io.spine.validate.ValidatingBuilder;

/**
 * An {@code ExecuteCommandAction} executes a command prepared by {@link CommandView}.
 *
 * @param <M> the type of the command message
 * @param <B> the validating builder for the command message
 * @author Dmytro Grankin
 */
public abstract class ExecuteCommandAction<M extends Message,
                                           B extends ValidatingBuilder<M, ? extends Message.Builder>>
        extends Action<CommandView<M, B>> {

    private static final String ACTION_NAME = "Finish";
    private static final String ACTION_SHORTCUT = "f";

    private final TodoClient client = AppConfig.getClient();

    protected ExecuteCommandAction() {
        super(ACTION_NAME, ACTION_SHORTCUT);
    }

    /**
     * Executes the obtained command from the specified source.
     *
     * @param source {@inheritDoc}
     */
    @Override
    public void execute(CommandView<M, B> source) {
        final M commandMessage = source.getState()
                                       .build();
        executeCommand(commandMessage);
        source.display(this);
    }

    protected abstract void executeCommand(M commandMessage);

    protected TodoClient getClient() {
        return client;
    }
}
