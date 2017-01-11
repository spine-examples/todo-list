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

import com.google.common.collect.Sets;
import com.google.protobuf.Message;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.LabelList;
import org.spine3.examples.todolist.LabelListEnrichment;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.LabelledTaskRestored;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.examples.todolist.projection.DraftTasksViewProjection;
import org.spine3.examples.todolist.projection.MyListViewProjection;
import org.spine3.server.entity.Entity;
import org.spine3.server.entity.EventDispatchingRepository;
import org.spine3.server.entity.IdSetEventFunction;

import java.util.Collections;
import java.util.Set;

import static org.spine3.examples.todolist.EnrichmentHelper.getEnrichment;

/**
 * Utility class for working with repositories.
 *
 * @author Illia Shepilov
 */
/* package */ class RepositoryHelper {

    private RepositoryHelper() {
    }

    /**
     * Adds the {@link IdSetEventFunction}s to the {@link MyListViewRepository}.
     *
     * @param repository {@code MyListViewRepository}
     */
    static void addIdSetFunctions(MyListViewRepository repository) {
        final IdSetEventFunction<TaskListId, TaskCreated> taskCreatedFn =
                (message, context) -> Collections.singleton(MyListViewProjection.ID);
        repository.addIdSetFunction(TaskCreated.class, taskCreatedFn);

        final IdSetEventFunction<TaskListId, TaskCompleted> taskCompletedFn =
                (message, context) -> Collections.singleton(MyListViewProjection.ID);
        repository.addIdSetFunction(TaskCompleted.class, taskCompletedFn);

        final IdSetEventFunction<TaskListId, TaskReopened> taskReopenedFn =
                (message, context) -> Collections.singleton(MyListViewProjection.ID);
        repository.addIdSetFunction(TaskReopened.class, taskReopenedFn);

        addCommonIdSetFunctions(repository, MyListViewProjection.ID);
    }

    /**
     * Adds the {@link IdSetEventFunction}s to the {@link DraftTasksViewRepository}.
     *
     * @param repository {@code DraftTasksViewRepository}
     */
    static void addIdSetFunctions(DraftTasksViewRepository repository) {
        final IdSetEventFunction<TaskListId, TaskDraftCreated> draftCreatedFn =
                (message, context) -> Collections.singleton(DraftTasksViewProjection.ID);
        repository.addIdSetFunction(TaskDraftCreated.class, draftCreatedFn);

        final IdSetEventFunction<TaskListId, TaskDraftFinalized> draftFinalizedFn =
                (message, context) -> Collections.singleton(DraftTasksViewProjection.ID);
        repository.addIdSetFunction(TaskDraftFinalized.class, draftFinalizedFn);

        addCommonIdSetFunctions(repository, DraftTasksViewProjection.ID);
    }

    /**
     * Adds the {@link IdSetEventFunction}s to the {@link LabelledTasksViewRepository}.
     *
     * @param repository {@code LabelledTasksViewRepository}
     */
    static void addIdSetFunctions(LabelledTasksViewRepository repository) {
        final IdSetEventFunction<TaskLabelId, LabelAssignedToTask> labelAssignedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        repository.addIdSetFunction(LabelAssignedToTask.class, labelAssignedFn);

        final IdSetEventFunction<TaskLabelId, LabelRemovedFromTask> labelRemovedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        repository.addIdSetFunction(LabelRemovedFromTask.class, labelRemovedFn);

        final IdSetEventFunction<TaskLabelId, LabelledTaskRestored> deletedTaskRestoredFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        repository.addIdSetFunction(LabelledTaskRestored.class, deletedTaskRestoredFn);

        final IdSetEventFunction<TaskLabelId, LabelDetailsUpdated> labelDetailsUpdatedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        repository.addIdSetFunction(LabelDetailsUpdated.class, labelDetailsUpdatedFn);

        final IdSetEventFunction<TaskLabelId, TaskDeleted> taskDeletedFn = (message, context) ->
                getLabelIdsSet(context);
        repository.addIdSetFunction(TaskDeleted.class, taskDeletedFn);

        final IdSetEventFunction<TaskLabelId, TaskReopened> taskReopenedFn = (message, context) ->
                getLabelIdsSet(context);
        repository.addIdSetFunction(TaskReopened.class, taskReopenedFn);

        final IdSetEventFunction<TaskLabelId, TaskCompleted> taskCompletedFn = (message, context) ->
                getLabelIdsSet(context);
        repository.addIdSetFunction(TaskCompleted.class, taskCompletedFn);

        final IdSetEventFunction<TaskLabelId, TaskPriorityUpdated> taskPriorityUpdatedFn = (message, context) ->
                getLabelIdsSet(context);
        repository.addIdSetFunction(TaskPriorityUpdated.class, taskPriorityUpdatedFn);

        final IdSetEventFunction<TaskLabelId, TaskDescriptionUpdated> taskDescriptionUpdatedFn = (message, context) ->
                getLabelIdsSet(context);
        repository.addIdSetFunction(TaskDescriptionUpdated.class, taskDescriptionUpdatedFn);

        final IdSetEventFunction<TaskLabelId, TaskDueDateUpdated> taskDueDateUpdatedFn = (message, context) ->
                getLabelIdsSet(context);
        repository.addIdSetFunction(TaskDueDateUpdated.class, taskDueDateUpdatedFn);
    }

    private static Set<TaskLabelId> getLabelIdsSet(EventContext context) {
        final LabelListEnrichment enrichment = getEnrichment(LabelListEnrichment.class, context);
        final LabelList labelList = enrichment.getLabelList();
        return Sets.newHashSet(labelList.getLabelIdList());
    }

    private static <M extends Message, P extends Entity<TaskListId, M>> void
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
