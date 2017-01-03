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

import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.server.event.enrich.EventEnricher;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Provides event enricher for test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventEnricherFactory {

    public static final String LABEL_TITLE = "title";
    private static final LabelDetails LABEL_DETAILS = LabelDetails.newBuilder()
                                                                  .setTitle(LABEL_TITLE)
                                                                  .setColor(LabelColor.BLUE)
                                                                  .build();

    private TestEventEnricherFactory() {
    }

    private static final Function<TaskLabelId, LabelDetails> LABEL_ID_TO_LABEL_DETAILS =
            new Function<TaskLabelId, LabelDetails>() {
                @Nullable
                @Override
                public LabelDetails apply(@Nullable TaskLabelId input) {
                    return LABEL_DETAILS;
                }
            };

    /**
     * Provides a pre-configured {@link EventEnricher} event instance.
     *
     * @return {@link EventEnricher}
     */
    public static EventEnricher eventEnricherInstance() {
        final EventEnricher result = EventEnricher.newBuilder()
                                                  .addFieldEnrichment(TaskLabelId.class,
                                                                      LabelDetails.class,
                                                                      LABEL_ID_TO_LABEL_DETAILS::apply)
                                                  .build();
        return result;
    }
}
