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
import io.spine.client.Subscription;
import io.spine.client.SubscriptionUpdate;
import io.spine.client.Target;
import io.spine.client.Topic;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A client of {@link SubscriptionService} that repeats the received messages to Firebase.
 *
 * <h2>Usage</h2>
 *
 * <p>{@code FirebaseSubscriptionRepeater} broadcasts the received messages within the specified
 * <a target="_blank" href="https://firebase.google.com/docs/firestore/">Cloud Firestore^</a>
 * database.
 *
 * <p>Use {@code FirebaseSubscriptionRepeater} as a subscription update delivery system. When
 * dealing with a number of subscribers (e.g. browser clients), the repeater should be considered
 * a more reliable solution comparing to delivering the updates over gRPC with
 * {@code SubscriptionService}.
 *
 * <p>To start using the repeater, follow these steps:
 * <ol>
 *     <li>Go to the
 *         <a target="_blank" href="https://console.firebase.google.com">Firebase Console^</a>,
 *         select the desired project and enable Cloud Firestore.
 *     <li>Create an instance of {@link Firestore} and pass it to a
 *         {@code FirebaseSubscriptionRepeater}.
 *     <li>{@linkplain io.spine.client.ActorRequestFactory Create} instances of {@code Topic} to
 *         subscribe to.
 *     <li>{@linkplain #broadcast(Topic) Broadcast} each of those {@code Topic}s with
 *         the {@code FirebaseSubscriptionRepeater} instance.
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
 * The simplest {@code Topic} matching that entity looks like:
 * <pre>
 *     {@code final Topic customerTopic = requestFactory.topics().allOf(Customer.class);}
 * </pre>
 *
 * <p>When the broadcast for {@code customerTopic} is started, the entity state updates are written
 * under {@code /example.customer.Customer/document-key}. The {@code document-key} here is a unique
 * key assigned to this instance of {@code Customer} by the repeater. Note that the document key
 * is not a valid ID of a {@code CustomerAggregate}. Do NOT depend any business logic on its value.
 *
 * <h3>Document structure</h3>
 *
 * <p>The Firestore documents created by the repeater have the following structure:
 * <ul>
 *     <li>{@code id}: string;
 *     <li>{@code bytes}: BLOB.
 * </ul>
 *
 * <p>The {@code id} field contains the {@linkplain io.spine.Identifier#toString(Object) string}
 * representation of the entity ID.
 *
 * <p>The {@code bytes} field contains the serialized entity state.
 *
 * <p>Consider providing a custom {@link io.spine.string.Stringifier Stringifier} for the ID type
 * if the ID is compound.
 *
 * <h2>Multitenancy</h2>
 *
 * <p>When working with multitenant {@code BoundedContext}s, the topic broadcast should be started
 * for each tenant explicitly. To start the broadcast for a certain tenant do:
 * <pre>
 *     {@code
 *     new TenantAwareOperation(tenantId) {
 *         \@Override
 *         public void run() {
 *             repeater.broadcast(topic);
 *         }
 *     }.execute();
 *     }
 * </pre>
 *
 * <p>Here the {@code tenantId} is the ID of the tenant to start the broadcast for.
 *
 * <p>In a multitenant bounded context, starting a broadcast without specifying an explicit tenant
 * may cause unpredictable behavior.
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
     * Creates a new instance of {@code Builder} for {@code FirebaseSubscriptionRepeater}
     * instances.
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
}
