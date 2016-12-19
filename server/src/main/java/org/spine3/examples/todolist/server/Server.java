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

package org.spine3.examples.todolist.server;

import com.google.common.base.Function;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.server.BoundedContext;
import org.spine3.server.CommandService;
import org.spine3.server.SubscriptionService;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.memory.InMemoryStorageFactory;
import org.spine3.server.transport.GrpcContainer;

import javax.annotation.Nullable;
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
    private final Function<TaskLabelId, String> TASK_LABEL_ID_TO_STRING = new Function<TaskLabelId, String>() {
        @Nullable
        @Override
        public String apply(@Nullable TaskLabelId input) {
            return input != null ? input.getValue() : "";
        }
    };
    private final Function<TaskId, String> TASK_ID_TO_STRING = new Function<TaskId, String>() {
        @Nullable
        @Override
        public String apply(@Nullable TaskId input) {
            return input != null ? input.getValue() : "";
        }
    };

    public Server(StorageFactory storageFactory) {
        final EventEnricher eventEnricher = initEventEnricher();
        this.boundedContext = initBoundedContext(storageFactory, eventEnricher);
        initRepositories(storageFactory);
        final CommandService commandService = initCommandService();
        final SubscriptionService subscriptionService = initSubscriptionService();
        this.grpcContainer = initGrpcContainer(commandService, subscriptionService);
    }

    private GrpcContainer initGrpcContainer(CommandService commandService, SubscriptionService subscriptionService) {
        final GrpcContainer result = GrpcContainer.newBuilder()
                                                  .addService(commandService)
                                                  .addService(subscriptionService)
                                                  .setPort(DEFAULT_CLIENT_SERVICE_PORT)
                                                  .build();
        return result;
    }

    private EventEnricher initEventEnricher() {
        final EventEnricher result = EventEnricher.newBuilder()
                                                  .addFieldEnrichment(TaskLabelId.class,
                                                                      String.class,
                                                                      TASK_LABEL_ID_TO_STRING)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      String.class,
                                                                      TASK_ID_TO_STRING)
                                                  .build();
        return result;
    }

    private BoundedContext initBoundedContext(StorageFactory storageFactory, EventEnricher eventEnricher) {
        final BoundedContext result = BoundedContext.newBuilder()
                                                    .setStorageFactory(storageFactory)
                                                    .setEventEnricher(eventEnricher)
                                                    .build();
        return result;
    }

    private void initRepositories(StorageFactory storageFactory) {
        final TaskAggregateRepository taskAggregateRepository = new TaskAggregateRepository(boundedContext);
        taskAggregateRepository.initStorage(storageFactory);
        boundedContext.register(taskAggregateRepository);

        final MyListViewProjectionRepository myListViewProjectionRepository = new MyListViewProjectionRepository(boundedContext);
        myListViewProjectionRepository.initStorage(storageFactory);
        boundedContext.register(myListViewProjectionRepository);
    }

    private SubscriptionService initSubscriptionService() {
        final SubscriptionService result = SubscriptionService.newBuilder()
                                                              .addBoundedContext(boundedContext)
                                                              .build();
        return result;
    }

    private CommandService initCommandService() {
        final CommandService result = CommandService.newBuilder()
                                                    .addBoundedContext(boundedContext)
                                                    .build();
        return result;
    }

    /**
     * Starts the service.
     *
     * @throws IOException if unable to bind
     */
    public void execute() throws IOException {
        start();
        log().info("Server started, listening to commands on the port " + DEFAULT_CLIENT_SERVICE_PORT);
        awaitTermination();
    }

    /**
     * Starts the service.
     *
     * @throws IOException if unable to bind
     */
    public void start() throws IOException {
        grpcContainer.start();
        grpcContainer.addShutdownHook();
    }

    /**
     * Waits for the service to become terminated.
     */
    public void awaitTermination() {
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
        boundedContext.close();
    }

    public static void main(String[] args) throws IOException {
        final Server server = new Server(InMemoryStorageFactory.getInstance());
        server.execute();
    }
}
