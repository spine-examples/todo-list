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

package io.spine.examples.todolist.view;

import io.spine.cli.Bot;
import io.spine.cli.NoOpView;
import io.spine.cli.action.Action;
import io.spine.cli.action.Shortcut;
import io.spine.cli.action.TransitionAction.TransitionActionProducer;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskItem;
import io.spine.examples.todolist.q.projection.TaskListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static io.spine.examples.todolist.testdata.Given.newDescription;
import static io.spine.examples.todolist.view.MyTasksListView.newOpenTaskViewProducer;
import static io.spine.examples.todolist.view.MyTasksListView.taskActionProducersFor;
import static java.util.Collections.nCopies;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("MyTasksListView should")
class MyTasksListViewTest extends ViewTest {

    private static final int VIEW_INDEX = 0;

    private final Bot bot = new Bot();
    private final TaskItem taskView = TaskItem.newBuilder()
                                              .setDescription(newDescription("task desc"))
                                              .build();

    @Test
    @DisplayName("refresh task list")
    void refreshTaskList() {
        bot.screen()
           .renderView(new NoOpView()); // Needed to cause addition of back action in the view.

        MyTasksListView view = new MyTasksListView();
        view.addAction(newOpenTaskViewProducer(taskView, 0));
        view.addAction(newOpenTaskViewProducer(taskView, 1));
        Set<Action> actionsToBeRemoved = view.getActions();

        bot.addAnswer("b");
        bot.screen()
           .renderView(view);
        Set<Action> refreshedActions = newHashSet(view.getActions());
        boolean containsActionsForRemoval = refreshedActions.retainAll(actionsToBeRemoved);
        assertFalse(containsActionsForRemoval);
    }

    @Test
    @DisplayName("create action for every task view")
    void createActions() {
        int tasksCount = 5;
        TaskListView taskListView = TaskListView.newBuilder()
                                                .addAllItems(nCopies(tasksCount, taskView))
                                                .build();
        MyListView myListView = MyListView.newBuilder()
                                          .setMyList(taskListView)
                                          .build();
        Collection<TransitionActionProducer> actions = taskActionProducersFor(myListView);
        assertEquals(tasksCount, actions.size());
    }

    @Test
    @DisplayName("create open task view producer")
    void createOpenTaskItemProducer() {
        String shortcutValue = String.valueOf(VIEW_INDEX + 1);
        Shortcut expectedShortcut = new Shortcut(shortcutValue);

        TransitionActionProducer<MyTasksListView, TaskView> producer =
                newOpenTaskViewProducer(taskView, VIEW_INDEX);

        String expectedDescription = taskView.getDescription()
                                             .getValue();
        assertEquals(expectedDescription, producer.getName());
        assertEquals(expectedShortcut, producer.getShortcut());
    }
}
