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

package io.spine.examples.todolist.view;

import com.google.common.annotations.VisibleForTesting;
import io.spine.cli.EditOperation;
import io.spine.cli.Screen;
import io.spine.cli.action.CommandAction;
import io.spine.cli.action.CommandAction.CommandActionProducer;
import io.spine.cli.action.Shortcut;
import io.spine.cli.view.CommandView;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;

import static io.spine.cli.action.EditCommandAction.editCommandActionProducer;
import static io.spine.examples.todolist.AppConfig.getClient;
import static java.util.Collections.singletonList;

/**
 * A {@code CommandView}, that allows to create a task in a quick mode.
 *
 * <p>To create a task in the way, user should specify a task description only.
 */
public final class NewTaskView extends CommandView<CreateBasicTask, CreateBasicTask.Builder> {

    static final String EMPTY_VALUE = "empty";
    static final String DESCRIPTION_LABEL = "Description:";

    private NewTaskView() {
        super("New task");
    }

    /**
     * Creates a new {@code NewTaskView} instance.
     *
     * @return the new instance.
     */
    public static NewTaskView create() {
        NewTaskView view = new NewTaskView();
        view.addAction(editCommandActionProducer("Start input", new Shortcut("i"),
                                                 singletonList(new DescriptionEditOperation())));
        view.addAction(new NewTaskProducer());
        return view;
    }

    /**
     * Updates ID of the command and renders the view.
     */
    @Override
    public void render(Screen screen) {
        getState().setId(TaskId.generate());
        super.render(screen);
    }

    @Override
    protected String renderState(CreateBasicTask.Builder state) {
        String rawDescription = state.getDescription()
                                     .getValue();
        String resultDescription = rawDescription.isEmpty()
                                   ? EMPTY_VALUE
                                   : rawDescription;
        return DESCRIPTION_LABEL + ' ' + resultDescription;
    }

    /**
     * The operation that updates the {@linkplain CreateBasicTask#getDescription() description}.
     */
    @VisibleForTesting
    static class DescriptionEditOperation
            implements EditOperation<CreateBasicTask, CreateBasicTask.Builder> {

        private static final String PROMPT = "Please enter the task description";

        /**
         * Prompts a user for a task description and updates state of the specified builder.
         */
        @Override
        public void start(Screen screen, CreateBasicTask.Builder builder) {
            String description = screen.promptUser(PROMPT);
            builder.setDescription(TaskDescription
                                           .newBuilder()
                                           .setValue(description)
                                           .vBuild());
        }
    }

    /**
     * The action for posting {@link CreateBasicTask} command to a server.
     */
    private static class CreateTask
            extends CommandAction<CreateBasicTask, CreateBasicTask.Builder> {

        private CreateTask(CommandView<CreateBasicTask, CreateBasicTask.Builder> source) {
            super(source);
        }

        @Override
        protected void post(CreateBasicTask commandMessage) {
            getClient().postCommand(commandMessage);
        }
    }

    /**
     * Producer of {@code NewTaskView}.
     */
    private static class NewTaskProducer extends CommandActionProducer<CreateBasicTask,
                                                                       CreateBasicTask.Builder,
                                                                       CreateTask> {

        @Override
        public CreateTask create(CommandView<CreateBasicTask, CreateBasicTask.Builder> source) {
            return new CreateTask(source);
        }
    }
}
