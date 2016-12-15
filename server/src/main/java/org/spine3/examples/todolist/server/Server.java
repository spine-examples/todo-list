package org.spine3.examples.todolist.server;

import com.google.common.base.Function;
import com.google.protobuf.Any;
import org.spine3.base.Enrichments;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.server.BoundedContext;
import org.spine3.server.CommandService;
import org.spine3.server.SubscriptionService;
import org.spine3.server.event.EventSubscriber;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.memory.InMemoryStorageFactory;
import org.spine3.server.transport.GrpcContainer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private static final Function<TaskLabelId, String> LABEL_ID_TO_STRING =
            labelId -> labelId == null ? "" : labelId.getValue();

    public Server(StorageFactory storageFactory) {
        final EventContext.Builder eventContextBuilder = EventContext.newBuilder();
        Map<String, Any> map = new HashMap<>();
        final Enrichments enrichments = Enrichments.newBuilder()
                                                   .putAllMap(map)
                                                   .build();
        eventContextBuilder.setEnrichments(enrichments);
        EventEnricher eventEnricher = EventEnricher.newBuilder()
                                                   .addFieldEnrichment(TaskLabelId.class,
                                                                       String.class,
                                                                       LABEL_ID_TO_STRING)
                                                   .build();
        // Create a bounded context.
        this.boundedContext = BoundedContext.newBuilder()
                                            .setEventEnricher(eventEnricher)
                                            .setStorageFactory(storageFactory)
                                            .build();

        final TaskRepository taskRepository = new TaskRepository(boundedContext);
        boundedContext.register(taskRepository);

        final EventSubscriber subscriber = new Subscriber();
        boundedContext.getEventBus()
                      .subscribe(subscriber);
        // Create a command service with this bounded context.
        final CommandService commandService = CommandService.newBuilder()
                                                            .addBoundedContext(boundedContext)
                                                            .build();
        final SubscriptionService subscriptionService = SubscriptionService.newBuilder()
                                                                           .addBoundedContext(boundedContext)
                                                                           .build();
        // Pass the service to a GRPC container
        this.grpcContainer = GrpcContainer.newBuilder()
                                          .addService(commandService)
                                          .addService(subscriptionService)
                                          .setPort(DEFAULT_CLIENT_SERVICE_PORT)
                                          .build();
    }

    public void start() throws IOException {
        grpcContainer.start();
        grpcContainer.addShutdownHook();
    }

    public void awaitTermination() {
        grpcContainer.awaitTermination();
    }

    public void shutdown() throws Exception {
        grpcContainer.shutdown();
        boundedContext.close();
    }

    public void execute() throws IOException {
        start();
        log().info("Server started, listening to commands on the port " + DEFAULT_CLIENT_SERVICE_PORT);
        awaitTermination();
    }

    public static void main(String[] args) throws IOException {
        final Server server = new Server(InMemoryStorageFactory.getInstance());
        server.execute();
    }

}
