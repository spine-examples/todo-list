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

package io.spine.examples.todolist.testdata;

import io.spine.server.BoundedContext;
import io.spine.server.event.EventBus;
import io.spine.server.storage.StorageFactorySwitch;

/**
 * Provides bounded context for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestBoundedContextFactory {

    private TestBoundedContextFactory() {
    }

    /**
     * Provides a new {@link BoundedContext} instance
     * built with the specified {@link EventBus.Builder} and {@link StorageFactorySwitch}.
     *
     * @param eventBus {@code EventBus.Builder} instance
     * @return the {@code BoundedContext} instance
     */
    public static BoundedContext boundedContextInstance(EventBus.Builder eventBus,
                                                        StorageFactorySwitch storageFactory) {
        return BoundedContext.newBuilder()
                             .setEventBus(eventBus)
                             .setStorageFactorySupplier(storageFactory)
                             .build();
    }
}
