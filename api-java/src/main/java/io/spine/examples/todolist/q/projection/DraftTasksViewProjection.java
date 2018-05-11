/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import io.spine.core.Subscribe;
import io.spine.examples.todolist.TaskDetails;
import io.spine.examples.todolist.TaskListId;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.events.TaskDraftFinalized;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.server.projection.Projection;

import java.util.ArrayList;
import java.util.List;

import static io.spine.examples.todolist.q.projection.ProjectionHelper.newTaskListView;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.removeViewsByTaskId;
import static io.spine.examples.todolist.q.projection.ProjectionHelper.updateTaskItemList;

/**
 * A projection state of tasks in a "draft" state.
 *
 * <p> This view includes all non-deleted tasks, which creation is in progress.
 *
 * @author Illia Shepilov
 */
public class DraftTasksViewProjection extends Projection<TaskListId,
                                                         DraftTasksView,
                                                         DraftTasksViewVBuilder> {

    /**
     * As long as there is just a single end-user in the app,
     * the {@link DraftTasksViewProjection} is a singleton.
     *
     * <p>The {@code ID} value should be the same for all JVMs
     * to support work with the same projection from execution to execution.
     */
    public static final TaskListId ID = TaskListId.newBuilder()
                                                  .setValue("DraftTasksViewProjectionSingleton")
                                                  .build();

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public DraftTasksViewProjection(TaskListId id) {
        super(id);
    }

    @Subscribe
    public void on(TaskDraftCreated event) {
        final TaskDetails taskDetails = event.getDetails();
        final TaskItem taskView = TaskItem.newBuilder()
                                          .setId(event.getId())
                                          .setDescription(taskDetails.getDescription())
                                          .setPriority(taskDetails.getPriority())
                                          .setCompleted(taskDetails.getCompleted())
                                          .build();
        final List<TaskItem> views = new ArrayList<>(getBuilder().getDraftTasks()
                                                                 .getItemsList());
        views.add(taskView);
        final TaskListView taskListView = TaskListView.newBuilder()
                                                      .addAllItems(views)
                                                      .build();
        getBuilder().setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(TaskDraftFinalized event) {
        final List<TaskItem> views = new ArrayList<>(getBuilder().getDraftTasks()
                                                                 .getItemsList());
        final TaskListView taskListView = removeViewsByTaskId(views, event.getTaskId());
        getBuilder().setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final List<TaskItem> views = new ArrayList<>(getBuilder().getDraftTasks()
                                                                 .getItemsList());
        final TaskListView taskListView = removeViewsByTaskId(views, event.getTaskId());
        getBuilder().setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(TaskDescriptionUpdated event) {
        final List<TaskItem> views = getBuilder().getDraftTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(TaskPriorityUpdated event) {
        final List<TaskItem> views = getBuilder().getDraftTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(TaskDueDateUpdated event) {
        final List<TaskItem> views = getBuilder().getDraftTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(LabelAssignedToTask event) {
        final List<TaskItem> views = getBuilder().getDraftTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final List<TaskItem> views = getBuilder().getDraftTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(LabelDetailsUpdated event) {
        final List<TaskItem> views = getBuilder().getDraftTasks()
                                                 .getItemsList();
        final List<TaskItem> updatedList = updateTaskItemList(views, event);
        final TaskListView taskListView = newTaskListView(updatedList);
        getBuilder().setDraftTasks(taskListView);
    }
}
