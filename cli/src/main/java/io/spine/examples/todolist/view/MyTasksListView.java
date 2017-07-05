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

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.DynamicTransitionAction;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.valueOf;

/**
 * A view of {@link MyListView}.
 *
 * <p>Each item of {@link MyListView} is represented by an {@link Action}.
 * The action gives short info about the task and leads to a {@link DetailsView} for the task.
 *
 * @author Dmytro Grankin
 */
class MyTasksListView extends ActionListView {

    private MyTasksListView() {
        super(false);
    }

    static MyTasksListView create(MyListView myListView) {
        final MyTasksListView view = new MyTasksListView();
        detailProducersOf(myListView).forEach(view::addAction);
        return view;
    }

    @VisibleForTesting
    static Collection<OpenTaskDetails.Producer> detailProducersOf(MyListView myListView) {
        final Collection<OpenTaskDetails.Producer> producers = new LinkedList<>();
        final List<TaskView> taskViews = myListView.getMyList()
                                                   .getItemsList();
        for (TaskView taskView : taskViews) {
            final int index = taskViews.indexOf(taskView);
            producers.add(newDetailsProducer(taskView, index));
        }
        return producers;
    }

    @VisibleForTesting
    static OpenTaskDetails.Producer newDetailsProducer(TaskView taskView, int viewIndex) {
        final String name = taskView.getDescription();
        final String shortcutValue = valueOf(viewIndex + 1);
        final Shortcut shortcut = new Shortcut(shortcutValue);
        return new OpenTaskDetails.Producer(name, shortcut, taskView.getId());
    }

    static <S extends View> OpenMyTasksList.Producer<S> newOpenTaskListProducer(String name,
                                                                                Shortcut shortcut) {
        return new OpenMyTasksList.Producer<>(name, shortcut);
    }

    /**
     * An {@code Action} for opening of {@code MyTasksListView}.
     */
    static class OpenMyTasksList<S extends View> extends DynamicTransitionAction<S, MyTasksListView> {

        private OpenMyTasksList(String name, Shortcut shortcut, S source) {
            super(name, shortcut, source);
        }

        @Override
        protected MyTasksListView createDestination() {
            final MyListView view = obtainMyTasks();
            return create(view);
        }

        private MyListView obtainMyTasks() {
            return getClient().getMyListView();
        }

        static class Producer<S extends View> extends DynamicTransitionActionProducer<S, MyTasksListView> {

            private Producer(String name, Shortcut shortcut) {
                super(name, shortcut);
            }

            @Override
            public DynamicTransitionAction<S, MyTasksListView> create(S source) {
                return new OpenMyTasksList<>(getName(), getShortcut(), source);
            }
        }
    }

    /**
     * An {@code Action} for opening of {@link MyTaskDetailsView}.
     */
    static class OpenTaskDetails extends DynamicTransitionAction<MyTasksListView, MyTaskDetailsView> {

        private final TaskId taskId;

        private OpenTaskDetails(String name, Shortcut shortcut, MyTasksListView source,
                                TaskId taskId) {
            super(name, shortcut, source);
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

        static class Producer extends DynamicTransitionActionProducer<MyTasksListView, MyTaskDetailsView> {

            private final TaskId taskId;

            private Producer(String name, Shortcut shortcut, TaskId taskId) {
                super(name, shortcut);
                checkNotNull(taskId);
                this.taskId = taskId;
            }

            @Override
            public DynamicTransitionAction<MyTasksListView, MyTaskDetailsView> create(
                    MyTasksListView source) {
                return new OpenTaskDetails(getName(), getShortcut(), source, taskId);
            }
        }
    }
}
