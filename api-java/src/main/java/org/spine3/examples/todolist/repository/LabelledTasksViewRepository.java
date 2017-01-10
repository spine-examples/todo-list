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

import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.LabelledTaskCompleted;
import org.spine3.examples.todolist.LabelledTaskDeleted;
import org.spine3.examples.todolist.LabelledTaskDescriptionUpdated;
import org.spine3.examples.todolist.LabelledTaskDueDateUpdated;
import org.spine3.examples.todolist.LabelledTaskPriorityUpdated;
import org.spine3.examples.todolist.LabelledTaskReopened;
import org.spine3.examples.todolist.LabelledTaskRestored;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.projection.LabelledTasksViewProjection;
import org.spine3.examples.todolist.view.LabelledTasksView;
import org.spine3.server.BoundedContext;
import org.spine3.server.entity.IdSetEventFunction;
import org.spine3.server.projection.ProjectionRepository;

import java.util.Collections;

/**
 * A repository for the {@link LabelledTasksViewProjection}.
 *
 * @author Illia Shepilov
 */
public class LabelledTasksViewRepository
        extends ProjectionRepository<TaskLabelId, LabelledTasksViewProjection, LabelledTasksView> {
    public LabelledTasksViewRepository(BoundedContext boundedContext) {
        super(boundedContext);
        addEventFunctions();
    }

    private void addEventFunctions() {
        final IdSetEventFunction<TaskLabelId, LabelAssignedToTask> labelAssignedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelAssignedToTask.class, labelAssignedFn);

        final IdSetEventFunction<TaskLabelId, LabelRemovedFromTask> labelRemovedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelRemovedFromTask.class, labelRemovedFn);

        final IdSetEventFunction<TaskLabelId, LabelledTaskRestored> deletedTaskRestoredFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelledTaskRestored.class, deletedTaskRestoredFn);

        final IdSetEventFunction<TaskLabelId, LabelledTaskDescriptionUpdated> taskDescriptionUpdatedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelledTaskDescriptionUpdated.class, taskDescriptionUpdatedFn);

        final IdSetEventFunction<TaskLabelId, LabelledTaskReopened> taskReopenedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelledTaskReopened.class, taskReopenedFn);

        final IdSetEventFunction<TaskLabelId, LabelledTaskPriorityUpdated> updatedTaskPriorityFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelledTaskPriorityUpdated.class, updatedTaskPriorityFn);

        final IdSetEventFunction<TaskLabelId, LabelledTaskDueDateUpdated> updatedTaskDueDateFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelledTaskDueDateUpdated.class, updatedTaskDueDateFn);

        final IdSetEventFunction<TaskLabelId, LabelledTaskCompleted> taskCompletedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelledTaskCompleted.class, taskCompletedFn);

        final IdSetEventFunction<TaskLabelId, LabelledTaskDeleted> taskDeletedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelledTaskDeleted.class, taskDeletedFn);

        final IdSetEventFunction<TaskLabelId, LabelDetailsUpdated> labelDetailsUpdatedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelDetailsUpdated.class, labelDetailsUpdatedFn);
    }
}
