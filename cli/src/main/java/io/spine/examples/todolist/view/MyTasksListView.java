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

package io.spine.examples.todolist.view;

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.DynamicTransitionAction;
import io.spine.examples.todolist.action.TransitionAction;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author Dmytro Grankin
 */
class MyTasksListView extends ActionListView {

    private MyTasksListView(MyListView myListView) {
        super(false, toActions(myListView));
    }

    private static Collection<Action> toActions(MyListView myListView) {
        final Collection<Action> actions = new LinkedList<>();
        final List<TaskView> taskViews = myListView.getMyList()
                                                   .getItemsList();
        for (int i = 0; i < taskViews.size(); i++) {
            final TaskView taskView = taskViews.get(i);
            final String name = taskView.getDescription();
            final String shortcut = String.valueOf(i + 1);
            final TransitionAction action = new OpenTaskDetails(name, shortcut, taskView.getId());
            actions.add(action);
        }
        return actions;
    }

    static OpenMyTasksList newTransitionAction(String name, String shortcut) {
        return new OpenMyTasksList(name, shortcut);
    }

    private static class OpenMyTasksList extends DynamicTransitionAction {

        private OpenMyTasksList(String name, String shortcut) {
            super(name, shortcut);
        }

        @Override
        protected View createDestination() {
            final MyListView view = obtainMyTasks();
            return new MyTasksListView(view);
        }

        private MyListView obtainMyTasks() {
            return getClient().getMyListView();
        }
    }

    private static class OpenTaskDetails extends DynamicTransitionAction<MyTaskDetailsView> {

        private final TaskId taskId;

        private OpenTaskDetails(String name, String shortcut, TaskId taskId) {
            super(name, shortcut);
            this.taskId = taskId;
        }

        @Override
        protected MyTaskDetailsView createDestination() {
            final List<TaskView> taskViews = getClient().getMyListView()
                                                        .getMyList()
                                                        .getItemsList();
            final Optional<TaskView> taskView = taskViews.stream()
                                                         .filter(view -> view.getId()
                                                                             .equals(taskId))
                                                         .findFirst();
            return new MyTaskDetailsView(taskView.get());
        }
    }
}
