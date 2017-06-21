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

package io.spine.examples.todolist.mode.list;

import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.examples.todolist.q.projection.TaskView;

import java.util.List;

import static io.spine.examples.todolist.mode.DisplayHelper.constructUserFriendlyTaskView;

/**
 * A {@code Mode} for displaying {@link DraftTasksView}.
 *
 * @author Dmytro Grankin
 */
public class DraftTasksListMode extends ListMode<TaskView> {

    private static final String EMPTY_DRAFT_TASKS = "No draft tasks.";

    @Override
    protected List<TaskView> receiveRecentState() {
        final DraftTasksView drafts = getClient().getDraftTasksView();
        return drafts.getDraftTasks()
                     .getItemsList();
    }

    @Override
    protected String getEmptyView() {
        return EMPTY_DRAFT_TASKS;
    }

    @Override
    protected String getItemView(TaskView item) {
        return constructUserFriendlyTaskView(item);
    }
}
