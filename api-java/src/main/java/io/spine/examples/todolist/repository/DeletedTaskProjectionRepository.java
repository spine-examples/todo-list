/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableSet;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.events.DeletedTaskRestored;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.q.projection.DeletedTask;
import io.spine.examples.todolist.q.projection.DeletedTaskProjection;
import io.spine.server.projection.ProjectionRepository;

/**
 * Repository for the {@link DeletedTaskProjection}.
 */
public class DeletedTaskProjectionRepository extends ProjectionRepository<TaskId,
                                                                          DeletedTaskProjection,
                                                                          DeletedTask> {

    @Override
    public void onRegistered() {
        super.onRegistered();
        rerouteEvents();
    }

    private void rerouteEvents() {
        eventRouting().route(TaskDeleted.class,
                             (message, context) -> ImmutableSet.of(message.getTaskId()));
        eventRouting().route(DeletedTaskRestored.class,
                             (message, context) -> ImmutableSet.of(message.getTaskId()));
    }
}
