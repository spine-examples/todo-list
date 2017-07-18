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

package io.spine.examples.todolist.q.projection;

import io.spine.annotation.Subscribe;
import io.spine.base.EventContext;
import io.spine.examples.todolist.TaskDefinition;
import io.spine.examples.todolist.TaskDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskListId;
import io.spine.examples.todolist.c.enrichments.TaskEnrichment;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskCreated;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDraftFinalized;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;
import io.spine.server.projection.Projection;

import java.util.ArrayList;
import java.util.List;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.EnrichmentHelper.getEnrichment;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.newTaskListView;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.removeViewsByTaskId;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.updateTaskViewList;

/**
 * A projection state of the finalized tasks.
 *
 * <p> Contains the task list view items.
 * <p> This view includes all tasks that are not in a draft state and not deleted.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("OverlyCoupledClass")
public class MyListViewProjection extends Projection<TaskListId, MyListView, MyListViewVBuilder> {

    /**
     * As long as there is just a single end-user in the app,
     * the {@link MyListViewProjection} is a singleton.
     */
    public static final TaskListId ID = TaskListId.newBuilder()
                                                  .setValue(newUuid())
                                                  .build();

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public MyListViewProjection(TaskListId id) {
        super(id);
    }

    @Subscribe
    public void on(TaskCreated event) {
        final TaskDetails taskDetails = event.getDetails();
        final TaskView taskView = TaskView.newBuilder()
                                          .setId(event.getId())
                                          .setDescription(taskDetails.getDescription())
                                          .setPriority(taskDetails.getPriority())
                                          .setCompleted(taskDetails.getCompleted())
                                          .build();
        addTaskView(taskView);
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final List<TaskView> views = new ArrayList<>(getState().getMyList()
                                                               .getItemsList());
        final TaskListView taskListView = removeViewsByTaskId(views, event.getTaskId());
        getBuilder().setMyList(taskListView);
    }

    @Subscribe
    public void on(TaskDescriptionUpdated event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(TaskPriorityUpdated event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(TaskDueDateUpdated event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(TaskCompleted event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(TaskReopened event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(LabelAssignedToTask event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(LabelDetailsUpdated event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(TaskDraftFinalized event, EventContext context) {
        final TaskId taskId = event.getTaskId();
        final TaskEnrichment enrichment = getEnrichment(TaskEnrichment.class, context);
        final TaskDefinition taskDefinition = enrichment.getTaskDefinition();
        final TaskView view = TaskView.newBuilder()
                                      .setId(taskId)
                                      .setDescription(taskDefinition.getDescription())
                                      .setDueDate(taskDefinition.getDueDate())
                                      .setPriority(taskDefinition.getPriority())
                                      .build();
        addTaskView(view);
    }

    private void updateMyListView(List<TaskView> updatedList) {
        final TaskListView listView = TaskListView.newBuilder()
                                                  .addAllItems(updatedList)
                                                  .build();
        getBuilder().setMyList(listView);
    }

    private void addTaskView(TaskView taskView) {
        final List<TaskView> views = new ArrayList<>(getState().getMyList()
                                                               .getItemsList());
        views.add(taskView);
        final TaskListView taskListView = newTaskListView(views);
        getBuilder().setMyList(taskListView);
    }
}
