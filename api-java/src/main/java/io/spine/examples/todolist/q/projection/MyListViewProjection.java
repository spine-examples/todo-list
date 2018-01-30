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
import io.spine.examples.todolist.Task;
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

import static io.spine.examples.todolist.EventEnrichments.getEnrichment;
import static io.spine.examples.todolist.q.projection.Projections.newTaskListView;
import static io.spine.examples.todolist.q.projection.Projections.removeViewsByTaskId;
import static io.spine.examples.todolist.q.projection.Projections.updateTaskItemList;

/**
 * A projection state of the finalized tasks.
 *
 * <p>Contains the task list view items.
 * <p>This view includes all tasks that are not in a draft state and not deleted.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("OverlyCoupledClass")
public class MyListViewProjection extends Projection<TaskListId, MyListView, MyListViewVBuilder> {

    /**
     * As long as there is just a single end-user in the app,
     * the {@link MyListViewProjection} is a singleton.
     *
     * <p>The {@code ID} value should be the same for all JVMs
     * to support work with the same projection from execution to execution.
     */
    public static final TaskListId ID = TaskListId.newBuilder()
                                                  .setValue("MyListViewProjectSingleton")
                                                  .build();

    /**
     * @see Projection#Projection(Object)
     */
    public MyListViewProjection(TaskListId id) {
        super(id);
    }

    @Subscribe
    public void on(TaskCreated event) {
        final TaskDetails taskDetails = event.getDetails();
        final TaskItem taskView = TaskItem.newBuilder()
                                          .setId(event.getId())
                                          .setDescription(taskDetails.getDescription())
                                          .setPriority(taskDetails.getPriority())
                                          .setCompleted(taskDetails.getCompleted())
                                          .build();
        addTaskItem(taskView);
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final List<TaskItem> views = new ArrayList<>(getBuilder().getMyList()
                                                                 .getItemsList());
        final TaskListView taskListView = removeViewsByTaskId(views, event.getTaskId());
        getBuilder().setMyList(taskListView);
    }

    @Subscribe
    public void on(TaskDescriptionUpdated event) {
        final List<TaskItem> views = getBuilder().getMyList()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(TaskPriorityUpdated event) {
        final List<TaskItem> views = getBuilder().getMyList()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(TaskDueDateUpdated event) {
        final List<TaskItem> views = getBuilder().getMyList()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(TaskCompleted event) {
        final List<TaskItem> views = getBuilder().getMyList()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(TaskReopened event) {
        final List<TaskItem> views = getBuilder().getMyList()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(LabelAssignedToTask event) {
        final List<TaskItem> views = getBuilder().getMyList()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final List<TaskItem> views = getBuilder().getMyList()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(LabelDetailsUpdated event) {
        final List<TaskItem> views = getBuilder().getMyList()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateMyListView(updatedList);
    }

    @Subscribe
    public void on(TaskDraftFinalized event, EventContext context) {
        final TaskId taskId = event.getTaskId();
        final TaskEnrichment enrichment = getEnrichment(TaskEnrichment.class, context);
        final Task task = enrichment.getTask();
        final TaskItem view = TaskItem.newBuilder()
                                      .setId(taskId)
                                      .setDescription(task.getDescription())
                                      .setDueDate(task.getDueDate())
                                      .setPriority(task.getPriority())
                                      .build();
        addTaskItem(view);
    }

    private void updateMyListView(List<TaskItem> updatedList) {
        final TaskListView listView = TaskListView.newBuilder()
                                                  .addAllItems(updatedList)
                                                  .build();
        getBuilder().setMyList(listView);
    }

    private void addTaskItem(TaskItem taskView) {
        final List<TaskItem> views = new ArrayList<>(getBuilder().getMyList()
                                                                 .getItemsList());
        views.add(taskView);
        final TaskListView taskListView = newTaskListView(views);
        getBuilder().setMyList(taskListView);
    }
}
