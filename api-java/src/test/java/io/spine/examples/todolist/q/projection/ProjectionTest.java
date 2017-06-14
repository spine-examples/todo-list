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

package io.spine.examples.todolist.q.projection;

import com.google.protobuf.Message;
import io.spine.base.Event;
import io.spine.examples.todolist.TaskListId;
import io.spine.examples.todolist.testdata.TestEventEnricherFactory;
import io.spine.server.command.EventFactory;
import io.spine.server.event.enrich.EventEnricher;
import io.spine.test.TestEventFactory;

import static io.spine.base.Identifier.newUuid;

/**
 * The parent class for the projection test classes.
 * Provides the common methods for testing.
 *
 * @author Illia Shepilov
 */
abstract class ProjectionTest {

    private final EventFactory eventFactory = TestEventFactory.newInstance(getClass());
    private final EventEnricher enricher = TestEventEnricherFactory.eventEnricherInstance();

    Event createEvent(Message messageOrAny) {
        final Event event = eventFactory.createEvent(messageOrAny, null);

        if (!enricher.canBeEnriched(event)) {
            return event;
        }

        return enricher.enrich(event);
    }

    TaskListId createTaskListId() {
        return TaskListId.newBuilder()
                         .setValue(newUuid())
                         .build();
    }
}