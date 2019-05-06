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
import io.spine.base.EventMessage;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskCreated;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;
import io.spine.examples.todolist.q.projection.TaskView;
import io.spine.examples.todolist.q.projection.TaskViewProjection;
import io.spine.server.projection.ProjectionRepository;

import java.util.function.Function;

/**
 * Repository for the {@link TaskViewProjection}.
 */
public class TaskViewRepository
        extends ProjectionRepository<TaskId, TaskViewProjection, TaskView> {

    @Override
    public void onRegistered() {
        super.onRegistered();
        reroute();
    }

    private void reroute() {
        toSingleProjection(TaskCreated.class, TaskCreated::getId);
        toSingleProjection(TaskDeleted.class, TaskDeleted::getTaskId);
        toSingleProjection(TaskCompleted.class, TaskCompleted::getTaskId);
        toSingleProjection(TaskDraftCreated.class, TaskDraftCreated::getId);
        toSingleProjection(TaskDescriptionUpdated.class, TaskDescriptionUpdated::getTaskId);
        toSingleProjection(TaskDueDateUpdated.class, TaskDueDateUpdated::getTaskId);
        toSingleProjection(TaskReopened.class, TaskReopened::getTaskId);
        toSingleProjection(TaskPriorityUpdated.class, TaskPriorityUpdated::getTaskId);
    }

    /**
     * Reroutes events of the specified class to the projection with ID specified by the second
     * parameter.
     *
     * @param eventClass
     *         class of event to reroute
     * @param fn
     *         function to obtain a task ID from the specified event type
     * @param <E>
     *         type of event to reroute
     */
    private <E extends EventMessage> void toSingleProjection(Class<E> eventClass,
                                                             Function<E, TaskId> fn) {
        eventRouting().route(eventClass, (message, context) -> ImmutableSet.of(fn.apply(message)));
    }

}
