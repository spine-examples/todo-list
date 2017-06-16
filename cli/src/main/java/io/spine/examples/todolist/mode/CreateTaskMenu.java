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

package io.spine.examples.todolist.mode;

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.mode.menu.Menu;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.UserIO.askUser;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;

/**
 * @author Illia Shepilov
 */
class CreateTaskMenu extends Menu {

    private static final String CREATE_ONE_MORE_TASK_QUESTION = "Do you want to create one more task?";

    CreateTaskMenu() {
        super(Menu.newBuilder()
                  .setMenuExit(BACK_TO_THE_MENU_MESSAGE)
                  .addMenuItem("Create the task.", new CreateTaskFullMode())
                  .addMenuItem("Create the draft task.", new CreateTaskDraftMode()));
    }

    private static class CreateTaskFullMode extends RepeatableAction {

        private static final String SET_DESCRIPTION_MESSAGE = "Please enter the task description";

        private CreateTaskFullMode() {
            super(CREATE_ONE_MORE_TASK_QUESTION);
        }

        @Override
        void doAction() {
            final CreateBasicTaskVBuilder builder = CreateBasicTaskVBuilder.newBuilder();

            final TaskId taskId = newTaskId(newUuid());
            builder.setId(taskId);

            final String description = askUser(SET_DESCRIPTION_MESSAGE);
            builder.setDescription(description);

            final CreateBasicTask createTask = builder.build();
            getClient().create(createTask);
        }
    }

    private static class CreateTaskDraftMode extends RepeatableAction {

        private CreateTaskDraftMode() {
            super(CREATE_ONE_MORE_TASK_QUESTION);
        }

        @Override
        void doAction() {
            final TaskId taskId = newTaskId(newUuid());
            final CreateDraft createTask = createDraftCmdInstance(taskId);
            getClient().create(createTask);
        }

        private static CreateDraft createDraftCmdInstance(TaskId taskId) {
            return CreateDraft.newBuilder()
                              .setId(taskId)
                              .build();
        }
    }

    private static TaskId newTaskId(String taskIdValue) {
        final TaskId result = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        return result;
    }
}
