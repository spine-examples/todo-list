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
import static io.spine.examples.todolist.mode.CreateTaskMenu.CreateTaskModeConstants.CREATE_ONE_MORE_TASK_QUESTION;
import static io.spine.examples.todolist.mode.CreateTaskMenu.CreateTaskModeConstants.SET_DESCRIPTION_MESSAGE;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.BACK;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.NEGATIVE_ANSWER;

/**
 * @author Illia Shepilov
 */
class CreateTaskMenu extends Menu {

    CreateTaskMenu() {
        super(Menu.newBuilder()
                  .setMenuExit(BACK_TO_THE_MENU_MESSAGE)
                  .addMenuItem("Create the task.", new CreateTaskFullMode())
                  .addMenuItem("Create the draft task.", new CreateTaskDraftMode()));
    }

    static class CreateTaskFullMode extends InteractiveMode {

        @Override
        public void start() {
            String line = "";
            while (!line.equals(BACK)) {
                createTask();
                final String approveValue = obtainApproveValue(CREATE_ONE_MORE_TASK_QUESTION);
                if (approveValue.equals(NEGATIVE_ANSWER)) {
                    return;
                }
                line = readLine();
            }
        }

        private void createTask() {
            final TaskId taskId = newTaskId(newUuid());
            final String description = askUser(SET_DESCRIPTION_MESSAGE);
            final CreateBasicTask createTask = createTaskCmd(taskId, description);
            getClient().create(createTask);
        }

        private static CreateBasicTask createTaskCmd(TaskId taskId, String description) {
            return CreateBasicTaskVBuilder.newBuilder()
                                          .setId(taskId)
                                          .setDescription(description)
                                          .build();
        }
    }

    static class CreateTaskDraftMode extends InteractiveMode {

        @Override
        public void start() {
            String line = "";
            while (!line.equals(BACK)) {
                createTaskDraft();
                final String approveValue = obtainApproveValue(CREATE_ONE_MORE_TASK_QUESTION);
                if (approveValue.equals(NEGATIVE_ANSWER)) {
                    return;
                }
                line = readLine();
            }
        }

        private void createTaskDraft() {
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

    static class CreateTaskModeConstants {
        static final String EMPTY = "";
        static final String CREATE_ONE_MORE_TASK_QUESTION = "Do you want to create one more task?(y/n)";
        static final String SET_DESCRIPTION_MESSAGE = "Please enter the task description";

        private CreateTaskModeConstants() {
        }
    }
}
