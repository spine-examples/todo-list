/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

import com.google.protobuf.Message;
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

import static com.google.common.collect.Sets.newHashSet;
import static io.spine.examples.todolist.EnrichmentHelper.getEnrichment;
import static java.util.Collections.singleton;

/**
 * Repository for the {@link LabelledTasksViewProjection}.
 *
 * @author Illia Shepilov
 */
public class LabelledTasksViewRepository
        extends ProjectionRepository<LabelId, LabelledTasksViewProjection, LabelledTasksView> {

    public LabelledTasksViewRepository() {
        super();
        addIdSetFunctions();
    }

    /**
     * Adds the {@link EventRoute}s to the repository.
     * Should to be overridden in an successor classes,
     * otherwise all successors will use {@code LabelId}
     * and only with specified events below.
     */
    protected void addIdSetFunctions() {
        final EventRouting<LabelId> routing = getEventRouting();
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

    private static <T extends Message> EventRoute<LabelId, T> fromContext() {
        return (message, context) -> getLabelIdsSet(context);
    }

    private static Set<LabelId> getLabelIdsSet(EventContext context) {
        final LabelsListEnrichment enrichment = getEnrichment(LabelsListEnrichment.class, context);
        final LabelIdsList labelsList = enrichment.getLabelIdsList();
        final Set<LabelId> result = newHashSet(labelsList.getIdsList());
        return result;
    }
}
