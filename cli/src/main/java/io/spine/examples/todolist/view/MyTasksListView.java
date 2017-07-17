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
import io.spine.examples.todolist.Screen;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction.TransitionActionProducer;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static io.spine.examples.todolist.AppConfig.getClient;
import static io.spine.examples.todolist.action.TransitionAction.newProducer;
import static java.lang.String.valueOf;

/**
 * A view of {@link MyListView}.
 *
 * <p>{@link MyListView} mainly consists of
 * {@linkplain io.spine.examples.todolist.action.TransitionAction transition actions}.
 * The action gives short info about the task and leads to a {@link MyTaskView}.
 *
 * @author Dmytro Grankin
 */
class MyTasksListView extends ActionListView {

    private static final String EMPTY_TASKS_LIST_MSG = "<no tasks>";

    @VisibleForTesting
    MyTasksListView() {
        super("My tasks list");
    }

    /**
     * Refreshes the tasks list and renders the view.
     *
     * @param screen {@inheritDoc}
     */
    @Override
    public void render(Screen screen) {
        clearActions();

        final MyListView myListView = getClient().getMyListView();
        final Collection<TransitionActionProducer> producers = taskActionProducersFor(myListView);

        if (producers.isEmpty()) {
            screen.println(EMPTY_TASKS_LIST_MSG);
        } else {
            producers.forEach(this::addAction);
        }
        super.render(screen);
    }

    @VisibleForTesting
    static Collection<TransitionActionProducer> taskActionProducersFor(MyListView myListView) {
        final Collection<TransitionActionProducer> producers = new LinkedList<>();
        final List<TaskView> taskViews = myListView.getMyList()
                                                   .getItemsList();
        for (TaskView taskView : taskViews) {
            final int index = taskViews.indexOf(taskView);
            producers.add(newOpenTaskViewProducer(taskView, index));
        }
        return producers;
    }

    @VisibleForTesting
    static TransitionActionProducer<MyTasksListView, MyTaskView>
    newOpenTaskViewProducer(TaskView taskView, int viewIndex) {
        final String name = taskView.getDescription();
        final String shortcutValue = valueOf(viewIndex + 1);
        final Shortcut shortcut = new Shortcut(shortcutValue);
        final MyTaskView destination = new MyTaskView(taskView.getId());
        return newProducer(name, shortcut, destination);
    }

    static <S extends View> TransitionActionProducer<S, MyTasksListView>
    newOpenTaskListProducer(String name, Shortcut shortcut) {
        return newProducer(name, shortcut, new MyTasksListView());
    }
}
