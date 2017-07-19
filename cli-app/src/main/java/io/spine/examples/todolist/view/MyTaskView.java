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

import io.spine.cli.view.EntityView;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.q.projection.TaskItem;

import java.util.List;
import java.util.Optional;

import static io.spine.examples.todolist.AppConfig.getClient;
import static io.spine.examples.todolist.view.DateFormatter.format;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.System.lineSeparator;

/**
 * An {@link EntityView} of a {@link TaskItem} from the
 * {@link io.spine.examples.todolist.q.projection.MyListView MyListView}.
 *
 * @author Dmytro Grankin
 */
class MyTaskView extends EntityView<TaskId, TaskItem> {

    static final String DUE_DATE_VALUE = "Due date: ";
    static final String DESCRIPTION_VALUE = "Description: ";
    static final String PRIORITY_VALUE = "Priority: ";

    MyTaskView(TaskId id) {
        super(id, "My task details");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TaskItem load(TaskId id) {
        final List<TaskItem> tasks = getClient().getMyListView()
                                                .getMyList()
                                                .getItemsList();
        final Optional<TaskItem> optionalTask = tasks.stream()
                                                     .filter(task -> task.getId()
                                                                         .equals(id))
                                                     .findFirst();
        if (optionalTask.isPresent()) {
            return optionalTask.get();
        }

        throw newIllegalStateException("There is no task with ID `%s`.", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String renderState(TaskItem state) {
        final String date = format(state.getDueDate());
        return new StringBuilder().append(DESCRIPTION_VALUE)
                                  .append(state.getDescription())
                                  .append(lineSeparator())
                                  .append(PRIORITY_VALUE)
                                  .append(state.getPriority())
                                  .append(lineSeparator())
                                  .append(DUE_DATE_VALUE)
                                  .append(date)
                                  .toString();
    }
}
