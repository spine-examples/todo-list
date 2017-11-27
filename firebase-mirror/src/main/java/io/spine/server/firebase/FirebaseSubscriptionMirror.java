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
import io.spine.server.BoundedContext;
import io.spine.server.SubscriptionService;
import io.spine.server.tenant.TenantAwareOperation;
import io.spine.server.tenant.TenantIndex;
import io.spine.server.tenant.TenantIndex.TenantIdConsumer;
import io.spine.type.TypeUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static io.spine.validate.Validate.isDefault;
import static java.util.stream.Collectors.toSet;

/**
 * A client of {@link SubscriptionService} that mirrors the received messages to Firebase.
 *
 * <h2>Usage</h2>
 *
 * <p>{@code FirebaseSubscriptionMirror} reflects the received messages into the specified
 * <a target="_blank" href="https://firebase.google.com/docs/firestore/">Cloud Firestore^</a>
 * firestore.
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
 * possible, use a custom firestore document instead:
 * <pre>
 *     {@code
 *     final Topic topic = // retrieve desired topic.
 *     final TenantId tenant = // get the tenant ID e.g. from ActorContext.
 *     final DocumentReference document = // dedicate a document for this tenant
 *     final FirebaseSubscriptionMirror mirror =
 *             FirebaseSubscriptionMirror.newBuilder()
 *                                       .setSubscriptionService(service)
 *                                       .setFirestoreDocument(document)
 *                                       .build();
 *     mirror.reflect(topic, tenant);
 *     }
 * </pre>
 *
 * <p>When specifying a custom firestore document, all
 * the <a href="#protocol">generated collections</a> will be placed under the given document
 * (i.e. be <a target="_blank" href="https://firebase.google.com/docs/firestore/data-model">
 * sub-collections^</a> of the given document).
 *
 * @author Dmytro Dashenkov
 * @see SubscriptionService
 */
public final class FirebaseSubscriptionMirror {

    private final SubscriptionService subscriptionService;

    @Nullable
    private final Firestore firestore;
    @Nullable
    private final DocumentReference document;

    private final Collection<TenantIndex> tenantStore;

    private FirebaseSubscriptionMirror(Builder builder) {
        this.subscriptionService = builder.subscriptionService;
        this.firestore = builder.firestore;
        this.document = builder.document;
        this.tenantStore = builder.getTenantIndexes();
    }

    /**
     * Starts reflecting the given {@link Topic} into the Cloud Firestore.
     *
     * <p>After this method invocation, the underlying {@link Firestore} (eventually) starts
     * receiving the entity state updates for the given {@code topic}.
     *
     * <p>See <a href="#protocol">class level doc</a> for the description of the storage protocol.
     *
     * @param topic the topic to reflect
     * @see #reflect(Topic, TenantId)
     */
    public void reflect(Topic topic) {
        checkNotNull(topic);
        final TenantId topicTenant = topic.getContext()
                                          .getTenantId();
        final TenantIdConsumer operation = reflectFunction(topic);
        final String targetType = topic.getTarget().getType();
        if (isDefault(topicTenant)) {
            log().info("Starting reflecting type {} for all tenants.", targetType);
            tenantStore.forEach(tenantIndex -> tenantIndex.forEachTenant(operation));
        } else {
            log().info("Starting reflecting type {} for tenant {}.", targetType, topicTenant);
            operation.accept(topicTenant);
        }
    }

    /**
     * Partially applies {@link #reflect(Topic, TenantId)} method to the given {@code topic}.
     *
     * @param topic the topic to reflect
     * @return a function
     */
    private TenantIdConsumer reflectFunction(Topic topic) {
        return tenantId -> reflect(topic, tenantId);
    }

    /**
     * Starts reflecting the given {@link Topic} into the Cloud Firestore for the given tenant.
     *
     * <p>Only the entity state updates of the given tenant will be reflected into Firebase.
     *
     * @param topic    the topic to reflect
     * @param tenantId the tenant whose entity updates will be reflected
     */
    private void reflect(Topic topic, TenantId tenantId) {
        new TenantAwareOperation(tenantId) {
            @Override
            public void run() {
                doReflect(topic);
            }
        }.execute();
    }

    /**
     * Starts reflecting {@code topic} for the current tenant.
     *
     * @param topic the topic to reflect
     */
    private void doReflect(Topic topic) {
        final Target target = topic.getTarget();
        final String key = toKey(target);
        final CollectionReference collectionReference;
        if (firestore == null) {
            checkNotNull(document);
            collectionReference = document.collection(key);
        } else {
            collectionReference = firestore.collection(key);
        }
        final StreamObserver<SubscriptionUpdate> updateObserver =
                new SubscriptionToFirebaseAdapter(collectionReference);
        final StreamObserver<Subscription> subscriptionObserver =
                new SubscriptionObserver(subscriptionService, updateObserver);
        subscriptionService.subscribe(topic, subscriptionObserver);
    }

    private static String toKey(Target target) {
        final TypeUrl typeUrl = TypeUrl.parse(target.getType());
        final String type = typeUrl.getPrefix() + '_' + typeUrl.getTypeName();
        return type;
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

        private final Set<BoundedContext> boundedContexts = newHashSet();

        private SubscriptionService subscriptionService;

        @Nullable
        private Firestore firestore;
        @Nullable
        private DocumentReference document;

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
         * Sets the firestore to the built mirror.
         *
         * <p>Either this method or {@link #setFirestoreDocument(DocumentReference)} must be called,
         * but not both.
         *
         * <p>In case if this method is called, the generated by the mirror collections will be
         * located in the firestore root.
         *
         * @param database the {@link Firestore} to use
         * @return self for method chaining
         * @see #setFirestoreDocument(DocumentReference)
         */
        public Builder setFirestore(Firestore database) {
            this.firestore = checkNotNull(database);
            return this;
        }

        /**
         * Sets the custom firestore document to the built mirror.
         *
         * <p>Either this method or {@link #setFirestore(Firestore)} must be called, but not both.
         *
         * <p>In case if this method is called, the generated by the mirror collections will be
         * located under the specified document.
         *
         * @param document the {@link DocumentReference} to write the data into
         * @return self for method chaining
         * @see #setFirestore(Firestore)
         */
        public Builder setFirestoreDocument(DocumentReference document) {
            this.document = checkNotNull(document);
            return this;
        }

        /**
         * Adds the {@code boundedContext} to this mirror.
         *
         * <p>At least one BoundedContext should be specified to build a mirror.
         *
         * @param boundedContext the {@link BoundedContext} to reflect entities from
         * @return self for method chaining
         */
        public Builder addBounedContext(BoundedContext boundedContext) {
            checkNotNull(boundedContext);
            this.boundedContexts.add(boundedContext);
            return this;
        }

        /**
         * Creates a new instance of {@code FirebaseSubscriptionMirror}.
         *
         * @return new instance of {@code FirebaseSubscriptionMirror} with the given parameters
         */
        public FirebaseSubscriptionMirror build() {
            checkNotNull(subscriptionService);
            checkState((firestore != null) ^ (document != null),
                       "Either Firestore or Firestore Document must be set, but not both.");
            checkState(!boundedContexts.isEmpty(),
                       "At least one BoundedContext should be specified.");
            return new FirebaseSubscriptionMirror(this);
        }

        private Collection<TenantIndex> getTenantIndexes() {
            final Set<TenantIndex> result = boundedContexts.stream()
                                                           .map(BoundedContext::getTenantIndex)
                                                           .collect(toSet());
            return result;
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(FirebaseSubscriptionMirror.class);
    }
}
