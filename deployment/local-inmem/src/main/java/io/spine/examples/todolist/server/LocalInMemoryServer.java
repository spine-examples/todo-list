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

package io.spine.examples.todolist.server;

import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.context.BoundedContexts.create;
import static io.spine.examples.todolist.context.BoundedContexts.getDefaultName;
import static io.spine.server.storage.memory.InMemoryStorageFactory.newInstance;

/**
 * A local {@link Server} using
 * {@link io.spine.server.storage.memory.InMemoryStorageFactory InMemoryStorageFactory}.
 *
 * <p>The server exposes its {@code gRPC API} at
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * @author Dmytro Grankin
 */
public class LocalInMemoryServer {

    private LocalInMemoryServer() {
        // Prevent instantiation of this class.
    }

    public static void main(String[] args) throws IOException {
        final StorageFactory storageFactory = newInstance(getDefaultName(), false);
        final BoundedContext boundedContext = create(storageFactory);
        final Server server = new Server(DEFAULT_CLIENT_SERVICE_PORT, boundedContext);
        server.start();
    }
}
