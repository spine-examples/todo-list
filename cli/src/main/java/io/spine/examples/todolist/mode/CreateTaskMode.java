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
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.client.TodoClient;
import jline.console.ConsoleReader;

import java.text.ParseException;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.BACK_TO_THE_PREVIOUS_MENU_QUESTION;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.CREATED_DRAFT_MESSAGE;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.CREATED_TASK_MESSAGE;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.CREATE_ONE_MORE_TASK_QUESTION;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.CREATE_TASK_PROMPT;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.CREATE_TASK_TITLE;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.DRAFT_FINALIZED_MESSAGE;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.HELP_MESSAGE;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.NEED_TO_FINALIZE_MESSAGE;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.SET_DESCRIPTION_MESSAGE;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.SET_DUE_DATE_MESSAGE;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.SET_DUE_DATE_QUESTION;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.SET_PRIORITY_MESSAGE;
import static io.spine.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.SET_PRIORITY_QUESTION;
import static io.spine.examples.todolist.mode.DisplayHelper.constructUserFriendlyDate;
import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.HELP_ADVICE;
import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.TODO_PROMPT;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.BACK;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.CANCEL_HINT;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.DATE_FORMAT;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.INCORRECT_COMMAND;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.LINE_SEPARATOR;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.NEGATIVE_ANSWER;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.POSITIVE_ANSWER;
import static io.spine.examples.todolist.mode.TodoListCommands.createFinalizeDraftCmd;
import static io.spine.examples.todolist.mode.TodoListCommands.createPriorityChange;
import static io.spine.examples.todolist.mode.TodoListCommands.createStringChange;
import static io.spine.examples.todolist.mode.TodoListCommands.createTimestampChange;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateTaskDescriptionCmd;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateTaskDueDateCmd;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateTaskPriorityCmd;
import static java.lang.String.format;

/**
 * @author Illia Shepilov
 */
class CreateTaskMode extends InteractiveMode {

    private Timestamp dueDate = Timestamp.getDefaultInstance();
    private TaskPriority priority = TaskPriority.TP_UNDEFINED;
    private String description;
    private final Map<String, Mode> modeMap = newHashMap();

    private final TodoClient client;
    private final ConsoleReader reader;

    CreateTaskMode(TodoClient client, ConsoleReader reader) {
        super(reader);
        this.reader = reader;
        this.client = client;
        initModeMap();
    }

    private void initModeMap() {
        modeMap.put("0", new HelpMode(HELP_MESSAGE));
        modeMap.put("1", new CreateTaskFullMode(reader));
        modeMap.put("2", new CreateTaskDraftMode(reader));
    }

    @Override
    public void start() {
        sendMessageToUser(CREATE_TASK_TITLE);
        reader.setPrompt(CREATE_TASK_PROMPT);
        String line = "";

        while (!line.equals(BACK)) {
            line = readLine();
            final Mode mode = modeMap.get(line);

            if (mode == null) {
                sendMessageToUser(INCORRECT_COMMAND);
                continue;
            }

            mode.start();
            final String approve = obtainApproveValue(BACK_TO_THE_PREVIOUS_MENU_QUESTION);
            if (approve.equals(NEGATIVE_ANSWER)) {
                sendMessageToUser(HELP_MESSAGE);
            }

            if (approve.equals(POSITIVE_ANSWER)) {
                line = BACK;
            }
        }

        reader.setPrompt(TODO_PROMPT);
    }

    private void updateDueDateIfNeeded(TaskId taskId) throws ParseException {
        final String approveValue = obtainApproveValue(SET_DUE_DATE_QUESTION);
        if (approveValue.equals(NEGATIVE_ANSWER)) {
            return;
        }

        final Timestamp dueDate;
        try {
            dueDate = obtainDueDate(SET_DUE_DATE_MESSAGE, true);
        } catch (InputCancelledException ignored) {
            return;
        }
        final TimestampChange change = createTimestampChange(dueDate);
        final UpdateTaskDueDate updateTaskDueDate = createUpdateTaskDueDateCmd(taskId, change);
        client.update(updateTaskDueDate);
        this.dueDate = dueDate;
    }

    private void updateTaskValuesIfNeeded(TaskId taskId) {
        try {
            updatePriorityIfNeeded(taskId);
            updateDueDateIfNeeded(taskId);
        } catch (ParseException e) {
            throw new ParseDateException(e);
        }
    }

    private void updatePriorityIfNeeded(TaskId taskId) {
        final String approveValue = obtainApproveValue(SET_PRIORITY_QUESTION);
        if (approveValue.equals(NEGATIVE_ANSWER)) {
            return;
        }

        final TaskPriority priority;
        try {
            priority = obtainTaskPriority(SET_PRIORITY_MESSAGE);
        } catch (InputCancelledException ignored) {
            return;
        }

        final PriorityChange change = createPriorityChange(priority);
        final UpdateTaskPriority updateTaskPriority = createUpdateTaskPriorityCmd(taskId, change);
        client.update(updateTaskPriority);
        this.priority = priority;
    }

    private void clearValues() {
        this.description = "";
        this.priority = TaskPriority.TP_UNDEFINED;
        this.dueDate = Timestamp.getDefaultInstance();
    }

    class CreateTaskFullMode extends InteractiveMode {

