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

package org.spine3.examples.todolist.testdata;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import org.spine3.base.CommandContext;
import org.spine3.base.Commands;
import org.spine3.base.EventContext;
import org.spine3.base.EventId;
import org.spine3.base.Events;
import org.spine3.protobuf.AnyPacker;
import org.spine3.users.UserId;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.protobuf.Timestamps.getCurrentTime;
import static org.spine3.protobuf.Values.newStringValue;
import static org.spine3.test.Tests.newUserId;

/**
 * Provides event context instances for test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventContextFactory {

    private static final Any AGGREGATE_ID = AnyPacker.pack(newStringValue(newUuid()));

    private TestEventContextFactory() {
    }

    /**
     * Provides a new {@link EventContext} instance.
     *
     * <p> Created event context contains Enrichments.
     * <p> Enrichments contains label details by task ID and by label ID enrichments.
     *
     * @return {@link EventContext} instance
     */
    public static EventContext eventContextInstance() {
        final Timestamp now = getCurrentTime();
        final UserId userId = newUserId(newUuid());
        final CommandContext commandContext = createCommandContext(userId, Commands.generateId(), now);
        final EventId eventId = Events.generateId();
        final EventContext.Builder builder = EventContext.newBuilder()
                                                         .setEventId(eventId)
                                                         .setCommandContext(commandContext)
                                                         .setProducerId(AGGREGATE_ID)
                                                         .setTimestamp(now);
        final EventContext result = builder.build();
        return result;
    }
}
