/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

import static java.util.Collections.singleton;
import static io.spine.examples.todolist.q.projection.MyListViewProjection.ID;

/**
 * Repository for the {@link MyListViewProjection}.
 *
 * @author Illia Shepilov
 */
public class MyListViewRepository
        extends ProjectionRepository<TaskListId, MyListViewProjection, MyListView> {

    @Override
    public void onRegistered() {
        super.onRegistered();
        setUpEventRoute();
    }

    /**
     * Adds the {@link io.spine.server.route.EventRoute EventRoute}s to the repository.
     *
     * <p>Override this method in successor classes, otherwise all successors will use
     * {@code MyListViewProjection.ID}.
     */
    protected void setUpEventRoute() {
        getEventRouting().replaceDefault(((message, context) -> singleton(ID)));
    }
}
