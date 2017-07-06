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

package io.spine.examples.todolist;

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskView;

import java.util.List;
import java.util.Optional;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A {@code DataSource} of the application.
 *
 * @author Dmytro Grankin
 */
public class DataSource {

    private final TodoClient client;

    DataSource(TodoClient client) {
        this.client = client;
    }

    public MyListView getMyListView() {
        return client.getMyListView();
    }

    public TaskView getMyTaskView(TaskId id) {
        final List<TaskView> taskViews = getMyListView().getMyList()
                                                        .getItemsList();
        final TaskView taskView = getTaskViewById(taskViews, id);
        return taskView;
    }

    @VisibleForTesting
    static TaskView getTaskViewById(List<TaskView> taskViews, TaskId id) {
        final Optional<TaskView> optionalView = taskViews.stream()
                                                         .filter(view -> view.getId()
                                                                             .equals(id))
                                                         .findFirst();
        if (optionalView.isPresent()) {
            return optionalView.get();
        }

        throw newIllegalStateException("There is not task with ID `%s`.", id);
    }
}
