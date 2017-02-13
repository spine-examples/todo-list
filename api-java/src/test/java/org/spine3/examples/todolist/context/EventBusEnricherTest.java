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

package org.spine3.examples.todolist.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.base.Event;
import org.spine3.base.Events;
import org.spine3.examples.todolist.c.events.TaskCreated;
import org.spine3.server.event.EventBus;
import org.spine3.server.storage.StorageFactorySwitch;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestEventContextFactory.eventContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskCreatedInstance;
import static org.spine3.test.Tests.hasPrivateParameterlessCtor;

/**
 * @author Illia Shepilov
 */
@DisplayName("EventBusEnricher should")
class EventBusEnricherTest {

    @Test
    @DisplayName("have the private constructor")
    public void havePrivateConstructor() {
        assertTrue(hasPrivateParameterlessCtor(EventBusEnricher.class));
    }
}
