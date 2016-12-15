package org.spine3.examples.todolist.server;

import org.spine3.server.BoundedContext;
import org.spine3.server.CommandService;
import org.spine3.server.SubscriptionService;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.memory.InMemoryStorageFactory;
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

    public Server(StorageFactory storageFactory) {
        this.boundedContext = BoundedContext.newBuilder()
                                            .setStorageFactory(storageFactory)
                                            .build();

        final TaskAggregateRepository taskAggregateRepository = new TaskAggregateRepository(boundedContext);
        taskAggregateRepository.initStorage(storageFactory);
        boundedContext.register(taskAggregateRepository);

        final MyListViewProjectionRepository myListViewProjectionRepository = new MyListViewProjectionRepository(boundedContext);
        myListViewProjectionRepository.initStorage(storageFactory);
        boundedContext.register(myListViewProjectionRepository);

        final CommandService commandService = CommandService.newBuilder()
                                                            .addBoundedContext(boundedContext)
                                                            .build();
        final SubscriptionService subscriptionService = SubscriptionService.newBuilder()
                                                                           .addBoundedContext(boundedContext)
                                                                           .build();
        this.grpcContainer = GrpcContainer.newBuilder()
                                          .addService(commandService)
                                          .addService(subscriptionService)
                                          .setPort(DEFAULT_CLIENT_SERVICE_PORT)
                                          .build();
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
