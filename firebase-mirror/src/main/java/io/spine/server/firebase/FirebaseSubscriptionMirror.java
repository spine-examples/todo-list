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

package io.spine.server.firebase;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import io.grpc.stub.StreamObserver;
import io.spine.client.Subscription;
import io.spine.client.SubscriptionUpdate;
import io.spine.client.Target;
import io.spine.client.Topic;
import io.spine.core.TenantId;
import io.spine.server.SubscriptionService;
import io.spine.server.tenant.TenantAwareOperation;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A client of {@link SubscriptionService} that mirrors the received messages to Firebase.
 *
 * <h2>Usage</h2>
 *
 * <p>{@code FirebaseSubscriptionMirror} reflects the received messages into the specified
 * <a target="_blank" href="https://firebase.google.com/docs/firestore/">Cloud Firestore^</a>
 * database.
 *
 * <p>Use {@code FirebaseSubscriptionMirror} as a subscription update delivery system. When
 * dealing with a number of subscribers (e.g. browser clients), the mirror should be considered
 * a more reliable solution comparing to delivering the updates over gRPC with
 * {@code SubscriptionService}.
 *
 * <p>To start using the mirror, follow these steps:
 * <ol>
 *     <li>Go to the
 *         <a target="_blank" href="https://console.firebase.google.com">Firebase Console^</a>,
 *         select the desired project and enable Cloud Firestore.
 *     <li>Create an instance of {@link Firestore} and pass it to a
 *         {@code FirebaseSubscriptionMirror}.
 *     <li>{@linkplain io.spine.client.ActorRequestFactory Create} instances of {@code Topic} to
 *         reflect.
 *     <li>{@linkplain #reflect(Topic) Reflect} each of those {@code Topic}s with
 *         the {@code FirebaseSubscriptionMirror} instance into the Cloud Firestore.
 * </ol>
 *
 * <a name="protocol"></a>
 *
 * <h2>Protocol</h2>
 *
 * <h3>Location</h3>
 *
 * <p>The mirror writes the received entity state updates to the given Firestore by the following
 * rules:
 * <ul>
 *     <li>A {@linkplain CollectionReference collection} is created per entity type. The name of
 *         the collection is equal to the Protobuf type name of the entity state.
 *     <li>The {@linkplain DocumentReference documents} in that collection represent the entity
 *         states. There is at most one document for each {@code Entity} instance in the collection.
 *     <li>When the entity is updated, the appropriate document is updated as well.
 * </ul>
 *
 * <p><b>Example:</b>
 * <p>Assume there is the
 * {@code CustomerAggregate extends Aggregate<CustomerId, Customer, CustomerVBuilder>} entity.
 * The simplest {@code Topic} matching that entity looks like:
 * <pre>
 *     {@code final Topic customerTopic = requestFactory.topics().allOf(Customer.class);}
 * </pre>
 *
 * <p>After starting reflecting the {@code customerTopic}, the entity state updates will be written
 * under {@code /example.customer.Customer/document-key}. The {@code document-key} here is a unique
 * key assigned to this instance of {@code Customer} by the mirror. Note that
 * the {@code document-key} is not a valid {@code CustomerAggregate} ID. Do NOT depend any business
 * logic on its value.
 *
 * <h3>Document structure</h3>
 *
 * <p>The Firestore documents created by the mirror have the following structure:
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
 * <a name="multitenancy"></a>
 *
 * <h2>Multitenancy</h2>
 *
 * <p>When working with multitenant {@code BoundedContext}s, reflecting a topic should be started
 * explicitly for each tenant. To do that, use {@link #reflect(Topic, TenantId)} instead of
 * {@link #reflect(Topic)}.
 *
 * <p>In a multitenant bounded context, starting a reflect without specifying an explicit tenant
 * may cause unpredictable behavior.
 *
 * <p>It may be convenient to have a separate Firebase project for each tenant. If this is not
 * possible, use a custom database location instead:
 * <pre>
 *     {@code
 *     final Topic topic = // retrieve desired topic.
 *     final TenantId tenant = // get the tenant ID e.g. from ActorContext.
 *     final DocumentReference location = // dedicate a document for this tenant
 *     final FirebaseSubscriptionMirror mirror =
 *             FirebaseSubscriptionMirror.newBuilder()
 *                                       .setSubscriptionService(service)
 *                                       .setDatabaseLocation(location)
 *                                       .build();
 *     mirror.reflect(topic, tenant);
 *     }
 * </pre>
 *
 * <p>When specifying a custom database location, all
 * the <a href="#protocol">generated collections</a> will be placed under the given location
 * (i.e. be <a target="_blank" href="https://firebase.google.com/docs/firestore/data-model">
 * sub-collections^</a> of the given document).
 *
 * @author Dmytro Dashenkov
 * @see SubscriptionService
 */
public final class FirebaseSubscriptionMirror {

    private final SubscriptionService subscriptionService;

    @Nullable
    private final Firestore database;
    @Nullable
    private final DocumentReference location;

    private FirebaseSubscriptionMirror(Builder builder) {
        this.subscriptionService = builder.subscriptionService;
        this.database = builder.database;
        this.location = builder.location;
    }

    /**
     * Starts reflecting the given {@link Topic} into the Cloud Firestore.
     *
     * <p>After this method invocation, the underlying {@link Firestore} (eventually) starts
     * receiving the entity state updates for the given {@code topic}.
     *
     * <p>See <a href="#protocol">class level doc</a> for the description of the storage protocol.
     *
     * <p><i>Warning:</i> DO NOT use this method in multi-tenant bounded contexts. Use
     * {@link #reflect(Topic, TenantId)} instead.
     *
     * @param topic the topic to reflect
     * @see #reflect(Topic, TenantId)
     */
    public void reflect(Topic topic) {
        checkNotNull(topic);
        final Target target = topic.getTarget();
        final String type = target.getType();
        final CollectionReference collectionReference;
        if (database == null) {
            checkNotNull(location);
            collectionReference = location.collection(type);
        } else {
            collectionReference = database.collection(type);
        }
        final StreamObserver<SubscriptionUpdate> updateObserver =
                new SubscriptionToFirebaseAdapter(collectionReference);
        final StreamObserver<Subscription> subscriptionObserver =
                new SubscriptionObserver(subscriptionService, updateObserver);
        subscriptionService.subscribe(topic, subscriptionObserver);
    }

    /**
     * Starts reflecting the given {@link Topic} into the Cloud Firestore for the given tenant.
     *
     * <p>Only the entity state updates of the given tenant will be reflected into Firebase.
     *
     * @param topic    the topic to reflect
     * @param tenantId the tenant whose entity updates will be reflected
     * @see #reflect(Topic)
     * @see <a href="#multitenancy">multitenancy note</a>
     */
    public void reflect(Topic topic, TenantId tenantId) {
        new TenantAwareOperation(tenantId) {
            @Override
            public void run() {
                reflect(topic);
            }
        }.execute();
    }

    /**
     * Creates a new instance of {@code Builder} for {@code FirebaseSubscriptionMirror}
     * instances.
     *
     * @return new instance of {@code Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for the {@code FirebaseSubscriptionMirror} instances.
     */
    public static final class Builder {

        private SubscriptionService subscriptionService;

        @Nullable
        private Firestore database;
        @Nullable
        private DocumentReference location;

        // Prevent direct instantiation.
        private Builder() {}

        /**
         * Sets a {@link SubscriptionService} to use in the built mirror.
         *
         * <p>Note that the service is not a gRPC stub but the implementation itself. This allows
         * not to deploy a {@code SubscriptionService} at all, but just use it in memory.
         *
         * @param subscriptionService the {@link SubscriptionService} to get the subscription
         *                            updates from
         * @return self for method chaining
         */
        public Builder setSubscriptionService(SubscriptionService subscriptionService) {
            this.subscriptionService = checkNotNull(subscriptionService);
            return this;
        }

        /**
         * Sets the database to the built mirror.
         *
         * <p>Either this method or {@link #setDatabaseLocation(DocumentReference)} must be called,
         * but not both.
         *
         * <p>In case if this method is called, the generated by the mirror collections will be
         * located in the database root.
         *
         * @param database the {@link Firestore} to use
         * @return self for method chaining
         * @see #setDatabaseLocation(DocumentReference)
         */
        public Builder setDatabase(Firestore database) {
            this.database = checkNotNull(database);
            return this;
        }

        /**
         * Sets the custom database location to the built mirror.
         *
         * <p>Either this method or {@link #setDatabase(Firestore)} must be called, but not both.
         *
         * <p>In case if this method is called, the generated by the mirror collections will be
         * located under the specified document.
         *
         * @param location the {@link DocumentReference} to write the data into
         * @return self for method chaining
         * @see #setDatabase(Firestore)
         */
        public Builder setDatabaseLocation(DocumentReference location) {
            this.location = checkNotNull(location);
            return this;
        }

        /**
         * Creates a new instance of {@code FirebaseSubscriptionMirror}.
         *
         * @return new instance of {@code FirebaseSubscriptionMirror} with the given parameters
         */
        public FirebaseSubscriptionMirror build() {
            checkNotNull(subscriptionService);
            checkState((database != null) ^ (location != null),
                       "Either database or databaseLocation must be set, but not both.");
            return new FirebaseSubscriptionMirror(this);
        }
    }
}
