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
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.server.SubscriptionService;
import io.spine.server.transport.GrpcContainer;

import java.io.IOException;

import static io.spine.server.event.EventStore.log;

/**
 * Sample gRPC server implementation.
 *
 * @author Illia Shepilov
 */
public class Server {

    private final int port;
    private final GrpcContainer grpcContainer;
    private final BoundedContext boundedContext;

    public Server(int port, BoundedContext boundedContext) {
        this.port = port;
        this.boundedContext = boundedContext;

        final CommandService commandService = initCommandService();
        final QueryService queryService = initQueryService();
        final SubscriptionService subscriptionService = initSubscriptionService();
        this.grpcContainer = initGrpcContainer(commandService, queryService, subscriptionService);
    }

    private SubscriptionService initSubscriptionService() {
        final SubscriptionService result = SubscriptionService.newBuilder()
                                                              .add(boundedContext)
                                                              .build();
        return result;
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

    private GrpcContainer initGrpcContainer(CommandService commandService,
                                            QueryService queryService,
                                            SubscriptionService subscriptionService) {
        final GrpcContainer result = GrpcContainer.newBuilder()
                                                  .addService(commandService)
                                                  .addService(queryService)
                                                  .addService(subscriptionService)
                                                  .setPort(port)
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
        log().info("Server started, listening to commands on the port {}.", port);
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
     * Initiates a shutdown of this {@code Server} instance.
     */
    public void shutdown() {
        grpcContainer.shutdown();
    }
}
