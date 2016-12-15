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

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.CorruptedProtocolBufferException;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.LabelAssignedToTask;
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
        final LabelledTasksView state = getState().newBuilderForType()
                                                  .setLabelTitle(enrichment.getLabelTitle())
                                                  .setLabelColor(enrichment.getLabelColor())
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
        final LabelledTasksView state = getState().newBuilderForType()
                                                  .setLabelledTasks(taskListView)
                                                  .setLabelTitle(enrichment.getLabelTitle())
                                                  .build();
        incrementState(state);
    }

    private LabelDetailsEnrichment getLabelEnrichment(EventContext context) {
        try {
            final Any any = context.getEnrichments()
                                   .getMapMap()
                                   .get("labelDetailsEnrichment");
            final LabelDetailsEnrichment result = LabelDetailsEnrichment.parseFrom(any.getTypeUrlBytes());
            return result;
        } catch (InvalidProtocolBufferException e) {
            throw new CorruptedProtocolBufferException("Unsuccessful protobuf parsing", e);
        }
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
        final LabelledTasksView result = getState().newBuilderForType()
                                                   .setLabelledTasks(taskListView)
                                                   .setLabelColor(enrichment.getLabelColor())
                                                   .setLabelTitle(enrichment.getLabelTitle())
                                                   .build();
        return result;
    }
}
