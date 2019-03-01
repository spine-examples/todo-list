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
 * <p>This view includes all non-deleted tasks whose creation is in progress.
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
     * @param id
     *         the ID for the new instance
     * @throws IllegalArgumentException
     *         if the ID is not of one of the supported types
     */
    public DraftTasksViewProjection(TaskListId id) {
        super(id);
    }

    @Subscribe
    public void on(TaskDraftCreated event) {
        TaskDetails taskDetails = event.getDetails();
        TaskItem taskView = TaskItem
                .newBuilder()
                .setId(event.getId())
                .setDescription(taskDetails.getDescription())
                .setPriority(taskDetails.getPriority())
                .setCompleted(taskDetails.getCompleted())
                .build();
        List<TaskItem> views = new ArrayList<>(builder().getDraftTasks()
                                                        .getItemsList());
        views.add(taskView);
        TaskListView taskListView = TaskListView
                .newBuilder()
                .addAllItems(views)
                .build();
        setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(TaskDraftFinalized event) {
        List<TaskItem> views = new ArrayList<>(builder().getDraftTasks()
                                                        .getItemsList());
        TaskListView taskListView = removeViewsByTaskId(views, event.getTaskId());
        setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(TaskDeleted event) {
        List<TaskItem> views = new ArrayList<>(builder().getDraftTasks()
                                                        .getItemsList());
        TaskListView taskListView = removeViewsByTaskId(views, event.getTaskId());
        setDraftTasks(taskListView);
    }

    @Subscribe
    public void on(TaskDescriptionUpdated event) {
        List<TaskItem> views = builder().getDraftTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateDraftTasks(updatedList);
    }

    @Subscribe
    public void on(TaskPriorityUpdated event) {
        List<TaskItem> views = builder().getDraftTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateDraftTasks(updatedList);
    }

    @Subscribe
    public void on(TaskDueDateUpdated event) {
        List<TaskItem> views = builder().getDraftTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateDraftTasks(updatedList);
    }

    @Subscribe
    public void on(LabelAssignedToTask event) {
        List<TaskItem> views = builder().getDraftTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateDraftTasks(updatedList);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        List<TaskItem> views = builder().getDraftTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateDraftTasks(updatedList);
    }

    @Subscribe
    public void on(LabelDetailsUpdated event) {
        List<TaskItem> views = builder().getDraftTasks()
                                        .getItemsList();
        List<TaskItem> updatedList = updateTaskItemList(views, event);
        updateDraftTasks(updatedList);
    }

    private void updateDraftTasks(List<TaskItem> updatedList) {
        TaskListView taskListView = newTaskListView(updatedList);
        setDraftTasks(taskListView);
    }

    private void setDraftTasks(TaskListView taskListView) {
        builder().setId(id())
                 .setDraftTasks(taskListView);
    }
}
