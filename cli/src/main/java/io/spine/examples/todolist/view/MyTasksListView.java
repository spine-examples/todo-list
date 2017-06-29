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

import io.spine.examples.todolist.AppConfig;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.DynamicTransitionAction;
import io.spine.examples.todolist.action.StaticTransitionAction;
import io.spine.examples.todolist.action.TransitionAction;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
            final View destination = new MyTaskDetailsView(taskView);
            final TransitionAction action = new StaticTransitionAction<>(name, shortcut,
                                                                         destination);
            actions.add(action);
        }
        return actions;
    }

    static OpenMyTasksListView newTransitionAction(String name, String shortcut) {
        return new OpenMyTasksListView(name, shortcut);
    }

    private static class OpenMyTasksListView extends DynamicTransitionAction {

        private OpenMyTasksListView(String name, String shortcut) {
            super(name, shortcut);
        }

        @Override
        protected View createDestination() {
            final MyListView view = obtainMyTasks();
            return new MyTasksListView(view);
        }

        private static MyListView obtainMyTasks() {
            return AppConfig.getClient()
                            .getMyListView();
        }
    }
}
