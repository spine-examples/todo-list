/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.q.projection;

import com.google.common.annotations.VisibleForTesting;
import io.spine.core.EventContext;
import io.spine.core.Subscribe;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.enrichments.DetailsEnrichment;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.events.LabelledTaskRestored;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;
import io.spine.server.projection.Projection;
import io.spine.util.Exceptions.newIllegalStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.spine.examples.todolist.q.projection.LabelColorView.valueOf;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.newTaskListView;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.removeViewsByLabelId;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.removeViewsByTaskId;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.updateTaskItemList;

/**
 * A projection state of the created tasks marked with a certain label.
 *
 * <p>Contains the data about the task view.
 * <p>This view includes all tasks per label that are neither in a draft state nor deleted.
 */
@SuppressWarnings("OverlyCoupledClass")
public class LabelledTasksViewProjection extends Projection<LabelId,
                                                            LabelledTasksView,
                                                            LabelledTasksViewVBuilder> {

    /**
     * Creates a new instance.
     *
     * @param id
     *         the ID for the new instance
     * @throws IllegalArgumentException
     *         if the ID is not of one of the supported types
     */
    @VisibleForTesting
    LabelledTasksViewProjection(LabelId id) {
        super(id);
    }

    @Subscribe
    void on(LabelAssignedToTask event, EventContext context) {
        LabelId labelId = event.getLabelId();
        TaskId taskId = event.getTaskId();
        addTaskItemAndUpdateLabelDetails(labelId, taskId, context);
    }

    @Subscribe
    void on(LabelledTaskRestored event, EventContext context) {
        LabelId labelId = event.getLabelId();
        TaskId taskId = event.getTaskId();
        addTaskItemAndUpdateLabelDetails(labelId, taskId, context);
    }

    @Subscribe
    void on(LabelRemovedFromTask event) {
        LabelId labelId = event.getLabelId();
        boolean isEquals = builder().getId()
                                    .equals(labelId);
        if (isEquals) {
            List<TaskItem> views = new ArrayList<>(builder().getLabelledTasks()
                                                            .getItemsList());
            TaskListView updatedView = removeViewsByLabelId(views, labelId);
            setLabelledTasks(updatedView);
        }
    }

    @Subscribe
    void on(TaskDeleted event) {
        setDeleted(false);
        List<TaskItem> views = new ArrayList<>(builder().getLabelledTasks()
                                                        .getItemsList());
        TaskListView updatedView = removeViewsByTaskId(views, event.getTaskId());
        setLabelledTasks(updatedView);
    }

    @Subscribe
    void on(TaskDescriptionUpdated event) {
        List<TaskItem> views = builder().getLabelledTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateLabelledTasks(updatedList);
    }

    @Subscribe
    void on(TaskPriorityUpdated event) {
        List<TaskItem> views = builder().getLabelledTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateLabelledTasks(updatedList);
    }

    @Subscribe
    void on(TaskDueDateUpdated event) {
        List<TaskItem> views = builder().getLabelledTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateLabelledTasks(updatedList);
    }

    @Subscribe
    void on(TaskCompleted event) {
        List<TaskItem> views = builder().getLabelledTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateLabelledTasks(updatedList);
    }

    @Subscribe
    void on(TaskReopened event) {
        List<TaskItem> views = builder().getLabelledTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateLabelledTasks(updatedList);
    }

    @Subscribe
    void on(LabelDetailsUpdated event) {
        List<TaskItem> views = builder().getLabelledTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        TaskListView taskListView = newTaskListView(updatedList);
        LabelDetails newDetails = event.getLabelDetailsChange()
                                       .getNewDetails();

        builder().setId(id())
                 .setLabelColor(valueOf(newDetails.getColor()))
                 .setLabelTitle(newDetails.getTitle())
                 .setLabelledTasks(taskListView);
    }

    private void addTaskItemAndUpdateLabelDetails(LabelId labelId,
                                                  TaskId taskId,
                                                  EventContext context) {
        builder().setId(labelId);
        Optional<DetailsEnrichment> details = context.find(DetailsEnrichment.class);
        if (!details.isPresent()) {
            String msg = "Could not get details enrichment from context %s.";
            throw newIllegalStateException(msg, context);
        }
        details.map(DetailsEnrichment::getTaskDetails)
               .map(taskDetails -> viewFor(taskDetails, labelId, taskId))
               .ifPresent(this::addTaskItem);
        details.map(DetailsEnrichment::getLabelDetails)
               .ifPresent(this::updateLabelDetails);
    }

    private static TaskItem viewFor(TaskDetails taskDetails, LabelId labelId, TaskId taskId) {
        return TaskItem.newBuilder()
                       .setId(taskId)
                       .setLabelId(labelId)
                       .setDescription(taskDetails.getDescription())
                       .setPriority(taskDetails.getPriority())
                       .build();
    }

    private void addTaskItem(TaskItem taskView) {
        List<TaskItem> views = new ArrayList<>(builder().getLabelledTasks()
                                                        .getItemsList());
        views.add(taskView);
        updateLabelledTasks(views);
    }

    private void updateLabelDetails(LabelDetails newDetails) {
        builder().setLabelTitle(newDetails.getTitle());

        if (newDetails.getColor() != LabelColor.LC_UNDEFINED) {
            String hexColor = valueOf(newDetails.getColor());
            builder().setLabelColor(hexColor);
        }
    }

    private void updateLabelledTasks(List<TaskItem> updatedList) {
        TaskListView taskListView = newTaskListView(updatedList);
        setLabelledTasks(taskListView);
    }

    private void setLabelledTasks(TaskListView updatedView) {
        builder().setId(id())
                 .setLabelledTasks(updatedView);
    }
}
