/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.server.tasks;

import io.spine.server.ContextSpec;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.AggregateStorage;
import io.spine.server.delivery.InboxStorage;
import io.spine.server.entity.Entity;
import io.spine.server.projection.Projection;
import io.spine.server.projection.ProjectionStorage;
import io.spine.server.storage.RecordStorage;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;

/**
 * An in-memory storage factory for the To-Do List application.
 *
 * <p>Such factory is sufficient for small applications run locally, but should not be used in a
 * production environment.
 *
 * <p>For the production scenarios consider using
 * <a href="https://github.com/SpineEventEngine/gcloud-java/">Spine library for
 * Google Cloud Datastore</a>.
 */
public final class TaskStorageFactory implements StorageFactory {

    private final InMemoryStorageFactory delegate = InMemoryStorageFactory.newInstance();

    @Override
    public <I> AggregateStorage<I>
    createAggregateStorage(ContextSpec ctx, Class<? extends Aggregate<I, ?, ?>> aggregateCls) {
        return delegate.createAggregateStorage(ctx, aggregateCls);
    }

    @Override
    public <I> RecordStorage<I>
    createRecordStorage(ContextSpec ctx, Class<? extends Entity<I, ?>> entityCls) {
        return delegate.createRecordStorage(ctx, entityCls);
    }

    @Override
    public <I> ProjectionStorage<I>
    createProjectionStorage(ContextSpec ctx, Class<? extends Projection<I, ?, ?>> projectionCls) {
        return delegate.createProjectionStorage(ctx, projectionCls);
    }

    @Override
    public InboxStorage createInboxStorage(boolean multitenant) {
        return delegate.createInboxStorage(multitenant);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
