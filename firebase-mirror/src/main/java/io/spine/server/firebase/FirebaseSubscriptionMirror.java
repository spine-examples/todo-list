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
import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import io.spine.client.ActorRequestFactory;
import io.spine.client.Subscription;
import io.spine.client.SubscriptionUpdate;
import io.spine.client.Target;
import io.spine.client.Topic;
import io.spine.client.TopicFactory;
import io.spine.core.ActorContext;
import io.spine.core.TenantId;
import io.spine.core.UserId;
import io.spine.server.BoundedContext;
import io.spine.server.SubscriptionService;
import io.spine.server.event.EventSubscriber;
import io.spine.server.tenant.TenantAwareOperation;
import io.spine.type.TypeUrl;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;

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
 *     <li>{@linkplain #reflect(TypeUrl) Reflect} the selected entity types to Firestore.
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
 *         the collection is equal to the Protobuf type URL of the entity state. The underscore
 *         symbol ({@code "_"}) is used instead of slash ({@code "/"}).
 *     <li>The {@linkplain DocumentReference documents} in that collection represent the entity
 *         states. There is at most one document for each {@code Entity} instance.
 *     <li>When the entity is updated, the appropriate document is updated as well.
 * </ul>
 *
 * <p><b>Example:</b>
 * <p>Assume there is the
 * {@code CustomerAggregate extends Aggregate<CustomerId, Customer, CustomerVBuilder>} entity.
 *
 * <p>After a call to {@code mirror.reflect(TypeUrl.of(Customer.class))}, the entity state updates
 * will be written under {@code /type.example.org_example.customer.Customer/document-key}.
 * The {@code document-key} here is a unique key assigned to this instance of {@code Customer} by
 * the mirror. Note that the {@code document-key} is not a valid {@code CustomerAggregate} ID.
 * Do NOT depend any business logic on its value.
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
 * <p>When working with multitenant {@code BoundedContext}s, reflecting is started for all
 * the tenants in the specified bounded contexts.
 *
 * <p>The most convenient way to separate the records of different tenants is to use a custom
 * {@link Builder#setDocumentSelector(Function) document selector} function:
 * <pre>
 *     {@code
 *     final CollectionReference root = // Dedicate a collection for all the subscriptions.
 *     final Function<Topic, Document> documentSelector = topic -> {
 *         // Each tenant has own document.
 *         final String userId = topic.getContext().getTenant().getValue();
 *         return root.document(userId);
 *     };
 *     final FirebaseSubscriptionMirror mirror =
 *             FirebaseSubscriptionMirror.newBuilder()
 *                                       .setDocumentSelector(documentSelector)
 *                                       .addBoundedContext(myBoundedContext)
 *                                       .build();
 *     mirror.reflect(TypeUrl.of(MyEntity.class));
 *     }
 * </pre>
 *
 * <p>In this example, each tenant gets a dedicated document. Note the Firestore document
 * <a target="_blank" href="https://firebase.google.com/docs/firestore/quotas">name limitations^</a>
 * when generating custom documents.
 *
 * <p>Note that the {@code documentSelector} function accepts a {@link Topic} formed by
 * the {@code FirebaseSubscriptionMirror}. Some fields (e.g. Topic.context.actor) may be absent or
 * carry no meaningful for locating a destination document information.
 *
 * <p>It may be convenient to have a separate Firebase project for each tenant. This can be done
 * with the {@code documentSelector} function as well.
 *
 * <p>If the destination document is predefined, it can be specified directly:
 * <pre>
 *     {@code
 *     final DocumentReference document = // Dedicate a document for subscriptions.
 *     final FirebaseSubscriptionMirror mirror =
 *             FirebaseSubscriptionMirror.newBuilder()
 *                                       .setFirestoreDocument(document)
 *                                       .addBoundedContext(myBoundedContext)
 *                                       .build();
 *     mirror.reflect(TypeUrl.of(MyEntity.class));
 *     }
 * </pre>
 *
 * <p>When using a custom firestore document, all the <a href="#protocol">generated collections</a>
 * will be placed under the given document (i.e. be <a target="_blank"
 * href="https://firebase.google.com/docs/firestore/data-model#subcollections">sub-collections^</a>
 * of the given document).
 *
 * @author Dmytro Dashenkov
 * @see SubscriptionService
 */
public final class FirebaseSubscriptionMirror {

    private static final String ACTOR_ID = FirebaseSubscriptionMirror.class.getSimpleName();
    private static final UserId ACTOR = UserId.newBuilder()
                                              .setValue(ACTOR_ID)
                                              .build();
    private static final ActorRequestFactory requestFactory = ActorRequestFactory.newBuilder()
                                                                                 .setActor(ACTOR)
                                                                                 .build();
    private static final TopicFactory topics = requestFactory.topic();

    private final SubscriptionService subscriptionService;

    @Nullable
    private final Firestore firestore;
    @Nullable
    private final DocumentReference document;
    @Nullable
    private final Function<Topic, DocumentReference> documentSelector;

    private final Set<TenantId> knownTenants;
    private final Set<Topic> subscriptionTopics;

    private FirebaseSubscriptionMirror(Builder builder) {
        this.subscriptionTopics = newConcurrentHashSet();
        this.firestore = builder.firestore;
        this.subscriptionService = builder.subscriptionService;
        this.document = builder.document;
        this.documentSelector = builder.documentSelector;
        final Collection<BoundedContext> contexts = newHashSet(builder.boundedContexts);
        this.knownTenants = newConcurrentHashSet();
        final EventSubscriber tenantEventSubscriber = createTenantEventSubscriber();
        registerEventSubscriber(contexts, tenantEventSubscriber);
        this.knownTenants.addAll(getAllTenants(contexts));
    }

    private EventSubscriber createTenantEventSubscriber() {
        final Consumer<TenantId> tenantCallback = tenantId -> {
            subscriptionTopics.forEach(topic -> reflect(topic, tenantId));
            knownTenants.add(tenantId);
        };
        final EventSubscriber result = new TenantEventSubscriber(tenantCallback);
        return result;
    }

    private static Collection<TenantId> getAllTenants(Collection<BoundedContext> contexts) {
        final Collection<TenantId> result = contexts.stream()
                                                    .map(BoundedContext::getTenantIndex)
                                                    .flatMap(index -> index.getAll().stream())
                                                    .collect(toSet());
        return result;
    }

    private static void registerEventSubscriber(Collection<BoundedContext> contexts,
                                                EventSubscriber eventSubscriber) {
        contexts.stream()
                .map(BoundedContext::getIntegrationBus)
                .forEach(bus -> bus.register(eventSubscriber));
    }

    /**
     * Starts reflecting the updates of entity state of the given {@linkplain TypeUrl type} into
     * the Cloud Firestore.
     *
     * <p>After this method invocation, the entity state updates (eventually) start being written to
     * the underlying {@link Firestore}.
     *
     * <p>See <a href="#protocol">class level doc</a> for the description of the storage protocol.
     *
     * @param type the type of the entities to reflect
     */
    public void reflect(TypeUrl type) {
        checkNotNull(type);
        final Class<? extends Message> entityClass = type.getJavaClass();
        final Topic topic = topics.allOf(entityClass);
        subscriptionTopics.add(topic);
        final Consumer<TenantId> operation = reflectFunction(topic);
        knownTenants.forEach(operation);
    }

    /**
     * Partially applies {@link #reflect(Topic, TenantId)} method to the given {@code topic}.
     *
     * @param topic the topic to reflect
     * @return a function
     */
    private Consumer<TenantId> reflectFunction(Topic topic) {
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
        final Topic subscriptionTopic = forTenant(topic, tenantId);
        new TenantAwareOperation(tenantId) {
            @Override
            public void run() {
                doReflect(subscriptionTopic);
            }
        }.execute();
    }

    /**
     * Starts reflecting {@code topic} for the current tenant.
     *
     * @param topic the topic to reflect
     */
    private void doReflect(Topic topic) {
        final CollectionReference collectionReference = collection(topic);
        final StreamObserver<SubscriptionUpdate> updateObserver =
                new SubscriptionUpdateObserver(collectionReference);
        final StreamObserver<Subscription> subscriptionObserver =
                new SubscriptionObserver(subscriptionService, updateObserver);
        subscriptionService.subscribe(topic, subscriptionObserver);
    }

    private CollectionReference collection(Topic topic) {
        final String collectionKey = toKey(topic.getTarget());
        final CollectionReference result;
        if (documentSelector != null) {
            final DocumentReference document = documentSelector.apply(topic);
            result = document.collection(collectionKey);
        } else if (document != null) {
            result = document.collection(collectionKey);
        } else {
            checkNotNull(firestore);
            result = firestore.collection(collectionKey);
        }
        return result;
    }

    private static String toKey(Target target) {
        final TypeUrl typeUrl = TypeUrl.parse(target.getType());
        final String type = typeUrl.getPrefix() + '_' + typeUrl.getTypeName();
        return type;
    }

    private static Topic forTenant(Topic topic, TenantId tenant) {
        final ActorContext context = topic.getContext()
                                          .toBuilder()
                                          .setTenantId(tenant)
                                          .build();
        final Topic result = topic.toBuilder()
                                  .setContext(context)
                                  .build();
        return result;
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

        private Function<Topic, DocumentReference> documentSelector;

        // Prevent direct instantiation.
        private Builder() {}

        /**
         * Sets a {@link SubscriptionService} to use in the built mirror.
         *
         * <p>Note that the service is not a gRPC stub but the implementation itself. This allows
         * not to deploy a {@code SubscriptionService} at all, but just use it in memory.
         *
         * <p>If no {@code SubscriptionService} is specified, an instance is built with the passed
         * {@linkplain #addBoundedContext(BoundedContext) bounded contexts}.
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
         * Sets a custom function mapping a {@code Topic} to the {@code DocumentReference} which
         * should be the parent the collection of the state updates.
         *
         * @param documentSelector the topic to location function
         * @return self for method chaining
         */
        public Builder setDocumentSelector(Function<Topic, DocumentReference> documentSelector) {
            checkNotNull(documentSelector);
            this.documentSelector = documentSelector;
            return this;
        }

        /**
         * Adds a bounded context to this mirror.
         *
         * <p>At least one {@code BoundedContext} should be specified to build a mirror.
         *
         * @param boundedContext the {@link BoundedContext} to reflect entities from
         * @return self for method chaining
         */
        public Builder addBoundedContext(BoundedContext boundedContext) {
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
            checkState(!boundedContexts.isEmpty(),
                       "At least one BoundedContext should be specified.");
            checkLocationSettings();
            if (subscriptionService == null) {
                final SubscriptionService.Builder builder = SubscriptionService.newBuilder();
                for (BoundedContext context : boundedContexts) {
                    builder.add(context);
                }
                subscriptionService = builder.build();
            }
            return new FirebaseSubscriptionMirror(this);
        }

        private void checkLocationSettings() {
            checkState(
                    (firestore != null) ^ (document != null) ^ (documentSelector != null),
                    "Only one of Firestore or Firestore Document or a document selector function " +
                            "must be set."
            );
        }
    }
}
