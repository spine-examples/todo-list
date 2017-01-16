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

package org.spine3.examples.todolist.q.projections;

import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.events.TaskCompleted;
import org.spine3.examples.todolist.c.events.TaskCreated;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.examples.todolist.c.events.TaskReopened;
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

import java.util.List;
import java.util.stream.Collectors;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.q.projections.ProjectionHelper.removeViewByTaskId;
import static org.spine3.examples.todolist.q.projections.ProjectionHelper.updateTaskViewList;

/**
 * A projections state of the finalized tasks.
 *
 * <p> Contains the task list view items.
 * <p> This view includes all tasks that are not in a draft state and not deleted.
 *
 * @author Illia Shepilov
 */
public class MyListViewProjection extends Projection<TaskListId, MyListView> {

    /**
     * Required the singleton {@link MyListViewProjection} according to the business rules.
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
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        views.add(taskView);
        final MyListView state = constructMyListViewState(views);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        final TaskListView taskListView = removeViewByTaskId(views, event.getId());
        final MyListView state = getState().newBuilderForType()
                                           .setMyList(taskListView)
                                           .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDescriptionUpdated event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final MyListView state = constructMyListViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskPriorityUpdated event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final MyListView state = constructMyListViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDueDateUpdated event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final MyListView state = constructMyListViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskCompleted event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final MyListView state = constructMyListViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskReopened event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final MyListView state = constructMyListViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelAssignedToTask event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final MyListView state = constructMyListViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final MyListView state = constructMyListViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelDetailsUpdated event) {
        final List<TaskView> views = getState().getMyList()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final MyListView state = constructMyListViewState(updatedList);
        incrementState(state);
    }

    private MyListView constructMyListViewState(List<TaskView> updatedList) {
        final TaskListView listView = TaskListView.newBuilder()
                                                  .addAllItems(updatedList)
                                                  .build();
        final MyListView result = getState().newBuilderForType()
                                            .setMyList(listView)
                                            .build();
        return result;
    }
}
