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

package org.spine3.examples.todolist.q.projections;

import org.spine3.base.EventContext;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelsList;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.enrichments.DetailsEnrichment;
import org.spine3.examples.todolist.c.enrichments.LabelsListEnrichment;
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
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

import java.util.List;
import java.util.stream.Collectors;

import static org.spine3.examples.todolist.EnrichmentHelper.getEnrichment;
import static org.spine3.examples.todolist.q.projections.ProjectionHelper.removeViewByTaskId;
import static org.spine3.examples.todolist.q.projections.ProjectionHelper.updateTaskViewList;

/**
 * A projections state of the created tasks marked with a certain label.
 *
 * <p> Contains the data about the task view.
 * <p> This view includes all tasks per label that are neither in a draft state nor deleted.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("OverlyCoupledClass")
public class LabelledTasksViewProjection extends Projection<TaskLabelId, LabelledTasksView> {

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public LabelledTasksViewProjection(TaskLabelId id) {
        super(id);
    }

    @Subscribe
    public void on(LabelAssignedToTask event, EventContext context) {
        final DetailsEnrichment enrichment = getEnrichment(DetailsEnrichment.class, context);
        final LabelsListEnrichment labelsEnrichment = getEnrichment(LabelsListEnrichment.class, context);
        final LabelsList labelsList = labelsEnrichment.getLabelsList();
        final TaskDetails taskDetails = enrichment.getTaskDetails();
        final TaskLabelId labelId = event.getLabelId();
        final TaskId taskId = event.getTaskId();

        final TaskView taskView = constructTaskView(taskDetails, labelId, taskId);
        final LabelDetails labelDetails = constructLabelDetails(labelId, labelsList);
        final LabelledTasksView state = addLabel(taskView, labelDetails).setLabelId(labelId)
                                                                        .build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelledTaskRestored event, EventContext context) {
        final DetailsEnrichment enrichment = getEnrichment(DetailsEnrichment.class, context);
        final LabelsListEnrichment labelsEnrichment = getEnrichment(LabelsListEnrichment.class, context);
        final LabelsList labelsList = labelsEnrichment.getLabelsList();
        final TaskDetails taskDetails = enrichment.getTaskDetails();
        final TaskLabelId labelId = event.getLabelId();
        final TaskId taskId = event.getTaskId();

        final TaskView taskView = constructTaskView(taskDetails, labelId, taskId);
        final LabelDetails labelDetails = constructLabelDetails(labelId, labelsList);
        final LabelledTasksView state = addLabel(taskView, labelDetails).build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final TaskLabelId labelId = event.getLabelId();
        final boolean isEquals = getState().getLabelId()
                                           .equals(labelId);
        if (isEquals) {
            final LabelledTasksView state = LabelledTasksView.newBuilder()
                                                             .setLabelId(getState().getLabelId())
                                                             .build();
            incrementState(state);
        }
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        final TaskListView taskListView = removeViewByTaskId(views, event.getTaskId());
        final LabelledTasksView labelledTasksView = getState();
        final LabelledTasksView state = getState().newBuilderForType()
                                                  .setLabelledTasks(taskListView)
                                                  .setLabelTitle(labelledTasksView.getLabelTitle())
                                                  .setLabelColor(labelledTasksView.getLabelColor())
                                                  .build();
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

    private static LabelDetails constructLabelDetails(TaskLabelId labelId, LabelsList labelsList) {
        final TaskLabel taskLabel = labelsList.getLabelsList()
                                              .stream()
                                              .filter(label -> label.getId()
                                                                    .equals(labelId))
                                              .findFirst()
                                              .orElse(TaskLabel.getDefaultInstance());
        return LabelDetails.newBuilder()
                           .setTitle(taskLabel.getTitle())
                           .setColor(taskLabel.getColor())
                           .build();
    }

    private static TaskView constructTaskView(TaskDetails taskDetails, TaskLabelId labelId, TaskId taskId) {
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
                                                   .setLabelColor(LabelColorView.valueOf(labelDetails.getColor()))
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
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        views.add(taskView);
        final TaskListView taskListView = TaskListView.newBuilder()
                                                      .addAllItems(views)
                                                      .build();
        final LabelledTasksView.Builder result = getState().newBuilderForType()
                                                           .setLabelledTasks(taskListView)
                                                           .setLabelTitle(labelDetails.getTitle());

        if (labelDetails.getColor() != LabelColor.LC_UNDEFINED) {
            final String hexColor = LabelColorView.valueOf(labelDetails.getColor());
            result.setLabelColor(hexColor);
        }

        return result;
    }
}
