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

package io.spine.examples.todolist.cli.view;

import io.spine.cli.action.Shortcut;
import io.spine.cli.view.ActionListView;

import static io.spine.cli.action.TransitionAction.transitionProducer;
import static io.spine.examples.todolist.cli.view.TaskListView.newOpenTaskListProducer;

/**
 * Menu of actions that are related to
 * {@link io.spine.examples.todolist.server.view.MyListView MyListView}.
 */
public final class MyTasksMenu extends ActionListView {

    private MyTasksMenu() {
        super("My tasks menu");
    }

    /**
     * Creates a new {@code MyTasksMenu} instance.
     *
     * @return the new instance
     */
    public static MyTasksMenu create() {
        MyTasksMenu view = new MyTasksMenu();
        view.addAction(transitionProducer("Create task", new Shortcut("c"), NewTaskView.create()));
        view.addAction(newOpenTaskListProducer("List tasks", new Shortcut("l")));
        return view;
    }
}