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

package org.spine3.examples.todolist.q.projection;

import org.spine3.base.EventContext;
import org.spine3.base.Subscribe;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.enrichments.DetailsEnrichment;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.events.LabelledTaskRestored;
import org.spine3.examples.todolist.c.events.TaskCompleted;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.examples.todolist.c.events.TaskReopened;
import org.spine3.server.projection.Projection;

import java.util.ArrayList;
import java.util.List;

import static org.spine3.examples.todolist.EnrichmentHelper.getEnrichment;
import static org.spine3.examples.todolist.q.projection.LabelColorView.valueOf;
import static org.spine3.examples.todolist.q.projection.ProjectionHelper.removeViewsByLabelId;
import static org.spine3.examples.todolist.q.projection.ProjectionHelper.removeViewsByTaskId;
import static org.spine3.examples.todolist.q.projection.ProjectionHelper.updateTaskViewList;

/**
 * A projection state of the created tasks marked with a certain label.
 *
 * <p> Contains the data about the task view.
 * <p> This view includes all tasks per label that are neither in a draft state nor deleted.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("OverlyCoupledClass")
public class LabelledTasksViewProjection extends Projection<LabelId, LabelledTasksView> {

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
        final DetailsEnrichment enrichment = getEnrichment(DetailsEnrichment.class, context);
        final TaskDetails taskDetails = enrichment.getTaskDetails();
        final LabelId labelId = event.getLabelId();
        final TaskId taskId = event.getTaskId();

        final TaskView taskView = viewFor(taskDetails, labelId, taskId);
        final LabelDetails labelDetails = enrichment.getLabelDetails();
        final LabelledTasksView state = addLabel(taskView, labelDetails).setLabelId(labelId)
                                                                        .build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelledTaskRestored event, EventContext context) {
        final DetailsEnrichment enrichment = getEnrichment(DetailsEnrichment.class, context);
        final TaskDetails taskDetails = enrichment.getTaskDetails();
        final LabelId labelId = event.getLabelId();
        final TaskId taskId = event.getTaskId();

        final TaskView taskView = viewFor(taskDetails, labelId, taskId);
        final LabelDetails labelDetails = enrichment.getLabelDetails();
        final LabelledTasksView state = addLabel(taskView, labelDetails).setLabelId(labelId)
                                                                        .build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final LabelId labelId = event.getLabelId();
        final boolean isEquals = getState().getLabelId()
                                           .equals(labelId);
        if (isEquals) {
            final List<TaskView> views = new ArrayList<>(getState().getLabelledTasks()
                                                                   .getItemsList());
            final TaskListView updatedView = removeViewsByLabelId(views, labelId);
            final LabelledTasksView state = toViewState(updatedView.getItemsList());
            incrementState(state);
        }
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final List<TaskView> views = new ArrayList<>(getState().getLabelledTasks()
                                                               .getItemsList());
        final TaskListView updatedView = removeViewsByTaskId(views, event.getTaskId());
        final LabelledTasksView state = toViewState(updatedView.getItemsList());
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDescriptionUpdated event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final LabelledTasksView state = toViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskPriorityUpdated event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final LabelledTasksView state = toViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDueDateUpdated event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final LabelledTasksView state = toViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskCompleted event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final LabelledTasksView state = toViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskReopened event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final LabelledTasksView state = toViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelDetailsUpdated event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final LabelDetails newDetails = event.getLabelDetailsChange()
                                             .getNewDetails();
        final LabelledTasksView state = toViewState(newDetails, updatedList);
        incrementState(state);
    }

    private static TaskView viewFor(TaskDetails taskDetails, LabelId labelId, TaskId taskId) {
        final TaskView result = TaskView.newBuilder()
                                        .setId(taskId)
                                        .setLabelId(labelId)
                                        .setDescription(taskDetails.getDescription())
                                        .setPriority(taskDetails.getPriority())
                                        .build();
        return result;
    }

    private LabelledTasksView toViewState(LabelDetails labelDetails, List<TaskView> updatedList) {
        final LabelledTasksView state = getState();
        final TaskListView listView = TaskListView.newBuilder()
                                                  .addAllItems(updatedList)
                                                  .build();
        final LabelledTasksView result = getState().newBuilderForType()
                                                   .setLabelId(state.getLabelId())
                                                   .setLabelColor(valueOf(labelDetails.getColor()))
                                                   .setLabelTitle(labelDetails.getTitle())
                                                   .setLabelledTasks(listView)
                                                   .build();
        return result;
    }

    private LabelledTasksView toViewState(List<TaskView> updatedList) {
        final LabelledTasksView state = getState();
        final TaskListView listView = TaskListView.newBuilder()
                                                  .addAllItems(updatedList)
                                                  .build();
        final LabelledTasksView result = getState().newBuilderForType()
                                                   .setLabelId(state.getLabelId())
                                                   .setLabelColor(state.getLabelColor())
                                                   .setLabelTitle(state.getLabelTitle())
                                                   .setLabelledTasks(listView)
                                                   .build();
        return result;
    }

    private LabelledTasksView.Builder addLabel(TaskView taskView, LabelDetails labelDetails) {
        final List<TaskView> views = new ArrayList<>(getState().getLabelledTasks()
                                                               .getItemsList());
        views.add(taskView);
        final TaskListView taskListView = TaskListView.newBuilder()
                                                      .addAllItems(views)
                                                      .build();
        final LabelledTasksView.Builder result = getState().newBuilderForType()
                                                           .setLabelledTasks(taskListView)
                                                           .setLabelTitle(labelDetails.getTitle());

        if (labelDetails.getColor() != LabelColor.LC_UNDEFINED) {
            final String hexColor = valueOf(labelDetails.getColor());
            result.setLabelColor(hexColor);
        }

        return result;
    }
}
