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

package io.spine.examples.todolist.mode;

import io.spine.examples.todolist.mode.menu.Menu;
import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.MyListView;

import java.util.List;

import static io.spine.examples.todolist.mode.DisplayHelper.constructUserFriendlyDraftTasks;
import static io.spine.examples.todolist.mode.DisplayHelper.constructUserFriendlyLabelledTasks;
import static io.spine.examples.todolist.mode.DisplayHelper.constructUserFriendlyMyList;

/**
 * @author Dmytro Grankin
 */
class ShowMenu extends Menu {

    private static final String EMPTY_DRAFT_TASKS = "No draft tasks.";
    private static final String EMPTY_LABELLED_TASKS = "No labelled tasks.";
    private static final String EMPTY_MY_LIST_TASKS = "Task list is empty.";

    ShowMenu() {
        super(Menu.newBuilder()
                  .addMenuItem("Show draft tasks.", new ShowDraftTasksMode())
                  .addMenuItem("Show labelled tasks.", new ShowLabelledTasksMode())
                  .addMenuItem("Show my tasks.", new ShowMyTasksMode()));
    }

    private static class ShowDraftTasksMode extends InteractiveMode {

        @Override
        public void start() {
            final DraftTasksView draftTasksView = getClient().getDraftTasksView();
            final boolean isEmpty = draftTasksView.getDraftTasks()
                                                  .getItemsList()
                                                  .isEmpty();
            final String message = isEmpty
                                   ? EMPTY_DRAFT_TASKS
                                   : constructUserFriendlyDraftTasks(draftTasksView);
            println(message);
        }
    }

    private static class ShowLabelledTasksMode extends InteractiveMode {

        @Override
        public void start() {
            final List<LabelledTasksView> labelledTasks = getClient().getLabelledTasksView();
            final String message = labelledTasks.isEmpty()
                                   ? EMPTY_LABELLED_TASKS
                                   : constructUserFriendlyLabelledTasks(labelledTasks);
            println(message);
        }
    }

    private static class ShowMyTasksMode extends InteractiveMode {

        @Override
        public void start() {
            final MyListView myListView = getClient().getMyListView();
            final int itemsCount = myListView.getMyList()
                                             .getItemsCount();
            final boolean isEmpty = itemsCount == 0;
            final String message = isEmpty
                                   ? EMPTY_MY_LIST_TASKS
                                   : constructUserFriendlyMyList(myListView);
            println(message);

        }
    }
}
