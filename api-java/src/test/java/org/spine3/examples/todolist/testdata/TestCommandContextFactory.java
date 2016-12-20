/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

import com.google.protobuf.Timestamp;
import org.spine3.base.CommandContext;
import org.spine3.base.CommandId;
import org.spine3.base.Commands;
import org.spine3.users.TenantId;
import org.spine3.users.UserId;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.protobuf.Timestamps.getCurrentTime;
import static org.spine3.test.Tests.newUserId;
import static org.spine3.time.ZoneOffsets.UTC;

/**
 * Provides command context instances for test needs.
 *
 * @author Illia Shepilov
 */
public class TestCommandContextFactory {

    /**
     * Prevent instantiation.
     */
    private TestCommandContextFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new {@link CommandContext} instance.
     *
     * @return {@link CommandContext} instance.
     */
    public static CommandContext createCommandContext() {
        final UserId userId = newUserId(newUuid());
        final Timestamp now = getCurrentTime();
        final CommandId commandId = Commands.generateId();
        return createCommandContext(userId, commandId, now);
    }

    /**
     * Creates a new {@link CommandContext} instance according to the parameters specified.
     *
     * @param userId    the actor's id
     * @param commandId the command's id
     * @param when      indicates when was created context
     * @return constructed instance of {@link CommandContext}
     */
    public static CommandContext createCommandContext(UserId userId, CommandId commandId, Timestamp when) {
        final CommandContext.Builder builder = CommandContext.newBuilder()
                                                             .setCommandId(commandId)
                                                             .setActor(userId)
                                                             .setTimestamp(when)
                                                             .setZoneOffset(UTC)
                                                             .setTenantId(TenantId.newBuilder()
                                                                                  .setValue(newUuid()));
        return builder.build();
    }
}
