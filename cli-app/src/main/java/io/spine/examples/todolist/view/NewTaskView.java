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

import com.google.common.annotations.VisibleForTesting;
import io.spine.cli.EditOperation;
import io.spine.cli.Screen;
import io.spine.cli.action.CommandAction;
import io.spine.cli.action.CommandAction.CommandActionProducer;
import io.spine.cli.action.Shortcut;
import io.spine.cli.view.CommandView;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;
import io.spine.examples.todolist.client.TodoClient;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.Identifier.newUuid;
import static io.spine.cli.action.EditCommandAction.editCommandActionProducer;
import static java.util.Collections.singletonList;

/**
 * A {@code CommandView}, that allows to create a task in a quick mode.
 *
 * <p>To create a task in the way, user should specify a task description only.
 *
 * @author Dmytro Grankin
 */
public class NewTaskView extends CommandView<CreateBasicTask, CreateBasicTaskVBuilder> {

    static final String EMPTY_VALUE = "empty";
    static final String DESCRIPTION_LABEL = "Description:";

    private NewTaskView() {
        super("New task");
    }

    /**
     * Creates a new {@code NewTaskView} instance.
     *
     * @param client the client to use
     * @return the new instance.
     */
    public static NewTaskView create(TodoClient client) {
        checkNotNull(client);
        final NewTaskView view = new NewTaskView();
        view.addAction(editCommandActionProducer("Start input", new Shortcut("i"),
                                                 singletonList(new DescriptionEditOperation())));
        view.addAction(new NewTaskProducer(client));
        return view;
    }

    /**
     * Updates ID of the command and renders the view.
     *
     * @param screen {@inheritDoc}
     */
    @Override
    public void render(Screen screen) {
        getState().setId(generatedId());
        super.render(screen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String renderState(CreateBasicTaskVBuilder state) {
        final String descriptionValue = state.getDescription()
                                             .isEmpty()
                                        ? EMPTY_VALUE
                                        : state.getDescription();
        return DESCRIPTION_LABEL + ' ' + descriptionValue;
    }

    private static TaskId generatedId() {
        return TaskId.newBuilder()
                     .setValue(newUuid())
                     .build();
    }

    /**
     * The operation that updates the {@linkplain CreateBasicTask#getDescription() description}.
     */
    @VisibleForTesting
    static class DescriptionEditOperation implements EditOperation<CreateBasicTask,
                                                                   CreateBasicTaskVBuilder> {

        private static final String PROMPT = "Please enter the task description";

        /**
         * Prompts a user for a task description and updates state of the specified builder.
         *
         * @param screen  {@inheritDoc}
         * @param builder {@inheritDoc}
         */
        @Override
        public void start(Screen screen, CreateBasicTaskVBuilder builder) {
            final String description = screen.promptUser(PROMPT);
            builder.setDescription(description);
        }
    }

    /**
     * The action for posting {@link CreateBasicTask} command to a server.
     */
    private static class CreateTask extends CommandAction<CreateBasicTask,
                                                          CreateBasicTaskVBuilder> {

        private final TodoClient client;

        private CreateTask(TodoClient client,
                           CommandView<CreateBasicTask, CreateBasicTaskVBuilder> source) {
            super(source);
            this.client = client;
        }

        @Override
        protected void post(CreateBasicTask commandMessage) {
            client.create(commandMessage);
        }
    }

    /**
     * Producer of {@code NewTaskView}.
     */
    private static class NewTaskProducer extends CommandActionProducer<CreateBasicTask,
                                                                       CreateBasicTaskVBuilder,
                                                                       CreateTask> {

        private final TodoClient client;

        private NewTaskProducer(TodoClient client) {
            this.client = client;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CreateTask create(CommandView<CreateBasicTask, CreateBasicTaskVBuilder> source) {
            return new CreateTask(client, source);
        }
    }
}
