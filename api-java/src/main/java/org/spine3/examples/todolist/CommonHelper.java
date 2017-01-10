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

package org.spine3.examples.todolist;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import org.spine3.base.EventContext;
import org.spine3.base.Events;

/**
 * @author Illia Shepilov
 */
public class CommonHelper {

    private CommonHelper(){}

    @SuppressWarnings("Guava")
    //As long as Spine API is based on Java 7, {@link Events#getEnrichment} uses Guava {@link Optional}.
    public static <T extends Message, E extends Class<T>> T getEnrichment(E enrichmentClass, EventContext context) {
        final Optional<T> enrichmentOptional = Events.getEnrichment(enrichmentClass, context);
        if (enrichmentOptional.isPresent()) {
            T result = enrichmentOptional.get();
            return result;
        }
        throw new EnrichmentNotFoundException(enrichmentClass + " not found");
    }
}
