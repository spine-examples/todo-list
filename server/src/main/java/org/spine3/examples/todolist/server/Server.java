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

package org.spine3.examples.todolist.server;

import org.spine3.server.BoundedContext;
import org.spine3.server.CommandService;
import org.spine3.server.QueryService;
import org.spine3.server.transport.GrpcContainer;

import java.io.IOException;

import static org.spine3.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static org.spine3.server.event.EventStore.log;

/**
 * Sample gRPC server implementation.
 *
 * @author Illia Shepilov
 */
public class Server {

    private final GrpcContainer grpcContainer;
    private final BoundedContext boundedContext;

    public Server(BoundedContext boundedContext) {
        this.boundedContext = boundedContext;

        final CommandService commandService = initCommandService();
        final QueryService queryService = initQueryService();
        this.grpcContainer = initGrpcContainer(commandService, queryService);
    }

    private QueryService initQueryService() {
        final QueryService result = QueryService.newBuilder()
                                                .add(boundedContext)
                                                .build();
        return result;
    }

    private CommandService initCommandService() {
        final CommandService result = CommandService.newBuilder()
                                                    .add(boundedContext)
                                                    .build();
        return result;
    }

    private static GrpcContainer initGrpcContainer(CommandService commandService, QueryService queryService) {
        final GrpcContainer result = GrpcContainer.newBuilder()
                                                  .addService(commandService)
                                                  .addService(queryService)
                                                  .setPort(DEFAULT_CLIENT_SERVICE_PORT)
                                                  .build();
        return result;
    }

    /**
     * Starts the service.
     *
     * @throws IOException if unable to bind
     */
    public void start() throws IOException {
        startServer();
        log().info("Server started, listening to commands on the port " + DEFAULT_CLIENT_SERVICE_PORT);
        awaitTermination();
    }

    private void startServer() throws IOException {
        grpcContainer.start();
        grpcContainer.addShutdownHook();
    }

    /**
     * Waits for the service to become terminated.
     */
    private void awaitTermination() {
        grpcContainer.awaitTermination();
    }

    /**
     * Initiates an orderly shutdown of {@link GrpcContainer} and closes {@link BoundedContext}.
     *
     * <p> Closes the {@code BoundedContext} performing all necessary clean-ups.
     *
     * @throws Exception caused by closing one of the {@link BoundedContext} components
     */
    public void shutdown() throws Exception {
        grpcContainer.shutdown();
    }
}
