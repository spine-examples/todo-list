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
import com.google.protobuf.util.Timestamps;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.validator.ApproveAnswerValidator;
import io.spine.examples.todolist.validator.DescriptionValidator;
import io.spine.examples.todolist.validator.DueDateValidator;
import io.spine.examples.todolist.validator.IdValidator;
import io.spine.examples.todolist.validator.LabelColorValidator;
import io.spine.examples.todolist.validator.NeitherCompletedNorDeletedValidator;
import io.spine.examples.todolist.validator.TaskPriorityValidator;
import io.spine.examples.todolist.validator.Validator;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_ID_MESSAGE;
import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.ENTER_LABEL_ID_MESSAGE;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.CANCEL_INPUT;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.DATE_FORMAT;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.INPUT_IS_CANCELED;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.LABEL_COLOR_VALUE;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.TASK_PRIORITY_VALUE;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * A {@code Mode}, that requires user input.
 *
 * @author Illia Shepilov
 */
public abstract class InteractiveMode extends Mode {

    private Validator priorityValidator;
    private Validator colorValidator;
    private Validator dueDateValidator;
    private Validator commonValidator;
    private Validator idValidator;
    private Validator descriptionValidator;
    private Validator approveValidator;
    private final Map<String, TaskPriority> priorityMap;
    private final Map<String, LabelColor> colorMap;
    private final ConsoleReader reader;
    private final TodoClient client;

    InteractiveMode(ConsoleReader reader, TodoClient client) {
        this.reader = reader;
        this.client = client;
        priorityMap = initPriorityMap();
        colorMap = initColorMap();
        initValidators();
    }

    protected LabelColor obtainLabelColor(String message) throws InputCancelledException {
        final String labelColorValue = obtainLabelColorValue(message);
        final LabelColor result = colorMap.get(labelColorValue);
        return result;
    }

    private String obtainLabelColorValue(String message) throws InputCancelledException {
        sendMessageToUser(message + LABEL_COLOR_VALUE);
        String color = readLine();

        if (CANCEL_INPUT.equals(color)) {
            throw new InputCancelledException(INPUT_IS_CANCELED);
        }

        color = color == null ? null : color.toUpperCase();
        final boolean isValid = colorValidator.validate(color);

        if (!isValid) {
            sendMessageToUser(colorValidator.getMessage());
            color = obtainLabelColorValue(message);
        }

        return color;
    }

    protected String obtainLabelTitle(String message) throws InputCancelledException {
        sendMessageToUser(message);
        String title = readLine();

        if (CANCEL_INPUT.equals(title)) {
            throw new InputCancelledException(INPUT_IS_CANCELED);
        }

        boolean isValid = commonValidator.validate(title);

        if (!isValid) {
            sendMessageToUser(commonValidator.getMessage());
            title = obtainLabelTitle(message);
        }
        return title;
    }

    protected String obtainDescription(String message, boolean isNew) throws
                                                                      InputCancelledException {
        sendMessageToUser(message);
        String description = readLine();

        if (CANCEL_INPUT.equals(description)) {
            throw new InputCancelledException(INPUT_IS_CANCELED);
        }

        if (description.isEmpty() && !isNew) {
            return description;
        }

        final boolean isValid = descriptionValidator.validate(description);

        if (!isValid) {
            sendMessageToUser(descriptionValidator.getMessage());
            description = obtainDescription(message, isNew);
        }
        return description;
    }

    protected Timestamp obtainDueDate(String message, boolean isNew) throws ParseException,
                                                                            InputCancelledException {
        final String dueDateValue = obtainDueDateValue(message, isNew);
        if (dueDateValue.isEmpty()) {
            return Timestamp.getDefaultInstance();
        }
        final SimpleDateFormat simpleDateFormat = getDateFormat();
        final long dueDateInMillis = simpleDateFormat.parse(dueDateValue)
                                                     .getTime();
        final Timestamp result = Timestamps.fromMillis(dueDateInMillis);
        return result;
    }

    private String obtainDueDateValue(String message, boolean isNew)
            throws ParseException, InputCancelledException {
        sendMessageToUser(message);
        String dueDateValue = readLine();

        if (CANCEL_INPUT.equals(dueDateValue)) {
            throw new InputCancelledException(INPUT_IS_CANCELED);
        }

        if (dueDateValue.isEmpty() && !isNew) {
            return dueDateValue;
        }

        final boolean isValid = dueDateValidator.validate(dueDateValue);

        if (!isValid) {
            sendMessageToUser(dueDateValidator.getMessage());
            dueDateValue = obtainDueDateValue(message, isNew);
        }
        return dueDateValue;
    }

    protected TaskPriority obtainTaskPriority(String message) throws InputCancelledException {
        final String priorityValue = obtainPriorityValue(message + TASK_PRIORITY_VALUE);
        final TaskPriority result = priorityMap.get(priorityValue);
        return result;
    }

