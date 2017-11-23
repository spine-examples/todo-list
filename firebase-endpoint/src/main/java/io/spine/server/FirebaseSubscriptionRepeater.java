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

package io.spine.server;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import io.grpc.stub.StreamObserver;
import io.spine.client.EntityStateUpdate;
import io.spine.client.Subscription;
import io.spine.client.SubscriptionUpdate;
import io.spine.client.Target;
import io.spine.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A client of {@link SubscriptionService} that repeats the received messages to Firebase.
 *
 * <h2>Usage</h2>
 *
 * <p>{@code FirebaseSubscriptionRepeater} broadcasts the received messages within the specified
 * <a target="_blank" href="https://firebase.google.com/docs/firestore/">Cloud Firestore^</a>
 * database.
 *
 * <p>Use Cloud Firestore as a subscription update delivery system. When dealing with a number of
 * subscribers (e.g. browser clients), the repeater should be considered more reliable comparing to
 * delivering the updates over gRPC with {@code SubscriptionService}.
 *
 * <p>To start using the Firebase repeater, follow these steps:
 * <ol>
 *     <li>Go to the
 *         <a target="_blank" href="https://console.firebase.google.com">Firebase Console^</a>,
 *         select the desired project and enable Cloud Firestore.
 *     <li>Create an instance of {@link Firestore} and pass in to a
 *         {@code FirebaseSubscriptionRepeater}.
 *     <li>{@linkplain io.spine.client.ActorRequestFactory Create} instances of {@code Topic} to
 *         subscribe to.
 *     <li>{@linkplain #broadcast(Topic) Subscribe} {@code FirebaseSubscriptionRepeater} to each of
 *         those {@code Topic}s.
 * </ol>
 *
 * <a name="protocol"></a>
 *
 * <h2>Protocol</h2>
 *
 * <h3>Location</h3>
 *
 * <p>The repeater writes the received entity state updates to the given Firestore by the following
 * rules:
 * <ul>
 *     <li>A {@linkplain CollectionReference collection} with the name equal to the Protobuf type
 *         name of the subscription target type is created.
 *     <li>The {@linkplain DocumentReference documents} in that collection represent the entity
 *         states. There is at most one document for each {@code Entity} instance of the target
 *         type in the collection.
 *     <li>When the entity is updated, the appropriate document is updated as well.
 * </ul>
 *
 * <p><b>Example:</b>
 * <p>Assuming there is the
 * {@code CustomerAggregate extends Aggregate<CustomerId, Customer, CustomerVBuilder>} entity.
 * The simplest {@code Topic} looks as follows:
 * <pre>
 *     {@code final Topic customerTopic = requestFactory.topics().allOf(Customer.class);}
 * </pre>
 *
 * <p>Then, if we {@linkplain #broadcast(Topic) start broadcasting} this topic with a repeater,
 * the entity state updates start being written under
 * the {@code /example.customer.Customer/document-key} path. The {@code document-key} here is
 * a unique key assigned to this instance of {@code Customer} by the repeater. Note that
 * the document key is not a valid identifier of a {@code CustomerAggregate} entity. Do NOT depend
 * any business logic on the document key.
 *
 * <h3>Document structure</h3>
 *
 * <p>The Firestore documents created by the repeater have the following structure:
 * <ul>
 *     <li>{@code id}: string;
 *     <li>{@code bytes}: BLOB.
 * </ul>
 *
 * <p>The {@code id} field contains the {@linkplain io.spine.string.Stringifiers#toString(Object)
 * string} representation of the entity ID.
 *
 * <p>The {@code bytes} field contains the serialized entity state.
 *
 * <p>To make it easy for a client to subscribe to a certain document, provide
 * a custom {@link io.spine.string.Stringifier Stringifier} for the message ID type.
 *
 * @author Dmytro Dashenkov
 * @see SubscriptionService
 */
public final class FirebaseSubscriptionRepeater {

    private final Firestore database;
    private final SubscriptionService subscriptionService;

    private FirebaseSubscriptionRepeater(Builder builder) {
        this.database = builder.database;
        this.subscriptionService = builder.subscriptionService;
    }

    /**
     * Starts broadcasting the given {@link Topic} withing the Cloud Firestore.
     *
     * <p>After this method invocation the underlying {@link Firestore} (eventually) starts
     * receiving the entity state updates for the given {@code topic}.
     *
     * <p>See <a href="#protocol">class level doc</a> for the description of the storage protocol.
     *
     * @param topic the topic to broadcast
     */
    public void broadcast(Topic topic) {
        checkNotNull(topic);
        final Target target = topic.getTarget();
        final String type = target.getType();
        final CollectionReference collectionReference = database.collection(type);
        final StreamObserver<SubscriptionUpdate> updateObserver =
                new SubscriptionToFirebaseAdapter(collectionReference);
        final StreamObserver<Subscription> subscriptionObserver =
                new SubscriptionObserver(subscriptionService, updateObserver);
        subscriptionService.subscribe(topic, subscriptionObserver);
    }

    /**
     * Creates a new instance of {@code Builder} for {@code FirebaseSubscriptionRepeater} instances.
     *
     * @return new instance of {@code Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for the {@code FirebaseSubscriptionRepeater} instances.
     */
    public static final class Builder {

        private SubscriptionService subscriptionService;
        private Firestore database;

        // Prevent direct instantiation.
        private Builder() {}

        public Builder setSubscriptionService(SubscriptionService subscriptionService) {
            this.subscriptionService = checkNotNull(subscriptionService);
            return this;
        }

        public Builder setDatabase(Firestore database) {
            this.database = checkNotNull(database);
            return this;
        }

        /**
         * Creates a new instance of {@code FirebaseSubscriptionRepeater}.
         *
         * @return new instance of {@code FirebaseSubscriptionRepeater} with the given
         *         parameters
         */
        public FirebaseSubscriptionRepeater build() {
            checkNotNull(database);
            checkNotNull(subscriptionService);
            return new FirebaseSubscriptionRepeater(this);
        }
    }

    /**
     * An implementation of {@link StreamObserver} which
     * {@linkplain SubscriptionService#activate activates} all the received {@link Subscription}s.
     *
     * <p>After activation the given {@code dataObserver} starts receiving the Entity state
     * updates.
     *
     * <p>The implementation throws a {@link RuntimeException} upon any
     * {@linkplain StreamObserver#onError error} and handles the
     * {@linkplain StreamObserver#onCompleted() successful completion} silently.
     */
    private static class SubscriptionObserver implements StreamObserver<Subscription> {

        private final SubscriptionService subscriptionService;
        private final StreamObserver<SubscriptionUpdate> dataObserver;

        private SubscriptionObserver(SubscriptionService service,
                                     StreamObserver<SubscriptionUpdate> dataObserver) {
            this.subscriptionService = service;
            this.dataObserver = dataObserver;
        }

        @Override
        public void onNext(Subscription subscription) {
            subscriptionService.activate(subscription, dataObserver);
        }

        @Override
        public void onError(Throwable t) {
            throw new IllegalStateException(t);
        }

        // NoOp
        @Override
        public void onCompleted() {}
    }

    /**
     * An implementation of {@link StreamObserver} publishing the received
     * {@link SubscriptionUpdate}s to the given
     * {@link CollectionReference Cloud Firestore location}.
     *
     * <p>The implementation logs a message upon either
     * {@linkplain StreamObserver#onCompleted() successful} or
     * {@linkplain StreamObserver#onError faulty} stream completion.
     *
     * @see FirestoreEntityStateUpdatePublisher
     */
    private static class SubscriptionToFirebaseAdapter
            implements StreamObserver<SubscriptionUpdate> {

        private final CollectionReference target;
        private final FirestoreEntityStateUpdatePublisher publisher;

        private SubscriptionToFirebaseAdapter(CollectionReference target) {
            this.target = target;
            this.publisher = new FirestoreEntityStateUpdatePublisher(target);
        }

        @Override
        public void onNext(SubscriptionUpdate value) {
            final Collection<EntityStateUpdate> payload = value.getEntityStateUpdatesList();
            publisher.publish(payload);
        }

        @Override
        public void onError(Throwable error) {
            log().error(format("Subscription with target `%s` has been completed with an error.",
                               target.getPath()),
                        error);
        }

        @Override
        public void onCompleted() {
            log().info("Subscription with target `{}` has been completed.", target.getPath());
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(FirebaseSubscriptionRepeater.class);
    }
}
