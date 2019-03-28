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
import io.spine.core.EventContext;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.LabelIdsList;
import io.spine.examples.todolist.c.enrichments.LabelsListEnrichment;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.events.LabelledTaskRestored;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.LabelledTasksViewProjection;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.route.EventRoute;
import io.spine.server.route.EventRouting;

import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.singleton;

/**
 * Repository for the {@link LabelledTasksViewProjection}.
 */
public class LabelledTasksViewRepository
        extends ProjectionRepository<LabelId, LabelledTasksViewProjection, LabelledTasksView> {

    @SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
    // Necessary so the implementors can specify their own routing schema.
    public LabelledTasksViewRepository() {
        super();
        setUpEventRoute();
    }

    /**
     * Adds the {@link EventRoute}s to the repository.
     * Should be overridden in the descendant classes,
     * otherwise all successors will use {@code LabelId}
     * and only with the events specified below.
     */
    @SuppressWarnings("OverlyCoupledMethod") // A lot of routed event types.
    protected void setUpEventRoute() {
        EventRouting<LabelId> routing = eventRouting();
        routing.route(LabelAssignedToTask.class,
                      (message, context) -> singleton(message.getLabelId()));
        routing.route(LabelRemovedFromTask.class,
                      (message, context) -> singleton(message.getLabelId()));
        routing.route(LabelledTaskRestored.class,
                      (message, context) -> singleton(message.getLabelId()));
        routing.route(LabelDetailsUpdated.class,
                      (message, context) -> singleton(message.getLabelId()));
        routing.route(TaskDeleted.class, fromContext());
        routing.route(TaskReopened.class, fromContext());
        routing.route(TaskCompleted.class, fromContext());
        routing.route(TaskPriorityUpdated.class, fromContext());
        routing.route(TaskDescriptionUpdated.class, fromContext());
        routing.route(TaskDueDateUpdated.class, fromContext());
    }

    private static <T extends EventMessage> EventRoute<LabelId, T> fromContext() {
        return (message, context) -> getLabelIdsSet(context);
    }

    private static Set<LabelId> getLabelIdsSet(EventContext context) {
        ImmutableSet<LabelId> result =
                context.find(LabelsListEnrichment.class)
                       .map(LabelsListEnrichment::getLabelIdsList)
                       .map(LabelIdsList::getIdsList)
                       .map(ImmutableSet::copyOf)
                       .orElseThrow(() -> labelIdSetNotFound(context));
        return result;
    }

    private static IllegalStateException labelIdSetNotFound(EventContext context) {
        return new IllegalStateException(
                format("Could not get label ID set from context %s.", context));
    }
}
