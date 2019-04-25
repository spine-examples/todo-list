/*
 * Copyright 2019, TeamDev. All rights reserved.
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
import io.spine.cli.Screen;
import io.spine.cli.action.Shortcut;
import io.spine.cli.action.TransitionAction;
import io.spine.cli.action.TransitionAction.TransitionActionProducer;
import io.spine.cli.view.ActionListView;
import io.spine.cli.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.spine.cli.action.TransitionAction.transitionProducer;
import static io.spine.examples.todolist.AppConfig.getClient;
import static java.lang.String.valueOf;

/**
 * A view of all tasks.
 *
 * <p>Mainly consists of
 * {@linkplain TransitionAction transition actions}.
 * The action gives short info about the task and leads to a {@link TaskView}.
 */
public final class MyTasksListView extends ActionListView {

    private static final String EMPTY_TASKS_LIST_MSG = "<no tasks>";

    @VisibleForTesting
    MyTasksListView() {
        super("My tasks list");
    }

    /**
     * Refreshes the tasks list and renders the view.
     */
    @Override
    public void render(Screen screen) {
        clearActions();

        List<io.spine.examples.todolist.q.projection.TaskView> views = getClient().getTaskViews();
        Collection<TransitionActionProducer> producers = taskActionProducersFor(views);

        if (producers.isEmpty()) {
            screen.println(EMPTY_TASKS_LIST_MSG);
        } else {
            producers.forEach(this::addAction);
        }
        super.render(screen);
    }

    /**
     * Creates {@link TransitionActionProducer} with {@code MyTasksListView} destination.
     *
     * @param name
     *         the name for the action
     * @param shortcut
     *         the shortcut for the action
     * @param <S>
     *         the type of the source view
     * @return the new producer
     */
    public static <S extends View> TransitionActionProducer<S, MyTasksListView>
    newOpenTaskListProducer(String name, Shortcut shortcut) {
        return transitionProducer(name, shortcut, new MyTasksListView());
    }

    @VisibleForTesting
    static Collection<TransitionActionProducer> taskActionProducersFor(
            List<io.spine.examples.todolist.q.projection.TaskView> taskViews) {
        Collection<TransitionActionProducer> producers = new ArrayList<>();
        for (int i = 0; i < taskViews.size(); i++) {
            io.spine.examples.todolist.q.projection.TaskView task = taskViews.get(i);
            producers.add(newOpenTaskViewProducer(task, i));
        }
        return producers;
    }

    @VisibleForTesting
    static TransitionActionProducer<MyTasksListView, TaskView>
    newOpenTaskViewProducer(io.spine.examples.todolist.q.projection.TaskView task, int viewIndex) {
        String name = task.getDescription()
                          .getValue();
        String shortcutValue = valueOf(viewIndex + 1);
        Shortcut shortcut = new Shortcut(shortcutValue);
        TaskView destination = new TaskView(task.getId());
        return transitionProducer(name, shortcut, destination);
    }
}
