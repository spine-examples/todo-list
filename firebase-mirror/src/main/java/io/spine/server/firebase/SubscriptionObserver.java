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

import io.grpc.stub.StreamObserver;
import io.spine.client.Subscription;
import io.spine.client.SubscriptionUpdate;
import io.spine.server.SubscriptionService;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * An implementation of {@link StreamObserver} of a {@link Subscription} stream which
 * {@linkplain SubscriptionService#activate activates} all the received {@link Subscription}s
 * with the specified {@code SubscriptionService}.
 *
 * <p>After activation, the given {@code dataObserver} starts receiving the Entity state updates.
 *
 * <p>The implementation throws a {@link RuntimeException} upon any
 * {@linkplain StreamObserver#onError error} and handles the
 * {@linkplain StreamObserver#onCompleted() successful completion} silently.
 *
 * @author Dmytro Dashenkov
 */
final class SubscriptionObserver implements StreamObserver<Subscription> {

    private final SubscriptionService subscriptionService;
    private final StreamObserver<SubscriptionUpdate> dataObserver;

    SubscriptionObserver(SubscriptionService service,
                         StreamObserver<SubscriptionUpdate> dataObserver) {
        this.subscriptionService = checkNotNull(service);
        this.dataObserver = checkNotNull(dataObserver);
    }

    @Override
    public void onNext(Subscription subscription) {
        checkNotNull(subscription);
        subscriptionService.activate(subscription, dataObserver);
    }

    @Override
    public void onError(Throwable t) {
        throw illegalStateWithCauseOf(t);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The {@code SubscriptionObserver} implementation performs no operation since no additional
     * terminating steps are required.
     */
    @Override
    public void onCompleted() {}
}
