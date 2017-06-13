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
import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskView;

import java.util.Date;
import java.util.List;

import static com.google.protobuf.util.Timestamps.toMillis;
import static io.spine.examples.todolist.mode.CommonMode.CommonModeConstants.DEFAULT_VALUE;
import static io.spine.examples.todolist.mode.InteractiveMode.getDateFormat;

/**
 * Serves as utility class for creating user friendly representation of the information.
 *
 * @author Illia Shepilov
 */
class DisplayHelper {

    private static final String MY_LIST_TASKS = "My list tasks";
    private static final String DRAFT_TASKS = "Draft tasks";
    private static final String LABELLED_TASKS = "Labelled tasks";
    private static final String TASK = "Task: ";
    private static final String LABEL_ID_VALUE = "Label id: ";
    private static final String TASK_ID_VALUE = "Task id: ";
    private static final String LABEL_TITLE_VALUE = "Label title: ";
    private static final String LABEL_COLOR_VALUE = "Label color: ";
    private static final String DESCRIPTION_VALUE = "Description: ";
    private static final String PRIORITY_VALUE = "Priority: ";
    private static final String DUE_DATE_VALUE = "Due date: ";

    private DisplayHelper() {
    }

    static String constructUserFriendlyDate(Timestamp timestamp) {
        final long millis = toMillis(timestamp);
        return millis == 0
               ? DEFAULT_VALUE
               : getDateFormat().format(new Date(millis));
    }

    static String constructUserFriendlyMyList(MyListView myListView) {
        final List<TaskView> viewList = myListView.getMyList()
                                                  .getItemsList();
        final StringBuilder builder = new StringBuilder(MY_LIST_TASKS);
        builder.append(InteractiveMode.ModeConstants.LINE_SEPARATOR);
        for (TaskView view : viewList) {
            constructUserFriendlyTaskView(builder, view);
        }
        return builder.toString();
    }

    static String constructUserFriendlyDraftTasks(DraftTasksView draftTasksView) {
        final List<TaskView> viewList = draftTasksView.getDraftTasks()
                                                      .getItemsList();
        final StringBuilder builder = new StringBuilder(DRAFT_TASKS);
        builder.append(InteractiveMode.ModeConstants.LINE_SEPARATOR);
        for (TaskView view : viewList) {
            constructUserFriendlyTaskView(builder, view);
        }
        return builder.toString();
    }

    static String constructUserFriendlyLabelledTasks(List<LabelledTasksView> labelledTasksView) {
        final StringBuilder builder = new StringBuilder(LABELLED_TASKS);
        builder.append(InteractiveMode.ModeConstants.LINE_SEPARATOR);
        for (LabelledTasksView labelledView : labelledTasksView) {
            constructLabelledView(builder, labelledView);
        }

        return builder.toString();
    }

    private static void constructLabelledView(StringBuilder builder,
                                              LabelledTasksView labelledView) {
        builder.append(LABEL_ID_VALUE)
               .append(InteractiveMode.ModeConstants.LINE_SEPARATOR)
               .append(LABEL_TITLE_VALUE)
               .append(labelledView.getLabelTitle())
               .append(InteractiveMode.ModeConstants.LINE_SEPARATOR)
               .append(LABEL_COLOR_VALUE)
               .append(labelledView.getLabelColor())
               .append(InteractiveMode.ModeConstants.LINE_SEPARATOR);
        final List<TaskView> viewList = labelledView.getLabelledTasks()
                                                    .getItemsList();
        for (TaskView view : viewList) {
            constructUserFriendlyTaskView(builder, view);
        }
    }

    private static void constructUserFriendlyTaskView(StringBuilder builder, TaskView view) {
        final String date = constructUserFriendlyDate(view.getDueDate());
        final String taskIdValue = view.getId()
                                       .getValue();
        builder.append(TASK)
               .append(InteractiveMode.ModeConstants.LINE_SEPARATOR)
               .append(TASK_ID_VALUE)
               .append(taskIdValue)
               .append(InteractiveMode.ModeConstants.LINE_SEPARATOR)
               .append(DESCRIPTION_VALUE)
               .append(view.getDescription())
               .append(InteractiveMode.ModeConstants.LINE_SEPARATOR)
               .append(PRIORITY_VALUE)
               .append(view.getPriority())
               .append(InteractiveMode.ModeConstants.LINE_SEPARATOR)
               .append(DUE_DATE_VALUE)
               .append(date)
               .append(InteractiveMode.ModeConstants.LINE_SEPARATOR)
               .append(LABEL_ID_VALUE)
               .append(view.getLabelId())
               .append(InteractiveMode.ModeConstants.LINE_SEPARATOR);
    }
}
