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

import com.google.common.collect.Maps;
import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import org.spine3.base.CommandContext;
import org.spine3.base.Commands;
import org.spine3.base.Enrichments;
import org.spine3.base.EventContext;
import org.spine3.base.EventId;
import org.spine3.base.Events;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsByLabelIdEnrichment;
import org.spine3.examples.todolist.LabelDetailsByTaskIdEnrichment;
import org.spine3.protobuf.AnyPacker;
import org.spine3.users.UserId;

import java.util.Map;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.protobuf.Timestamps.getCurrentTime;
import static org.spine3.protobuf.Values.newStringValue;
import static org.spine3.test.Tests.newUserId;

/**
 * Provides event context instances for test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventContextFactory {

    private static final String TITLE = "label title";
    private static final LabelColor COLOR = LabelColor.GREEN;
    private static final String ENRICHMENT_BY_LABEL_ID = "spine.examples.todolist.LabelDetailsByLabelIdEnrichment";
    private static final String ENRICHMENT_BY_TASK_ID = "spine.examples.todolist.LabelDetailsByTaskIdEnrichment";
    private static final Any AGGREGATE_ID = AnyPacker.pack(newStringValue(newUuid()));

    private TestEventContextFactory() {
    }

    /**
     * Provides a new {@link EventContext} instance.
     *
     * <p> Created event context contains Enrichments.
     * <p> Enrichments contains label details by task id and by label ID enrichments.
     *
     * @return {@link EventContext} instance
     */
    public static EventContext eventContextInstance() {
        final Enrichments enrichments = createEnrichments();
        final Timestamp now = getCurrentTime();
        final UserId userId = newUserId(newUuid());
        final CommandContext commandContext =
                TestCommandContextFactory.createCommandContext(userId, Commands.generateId(), now);
        final EventId eventId = Events.generateId();
        final EventContext.Builder builder = EventContext.newBuilder()
                                                         .setEnrichments(enrichments)
                                                         .setEventId(eventId)
                                                         .setCommandContext(commandContext)
                                                         .setProducerId(AGGREGATE_ID)
                                                         .setTimestamp(now);
        return builder.build();
    }

    private static Enrichments createEnrichments() {
        final LabelDetails.Builder labelDetails = LabelDetails.newBuilder()
                                                              .setTitle(TITLE)
                                                              .setColor(COLOR);
        final LabelDetailsByLabelIdEnrichment enrichmentByLabelId =
                LabelDetailsByLabelIdEnrichment.newBuilder()
                                               .setLabelDetails(labelDetails)
                                               .build();
        final LabelDetailsByTaskIdEnrichment enrichmentByTaskId =
                LabelDetailsByTaskIdEnrichment.newBuilder()
                                              .setLabelDetails(labelDetails)
                                              .build();
        final Map<String, Any> enrichmentsMap = Maps.newHashMap();
        enrichmentsMap.put(ENRICHMENT_BY_LABEL_ID, AnyPacker.pack(enrichmentByLabelId));
        enrichmentsMap.put(ENRICHMENT_BY_TASK_ID, AnyPacker.pack(enrichmentByTaskId));
        final Enrichments result = Enrichments.newBuilder()
                                              .putAllMap(enrichmentsMap)
                                              .build();
        return result;
    }
}
