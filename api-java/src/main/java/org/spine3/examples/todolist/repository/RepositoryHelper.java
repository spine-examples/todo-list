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

package org.spine3.examples.todolist.repository;

import com.google.protobuf.Message;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.server.entity.Entity;
import org.spine3.server.entity.EventDispatchingRepository;
import org.spine3.server.entity.IdSetEventFunction;

import java.util.Collections;

/**
 * Utility class for working with repositories.
 *
 * @author Illia Shepilov
 */
/* package */ class RepositoryHelper {

    private RepositoryHelper() {
    }

    /**
     * Adds the {@link IdSetEventFunction}s to the repository.
     *
     * @param repository the {@link EventDispatchingRepository}
     * @param id         the {@link TaskListId}
     */
    /* package */ static <M extends Message, P extends Entity<TaskListId, M>> void
    addCommonIdSetFunctions(EventDispatchingRepository<TaskListId, P, M> repository, TaskListId id) {
        final IdSetEventFunction<TaskListId, TaskDeleted> taskDeletedFn =
                (message, context) -> Collections.singleton(id);
        repository.addIdSetFunction(TaskDeleted.class, taskDeletedFn);

        final IdSetEventFunction<TaskListId, TaskDescriptionUpdated> taskDescriptionUpdatedFn =
                (message, context) -> Collections.singleton(id);
        repository.addIdSetFunction(TaskDescriptionUpdated.class, taskDescriptionUpdatedFn);

        final IdSetEventFunction<TaskListId, TaskPriorityUpdated> updatedTaskPriorityFn =
                (message, context) -> Collections.singleton(id);
        repository.addIdSetFunction(TaskPriorityUpdated.class, updatedTaskPriorityFn);

        final IdSetEventFunction<TaskListId, TaskDueDateUpdated> updatedTaskDueDateFn =
                (message, context) -> Collections.singleton(id);
        repository.addIdSetFunction(TaskDueDateUpdated.class, updatedTaskDueDateFn);

        final IdSetEventFunction<TaskListId, LabelDetailsUpdated> labelDetailsUpdatedFn =
                (message, context) -> Collections.singleton(id);
        repository.addIdSetFunction(LabelDetailsUpdated.class, labelDetailsUpdatedFn);

        final IdSetEventFunction<TaskListId, LabelAssignedToTask> labelAssignedFn =
                (message, context) -> Collections.singleton(id);
        repository.addIdSetFunction(LabelAssignedToTask.class, labelAssignedFn);

        final IdSetEventFunction<TaskListId, LabelRemovedFromTask> labelRemovedFn =
                (message, context) -> Collections.singleton(id);
        repository.addIdSetFunction(LabelRemovedFromTask.class, labelRemovedFn);
    }
}
