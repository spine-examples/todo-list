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

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import io.spine.server.entity.Entity;
import io.spine.server.entity.EntityRecord;
import io.spine.server.entity.FieldMasks;
import io.spine.server.entity.storage.EntityQuery;
import io.spine.server.entity.storage.EntityRecordWithColumns;
import io.spine.server.storage.RecordStorage;
import io.spine.string.Stringifiers;
import io.spine.type.TypeUrl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Optional.fromNullable;
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
        final Topic topic = new RecordTopic(entityClass, id);
        final EntityRecord record = storage.<EntityRecord>readLast(topic).orElse(null);
        return fromNullable(record);
    }

    @Override
    protected Iterator<EntityRecord> readMultipleRecords(Iterable<I> ids) {
        final List<EntityRecord> records = newLinkedList();
        for (I id : ids) {
            records.add(read(id).orNull());
        }
        return records.iterator();
    }

    @Override
    protected Iterator<EntityRecord> readMultipleRecords(Iterable<I> ids, FieldMask fieldMask) {
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
        // TODO:2017-09-04:dmytro.dashenkov: TBD.
        return null;
    }

    @Override
    protected Iterator<EntityRecord> readAllRecords(EntityQuery<I> query, FieldMask fieldMask) {
        throw new UnsupportedOperationException("Method readAllRecords unimplemented!");
    }

    @Override
    protected void writeRecord(I id, EntityRecordWithColumns record) {
        final Topic topic = new RecordTopic(entityClass, id);
        storage.write(topic, id, record.getRecord());
    }

    @Override
    protected void writeRecords(Map<I, EntityRecordWithColumns> records) {

    }

    @Override
    public Iterator<I> index() {
        throw new UnsupportedOperationException("Method index unimplemented!");
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

    private static class RecordTopic implements Topic {

        private static final String PREFIX = "entityRecord_";

        private final String value;

        private RecordTopic(Class<?> type, Object id) {
            this.value = PREFIX + type.getName() + Stringifiers.toString(id);
        }

        @Override
        public String name() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RecordTopic that = (RecordTopic) o;
            return Objects.equal(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
