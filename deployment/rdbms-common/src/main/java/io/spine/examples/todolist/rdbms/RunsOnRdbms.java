/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.rdbms;

import io.spine.base.Production;
import io.spine.examples.todolist.server.Server;
import io.spine.examples.todolist.server.tasks.TasksContextFactory;
import io.spine.server.BoundedContext;
import io.spine.server.ServerEnvironment;
import io.spine.server.transport.memory.InMemoryTransportFactory;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.server.Server.newServer;

/**
 * An abstract base for To-Do List servers backed by a relational database.
 *
 * <p>The server can be run by passing the {@code args} from the {@code main} method to
 * {@link #start(String[])}.
 */
public abstract class RunsOnRdbms {

    /**
     * Starts the server.
     *
     * <p>Please pass the command line arguments from {@code main} to this method.
     *
     * @param args
     *         command line arguments from the {@code main} method
     */
    public final void start(String[] args) throws IOException {
        RelationalStorage storage = storage(args);

        ServerEnvironment serverEnvironment = ServerEnvironment.instance();
        serverEnvironment.use(storage.storageFactory(), Production.class)
                         .use(InMemoryTransportFactory.newInstance(), Production.class);
        BoundedContext context = TasksContextFactory.create();
        Server server = newServer(DEFAULT_CLIENT_SERVICE_PORT, context);
        server.start();
    }

    /**
     * Constructs a {@code RelationalStorage} using the command line arguments.
     *
     * <p>Extenders are free to ignore the arguments and use other data to assemble the storage.
     *
     * @param args
     *         command line arguments specified to the {@code main} method
     */
    protected abstract RelationalStorage storage(String[] args);
}
