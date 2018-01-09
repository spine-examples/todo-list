/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import static io.spine.examples.todolist.EnrichmentHelper.getEnrichment;
import static io.spine.examples.todolist.q.projection.LabelColorView.valueOf;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.newTaskListView;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.removeViewsByLabelId;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.removeViewsByTaskId;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.updateTaskItemList;

/**
 * A projection state of the created tasks marked with a certain label.
 *
 * <p> Contains the data about the task view.
 * <p> This view includes all tasks per label that are neither in a draft state nor deleted.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("OverlyCoupledClass")
public class LabelledTasksViewProjection extends Projection<LabelId,
                                                            LabelledTasksView,
                                                            LabelledTasksViewVBuilder> {

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public LabelledTasksViewProjection(LabelId id) {
        super(id);
    }

    @Subscribe
    public void on(LabelAssignedToTask event, EventContext context) {
        final LabelId labelId = event.getLabelId();
        final TaskId taskId = event.getTaskId();
        addTaskItemAndUpdateLabelDetails(labelId, taskId, context);
    }

    @Subscribe
    public void on(LabelledTaskRestored event, EventContext context) {
        final LabelId labelId = event.getLabelId();
        final TaskId taskId = event.getTaskId();
        addTaskItemAndUpdateLabelDetails(labelId, taskId, context);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final LabelId labelId = event.getLabelId();
        final boolean isEquals = getBuilder().getLabelId()
                                             .equals(labelId);
        if (isEquals) {
            final List<TaskItem> views = new ArrayList<>(getBuilder().getLabelledTasks()
                                                                     .getItemsList());
            final TaskListView updatedView = removeViewsByLabelId(views, labelId);
            getBuilder().setLabelledTasks(updatedView);
        }
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final List<TaskItem> views = new ArrayList<>(getBuilder().getLabelledTasks()
                                                                 .getItemsList());
        final TaskListView updatedView = removeViewsByTaskId(views, event.getTaskId());
        getBuilder().setLabelledTasks(updatedView);
    }

    @Subscribe
    public void on(TaskDescriptionUpdated event) {
        final List<TaskItem> views = getBuilder().getLabelledTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setLabelledTasks(taskListView);
    }

    @Subscribe
    public void on(TaskPriorityUpdated event) {
        final List<TaskItem> views = getBuilder().getLabelledTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setLabelledTasks(taskListView);
    }

    @Subscribe
    public void on(TaskDueDateUpdated event) {
        final List<TaskItem> views = getBuilder().getLabelledTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setLabelledTasks(taskListView);
    }

    @Subscribe
    public void on(TaskCompleted event) {
        final List<TaskItem> views = getBuilder().getLabelledTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setLabelledTasks(taskListView);
    }

    @Subscribe
    public void on(TaskReopened event) {
        final List<TaskItem> views = getBuilder().getLabelledTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setLabelledTasks(taskListView);
    }

    @Subscribe
    public void on(LabelDetailsUpdated event) {
        final List<TaskItem> views = getBuilder().getLabelledTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        final LabelDetails newDetails = event.getLabelDetailsChange()
                                             .getNewDetails();

        getBuilder().setLabelColor(valueOf(newDetails.getColor()))
                    .setLabelTitle(newDetails.getTitle())
                    .setLabelledTasks(taskListView);
    }

    private void addTaskItemAndUpdateLabelDetails(LabelId labelId, TaskId taskId,
                                                  EventContext context) {
        final DetailsEnrichment enrichment = getEnrichment(DetailsEnrichment.class, context);
        final TaskDetails taskDetails = enrichment.getTaskDetails();

        final TaskItem taskView = viewFor(taskDetails, labelId, taskId);
        final LabelDetails labelDetails = enrichment.getLabelDetails();

        getBuilder().setLabelId(labelId);
        addTaskItem(taskView);
        updateLabelDetails(labelDetails);
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
        final List<TaskItem> views = new ArrayList<>(getBuilder().getLabelledTasks()
                                                                 .getItemsList());
        views.add(taskView);
        final TaskListView taskListView = newTaskListView(views);
        getBuilder().setLabelledTasks(taskListView);
    }

    private void updateLabelDetails(LabelDetails newDetails) {
        getBuilder().setLabelTitle(newDetails.getTitle());

        if (newDetails.getColor() != LabelColor.LC_UNDEFINED) {
            final String hexColor = valueOf(newDetails.getColor());
            getBuilder().setLabelColor(hexColor);
        }
    }
}
