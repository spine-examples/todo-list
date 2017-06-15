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
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.mode.menu.Menu;

import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static io.spine.examples.todolist.mode.TodoListCommands.createFinalizeDraftCmd;

/**
 * @author Dmytro Grankin
 */
class TaskStatusMenu extends Menu {

    TaskStatusMenu() {
        super(Menu.newBuilder()
                  .setMenuExit(BACK_TO_THE_MENU_MESSAGE)
                  .addMenuItem("Finalize the draft.", new FinalizeDraftMode())
                  .addMenuItem("Complete the task.", new CompleteTaskMode())
                  .addMenuItem("Reopen the task.", new ReopenTaskMode())
                  .addMenuItem("Delete the task.", new DeleteTaskMode())
                  .addMenuItem("Restore the task.", new RestoreTaskMode()));
    }

    private static class FinalizeDraftMode extends InteractiveMode {

        @Override
        public void start() {
            final TaskId taskId;
            try {
                taskId = obtainTaskId();
            } catch (InputCancelledException ignored) {
                return;
            }
            final FinalizeDraft finalizeDraft = createFinalizeDraftCmd(taskId);
            getClient().finalize(finalizeDraft);
        }
    }

    private static class DeleteTaskMode extends InteractiveMode {

        @Override
        public void start() {
            final TaskId taskId;
            try {
                taskId = obtainTaskId();
            } catch (InputCancelledException ignored) {
                return;
            }
            final DeleteTask deleteTask = createDeleteTaskCmd(taskId);
            getClient().delete(deleteTask);
        }

        private static DeleteTask createDeleteTaskCmd(TaskId taskId) {
            return DeleteTask.newBuilder()
                             .setId(taskId)
                             .build();
        }
    }

    private static class ReopenTaskMode extends InteractiveMode {

        @Override
        public void start() {
            final TaskId taskId;
            try {
                taskId = obtainTaskId();
            } catch (InputCancelledException ignored) {
                return;
            }
            final ReopenTask reopenTask = createReopenTaskCmd(taskId);
            getClient().reopen(reopenTask);
        }

        private static ReopenTask createReopenTaskCmd(TaskId taskId) {
            return ReopenTask.newBuilder()
                             .setId(taskId)
                             .build();
        }
    }

    private static class RestoreTaskMode extends InteractiveMode {

        @Override
        public void start() {
            final TaskId taskId;
            try {
                taskId = obtainTaskId();
            } catch (InputCancelledException ignored) {
                return;
            }
            final RestoreDeletedTask restoreDeletedTask = createRestoreDeletedTaskCmd(taskId);
            getClient().restore(restoreDeletedTask);
        }

        private static RestoreDeletedTask createRestoreDeletedTaskCmd(TaskId taskId) {
            return RestoreDeletedTask.newBuilder()
                                     .setId(taskId)
                                     .build();
        }
    }

    private static class CompleteTaskMode extends InteractiveMode {

        @Override
        public void start() {
            final TaskId taskId;
            try {
                taskId = obtainTaskId();
            } catch (InputCancelledException ignored) {
                return;
            }
            final CompleteTask completeTask = CompleteTask.newBuilder()
                                                          .setId(taskId)
                                                          .build();
            getClient().complete(completeTask);
        }
    }
}
