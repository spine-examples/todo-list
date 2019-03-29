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

import io.spine.base.EventMessage;
import io.spine.core.Event;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskListId;
import io.spine.examples.todolist.testdata.TestEventEnricherFactory;
import io.spine.server.enrich.Enricher;
import io.spine.server.event.EventFactory;
import io.spine.server.type.EventEnvelope;
import io.spine.testing.server.TestEventFactory;

import static io.spine.base.Identifier.newUuid;

/**
 * The parent class for the projection test classes.
 * Provides the common methods for testing.
 */
abstract class ProjectionTest {

    private final EventFactory eventFactory = TestEventFactory.newInstance(getClass());
    private final Enricher enricher = TestEventEnricherFactory.eventEnricherInstance();

    Event createEvent(EventMessage messageOrAny) {
        Event event = eventFactory.createEvent(messageOrAny, null);
        EventEnvelope envelope = EventEnvelope.of(event);
        return enricher.enrich(envelope)
                       .outerObject();
    }

    TaskListId createTaskListId() {
        return TaskListId.newBuilder()
                         .setValue(newUuid())
                         .build();
    }

    TaskId newTaskId(){
        return TaskId
                .newBuilder()
                .setValue(newUuid())
                .build();
    }
}
