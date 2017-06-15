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
import io.spine.examples.todolist.AppConfig;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.validator.DescriptionValidator;
import io.spine.examples.todolist.validator.DueDateValidator;
import io.spine.examples.todolist.validator.IdValidator;
import io.spine.examples.todolist.validator.LabelColorValidator;
import io.spine.examples.todolist.validator.NeitherCompletedNorDeletedValidator;
import io.spine.examples.todolist.validator.TaskPriorityValidator;
import io.spine.examples.todolist.validator.Validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static io.spine.examples.todolist.UserIO.askUser;
import static io.spine.examples.todolist.UserIO.println;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.DATE_FORMAT;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.ENTER_ID_MESSAGE;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.ENTER_LABEL_ID_MESSAGE;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.LABEL_COLOR_VALUE;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.TASK_PRIORITY_VALUE;

/**
 * @author Dmytro Grankin
 */
public abstract class Mode {

    private Validator<String> priorityValidator;
    private Validator<String> colorValidator;
    private Validator<String> dueDateValidator;
    private Validator<String> commonValidator;
    private Validator<String> idValidator;
    private Validator<String> descriptionValidator;
    private final Map<String, TaskPriority> priorityMap;
    private final Map<String, LabelColor> colorMap;
    private final TodoClient client = AppConfig.getClient();

    public abstract void start();

    protected Mode() {
        priorityMap = initPriorityMap();
        colorMap = initColorMap();
        initValidators();
    }

    protected TodoClient getClient() {
        return client;
    }

    protected LabelColor obtainLabelColor(String message) {
        final String labelColorValue = obtainLabelColorValue(message);
        final LabelColor result = colorMap.get(labelColorValue);
        return result;
    }

    private String obtainLabelColorValue(String message) {
        String color = askUser(message + LABEL_COLOR_VALUE);

        color = color == null ? null : color.toUpperCase();
        final boolean isValid = colorValidator.validate(color);

        if (!isValid) {
            println(colorValidator.getMessage());
            color = obtainLabelColorValue(message);
        }

        return color;
    }

    protected String obtainLabelTitle(String message) {
        String title = askUser(message);

        boolean isValid = commonValidator.validate(title);

        if (!isValid) {
            println(commonValidator.getMessage());
            title = obtainLabelTitle(message);
        }
        return title;
    }

    protected String obtainDescription(String message, boolean isNew) {
        String description = askUser(message);

        if (description.isEmpty() && !isNew) {
            return description;
        }

        final boolean isValid = descriptionValidator.validate(description);

        if (!isValid) {
            println(descriptionValidator.getMessage());
            description = obtainDescription(message, isNew);
        }
        return description;
    }

    protected Timestamp obtainDueDate(String message, boolean isNew) throws ParseException {
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

    private String obtainDueDateValue(String message, boolean isNew) throws ParseException {
        String dueDateValue = askUser(message);

        if (dueDateValue.isEmpty() && !isNew) {
            return dueDateValue;
        }

        final boolean isValid = dueDateValidator.validate(dueDateValue);

        if (!isValid) {
            println(dueDateValidator.getMessage());
            dueDateValue = obtainDueDateValue(message, isNew);
        }
        return dueDateValue;
    }

    protected TaskPriority obtainTaskPriority(String message) {
        final String priorityValue = obtainPriorityValue(message + TASK_PRIORITY_VALUE);
        final TaskPriority result = priorityMap.get(priorityValue);
        return result;
    }

    private String obtainPriorityValue(String message) {
        String priority = askUser(message);

        priority = priority == null ? null : priority.toUpperCase();
        final boolean isValid = priorityValidator.validate(priority);

        if (!isValid) {
            println(priorityValidator.getMessage());
            priority = obtainPriorityValue(message);
        }
        return priority;
    }

    protected LabelId obtainLabelId() {
        final String idValue = obtainIdValue(ENTER_LABEL_ID_MESSAGE);
        final LabelId result = newLabelId(idValue);
        return result;
    }

    protected TaskId obtainTaskId() {
        final String idValue = obtainIdValue(ENTER_ID_MESSAGE);
        final TaskId result = newTaskId(idValue);
        return result;
    }

    private String obtainIdValue(String message) {
        String taskIdValue = askUser(message);

        final boolean isValid = idValidator.validate(taskIdValue);
        if (!isValid) {
            println(idValidator.getMessage());
            taskIdValue = obtainIdValue(message);
        }
        return taskIdValue;
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

    static SimpleDateFormat getDateFormat() {
        final SimpleDateFormat result = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return result;
    }

    static class ModeConstants {
        static final String DATE_FORMAT = "yyyy-MM-dd";
        static final String ENTER_LABEL_ID_MESSAGE = "Please enter the label ID: ";
        static final String ENTER_ID_MESSAGE = "Please enter the task ID: ";
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
        static final String BACK_TO_THE_MENU_MESSAGE = "Back to the previous menu.";
        static final String DEFAULT_VALUE = "default";

        private ModeConstants() {
        }
    }
}
