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
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction.TransitionActionProducer;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static io.spine.examples.todolist.AppConfig.getDataSource;
import static io.spine.examples.todolist.action.TransitionAction.newProducer;
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

    @VisibleForTesting
    static MyTasksListView create() {
        final MyTasksListView view = new MyTasksListView();
        final MyListView myListView = getDataSource().getMyListView();
        detailProducersFor(myListView).forEach(view::addAction);
        return view;
    }

    @VisibleForTesting
    static Collection<TransitionActionProducer> detailProducersFor(MyListView myListView) {
        final Collection<TransitionActionProducer> producers = new LinkedList<>();
        final List<TaskView> taskViews = myListView.getMyList()
                                                   .getItemsList();
        for (TaskView taskView : taskViews) {
            final int index = taskViews.indexOf(taskView);
            producers.add(newDetailsProducer(taskView, index));
        }
        return producers;
    }

    @VisibleForTesting
    static TransitionActionProducer<MyTasksListView, MyTaskDetailsView>
    newDetailsProducer(TaskView taskView, int viewIndex) {
        final String name = taskView.getDescription();
        final String shortcutValue = valueOf(viewIndex + 1);
        final Shortcut shortcut = new Shortcut(shortcutValue);
        final MyTaskDetailsView destination = new MyTaskDetailsView(taskView.getId());
        return newProducer(name, shortcut, destination);
    }

    static <S extends View> TransitionActionProducer<S, MyTasksListView>
    newOpenTaskListProducer(String name, Shortcut shortcut) {
        return newProducer(name, shortcut, create());
    }
}
