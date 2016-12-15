package org.spine3.examples.todolist.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.base.Command;
import org.spine3.base.Identifiers;
import org.spine3.base.Response;
import org.spine3.client.CommandFactory;
import org.spine3.client.Subscription;
import org.spine3.client.SubscriptionUpdate;
import org.spine3.client.Target;
import org.spine3.client.Topic;
import org.spine3.client.grpc.CommandServiceGrpc;
import org.spine3.client.grpc.SubscriptionServiceGrpc;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.Task;
import org.spine3.protobuf.Messages;
import org.spine3.protobuf.TypeUrl;
import org.spine3.time.ZoneOffsets;
import org.spine3.users.UserId;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.spine3.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static org.spine3.protobuf.Messages.toText;

/**
 * Sample gRPC client implementation.
 *
 * @author Illia Shepilov
 */
public class Client {

    private final CommandFactory commandFactory;
    private final ManagedChannel channel;
    private final CommandServiceGrpc.CommandServiceBlockingStub blockingClient;
    private final SubscriptionServiceGrpc.SubscriptionServiceStub nonBlockingClient;
    private final Topic taskTopic;

    private final StreamObserver<Subscription> taskUpdateObserver = new StreamObserver<Subscription>() {
        @Override
        public void onNext(Subscription value) {
            log().debug("Task updated. Value is {}", value);
            nonBlockingClient.activate(value, observer);
        }

        @Override
        public void onError(Throwable t) {
            log().error("Subscription streaming error occurred", t);
        }

        @Override
        public void onCompleted() {
            log().info("Subscription stream completed.");
        }
    };

    private final StreamObserver<SubscriptionUpdate> observer = new StreamObserver<SubscriptionUpdate>() {
        @Override
        public void onNext(SubscriptionUpdate update) {
            final String updateText = Messages.toText(update);
            log().info(updateText);
        }

        @Override
        public void onError(Throwable throwable) {
            log().error("Streaming error occurred", throwable);
        }

        @Override
        public void onCompleted() {
            log().info("Stream completed.");
        }
    };

    /**
     * Construct the client connecting to server at {@code host:port}.
     */
    public Client(String host, int port) {
        final TypeUrl taskTypeUrl = TypeUrl.of(Task.getDescriptor());
        final Target.Builder target = Target.newBuilder()
                                            .setType(taskTypeUrl.getTypeName());
        taskTopic = Topic.newBuilder()
                         .setTarget(target)
                         .build();
        commandFactory = CommandFactory.newBuilder()
                                       .setActor(UserId.newBuilder()
                                                       .setValue(Identifiers.newUuid())
                                                       .build())
                                       .setZoneOffset(ZoneOffsets.UTC)
                                       .build();
        channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext(true)
                .build();
        blockingClient = CommandServiceGrpc.newBlockingStub(channel);
        nonBlockingClient = SubscriptionServiceGrpc.newStub(channel);
    }

    private void subscribe() {
        nonBlockingClient.subscribe(taskTopic, taskUpdateObserver);
    }

    /**
     * Sends requests to the server.
     */
    public static void main(String[] args) throws InterruptedException {
        final Client client = new Client("localhost", DEFAULT_CLIENT_SERVICE_PORT);
        client.subscribe();

        final List<Command> requests = client.generateRequests();

        for (Command request : requests) {
            log().info("Sending a request: " + request.getMessage()
                                                      .getTypeUrl() + "...");
            final Response result = client.post(request);
            log().info("Result: " + toText(result));
        }

        client.shutdown();
    }

    /**
     * Creates several test requests.
     */
    private List<Command> generateRequests() {
        final List<Command> commands = newArrayList();
        commands.add(createTask());
        return commands;
    }

    private Command createTask() {
        final CreateBasicTask message = CreateBasicTask.newBuilder()
                                                       .setDescription("task description")
                                                       .build();
        return commandFactory.create(message);
    }

    /**
     * Shutdown the connection channel.
     *
     * @throws InterruptedException if waiting is interrupted.
     */
    private void shutdown() throws InterruptedException {
        channel.shutdown()
               .awaitTermination(10, SECONDS);
    }

    /**
     * Sends a request to the server.
     */
    private Response post(Command request) {
        Response result = null;
        try {
            result = blockingClient.post(request);
        } catch (RuntimeException e) {
            log().warn("failed", e);
        }
        return result;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(Client.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

}