    private String obtainPriorityValue(String message) throws InputCancelledException {
        sendMessageToUser(message);
        String priority = readLine();

        if (CANCEL_INPUT.equals(priority)) {
            throw new InputCancelledException(INPUT_IS_CANCELED);
        }

        priority = priority == null ? null : priority.toUpperCase();
        final boolean isValid = priorityValidator.validate(priority);

        if (!isValid) {
            sendMessageToUser(priorityValidator.getMessage());
            priority = obtainPriorityValue(message);
        }
        return priority;
    }

    protected LabelId obtainLabelId() throws InputCancelledException {
        final String idValue = obtainIdValue(ENTER_LABEL_ID_MESSAGE);
        final LabelId result = newLabelId(idValue);
        return result;
    }

    protected TaskId obtainTaskId() throws InputCancelledException {
        final String idValue = obtainIdValue(ENTER_ID_MESSAGE);
        final TaskId result = newTaskId(idValue);
        return result;
    }

    private String obtainIdValue(String message) throws InputCancelledException {
        sendMessageToUser(message);
        String taskIdValue = readLine();

        if (CANCEL_INPUT.equals(taskIdValue)) {
            throw new InputCancelledException(INPUT_IS_CANCELED);
        }

        final boolean isValid = idValidator.validate(taskIdValue);
        if (!isValid) {
            sendMessageToUser(idValidator.getMessage());
            taskIdValue = obtainIdValue(message);
        }
        return taskIdValue;
    }

    protected String obtainApproveValue(String message) {
        sendMessageToUser(message);
        String approveValue = readLine();
        final boolean isValid = approveValidator.validate(approveValue);
        if (!isValid) {
            sendMessageToUser(approveValidator.getMessage());
            approveValue = obtainApproveValue(message);
        }
        return approveValue;
    }

    protected static LabelId newLabelId(String labelIdValue) {
        final LabelId result = LabelId.newBuilder()
                                      .setValue(labelIdValue)
                                      .build();
        return result;
    }

    protected static TaskId newTaskId(String taskIdValue) {
        final TaskId result = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        return result;
    }

    private void initValidators() {
        descriptionValidator = new DescriptionValidator();
        dueDateValidator = new DueDateValidator(getDateFormat());
        idValidator = new IdValidator();
        priorityValidator = new TaskPriorityValidator(priorityMap);
        commonValidator = new NeitherCompletedNorDeletedValidator();
        colorValidator = new LabelColorValidator(colorMap);
        approveValidator = new ApproveAnswerValidator();
    }

    private static Map<String, TaskPriority> initPriorityMap() {
        final Map<String, TaskPriority> priorityMap = newHashMap();
        priorityMap.put("0", TaskPriority.TP_UNDEFINED);
        priorityMap.put("1", TaskPriority.LOW);
        priorityMap.put("2", TaskPriority.NORMAL);
        priorityMap.put("3", TaskPriority.HIGH);
        return priorityMap;
    }

    private static Map<String, LabelColor> initColorMap() {
        final Map<String, LabelColor> colorMap = newHashMap();
        colorMap.put("0", LabelColor.LC_UNDEFINED);
        colorMap.put("1", LabelColor.GRAY);
        colorMap.put("2", LabelColor.RED);
        colorMap.put("3", LabelColor.GREEN);
        colorMap.put("4", LabelColor.BLUE);
        return colorMap;
    }

    protected String readLine() {
        try {
            return getReader().readLine();
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    static SimpleDateFormat getDateFormat() {
        final SimpleDateFormat result = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return result;
    }

    public ConsoleReader getReader() {
        return reader;
    }

    public TodoClient getClient() {
        return client;
    }

    static class ModeConstants {
        static final String DATE_FORMAT = "yyyy-MM-dd";
        static final String CANCEL_HINT = "Enter `c` to cancel the input.";
        static final String INPUT_IS_CANCELED = "Input is canceled";
        static final String LINE_SEPARATOR = System.lineSeparator();
        static final String TASK_PRIORITY_VALUE = LINE_SEPARATOR +
                "Valid task priority:" + LINE_SEPARATOR +
                "1: LOW;" + LINE_SEPARATOR +
                "2: NORMAL;" + LINE_SEPARATOR +
                "3: HIGH.";
        static final String LABEL_COLOR_VALUE = LINE_SEPARATOR +
                "Valid label colors:" + LINE_SEPARATOR +
                "1: GRAY;" + LINE_SEPARATOR +
                "2: RED;" + LINE_SEPARATOR +
                "3: GREEN;" + LINE_SEPARATOR +
                "4: BLUE.";
        static final String BACK_TO_THE_MENU_MESSAGE = "back: Back to the previous menu.";
        static final String BACK = "back";
        static final String POSITIVE_ANSWER = "y";
        static final String NEGATIVE_ANSWER = "n";
        static final String CANCEL_INPUT = "c";
        static final String INCORRECT_COMMAND = "Incorrect command.";

        private ModeConstants() {
        }
    }
}
