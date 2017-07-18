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
import io.spine.cli.action.Shortcut;
import io.spine.cli.view.CommandView;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;

import static io.spine.base.Identifier.newUuid;
import static io.spine.cli.action.EditCommandAction.editCommandActionProducer;
import static io.spine.examples.todolist.AppConfig.getClient;
import static java.util.Collections.singletonList;

/**
 * A {@code CommandView}, that allows to create a task in a quick mode.
 *
 * <p>To create a task in the way, user should specify a task description only.
 *
 * @author Dmytro Grankin
 */
public class TaskCreationView extends CommandView<CreateBasicTask, CreateBasicTaskVBuilder> {

    static final String EMPTY_VALUE = "empty";
    static final String DESCRIPTION_LABEL = "Description:";

    private TaskCreationView() {
        super("New task");
    }

    public static TaskCreationView create() {
        final TaskCreationView view = new TaskCreationView();
        view.addAction(editCommandActionProducer("Start input", new Shortcut("i"),
                                                 singletonList(new DescriptionEditOperation())));
        view.addAction(new CreateTask.CreateTaskProducer());
        return view;
    }

    @Override
    public void render(Screen screen) {
        getState().setId(generatedId());
        super.render(screen);
    }

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

    @VisibleForTesting
    static class DescriptionEditOperation implements EditOperation<CreateBasicTask,
                                                                   CreateBasicTaskVBuilder> {

        private static final String PROMPT = "Please enter the task description";

        @Override
        public void start(Screen screen, CreateBasicTaskVBuilder state) {
            final String description = screen.promptUser(PROMPT);
            state.setDescription(description);
        }
    }

    private static class CreateTask extends CommandAction<CreateBasicTask,
                                                          CreateBasicTaskVBuilder> {

        private CreateTask(CommandView<CreateBasicTask, CreateBasicTaskVBuilder> source) {
            super(source);
        }

        @Override
        protected void post(CreateBasicTask commandMessage) {
            getClient().create(commandMessage);
        }

        static class CreateTaskProducer extends CommandActionProducer<CreateBasicTask,
                                                                      CreateBasicTaskVBuilder,
                                                                      CreateTask> {

            @Override
            public CreateTask create(CommandView<CreateBasicTask, CreateBasicTaskVBuilder> source) {
                return new CreateTask(source);
            }
        }
    }
}
