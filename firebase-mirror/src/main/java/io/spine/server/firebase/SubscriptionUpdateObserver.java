/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
import io.grpc.stub.StreamObserver;
import io.spine.client.EntityStateUpdate;
import io.spine.client.SubscriptionUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * An implementation of {@link StreamObserver} publishing the received {@link SubscriptionUpdate}s
 * to the given {@link CollectionReference Cloud Firestore location}.
 *
 * <p>The implementation logs a message upon either
 * {@linkplain StreamObserver#onCompleted() successful} or
 * {@linkplain StreamObserver#onError faulty} stream completion.
 *
 * @author Dmytro Dashenkov
 * @see FirestoreSubscriptionPublisher
 */
final class SubscriptionUpdateObserver implements StreamObserver<SubscriptionUpdate> {

    private final String path;
    private final FirestoreSubscriptionPublisher publisher;

    SubscriptionUpdateObserver(CollectionReference target) {
        checkNotNull(target);
        this.path = target.getPath();
        this.publisher = new FirestoreSubscriptionPublisher(target);
    }

    @Override
    public void onNext(SubscriptionUpdate value) {
        final Collection<EntityStateUpdate> payload = value.getEntityStateUpdatesList();
        publisher.publish(payload);
    }

    @Override
    public void onError(Throwable error) {
        log().error(format("Subscription with target `%s` has been completed with an error.", path),
                    error);
    }

    @Override
    public void onCompleted() {
        log().info("Subscription with target `{}` has been completed.", path);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(SubscriptionUpdateObserver.class);
    }
}
