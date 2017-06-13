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
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.client.TodoClient;
import jline.console.ConsoleReader;

import java.text.ParseException;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.DEFAULT_VALUE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_NEW_COLOR_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_NEW_DATE_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_NEW_DESCRIPTION_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_NEW_PRIORITY_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_NEW_TITLE_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_PREVIOUS_COLOR_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_PREVIOUS_DATE_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_PREVIOUS_DESCRIPTION_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_PREVIOUS_PRIORITY_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_PREVIOUS_TITLE_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.UPDATED_DESCRIPTION_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.UPDATED_DUE_DATE_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.UPDATED_LABEL_DETAILS_MESSAGE;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.UPDATED_PRIORITY_MESSAGE;
import static io.spine.examples.todolist.mode.DisplayHelper.constructUserFriendlyDate;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.CANCEL_HINT;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.LINE_SEPARATOR;
import static io.spine.examples.todolist.mode.TodoListCommands.createLabelDetails;
import static io.spine.examples.todolist.mode.TodoListCommands.createLabelDetailsChange;
import static io.spine.examples.todolist.mode.TodoListCommands.createPriorityChange;
import static io.spine.examples.todolist.mode.TodoListCommands.createStringChange;
import static io.spine.examples.todolist.mode.TodoListCommands.createTimestampChangeMode;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateLabelDetailsCmd;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateTaskDescriptionCmd;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateTaskDueDateCmd;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateTaskPriorityCmd;
import static java.lang.String.format;

/**
 * @author Illia Shepilov
 */
abstract class CommonMode extends InteractiveMode {

    private Map<String, Mode> modeMap;

    CommonMode(TodoClient client, ConsoleReader reader) {
        super(reader, client);
        initModeMap(reader, client);
    }

    private void initModeMap(ConsoleReader reader, TodoClient client) {
        modeMap = newHashMap();
        modeMap.put("2", new UpdateTaskDescriptionMode(reader, client));
        modeMap.put("3", new UpdateTaskPriorityMode(reader, client));
        modeMap.put("4", new UpdateTaskDueDateMode(reader, client));
        modeMap.put("5", new UpdateLabelDetailsMode(reader, client));
        modeMap.put("6", new DeleteTaskMode(reader, client));
        modeMap.put("7", new ReopenTaskMode(reader, client));
        modeMap.put("8", new RestoreTaskMode(reader, client));
        modeMap.put("9", new CompleteTaskMode(reader, client));
        modeMap.put("10", new AssignLabelToTaskMode(reader, client));
        modeMap.put("11", new RemoveLabelFromTaskMode(reader, client));
    }

    Map<String, Mode> getModeMap() {
        return newHashMap(modeMap);
    }

    private static class UpdateTaskDescriptionMode extends InteractiveMode {

        private UpdateTaskDescriptionMode(ConsoleReader reader, TodoClient client) {
            super(reader, client);
        }

        @Override
        public void start() {
            final String newDescription;
            final String previousDescription;
            final TaskId taskId;
            try {
                taskId = obtainTaskId();
                newDescription = obtainDescription(ENTER_NEW_DESCRIPTION_MESSAGE, true);
                previousDescription = obtainDescription(ENTER_PREVIOUS_DESCRIPTION_MESSAGE, false);
            } catch (InputCancelledException ignored) {
                return;
            }
            final StringChange change = createStringChange(newDescription, previousDescription);
            final UpdateTaskDescription updateTaskDescription =
                    createUpdateTaskDescriptionCmd(taskId, change);
            getClient().update(updateTaskDescription);
            final String previousDescriptionValue = previousDescription.isEmpty()
                                                    ? DEFAULT_VALUE
                                                    : previousDescription;
            final String message = format(UPDATED_DESCRIPTION_MESSAGE,
                                          previousDescriptionValue, newDescription);
            sendMessageToUser(message);
        }
    }

    private static class UpdateTaskPriorityMode extends InteractiveMode {

        private UpdateTaskPriorityMode(ConsoleReader reader, TodoClient client) {
            super(reader, client);
        }

