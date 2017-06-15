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

import com.google.protobuf.Timestamp;
import io.spine.change.StringChange;
import io.spine.change.TimestampChange;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.mode.menu.Menu;

import java.text.ParseException;

import static io.spine.examples.todolist.UserIO.println;
import static io.spine.examples.todolist.mode.DisplayHelper.constructUserFriendlyDate;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static io.spine.examples.todolist.mode.TodoListCommands.createPriorityChange;
import static io.spine.examples.todolist.mode.TodoListCommands.createStringChange;
import static io.spine.examples.todolist.mode.TodoListCommands.createTimestampChangeMode;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateTaskDescriptionCmd;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateTaskDueDateCmd;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateTaskPriorityCmd;
import static java.lang.String.format;

/**
 * @author Dmytro Grankin
 */
public class UpdateTaskStateMenu extends Menu {

    public UpdateTaskStateMenu() {
        super(Menu.newBuilder()
                  .setMenuExit(BACK_TO_THE_MENU_MESSAGE)
                  .addMenuItem("Update the task description.", new UpdateTaskDescriptionMode())
                  .addMenuItem("Update the task priority.", new UpdateTaskPriorityMode())
                  .addMenuItem("Update the task due date.", new UpdateTaskDueDateMode()));
    }

    private static class UpdateTaskDescriptionMode extends Mode {

        private static final String ENTER_NEW_DESCRIPTION_MESSAGE =
                "Please enter the new task description:";
        private static final String ENTER_PREVIOUS_DESCRIPTION_MESSAGE =
                "Please enter the previous task description:";
        private static final String UPDATED_DESCRIPTION_MESSAGE =
                "The task description updated. %s --> %s";

        @Override
        public void start() {
            final TaskId taskId = obtainTaskId();
            final String newDescription = obtainDescription(ENTER_NEW_DESCRIPTION_MESSAGE, true);
            final String previousDescription = obtainDescription(ENTER_PREVIOUS_DESCRIPTION_MESSAGE,
                                                                 false);
            final StringChange change = createStringChange(newDescription, previousDescription);
            final UpdateTaskDescription updateTaskDescription =
                    createUpdateTaskDescriptionCmd(taskId, change);
            getClient().update(updateTaskDescription);
            final String previousDescriptionValue = previousDescription.isEmpty()
                                                    ? ModeConstants.DEFAULT_VALUE
                                                    : previousDescription;
            final String message = format(UPDATED_DESCRIPTION_MESSAGE,
                                          previousDescriptionValue, newDescription);
            println(message);
        }
    }

    private static class UpdateTaskPriorityMode extends Mode {

        private static final String ENTER_NEW_PRIORITY_MESSAGE =
                "Please enter the new task priority:";
        private static final String ENTER_PREVIOUS_PRIORITY_MESSAGE =
                "Please enter the previous task priority:";
        private static final String UPDATED_PRIORITY_MESSAGE = "The task priority updated. %s --> %s";

        @Override
        public void start() {
            final TaskId taskId = obtainTaskId();
            final TaskPriority newTaskPriority = obtainTaskPriority(ENTER_NEW_PRIORITY_MESSAGE);
            final TaskPriority previousTaskPriority =
                    obtainTaskPriority(ENTER_PREVIOUS_PRIORITY_MESSAGE);

            final PriorityChange change = createPriorityChange(newTaskPriority,
                                                               previousTaskPriority);
            final UpdateTaskPriority updateTaskPriority = createUpdateTaskPriorityCmd(taskId,
                                                                                      change);
            getClient().update(updateTaskPriority);
            final String message = format(UPDATED_PRIORITY_MESSAGE,
                                          previousTaskPriority, newTaskPriority);
            println(message);
        }
    }

    private static class UpdateTaskDueDateMode extends Mode {

        private static final String ENTER_NEW_DATE_MESSAGE =
                "Please enter the new task due date:";
        private static final String ENTER_PREVIOUS_DATE_MESSAGE =
                "Please enter the previous task due date:";
        private static final String UPDATED_DUE_DATE_MESSAGE = "The task due date updated. %s --> %s";

        @Override
        public void start() {
            final TaskId taskId = obtainTaskId();
            final Timestamp newDueDate;
            final Timestamp previousDueDate;
            try {
                newDueDate = obtainDueDate(ENTER_NEW_DATE_MESSAGE, true);
                previousDueDate = obtainDueDate(ENTER_PREVIOUS_DATE_MESSAGE, false);
            } catch (ParseException e) {
                throw new ParseDateException(e);
            }
            final TimestampChange change = createTimestampChangeMode(newDueDate, previousDueDate);
            final UpdateTaskDueDate updateTaskDueDate = createUpdateTaskDueDateCmd(taskId, change);
            getClient().update(updateTaskDueDate);
            final boolean isEmpty = previousDueDate.getSeconds() == 0;
            final String previousDueDateForUser = isEmpty
                                                  ? ModeConstants.DEFAULT_VALUE
                                                  : constructUserFriendlyDate(previousDueDate);
            final String message = format(UPDATED_DUE_DATE_MESSAGE,
                                          previousDueDateForUser,
                                          constructUserFriendlyDate(newDueDate));
            println(message);
        }
    }
}
