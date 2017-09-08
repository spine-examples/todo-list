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

import com.google.common.base.Optional;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import io.spine.server.entity.Entity;
import io.spine.server.entity.EntityRecord;
import io.spine.server.entity.FieldMasks;
import io.spine.server.entity.LifecycleFlags;
import io.spine.server.entity.storage.EntityQuery;
import io.spine.server.entity.storage.EntityRecordWithColumns;
import io.spine.server.storage.RecordStorage;
import io.spine.type.TypeUrl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterators.filter;
import static com.google.common.collect.Iterators.transform;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.protobuf.AnyPacker.pack;

/**
 * @author Dmytro Dashenkov
 */
public class KafkaRecordStorage<I> extends RecordStorage<I> {

    private final Class<? extends Entity<I, ?>> entityClass;
    private final KafkaWrapper storage;

    public KafkaRecordStorage(Class<? extends Entity<I, ?>> entityClass,
                              KafkaWrapper storage,
                              boolean multitenant) {
        super(multitenant);
        this.storage = storage;
        this.entityClass = entityClass;
    }

    @Override
    public boolean delete(I id) {
        throw new UnsupportedOperationException("Method delete unimplemented!");
    }

    @SuppressWarnings("Guava") // Spine Java 7 API
    @Override
    protected Optional<EntityRecord> readRecord(I id) {
        final Topic topic = Topic.forRecord(entityClass, id);
        final EntityRecord record = storage.<EntityRecord>read(topic, id).orElse(null);
        return fromNullable(record);
    }

    @Override
    protected Iterator<EntityRecord> readMultipleRecords(Iterable<I> ids) {
        // TODO:2017-09-04:dmytro.dashenkov: Optimize.
        final List<EntityRecord> records = newLinkedList();
        for (I id : ids) {
            records.add(read(id).orNull());
        }
        return records.iterator();
    }

    @Override
    protected Iterator<EntityRecord> readMultipleRecords(Iterable<I> ids, FieldMask fieldMask) {
        // TODO:2017-09-04:dmytro.dashenkov: Optimize.
        final List<EntityRecord> records = newLinkedList();
        for (I id : ids) {
            final EntityRecord record = read(id).transform(maskState(fieldMask)::apply)
                                                .orNull();
            records.add(record);
        }
        return records.iterator();
    }

    @Override
    protected Iterator<EntityRecord> readAllRecords() {
        return readAllRecords(FieldMask.getDefaultInstance());
    }

    @Override
    protected Iterator<EntityRecord> readAllRecords(FieldMask fieldMask) {
        final Iterator<EntityRecord> result = storage.readAll(entityClass);
        final Iterator<EntityRecord> filtered = filter(result, record -> {
            checkNotNull(record);
            final LifecycleFlags flags = record.getLifecycleFlags();
            return !(flags.getArchived() && flags.getDeleted());
        });
        final Iterator<EntityRecord> maskedResult = transform(filtered,
                                                              maskState(fieldMask)::apply);
        return maskedResult;
    }

    @Override
    protected Iterator<EntityRecord> readAllRecords(EntityQuery<I> query, FieldMask fieldMask) {
        throw new UnsupportedOperationException("Method readAllRecords unimplemented!");
    }

    @Override
    protected void writeRecord(I id, EntityRecordWithColumns record) {
        final Topic topic = Topic.forRecord(entityClass, id);
        storage.write(entityClass, topic, id, record.getRecord());
    }

    @Override
    protected void writeRecords(Map<I, EntityRecordWithColumns> records) {
        // TODO:2017-09-04:dmytro.dashenkov: Optimize.
        records.forEach(this::writeRecord);
    }

    @Override
    public Iterator<I> index() {
        throw new UnsupportedOperationException("Method index unimplemented!");
    }

    KafkaWrapper getKafkaStorage() {
        return storage;
    }

    Class<? extends Entity> getEntityClass() {
        return entityClass;
    }

    private static Function<EntityRecord, EntityRecord> maskState(FieldMask fieldMask) {
        return entityRecord -> {
            final Message state = entityRecord.getState();
            final TypeUrl typeUrl = TypeUrl.from(state.getDescriptorForType());
            final Message masked = FieldMasks.applyMask(fieldMask, state, typeUrl);
            final EntityRecord result = entityRecord.toBuilder()
                                                    .setState(pack(masked))
                                                    .build();
            return result;
        };
    }
}
