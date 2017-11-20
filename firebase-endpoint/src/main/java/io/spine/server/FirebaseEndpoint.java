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
import io.spine.client.Subscription;
import io.spine.client.SubscriptionUpdate;
import io.spine.client.Target;
import io.spine.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.of;
import static io.spine.protobuf.AnyPacker.unpack;

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

        private static final String BYTES_KEY = "bytes";

        private final CollectionReference target;

        private SubscriptionToFirebaseAdapter(CollectionReference target) {
            this.target = target;
        }

        @Override
        public void onNext(SubscriptionUpdate value) {
            final Collection<Any> payload = value.getUpdatesList();
            final WriteBatch batch = target.getFirestore().batch();
            for (Any msg : payload) {
                final Message message = unpack(msg);
                final Map<String, Object> data = of(BYTES_KEY, message.toByteArray());
                final DocumentReference targetDocument = route(target, msg);
                batch.set(targetDocument, data);
            }
            batch.commit();
        }

        private static DocumentReference route(CollectionReference collection, Any msg) {
            return collection.document();
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

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(FirebaseEndpoint.class);
    }
}
