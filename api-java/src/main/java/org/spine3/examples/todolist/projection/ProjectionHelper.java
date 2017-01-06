/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.projection;

import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.LabelledTaskCompleted;
import org.spine3.examples.todolist.LabelledTaskDescriptionUpdated;
import org.spine3.examples.todolist.LabelledTaskDueDateUpdated;
import org.spine3.examples.todolist.LabelledTaskPriorityUpdated;
import org.spine3.examples.todolist.LabelledTaskReopened;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.spine3.examples.todolist.view.TaskView.newBuilder;

/**
 * Class provides methods to manipulate and handle views.
 *
 * @author Illia Shepilov
 */
/* package */ class ProjectionHelper {

    private ProjectionHelper() {
    }

    /**
     * Removes the matching {@link TaskView} from the list of views by the task ID.
     *
     * @param views the list of the {@link TaskView}
     * @param id    the task ID of the task view
     * @return {@link TaskListView} without deleted task view
     */
    /* package */ static TaskListView removeViewByTaskId(List<TaskView> views, TaskId id) {
        final TaskView taskView = views.stream()
                                       .filter(t -> t.getId()
                                                     .equals(id))
                                       .findFirst()
                                       .orElse(null);
        final TaskListView result = removeTaskView(views, taskView);
        return result;
    }

    /**
     * Removes the matching {@link TaskView} from the given list of views by the label ID.
     *
     * @param views the list of the {@link TaskView}
     * @param id    the label ID of the task view
     * @return {@link TaskListView} without deleted task view
     */
    /* package */ static TaskListView removeViewByLabelId(List<TaskView> views, TaskLabelId id) {
        final TaskView taskView = views.stream()
                                       .filter(t -> t.getLabelId()
                                                     .equals(id))
                                       .findFirst()
                                       .orElse(null);
        final TaskListView result = removeTaskView(views, taskView);
        return result;
    }

    private static TaskListView removeTaskView(List<TaskView> views, @Nullable TaskView taskView) {
        if (taskView != null) {
            views.remove(taskView);
        }
        final TaskListView result = TaskListView.newBuilder()
                                                .addAllItems(views)
                                                .build();
        return result;
    }

    /**
     * Updates the label details of the matching {@link TaskView} according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link LabelDetailsUpdated} instance
     * @return the list of the {@link TaskView} with updated {@link LabelDetails}
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, LabelDetailsUpdated event) {
        final int listSize = views.size();
        final List<TaskView> updatedList = new ArrayList<>(listSize);
        for (TaskView view : views) {
            TaskView addedView = view;
            final boolean willUpdate = view.getLabelId()
                                           .equals(event.getLabelId());
            if (willUpdate) {
                final LabelDetails labelDetails = event.getNewDetails();
                addedView = newBuilder().setLabelColor(labelDetails.getColor())
                                        .setDueDate(view.getDueDate())
                                        .setPriority(view.getPriority())
                                        .setDescription(view.getDescription())
                                        .setLabelId(view.getLabelId())
                                        .build();
            }
            updatedList.add(addedView);
        }
        return updatedList;
    }

    /**
     * Removes the label from the matching {@link TaskView} according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link LabelRemovedFromTask} instance
     * @return the list of the {@link TaskView} which does not contains specified label
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, LabelRemovedFromTask event) {
        final TaskId targetTaskId = event.getId();

        final TaskTransformation updateFn = builder -> builder.setLabelId(TaskLabelId.getDefaultInstance());
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Adds the label to the matching {@link TaskView} according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link LabelAssignedToTask} instance
     * @return the list of the {@link TaskView} which contains specified label
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, LabelAssignedToTask event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> builder.setLabelId(event.getLabelId());
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Marks the matching {@link TaskView} as uncompleted according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link TaskReopened} instance
     * @return the list of the {@link TaskView}
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, TaskReopened event) {
        final TaskId targetTaskId = event.getId();

        final TaskTransformation updateFn = builder -> builder.setCompleted(false);
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Marks the matching {@link TaskView} as completed according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link TaskCompleted} instance
     * @return the list of the {@link TaskView}
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, TaskCompleted event) {
        final TaskId targetTaskId = event.getId();

        final TaskTransformation updateFn = builder -> builder.setCompleted(true);
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Updates task due date of the matching {@link TaskView} according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link TaskDueDateUpdated} instance
     * @return the list of the {@link TaskView} with updated task due date
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, TaskDueDateUpdated event) {
        final TaskId targetTaskId = event.getId();

        final TaskTransformation updateFn = builder -> builder.setDueDate(event.getNewDueDate());
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Updates the task priority of the matching {@link TaskView} according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link TaskPriorityUpdated} instance
     * @return the list of the {@link TaskView} with updated task priority
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, TaskPriorityUpdated event) {
        final TaskId targetTaskId = event.getId();

        final TaskTransformation updateFn = builder -> builder.setPriority(event.getNewPriority());
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Updates the task description of the matching {@link TaskView} according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link TaskDescriptionUpdated} instance
     * @return the list of the {@link TaskView} with updated task description
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, TaskDescriptionUpdated event) {
        final TaskId targetTaskId = event.getId();

        final TaskTransformation updateFn = builder -> builder.setDescription(event.getNewDescription());
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Marks the matching {@link TaskView} as uncompleted according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link TaskReopened} instance
     * @return the list of the {@link TaskView}
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, LabelledTaskReopened event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> builder.setCompleted(false);
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Marks the matching {@link TaskView} as completed according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link TaskCompleted} instance
     * @return the list of the {@link TaskView}
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, LabelledTaskCompleted event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> builder.setCompleted(true);
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Updates the task due date of the matching {@link TaskView} according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link TaskDueDateUpdated} instance
     * @return the list of the {@link TaskView} with updated task due date
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, LabelledTaskDueDateUpdated event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> builder.setDueDate(event.getNewDueDate());
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Updates the task priority of the matching {@link TaskView} according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link TaskPriorityUpdated} instance
     * @return the list of the {@link TaskView} with updated task priority
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, LabelledTaskPriorityUpdated event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> builder.setPriority(event.getNewPriority());
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    /**
     * Updates the task description of the matching {@link TaskView} according to the event data.
     *
     * @param views the list of the {@link TaskView}
     * @param event {@link TaskDescriptionUpdated} instance
     * @return the list of the {@link TaskView} with updated task description
     */
    /* package */ static List<TaskView> updateTaskViewList(List<TaskView> views, LabelledTaskDescriptionUpdated event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> builder.setDescription(event.getNewDescription());
        final List<TaskView> result = transformWithUpdate(views, targetTaskId, updateFn);
        return result;
    }

    @SuppressWarnings("MethodWithMultipleLoops")    // It's fine, as there aren't a lot of transformations per task.
    private static List<TaskView> transformWithUpdate(List<TaskView> views,
                                                      TaskId targetTaskId,
                                                      TaskTransformation transformation) {
        final int listSize = views.size();
        final List<TaskView> updatedList = new ArrayList<>(listSize);
        for (TaskView view : views) {
            TaskView addedView = view;
            final boolean willUpdate = view.getId()
                                           .equals(targetTaskId);
            if (willUpdate) {
                TaskView.Builder resultBuilder = newBuilder();

                resultBuilder = resultBuilder.mergeFrom(view);
                resultBuilder = transformation.apply(resultBuilder);

                addedView = resultBuilder.build();
            }
            updatedList.add(addedView);
        }
        return updatedList;
    }

    /**
     * A common interface for the {@link TaskView} transformations.
     */
    private interface TaskTransformation extends Function<TaskView.Builder, TaskView.Builder> {
    }
}