        @Override
        public void start() {
            final TaskId taskId;
            final TaskPriority newTaskPriority;
            final TaskPriority previousTaskPriority;
            try {
                taskId = obtainTaskId();
                newTaskPriority = obtainTaskPriority(ENTER_NEW_PRIORITY_MESSAGE);
                previousTaskPriority = obtainTaskPriority(ENTER_PREVIOUS_PRIORITY_MESSAGE);
            } catch (InputCancelledException ignored) {
                return;
            }
            final PriorityChange change = createPriorityChange(newTaskPriority,
                                                               previousTaskPriority);
            final UpdateTaskPriority updateTaskPriority = createUpdateTaskPriorityCmd(taskId,
                                                                                      change);
            getClient().update(updateTaskPriority);
            final String message = format(UPDATED_PRIORITY_MESSAGE,
                                          previousTaskPriority, newTaskPriority);
            sendMessageToUser(message);
        }
    }

    private static class UpdateTaskDueDateMode extends InteractiveMode {

        private UpdateTaskDueDateMode(ConsoleReader reader, TodoClient client) {
            super(reader, client);
        }

        @Override
        public void start() {
            final TaskId taskId;
            final Timestamp newDueDate;
            final Timestamp previousDueDate;
            try {
                taskId = obtainTaskId();
                newDueDate = obtainDueDate(ENTER_NEW_DATE_MESSAGE, true);
                previousDueDate = obtainDueDate(ENTER_PREVIOUS_DATE_MESSAGE, false);
            } catch (InputCancelledException ignored) {
                return;
            } catch (ParseException e) {
                throw new ParseDateException(e);
            }
            final TimestampChange change = createTimestampChangeMode(newDueDate, previousDueDate);
            final UpdateTaskDueDate updateTaskDueDate = createUpdateTaskDueDateCmd(taskId, change);
            getClient().update(updateTaskDueDate);
            final boolean isEmpty = previousDueDate.getSeconds() == 0;
            final String previousDueDateForUser = isEmpty
                                                  ? DEFAULT_VALUE
                                                  : constructUserFriendlyDate(previousDueDate);
            final String message = format(UPDATED_DUE_DATE_MESSAGE,
                                          previousDueDateForUser,
                                          constructUserFriendlyDate(newDueDate));
            sendMessageToUser(message);
        }
    }

    private static class UpdateLabelDetailsMode extends InteractiveMode {

        private UpdateLabelDetailsMode(ConsoleReader reader, TodoClient client) {
            super(reader, client);
        }

        @Override
        public void start() {
            final LabelId labelId;
            final String newTitle;
            final String previousTitle;
            final LabelColor newColor;
            final LabelColor previousColor;
            try {
                labelId = obtainLabelId();
                newTitle = obtainLabelTitle(ENTER_NEW_TITLE_MESSAGE);
                previousTitle = obtainLabelTitle(ENTER_PREVIOUS_TITLE_MESSAGE);
                newColor = obtainLabelColor(ENTER_NEW_COLOR_MESSAGE);
                previousColor = obtainLabelColor(ENTER_PREVIOUS_COLOR_MESSAGE);
            } catch (InputCancelledException ignored) {
                return;
            }
            final LabelDetails newLabelDetails = createLabelDetails(newTitle, newColor);
            final LabelDetails previousLabelDetails = createLabelDetails(previousTitle,
                                                                         previousColor);
            final LabelDetailsChange change = createLabelDetailsChange(newLabelDetails,
                                                                       previousLabelDetails);
            final UpdateLabelDetails updateLabelDetails = createUpdateLabelDetailsCmd(labelId,
                                                                                      change);
            getClient().update(updateLabelDetails);

            final String message = format(UPDATED_LABEL_DETAILS_MESSAGE,
                                          previousColor, newColor, previousTitle, newTitle);
            sendMessageToUser(message);
        }
    }

    private static class DeleteTaskMode extends InteractiveMode {

        private DeleteTaskMode(ConsoleReader reader, TodoClient client) {
            super(reader, client);
        }

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

        private ReopenTaskMode(ConsoleReader reader, TodoClient client) {
            super(reader, client);
        }

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

        private RestoreTaskMode(ConsoleReader reader, TodoClient client) {
            super(reader, client);
        }

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

