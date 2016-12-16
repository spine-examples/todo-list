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
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.EnrichmentNotFoundException;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsEnrichment;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.view.LabelledTasksView;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

import java.util.List;
import java.util.stream.Collectors;

import static org.spine3.base.Events.getEnrichment;
import static org.spine3.examples.todolist.projection.ProjectionHelper.removeViewByLabelId;
import static org.spine3.examples.todolist.projection.ProjectionHelper.removeViewByTaskId;

/**
 * A projection state of the created tasks marked with a certain label.
 *
 * <p> Contains the data about the task view.
 * <p>
 * <p> This view includes all tasks per label that are neither in a draft state nor deleted.
 *
 * @author Illia Shepilov
 */
public class LabelledTaskViewProjection extends Projection<TaskLabelId, LabelledTasksView> {

    private static final String RED_COLOR_HEX = "#ff0000";
    private static final String BLUE_COLOR_HEX = "#0000ff";
    private static final String GREEN_COLOR_HEX = "#008000";
    private static final String GRAY_COLOR_HEX = "#808080";

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public LabelledTaskViewProjection(TaskLabelId id) {
        super(id);
    }

    @Subscribe
    public void on(LabelAssignedToTask event, EventContext context) {
        final TaskView taskView = TaskView.newBuilder()
                                          .setId(event.getId())
                                          .setLabelId(event.getLabelId())
                                          .build();
        final LabelledTasksView state = addLabel(taskView, context);
        incrementState(state);
    }

    @Subscribe
    public void on(DeletedTaskRestored event, EventContext context) {
        final TaskView taskView = TaskView.newBuilder()
                                          .setId(event.getId())
                                          .build();
        final LabelledTasksView state = addLabel(taskView, context);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event, EventContext context) {
        final LabelDetailsEnrichment enrichment = getLabelEnrichment(context);
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        final TaskListView taskListView = removeViewByLabelId(views, event.getLabelId());
        final LabelDetails labelDetails = enrichment.getLabelDetails();
        final String hexColor = convertFromLabelColorToHex(labelDetails.getColor());
        final LabelledTasksView state = getState().newBuilderForType()
                                                  .setLabelTitle(labelDetails.getTitle())
                                                  .setLabelColor(hexColor)
                                                  .setLabelledTasks(taskListView)
                                                  .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDeleted event, EventContext context) {
        final LabelDetailsEnrichment enrichment = getLabelEnrichment(context);
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        final TaskListView taskListView = removeViewByTaskId(views, event.getId());
        final LabelDetails labelDetails = enrichment.getLabelDetails();
        final String hexColor = convertFromLabelColorToHex(labelDetails.getColor());
        final LabelledTasksView state = getState().newBuilderForType()
                                                  .setLabelledTasks(taskListView)
                                                  .setLabelTitle(labelDetails.getTitle())
                                                  .setLabelColor(hexColor)
                                                  .build();
        incrementState(state);
    }

    private LabelDetailsEnrichment getLabelEnrichment(EventContext context) {
        final Optional<LabelDetailsEnrichment> enrichmentOptional = getEnrichment(LabelDetailsEnrichment.class, context);
        if (enrichmentOptional.isPresent()) {
            LabelDetailsEnrichment result = enrichmentOptional.get();
            return result;
        }
        throw new EnrichmentNotFoundException("LabelDetailsEnrichment not found");
    }

    private LabelledTasksView addLabel(TaskView taskView, EventContext context) {
        final LabelDetailsEnrichment enrichment = getLabelEnrichment(context);
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        views.add(taskView);
        final TaskListView taskListView = TaskListView.newBuilder()
                                                      .addAllItems(views)
                                                      .build();
        final LabelDetails labelDetails = enrichment.getLabelDetails();
        final String hexColor = convertFromLabelColorToHex(labelDetails.getColor());
        final LabelledTasksView result = getState().newBuilderForType()
                                                   .setLabelledTasks(taskListView)
                                                   .setLabelColor(hexColor)
                                                   .setLabelTitle(labelDetails.getTitle())
                                                   .build();
        return result;
    }

    private String convertFromLabelColorToHex(LabelColor color) {
        if (color == LabelColor.RED) {
            return RED_COLOR_HEX;
        }

        if (color == LabelColor.BLUE) {
            return BLUE_COLOR_HEX;
        }

        if (color == LabelColor.GREEN) {
            return GREEN_COLOR_HEX;
        }

        return GRAY_COLOR_HEX;
    }
}
