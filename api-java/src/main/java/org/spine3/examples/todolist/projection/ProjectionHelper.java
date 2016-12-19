/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.projection;

import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;

import java.util.List;

/**
 * Class provides methods to manipulate and handle views.
 *
 * @author Illia Shepilov
 */
/* package */ class ProjectionHelper {

    /**
     * Prevent instantiation.
     */
    private ProjectionHelper() {
        throw new UnsupportedOperationException("Cannot be instantiated.");
    }

    /**
     * Removes {@link TaskView} from list of task view by specified task's id.
     *
     * @param views list of the {@link TaskView}
     * @param id    task's id
     * @return {@link TaskListView} without deleted task view
     */
    /* package */
    static TaskListView removeViewByTaskId(List<TaskView> views, TaskId id) {
        final TaskView taskView = views.stream()
                                       .filter(t -> t.getId()
                                                     .equals(id))
                                       .findFirst()
                                       .orElse(null);
        views.remove(taskView);
        final TaskListView result = TaskListView.newBuilder()
                                               .addAllItems(views)
                                               .build();
        return result;
    }

    /**
     * Removes {@link TaskView} from list of task view by specified task's label id.
     *
     * @param views list of the {@link TaskView}
     * @param id    task's label id
     * @return {@link TaskListView} without deleted task view
     */
    /*package*/
    static TaskListView removeViewByLabelId(List<TaskView> views, TaskLabelId id) {
        final TaskView taskView = views.stream()
                                       .filter(t -> t.getLabelId()
                                                     .equals(id))
                                       .findFirst()
                                       .orElse(null);
        if (taskView != null) {
            views.remove(taskView);
        }

        final TaskListView result = TaskListView.newBuilder()
                                                .addAllItems(views)
                                                .build();
        return result;
    }
}