        private CreateTaskFullMode(ConsoleReader reader) {
            super(reader);
        }

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
            try {
                createTask(taskId);
            } catch (InputCancelledException ignored) {
                return;
            }
            updateTaskValuesIfNeeded(taskId);

            final String userFriendlyDate = constructUserFriendlyDate(dueDate);
            final String idValue = taskId.getValue();
            final String result = format(CREATED_TASK_MESSAGE,
                                         idValue, description, priority, userFriendlyDate);
            sendMessageToUser(result);

            clearValues();
        }

        private void createTask(TaskId taskId) throws InputCancelledException {
            final String description = obtainDescription(SET_DESCRIPTION_MESSAGE, true);
            final CreateBasicTask createTask = createTaskCmd(taskId, description);
            client.create(createTask);
            CreateTaskMode.this.description = description;
        }

        private CreateBasicTask createTaskCmd(TaskId taskId, String description) {
            return CreateBasicTask.newBuilder()
                                  .setId(taskId)
                                  .setDescription(description)
                                  .build();
        }
    }

    class CreateTaskDraftMode extends InteractiveMode {

        private CreateTaskDraftMode(ConsoleReader reader) {
            super(reader);
        }

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
            try {
                createTaskDraft(taskId);
            } catch (InputCancelledException ignored) {
                return;
            }
            updateTaskValuesIfNeeded(taskId);

            final String userFriendlyDate = constructUserFriendlyDate(dueDate);
            final String idValue = taskId.getValue();
            final String result = format(CREATED_DRAFT_MESSAGE,
                                         idValue, description, priority, userFriendlyDate);
            sendMessageToUser(result);

            finalizeDraftIfNeeded(taskId);
            clearValues();
        }

        private void createTaskDraft(TaskId taskId) throws InputCancelledException {
            final String description = obtainDescription(SET_DESCRIPTION_MESSAGE, true);

            final CreateDraft createTask = createDraftCmdInstance(taskId);
            client.create(createTask);

            final StringChange change = createStringChange(description);
            final UpdateTaskDescription updateTaskDescription =
                    createUpdateTaskDescriptionCmd(taskId, change);
            client.update(updateTaskDescription);
            CreateTaskMode.this.description = description;
        }

        private void finalizeDraftIfNeeded(TaskId taskId) {
            final String approveValue = obtainApproveValue(NEED_TO_FINALIZE_MESSAGE);
            if (approveValue.equals(NEGATIVE_ANSWER)) {
                return;
            }
            final FinalizeDraft finalizeDraft = createFinalizeDraftCmd(taskId);
            client.finalize(finalizeDraft);
            sendMessageToUser(DRAFT_FINALIZED_MESSAGE);
        }

        private CreateDraft createDraftCmdInstance(TaskId taskId) {
            return CreateDraft.newBuilder()
                              .setId(taskId)
                              .build();
        }
    }

    static class CreateTaskModeConstants {
        static final String EMPTY = "";
        static final String SET_PRIORITY_QUESTION = "Do you want to set the task priority?(y/n)";
        static final String SET_DUE_DATE_QUESTION = "Do you want to set the task due date?(y/n)";
        static final String CREATE_ONE_MORE_TASK_QUESTION = "Do you want to create one more task?(y/n)";
        static final String BACK_TO_THE_PREVIOUS_MENU_QUESTION = "Do you want go back to the main menu?(y/n)";
        static final String CREATE_TASK_PROMPT = "create-task>";
        private static final String CREATE_TASK_MODE =
                "******************** Create task menu ********************" + LINE_SEPARATOR;
        static final String NEED_TO_FINALIZE_MESSAGE = "Do you want to finalize the created task draft?(y/n)";
        static final String DRAFT_FINALIZED_MESSAGE = "Task draft finalized.";
        static final String SET_DESCRIPTION_MESSAGE = "Please enter the task description " +
                "(should contain at least 3 symbols): " + LINE_SEPARATOR + CANCEL_HINT;
        static final String SET_DUE_DATE_MESSAGE =
                "Please enter the task due date." + LINE_SEPARATOR +
                        "The correct format is: " + DATE_FORMAT + LINE_SEPARATOR + CANCEL_HINT;
        static final String SET_PRIORITY_MESSAGE =
                "Please enter the task priority." + LINE_SEPARATOR + CANCEL_HINT;
        static final String HELP_MESSAGE = "0:    Help." + LINE_SEPARATOR +
                "1:    Create the task with specified parameters[description is required]." +
                LINE_SEPARATOR +
                "2:    Create the task with specified parameters[description is required][FAST MODE]." +
                LINE_SEPARATOR + BACK_TO_THE_MENU_MESSAGE;
        static final String TASK_PARAMS_DESCRIPTION = "id: %s" + LINE_SEPARATOR +
                "description: %s" + LINE_SEPARATOR +
                "priority: %s" + LINE_SEPARATOR +
                "due date: %s";
        static final String CREATED_DRAFT_MESSAGE =
                "Created task draft with parameters:" + LINE_SEPARATOR + TASK_PARAMS_DESCRIPTION;
        static final String CREATED_TASK_MESSAGE =
                "Created task with parameters:" + LINE_SEPARATOR + TASK_PARAMS_DESCRIPTION;
        static final String CREATE_TASK_TITLE = CREATE_TASK_MODE + HELP_ADVICE + HELP_MESSAGE;

        private CreateTaskModeConstants() {
        }
    }
}
