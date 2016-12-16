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
import org.spine3.base.Enrichments;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsEnrichment;
import org.spine3.protobuf.AnyPacker;

import java.util.Map;

/**
 * Provides event context instances for test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventContextFactory {

    private static final String TITLE = "label title";
    private static final LabelColor COLOR = LabelColor.GREEN;
    private static final String ENRICHMENT = "spine.examples.todolist.LabelDetailsEnrichment";

    /**
     * Provides a new event context {@link EventContext} instance.
     *
     * <p> Created event context contains Enrichments.
     * <p>
     * <p> Enrichments contains LabelTitleEnrichment.
     *
     * @return {@link EventContext} instance
     */
    public static EventContext eventContextInstance() {
        final LabelDetails.Builder labelDetails = LabelDetails.newBuilder()
                                                              .setTitle(TITLE)
                                                              .setColor(COLOR);
        final LabelDetailsEnrichment enrichment = LabelDetailsEnrichment.newBuilder()
                                                                        .setLabelDetails(labelDetails)
                                                                        .build();
        final Enrichments enrichments = enrichmentsInstance(enrichment);
        final EventContext result = EventContext.newBuilder()
                                                .setEnrichments(enrichments)
                                                .build();
        return result;
    }

    private static Enrichments enrichmentsInstance(LabelDetailsEnrichment enrichment) {
        final Map<String, Any> enrichmentsMap = Maps.newHashMap();
        enrichmentsMap.put(ENRICHMENT, AnyPacker.pack(enrichment));
        final Enrichments result = Enrichments.newBuilder()
                                              .putAllMap(enrichmentsMap)
                                              .build();
        return result;
    }
}
