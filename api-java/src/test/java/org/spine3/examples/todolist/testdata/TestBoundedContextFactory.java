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

import org.spine3.server.BoundedContext;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

/**
 * Provides bounded context for test needs.
 *
 * @author Illia Shepilov
 */
public class TestBoundedContextFactory {

    /**
     * Prevent instantiation.
     */
    private TestBoundedContextFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Provides a new {@link BoundedContext} instance
     * by specified {@link EventEnricher} and {@link InMemoryStorageFactory}.
     *
     * @param eventEnricher {@link EventEnricher} instance
     * @return {@link BoundedContext} instance
     */
    public static BoundedContext boundedContextInstance(EventEnricher eventEnricher,
                                                        InMemoryStorageFactory inMemoryStorageFactory) {
        final BoundedContext result = BoundedContext.newBuilder()
                                                    .setEventEnricher(eventEnricher)
                                                    .setStorageFactory(inMemoryStorageFactory)
                                                    .build();
        return result;
    }
}
