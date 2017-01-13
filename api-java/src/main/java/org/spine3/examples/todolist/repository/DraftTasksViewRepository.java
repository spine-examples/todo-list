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

import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.projection.DraftTasksViewProjection;
import org.spine3.examples.todolist.view.DraftTasksView;
import org.spine3.server.BoundedContext;
import org.spine3.server.entity.IdSetEventFunction;
import org.spine3.server.projection.ProjectionRepository;

import java.util.Collections;

import static org.spine3.examples.todolist.projection.DraftTasksViewProjection.ID;
import static org.spine3.examples.todolist.repository.RepositoryHelper.addCommonIdSetFunctions;

/**
 * Repository for the {@link DraftTasksViewProjection}.
 *
 * @author Illia Shepilov
 */
public class DraftTasksViewRepository
        extends ProjectionRepository<TaskListId, DraftTasksViewProjection, DraftTasksView> {
    public DraftTasksViewRepository(BoundedContext boundedContext) {
        super(boundedContext);
        addIdSetFunctions();
    }

    /**
     * Adds the {@link IdSetEventFunction}s to the repository.
     * Should to be overridden in an successor classes,
     * otherwise all successors will use {@code DraftTasksViewProjection.ID}
     * and only with specified events below.
     */
    protected void addIdSetFunctions() {
        final IdSetEventFunction<TaskListId, TaskDraftCreated> draftCreatedFn =
                (message, context) -> Collections.singleton(ID);
        addIdSetFunction(TaskDraftCreated.class, draftCreatedFn);

        final IdSetEventFunction<TaskListId, TaskDraftFinalized> draftFinalizedFn =
                (message, context) -> Collections.singleton(ID);
        addIdSetFunction(TaskDraftFinalized.class, draftFinalizedFn);

        addCommonIdSetFunctions(this, ID);
    }
}
