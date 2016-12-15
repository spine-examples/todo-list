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

import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.view.MyListView;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

import java.util.List;
import java.util.stream.Collectors;

import static org.spine3.examples.todolist.projection.ProjectionHelper.removeViewByTaskId;

/**
 * A projection state of created tasks.
 *
 * <p> Contains the data about the task list view.
 * <p>
 * <p> This view includes all tasks that are not in a draft state and not deleted.
 *
 * @author Illia Shepilov
 */
public class MyListViewProjection extends Projection<TaskListId, MyListView> {

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
        final TaskListView taskListView = TaskListView.newBuilder()
                                                      .addAllItems(views)
                                                      .build();
        final MyListView state = getState().newBuilderForType()
                                           .setMyList(taskListView)
                                           .build();
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
}
