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
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.MyListViewProjection;
import io.spine.server.projection.ProjectionRepository;

import static io.spine.examples.todolist.q.projection.MyListViewProjection.ID;
import static java.util.Collections.singleton;

/**
 * Repository for the {@link MyListViewProjection}.
 */
public class MyListViewRepository
        extends ProjectionRepository<TaskListId, MyListViewProjection, MyListView> {

    @SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
    // Necessary so the implementors can specify their own routing schema.
    public MyListViewRepository() {
        super();
        setUpEventRoute();
    }

    /**
     * Adds the {@link io.spine.server.route.EventRoute EventRoute}s to the repository.
     * Should be overridden in the descendant classes,
     * otherwise all successors will use {@code MyListViewProjection.ID}
     * and only with the events specified below.
     */
    protected void setUpEventRoute() {
        eventRouting().replaceDefault((message, context) -> singleton(ID));
    }
}
