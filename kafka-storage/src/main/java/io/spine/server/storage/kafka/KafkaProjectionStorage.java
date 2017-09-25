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

package io.spine.server.storage.kafka;

import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import io.spine.server.entity.Entity;
import io.spine.server.entity.EntityRecord;
import io.spine.server.projection.ProjectionStorage;
import io.spine.server.storage.RecordStorage;

import javax.annotation.Nullable;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Kafka based implementation of {@link ProjectionStorage}.
 *
 * @author Dmytro Dashenkov
 */
public class KafkaProjectionStorage<I> extends ProjectionStorage<I> {

    private final KafkaRecordStorage<I> delegate;

    protected KafkaProjectionStorage(KafkaRecordStorage<I> delegate,
                                     boolean multitenant) {
        super(multitenant);
        this.delegate = delegate;
    }

    @Override
    protected void writeLastHandledEventTime(Timestamp time) {
        checkNotClosed();
        checkNotNull(time);
        final KafkaWrapper storage = delegate.getKafkaStorage();
        final Class<? extends Entity> cls = delegate.getEntityClass();
        final Topic topic = Topic.forLastHandledEventTime(cls);
        storage.write(topic, topic.getName(), time);
    }

    @Nullable
    @Override
    protected Timestamp readLastHandledEventTime() {
        checkNotClosed();
        final KafkaWrapper storage = delegate.getKafkaStorage();
        final Class<? extends Entity> cls = delegate.getEntityClass();
        final Topic topic = Topic.forLastHandledEventTime(cls);
        final Timestamp timestamp = storage.<Timestamp>readLast(topic).orElse(null);
        return timestamp;
    }

    @Override
    protected RecordStorage<I> recordStorage() {
        return delegate;
    }

    @Override
    public boolean delete(I id) {
        return delegate.delete(id);
    }

    @Override
    protected Iterator<EntityRecord> readMultipleRecords(Iterable<I> ids) {
        return delegate.readMultipleRecords(ids);
    }

    @Override
    protected Iterator<EntityRecord> readMultipleRecords(Iterable<I> ids, FieldMask fieldMask) {
        return delegate.readMultipleRecords(ids, fieldMask);
    }

    @Override
    protected Iterator<EntityRecord> readAllRecords() {
        return delegate.readAllRecords();
    }

    @Override
    protected Iterator<EntityRecord> readAllRecords(FieldMask fieldMask) {
        return delegate.readAllRecords(fieldMask);
    }

    @Override
    public Iterator<I> index() {
        return delegate.index();
    }
}
