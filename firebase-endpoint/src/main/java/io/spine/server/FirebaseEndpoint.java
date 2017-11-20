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
import com.google.cloud.firestore.WriteBatch;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import io.spine.client.EntityStateUpdate;
import io.spine.client.Subscription;
import io.spine.client.SubscriptionUpdate;
import io.spine.client.Target;
import io.spine.client.Topic;
import io.spine.string.Stringifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.of;
import static io.spine.protobuf.AnyPacker.unpack;
import static java.lang.String.format;

/**
 * @author Dmytro Dashenkov
 */
public final class FirebaseEndpoint {

    private final Firestore database;
    private final SubscriptionService subscriptionService;

    private FirebaseEndpoint(Builder builder) {
        this.database = builder.database;
        this.subscriptionService = builder.subscriptionService;
    }

    public void subscribe(Topic topic) {
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
     * Creates a new instance of {@code Builder} for {@code FirebaseEndpoint} instances.
     *
     * @return new instance of {@code Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for the {@code FirebaseEndpoint} instances.
     */
    public static final class Builder {

        private SubscriptionService subscriptionService;
        private Firestore database;

        private Builder() {
            // Prevent direct instantiation.
        }

        public Builder setSubscriptionService(SubscriptionService subscriptionService) {
            this.subscriptionService = subscriptionService;
            return this;
        }

        public Builder setDatabase(Firestore database) {
            this.database = database;
            return this;
        }

        /**
         * Creates a new instance of {@code KafkaAggregateMessageDispatcher}.
         *
         * @return new instance of {@code KafkaAggregateMessageDispatcher} with the given
         * parameters
         */
        public FirebaseEndpoint build() {
            return new FirebaseEndpoint(this);
        }
    }

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
            throw new RuntimeException(t);
        }

        @Override
        public void onCompleted() {
            // NoOp
        }
    }

    private static class SubscriptionToFirebaseAdapter
            implements StreamObserver<SubscriptionUpdate> {

        private static final Pattern INVALID_KEY_CHARS = Pattern.compile("[^\\w\\d]");

        private static final String BYTES_KEY = "bytes";

        private final CollectionReference target;

        private SubscriptionToFirebaseAdapter(CollectionReference target) {
            this.target = target;
        }

        @Override
        public void onNext(SubscriptionUpdate value) {
            final Collection<EntityStateUpdate> payload = value.getEntityStateUpdatesList();
            final WriteBatch batch = target.getFirestore().batch();
            for (EntityStateUpdate update : payload) {
                final Any updateState = update.getState();
                final Message message = unpack(updateState);
                final Map<String, Object> data = of(BYTES_KEY, message.toByteArray());
                final Any updateId = update.getId();
                final Message entityId = unpack(updateId);
                final DocumentReference targetDocument = route(updateId);
                log().info("Writing state update of type {} (id: {}) into Firestore location {}.",
                           updateState.getTypeUrl(), entityId, targetDocument.getPath());
                batch.set(targetDocument, data);
            }
            batch.commit();
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

        private DocumentReference route(Message entityId) {
            final String id = Stringifiers.toString(entityId);
            final String documentKey = escapeKey(id);
            final DocumentReference result = target.document(documentKey);
            return result;
        }

        private static String escapeKey(String dirtyKey) {
            final String trimmedKey = trimUnderscore(dirtyKey);
            final String result = INVALID_KEY_CHARS.matcher(trimmedKey)
                                                   .replaceAll("");
            return result;
        }

        @SuppressWarnings("BreakStatement") // Natural in this case.
        private static String trimUnderscore(String key) {
            int underscoreCounter = 0;
            for (char character : key.toCharArray()) {
                if (character == '_') {
                    underscoreCounter++;
                } else {
                    break;
                }
            }
            final String result = underscoreCounter > 0
                                  ? key.substring(underscoreCounter)
                                  : key;
            return result;
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(FirebaseEndpoint.class);
    }
}
