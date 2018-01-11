/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.examples.todolist;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import io.spine.core.Enrichments;
import io.spine.core.EventContext;

/**
 * Utility class for working with enrichments.
 *
 * @author Illia Shepilov
 */
public class EnrichmentHelper {

    private EnrichmentHelper() {
    }

    /**
     * Obtains enrichment from the {@link EventContext} according to the enrichment class.
     *
     * @param enrichmentClass the class of the enrichment
     * @param context         the {@code EventContext}
     * @return the enrichment if it is present, throws {@code EnrichmentNotFoundException} otherwise
     */
    @SuppressWarnings("Guava") // Spine API is Java 7-based
                               // and uses {@code Optional} from Google Guava.
    public static <T extends Message, E extends Class<T>>
    T getEnrichment(E enrichmentClass, EventContext context) {
        final Optional<T> enrichmentOptional = Enrichments.getEnrichment(enrichmentClass, context);
        if (enrichmentOptional.isPresent()) {
            return enrichmentOptional.get();
        }
        throw new EnrichmentNotFoundException(enrichmentClass + " not found");
    }
}
