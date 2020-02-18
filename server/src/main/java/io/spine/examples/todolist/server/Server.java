/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import io.spine.logging.Logging;
import io.spine.server.BoundedContext;
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.server.SubscriptionService;
import io.spine.server.GrpcContainer;

import java.io.IOException;

/**
 * Sample gRPC server implementation.
 */
public final class Server implements Logging {

    private final int port;
    private final GrpcContainer grpcContainer;
    private final BoundedContext context;

    /**
     * Creates a server with the {@link CommandService Command}, {@link QueryService Query} and
     * {@link SubscriptionService Subscription} gRPC services.
     *
     * @param port
     *         the port to bind the server to
     * @param boundedContext
     *         the {@link BoundedContext} to serve
     * @return a new instance of {@code Server}
     */
    public static Server newServer(int port, BoundedContext boundedContext) {
        return new Server(port, boundedContext);
    }

    /**
     * Creates a new instance of {@code Server}.
     *
     * @param port
     *         the port to bind the server to
     * @param context
     *         the {@link BoundedContext} to serve
     */
    private Server(int port, BoundedContext context) {
        this.port = port;
        this.context = context;

        CommandService commandService = initCommandService();
        QueryService queryService = initQueryService();
        SubscriptionService subscriptionService = initSubscriptionService();
        this.grpcContainer = initGrpcContainer(commandService, queryService, subscriptionService);
    }

    private SubscriptionService initSubscriptionService() {
        SubscriptionService result = SubscriptionService
                .newBuilder()
                .add(context)
                .build();
        return result;
    }

    private QueryService initQueryService() {
        QueryService result = QueryService
                .newBuilder()
                .add(context)
                .build();
        return result;
    }

    private CommandService initCommandService() {
        CommandService result = CommandService
                .newBuilder()
                .add(context)
                .build();
        return result;
    }

    /**
     * Creates a {@link GrpcContainer} for this server.
     *
     * @param commandService
     *         the {@link CommandService} to deploy
     * @param queryService
     *         the {@link QueryService} to deploy
     * @param subscriptionService
     *         the {@link SubscriptionService} to deploy or {@code null} if no
     *         {@code SubscriptionService} is intended for this server
     * @return a new instance of {@link GrpcContainer}
     */
    private GrpcContainer initGrpcContainer(CommandService commandService,
                                            QueryService queryService,
                                            SubscriptionService subscriptionService) {
        GrpcContainer.Builder result = GrpcContainer
                .atPort(port)
                .addService(commandService)
                .addService(queryService)
                .addService(subscriptionService);
        return result.build();
    }

    /**
     * Starts the service.
     *
     * @throws IOException
     *         if unable to bind
     */
    public void start() throws IOException {
        startServer();
        _info().log("Server started, listening to commands on the port %s.", port);
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
