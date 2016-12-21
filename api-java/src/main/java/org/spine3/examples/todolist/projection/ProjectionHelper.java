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
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;

import java.util.ArrayList;
import java.util.List;

/**
 * Class provides methods to manipulate and handle views.
 *
 * @author Illia Shepilov
 */
/* package */ class ProjectionHelper {

    /**
     * Prevent instantiation.
     */
    private ProjectionHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes {@link TaskView} from list of task view by specified task id.
     *
     * @param views list of the {@link TaskView}
     * @param id    task's id
     * @return {@link TaskListView} without deleted task view
     */
    /* package */
    static TaskListView removeViewByTaskId(List<TaskView> views, TaskId id) {
        final TaskView taskView = views.stream()
                                       .filter(t -> t.getId()
                                                     .equals(id))
                                       .findFirst()
                                       .orElse(null);
        views.remove(taskView);
        final TaskListView result = TaskListView.newBuilder()
                                                .addAllItems(views)
                                                .build();
        return result;
    }

    /**
     * Removes {@link TaskView} from list of task view by specified task label id.
     *
     * @param views list of the {@link TaskView}
     * @param id    task label id
     * @return {@link TaskListView} without deleted task view
     */
    /*package*/
    static TaskListView removeViewByLabelId(List<TaskView> views, TaskLabelId id) {
        final TaskView taskView = views.stream()
                                       .filter(t -> t.getLabelId()
                                                     .equals(id))
                                       .findFirst()
                                       .orElse(null);
        if (taskView != null) {
            views.remove(taskView);
        }

        final TaskListView result = TaskListView.newBuilder()
                                                .addAllItems(views)
                                                .build();
        return result;
    }

    /**
     * Updates {@link TaskView} label details by specified {@link TaskLabelId}.
     *
     * @param views list of {@link TaskView}
     * @param event {@link LabelDetailsUpdated} instance
     * @return list of {@link TaskView} with updated {@link LabelDetails}
     */
    /* package */
    static List<TaskView> constructTaskViewList(List<TaskView> views, LabelDetailsUpdated event) {
        final List<TaskView> updatedList = new ArrayList<>();
        for (TaskView view : views) {
            TaskView addedView = view;
            final boolean willUpdate = view.getLabelId()
                                           .equals(event.getId());
            if (willUpdate) {
                final LabelDetails labelDetails = event.getNewDetails();
                addedView = TaskView.newBuilder()
                                    .setLabelColor(labelDetails.getColor())
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
     * Removes label from each {@link TaskView}, which contains into list.
     *
     * @param views list of {@link TaskView}
     * @param event {@link LabelRemovedFromTask} instance
     * @return list of {@link TaskView} which does not contains specified label
     */
    /* package */
    static List<TaskView> constructTaskViewList(List<TaskView> views, LabelRemovedFromTask event) {
        final List<TaskView> updatedList = new ArrayList<>();
        for (TaskView view : views) {
            TaskView addedView = view;
            final boolean isRemoved = view.getId()
                                          .equals(event.getId());
            if (isRemoved) {
                addedView = TaskView.newBuilder()
                                    .setDueDate(view.getDueDate())
                                    .setPriority(view.getPriority())
                                    .setDescription(view.getDescription())
                                    .setLabelId(TaskLabelId.getDefaultInstance())
                                    .build();
            }
            updatedList.add(addedView);
        }
        return updatedList;
    }

    /**
     * Add label to {@link TaskView}, which contains into list.
     *
     * @param views list of {@link TaskView}
     * @param event {@link LabelAssignedToTask} instance
     * @return list of {@link TaskView} which contains specified label
     */
    /* package */
    static List<TaskView> constructTaskViewList(List<TaskView> views, LabelAssignedToTask event) {
        final List<TaskView> updatedList = new ArrayList<>();
        for (TaskView view : views) {
            TaskView addedView = view;
            final boolean willUpdate = view.getId()
                                           .equals(event.getId());
            if (willUpdate) {
                addedView = TaskView.newBuilder()
                                    .setId(event.getId())
                                    .setLabelId(event.getLabelId())
                                    .setDueDate(view.getDueDate())
                                    .setPriority(view.getPriority())
                                    .setDescription(view.getDescription())
                                    .setLabelColor(view.getLabelColor())
                                    .build();
            }
            updatedList.add(addedView);
        }
        return updatedList;
    }

    /**
     * Mark each {@link TaskView} into list as uncompleted, if {@link TaskId} of task view equals task id of event.
     *
     * @param views list of {@link TaskView}
     * @param event {@link TaskReopened} instance
     * @return list of {@link TaskView}
     */
    /* package */
    static List<TaskView> constructTaskViewList(List<TaskView> views, TaskReopened event) {
        final List<TaskView> updatedList = new ArrayList<>();
        for (TaskView view : views) {
            TaskView addedView = view;
            final boolean willUpdate = view.getId()
                                           .equals(event.getId());
            if (willUpdate) {
                addedView = TaskView.newBuilder()
                                    .setId(event.getId())
                                    .setDueDate(view.getDueDate())
                                    .setPriority(view.getPriority())
                                    .setDescription(view.getDescription())
                                    .setLabelId(view.getLabelId())
                                    .setLabelColor(view.getLabelColor())
                                    .setCompleted(false)
                                    .build();
            }
            updatedList.add(addedView);
        }
        return updatedList;
    }

    /**
     * Mark each {@link TaskView} into list as completed, if {@link TaskId} of task view equals task id of event.
     *
     * @param views list of {@link TaskView}
     * @param event {@link TaskCompleted} instance
     * @return list of {@link TaskView}
     */
    /* package */
    static List<TaskView> constructTaskViewList(List<TaskView> views, TaskCompleted event) {
        final List<TaskView> updatedList = new ArrayList<>();
        for (TaskView view : views) {
            TaskView addedView = view;
            final boolean willUpdate = view.getId()
                                           .equals(event.getId());
            if (willUpdate) {
                addedView = TaskView.newBuilder()
                                    .setId(event.getId())
                                    .setDueDate(view.getDueDate())
                                    .setPriority(view.getPriority())
                                    .setDescription(view.getDescription())
                                    .setLabelId(view.getLabelId())
                                    .setLabelColor(view.getLabelColor())
                                    .setCompleted(true)
                                    .build();
            }
            updatedList.add(addedView);
        }
        return updatedList;
    }

    /**
     * Updates task due date into  {@link TaskView}.
     *
     * @param views list of {@link TaskView}
     * @param event {@link TaskDueDateUpdated} instance
     * @return list of {@link TaskView} with updated task due date
     */
    /* package */
    static List<TaskView> constructTaskViewList(List<TaskView> views, TaskDueDateUpdated event) {
        final List<TaskView> updatedList = new ArrayList<>();
        for (TaskView view : views) {
            TaskView addedView = view;
            final boolean willUpdate = view.getId()
                                           .equals(event.getId());
            if (willUpdate) {
                addedView = TaskView.newBuilder()
                                    .setId(event.getId())
                                    .setDueDate(event.getNewDueDate())
                                    .setPriority(view.getPriority())
                                    .setDescription(view.getDescription())
                                    .setLabelId(view.getLabelId())
                                    .setLabelColor(view.getLabelColor())
                                    .setCompleted(view.getCompleted())
                                    .build();
            }
            updatedList.add(addedView);
        }
        return updatedList;
    }

    /**
     * Updates task priority into  {@link TaskView}.
     *
     * @param views list of {@link TaskView}
     * @param event {@link TaskPriorityUpdated} instance
     * @return list of {@link TaskView} with updated task priority
     */
    /* package */
    static List<TaskView> constructTaskViewList(List<TaskView> views, TaskPriorityUpdated event) {
        final List<TaskView> updatedList = new ArrayList<>();
        for (TaskView view : views) {
            TaskView addedView = view;
            final boolean willUpdate = view.getId()
                                           .equals(event.getId());
            if (willUpdate) {
                addedView = TaskView.newBuilder()
                                    .setId(event.getId())
                                    .setPriority(event.getNewPriority())
                                    .setDescription(view.getDescription())
                                    .setLabelId(view.getLabelId())
                                    .setLabelColor(view.getLabelColor())
                                    .setDueDate(view.getDueDate())
                                    .setCompleted(view.getCompleted())
                                    .build();
            }
            updatedList.add(addedView);
        }
        return updatedList;
    }

    /**
     * Updates task description into  {@link TaskView}.
     *
     * @param views list of {@link TaskView}
     * @param event {@link TaskDescriptionUpdated} instance
     * @return list of {@link TaskView} with updated task description
     */
    /* package */
    static List<TaskView> constructTaskViewList(List<TaskView> views, TaskDescriptionUpdated event) {
        final List<TaskView> updatedList = new ArrayList<>();
        for (TaskView view : views) {
            TaskView addedView = view;
            final boolean willUpdate = view.getId()
                                           .equals(event.getId());
            if (willUpdate) {
                addedView = TaskView.newBuilder()
                                    .setId(event.getId())
                                    .setDescription(event.getNewDescription())
                                    .setLabelId(view.getLabelId())
                                    .setLabelColor(view.getLabelColor())
                                    .setDueDate(view.getDueDate())
                                    .setPriority(view.getPriority())
                                    .setCompleted(view.getCompleted())
                                    .build();
            }
            updatedList.add(addedView);
        }
        return updatedList;
    }
}
