/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.repository;

import io.spine.examples.todolist.TaskListId;
import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.examples.todolist.q.projection.DraftTasksViewProjection;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.route.EventRoute;

import static io.spine.examples.todolist.q.projection.DraftTasksViewProjection.ID;
import static java.util.Collections.singleton;

/**
 * Repository for the {@link DraftTasksViewProjection}.
 */
public class DraftTasksViewRepository
        extends ProjectionRepository<TaskListId, DraftTasksViewProjection, DraftTasksView> {

    public DraftTasksViewRepository() {
        super();
        setUpEventRoute();
    }

    /**
     * Adds the {@link EventRoute}s to the repository.
     * Should to be overridden in an successor classes,
     * otherwise all successors will use {@code DraftTasksViewProjection.ID}
     * and only with specified events below.
     */
    protected void setUpEventRoute() {
        eventRouting().replaceDefault(((message, context) -> singleton(ID)));
    }
}
