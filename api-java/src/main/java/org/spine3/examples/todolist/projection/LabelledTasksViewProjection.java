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

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import org.spine3.base.EventContext;
import org.spine3.base.Events;
import org.spine3.examples.todolist.EnrichmentNotFoundException;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsEnrichment;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.LabelledTaskCompleted;
import org.spine3.examples.todolist.LabelledTaskDeleted;
import org.spine3.examples.todolist.LabelledTaskDescriptionUpdated;
import org.spine3.examples.todolist.LabelledTaskDueDateUpdated;
import org.spine3.examples.todolist.LabelledTaskPriorityUpdated;
import org.spine3.examples.todolist.LabelledTaskReopened;
import org.spine3.examples.todolist.LabelledTaskRestored;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.view.LabelledTasksView;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

import java.util.List;
import java.util.stream.Collectors;

import static org.spine3.examples.todolist.projection.ProjectionHelper.constructTaskViewList;
import static org.spine3.examples.todolist.projection.ProjectionHelper.removeViewByTaskId;

/**
 * A projection state of the created tasks marked with a certain label.
 *
 * <p> Contains the data about the task view.
 * <p> This view includes all tasks per label that are neither in a draft state nor deleted.
 *
 * @author Illia Shepilov
 */
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
        final LabelDetailsEnrichment enrichment = getEnrichment(LabelDetailsEnrichment.class, context);
        final LabelDetails labelDetails = enrichment.getLabelDetails();
        final TaskView taskView = TaskView.newBuilder()
                                          .setId(event.getId())
                                          .setLabelId(event.getLabelId())
                                          .build();
        final LabelledTasksView state = addLabel(taskView, labelDetails).setLabelId(event.getLabelId())
                                                                        .build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelledTaskRestored event, EventContext context) {
        final LabelDetailsEnrichment enrichment = getEnrichment(LabelDetailsEnrichment.class, context);
        final LabelDetails labelDetails = enrichment.getLabelDetails();
        final TaskView taskView = TaskView.newBuilder()
                                          .setId(event.getTaskId())
                                          .build();
        final LabelledTasksView state = addLabel(taskView, labelDetails).build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final TaskLabelId labelId = event.getLabelId();

        if (labelId.equals(getState().getLabelId())) {
            final LabelledTasksView state = LabelledTasksView.getDefaultInstance();
            incrementState(state);
        }
    }

    @Subscribe
    public void on(LabelledTaskDeleted event) {
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
    public void on(LabelledTaskDescriptionUpdated event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = constructTaskViewList(views, event);
        final LabelledTasksView state = constructLabelledViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelledTaskPriorityUpdated event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = constructTaskViewList(views, event);
        final LabelledTasksView state = constructLabelledViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelledTaskDueDateUpdated event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = constructTaskViewList(views, event);
        final LabelledTasksView state = constructLabelledViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelledTaskCompleted event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = constructTaskViewList(views, event);
        final LabelledTasksView state = constructLabelledViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelledTaskReopened event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = constructTaskViewList(views, event);
        final LabelledTasksView state = constructLabelledViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelDetailsUpdated event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = constructTaskViewList(views, event);
        final LabelledTasksView state = constructLabelledViewState(updatedList);
        incrementState(state);
    }

    private LabelledTasksView constructLabelledViewState(List<TaskView> updatedList) {
        final LabelledTasksView state = getState();
        final TaskListView listView = TaskListView.newBuilder()
                                                  .addAllItems(updatedList)
                                                  .build();
        final LabelledTasksView result = getState().newBuilderForType()
                                                   .setLabelColor(state.getLabelColor())
                                                   .setLabelTitle(state.getLabelTitle())
                                                   .setLabelledTasks(listView)
                                                   .build();
        return result;
    }

    @SuppressWarnings("Guava")
    //As long as Spine API is based on Java 7, {@link Events#getEnrichment} uses Guava {@link Optional}.
    private static <T extends Message, E extends Class<T>> T getEnrichment(E enrichmentClass, EventContext context) {
        final Optional<T> enrichmentOptional = Events.getEnrichment(enrichmentClass, context);
        if (enrichmentOptional.isPresent()) {
            T result = enrichmentOptional.get();
            return result;
        }
        throw new EnrichmentNotFoundException(enrichmentClass + " not found");
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
