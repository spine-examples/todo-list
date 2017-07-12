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

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.action.CommandAction;
import io.spine.examples.todolist.action.EditCommandAction;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;
import io.spine.examples.todolist.view.command.TaskCreationView.EnterDescription.EnterDescriptionProducer;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.AppConfig.getClient;

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
        view.addAction(new EnterDescriptionProducer("Enter description", new Shortcut("d")));
        view.addAction(new CreateTask.CreateTaskProducer());
        return view;
    }

    @Override
    protected void renderBody() {
        getState().setId(generatedId());
        super.renderBody();
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
    static class EnterDescription extends EditCommandAction<CreateBasicTask,
                                                            CreateBasicTaskVBuilder> {

        private static final String PROMPT = "Please enter the task description";

        EnterDescription(String name, Shortcut shortcut,
                         CommandView<CreateBasicTask, CreateBasicTaskVBuilder> source) {
            super(name, shortcut, source);
        }

        @Override
        protected void edit() {
            final String description = getScreen().promptUser(PROMPT);
            getBuilder().setDescription(description);
        }

        static class EnterDescriptionProducer extends EditCommandActionProducer<CreateBasicTask,
                                                                                CreateBasicTaskVBuilder,
                                                                                EnterDescription> {

            private EnterDescriptionProducer(String name, Shortcut shortcut) {
                super(name, shortcut);
            }

            @Override
            public EnterDescription create(CommandView<CreateBasicTask,
                                           CreateBasicTaskVBuilder> source) {
                return new EnterDescription(getName(), getShortcut(), source);
            }
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
