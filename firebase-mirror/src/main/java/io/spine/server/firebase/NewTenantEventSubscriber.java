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

import io.spine.core.Subscribe;
import io.spine.core.TenantId;
import io.spine.server.event.EventSubscriber;
import io.spine.server.tenant.TenantAdded;

import java.util.Set;
import java.util.function.Consumer;

import static com.google.common.collect.Sets.newConcurrentHashSet;

/**
 * An {@link EventSubscriber} for the {@link TenantAdded} events.
 *
 * <p>Triggers a callback on new tenant.
 *
 * <p>Not all the events cause the callback invocation, but only those that introduce a new
 * (previously unknown) tenant ID.
 *
 * @author Dmytro Dashenkov
 */
final class NewTenantEventSubscriber extends EventSubscriber {

    private final Set<TenantId> acknowledgedTenants = newConcurrentHashSet();
    private final Consumer<TenantId> tenantCallback;

    NewTenantEventSubscriber(Consumer<TenantId> tenantCallback) {
        super();
        this.tenantCallback = tenantCallback;
    }

    @Subscribe(external = true)
    public void on(TenantAdded event) {
        final TenantId tenantId = event.getId();
        log().info("Received TenantAdded event. New tenant ID is: {}", tenantId);
        if (!acknowledgedTenants.contains(tenantId)) {
            acknowledgedTenants.add(tenantId);
            tenantCallback.accept(tenantId);
        }
    }
}
