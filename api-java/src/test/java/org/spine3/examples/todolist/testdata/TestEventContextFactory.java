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

import com.google.protobuf.Any;
import org.spine3.base.Enrichments;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.LabelDetailsEnrichment;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides event context instances for test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventContextFactory {

    private static final String ENRICHER = "labelDetails";
    private static final String TITLE = "label's title";

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
        final LabelDetailsEnrichment enrichment = LabelDetailsEnrichment.newBuilder()
                                                                        .setLabelTitle(TITLE)
                                                                        .build();
        final Enrichments enrichments = enrichmentsInstance(enrichment);
        return EventContext.newBuilder()
                           .setEnrichments(enrichments)
                           .build();
    }

    private static Enrichments enrichmentsInstance(LabelDetailsEnrichment enrichment) {
        final Enrichments.Builder builder = Enrichments.newBuilder();
        final Map<String, Any> map = new HashMap<>();
        map.put(ENRICHER, Any.newBuilder()
                             .setTypeUrlBytes(enrichment.toByteString())
                             .build());
        return builder.putAllMap(map)
                      .build();
    }
}
