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

import io.spine.examples.todolist.action.CommandAction;
import io.spine.examples.todolist.action.ExecuteCommandAction;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;

/**
 * A {@code CommandView}, that allows to create a task in the quick mode.
 *
 * <p>To create a task in the way, user should specify a task description only.
 *
 * @author Dmytro Grankin
 */
public class TaskCreationView extends CommandView<CreateBasicTask, CreateBasicTaskVBuilder> {

    public TaskCreationView() {
        super(false, newBuilder());
        addAction(new EnterDescription("Enter description", "d"));
        addAction(new ExecuteCommand(getState()));
    }

    private static CreateBasicTaskVBuilder newBuilder() {
        return CreateBasicTaskVBuilder.newBuilder();
    }

    private static class EnterDescription extends CommandAction<CreateBasicTask,
                                                                CreateBasicTaskVBuilder> {

        private static final String PROMPT = "Please enter the task description";

        private EnterDescription(String name, String shortcut) {
            super(name, shortcut);
        }

        @Override
        protected void updateState(CreateBasicTaskVBuilder state) {
            final String description = promptUser(PROMPT);
            state.setDescription(description);
        }
    }

    private static class ExecuteCommand extends ExecuteCommandAction<CreateBasicTask,
                                                                     CreateBasicTaskVBuilder> {

        private ExecuteCommand(CreateBasicTaskVBuilder state) {
            super(state);
        }

        @Override
        protected void executeCommand(CreateBasicTask commandMessage) {
            getClient().create(commandMessage);
        }
    }
}