        private CompleteTaskMode(ConsoleReader reader, TodoClient client) {
            super(reader, client);
        }

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

    private static class AssignLabelToTaskMode extends InteractiveMode {

        private AssignLabelToTaskMode(ConsoleReader reader, TodoClient client) {
            super(reader, client);
        }

        @Override
        public void start() {
            final TaskId taskId;
            final LabelId labelId;
            try {
                taskId = obtainTaskId();
                labelId = obtainLabelId();
            } catch (InputCancelledException ignored) {
                return;
            }
            final AssignLabelToTask assignLabelToTask = createAssignLabelToTaskCmd(taskId, labelId);
            getClient().assignLabel(assignLabelToTask);
        }

        private static AssignLabelToTask createAssignLabelToTaskCmd(TaskId taskId,
                                                                    LabelId labelId) {
            return AssignLabelToTask.newBuilder()
                                    .setId(taskId)
                                    .setLabelId(labelId)
                                    .build();
        }
    }

    private static class RemoveLabelFromTaskMode extends InteractiveMode {

        private RemoveLabelFromTaskMode(ConsoleReader reader, TodoClient client) {
            super(reader, client);
        }

        @Override
        public void start() {
            final TaskId taskId;
            final LabelId labelId;
            try {
                taskId = obtainTaskId();
                labelId = obtainLabelId();
            } catch (InputCancelledException ignored) {
                return;
            }
            final RemoveLabelFromTask removeLabelFromTask = constructRemoveLabelFromTaskCmd(taskId,
                                                                                            labelId);
            getClient().removeLabel(removeLabelFromTask);
        }

        private static RemoveLabelFromTask constructRemoveLabelFromTaskCmd(TaskId taskId,
                                                                           LabelId labelId) {
            return RemoveLabelFromTask.newBuilder()
                                      .setId(taskId)
                                      .setLabelId(labelId)
                                      .build();
        }
    }

    static class CommonModeConstants {
        static final String DEFAULT_VALUE = "default";
        static final String ENTER_ID_MESSAGE =
                "Please enter the task ID: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_NEW_DESCRIPTION_MESSAGE =
                "Please enter the new task description: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_PREVIOUS_DESCRIPTION_MESSAGE =
                "Please enter the previous task description: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_NEW_PRIORITY_MESSAGE =
                "Please enter the new task priority: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_PREVIOUS_PRIORITY_MESSAGE =
                "Please enter the previous task priority: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_NEW_DATE_MESSAGE =
                "Please enter the new task due date: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_PREVIOUS_DATE_MESSAGE =
                "Please enter the previous task due date: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_NEW_TITLE_MESSAGE =
                "Please enter the new label title: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_PREVIOUS_TITLE_MESSAGE =
                "Please enter the previous label title: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_NEW_COLOR_MESSAGE = "Please enter the new label color: " +
                LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_PREVIOUS_COLOR_MESSAGE =
                "Please enter the previous label color: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String UPDATED_DESCRIPTION_MESSAGE = "The task description updated. %s --> %s";
        static final String UPDATED_PRIORITY_MESSAGE = "The task priority updated. %s --> %s";
        static final String UPDATED_DUE_DATE_MESSAGE = "The task due date updated. %s --> %s";
        static final String UPDATED_LABEL_DETAILS_MESSAGE =
                "The label details updated." + LINE_SEPARATOR +
                        "The label color: %s --> %s." + LINE_SEPARATOR +
                        "The label title: %s --> %s";
        static final String HELP_MESSAGE =
                "2:    Update the task description." + LINE_SEPARATOR +
                        "3:    Update the task priority." + LINE_SEPARATOR +
                        "4:    Update the task due date." + LINE_SEPARATOR +
                        "5:    Update the label details." + LINE_SEPARATOR +
                        "6:    Delete the task." + LINE_SEPARATOR +
                        "7:    Reopen the task." + LINE_SEPARATOR +
                        "8:    Restore the task." + LINE_SEPARATOR +
                        "9:    Complete the task." + LINE_SEPARATOR +
                        "10:   Assign the label to task." + LINE_SEPARATOR +
                        "11:   Remove the label from task.";

        private CommonModeConstants() {
        }
    }
}
