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

import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction.TransitionActionProducer;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskListView;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static io.spine.examples.todolist.view.MyTasksListView.newDetailsProducer;
import static io.spine.examples.todolist.view.MyTasksListView.producersFor;
import static java.util.Collections.nCopies;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Dmytro Grankin
 */
@DisplayName("MyTasksListView should")
class MyTasksListViewTest {

    private static final int VIEW_INDEX = 0;

    private final MyTasksListView myTasksListView = new MyTasksListView();
    private final TaskView taskView = TaskView.newBuilder()
                                              .setDescription("task desc")
                                              .build();

    @Test
    @DisplayName("not be root view")
    void notBeRootView() {
        assertFalse(myTasksListView.isRootView());
    }

    @Test
    @DisplayName("create action for every task view")
    void createActions() {
        final int tasksCount = 5;
        final TaskListView taskListView = TaskListView.newBuilder()
                                                      .addAllItems(nCopies(tasksCount, taskView))
                                                      .build();
        final MyListView myListView = MyListView.newBuilder()
                                                .setMyList(taskListView)
                                                .build();
        final Collection<TransitionActionProducer> actions = producersFor(myListView);
        assertEquals(tasksCount, actions.size());
    }

    @Test
    @DisplayName("create details producer")
    void createOpenTaskDetailsAction() {
        final String shortcutValue = String.valueOf(VIEW_INDEX + 1);
        final Shortcut expectedShortcut = new Shortcut(shortcutValue);

        final TransitionActionProducer<MyTasksListView, MyTaskDetailsView> producer =
                newDetailsProducer(taskView, VIEW_INDEX);

        assertEquals(taskView.getDescription(), producer.getName());
        assertEquals(expectedShortcut, producer.getShortcut());
    }
}
