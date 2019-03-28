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

package io.spine.examples.todolist.q.projection;

import io.spine.core.EventContext;
import io.spine.core.Subscribe;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.enrichments.TaskEnrichment;
import io.spine.examples.todolist.c.events.DeletedTaskRestored;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.server.projection.Projection;
import io.spine.util.Exceptions.newIllegalStateException

/**
 * A task that has been deleted.
 *
 * <p>Tasks can be deleted with a {@code DeleteTask} command.
 *
 * <p>Deleted task can be restored with a {@code RestoreDeletedTask} command.
 */
public class DeletedTaskProjection extends Projection<TaskId, DeletedTask, DeletedTaskVBuilder> {

    /**
     * Creates a new instance.
     *
     * @param id
     *         the ID for the new instance
     * @throws IllegalArgumentException
     *         if the ID is not of one of the supported types
     */
    private DeletedTaskProjection(TaskId id) {
        super(id);
    }

    @Subscribe
    void on(TaskDeleted deleted, EventContext context) {
        TaskId id = deleted.getTaskId();
        DeletedTaskVBuilder builder = builder();
        builder.setId(id);
        TaskDescription description =
                context.find(TaskEnrichment.class)
                       .map(TaskEnrichment::getTask)
                       .map(Task::getDescription)
                       .orElseThrow(DeletedTaskProjection::couldNotObtainEnrichment);
        builder.setDescription(description);
    }

    @Subscribe
    void on(DeletedTaskRestored taskRestored) {
        setDeleted(true);
    }

    private static IllegalStateException couldNotObtainEnrichment() {
        return newIllegalStateException(
                "Could not obtain task enrichment from the context of `TaskDeleted` event.");
    }
}
