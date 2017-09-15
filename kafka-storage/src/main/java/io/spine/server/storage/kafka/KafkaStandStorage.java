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

import com.google.common.base.Converter;
import com.google.common.base.Optional;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.server.entity.EntityRecord;
import io.spine.server.entity.storage.EntityRecordWithColumns;
import io.spine.server.stand.AggregateStateId;
import io.spine.server.stand.StandStorage;
import io.spine.string.Stringifier;
import io.spine.string.StringifierRegistry;
import io.spine.type.TypeUrl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Iterators.transform;

/**
 * A Kafka based implementation of {@link StandStorage}.
 *
 * @author Dmytro Dashenkov
 */
public class KafkaStandStorage extends StandStorage {

    private final KafkaRecordStorage<Message> delegate;

    protected KafkaStandStorage(KafkaRecordStorage<Message> delegate,
                                boolean multitenant) {
        super(multitenant);
        this.delegate = delegate;
    }

    @Override
    public Iterator<EntityRecord> readAllByType(TypeUrl type) {
        throw new UnsupportedOperationException("Method readAllByType is unimplemented!");
    }

    @Override
    public Iterator<EntityRecord> readAllByType(TypeUrl type, FieldMask fieldMask) {
        throw new UnsupportedOperationException("Method readAllByType is unimplemented!");
    }

    @Override
    public boolean delete(AggregateStateId id) {
        final Message key = checkNotNull(IdTransformer.direct().convert(id));
        return delegate.delete(key);
    }

    @SuppressWarnings("Guava") // Spine Java 7 API
    @Override
    protected Optional<EntityRecord> readRecord(AggregateStateId id) {
        final Message key = checkNotNull(IdTransformer.direct().convert(id));
        return delegate.readRecord(key);
    }

    @Override
    protected Iterator<EntityRecord> readMultipleRecords(Iterable<AggregateStateId> ids) {
        @SuppressWarnings("StaticPseudoFunctionalStyleMethod")
            // Better in several reasons:
            //  1. Executed lazily.
            //  2. Shorter notation.
            //  3. Potentially less GC overhead.
        final Iterable<Message> keys = transform(ids, IdTransformer.direct()::convert);
        return delegate.readMultipleRecords(keys);
    }

    @Override
    protected Iterator<EntityRecord> readMultipleRecords(Iterable<AggregateStateId> ids,
                                                         FieldMask fieldMask) {
        @SuppressWarnings("StaticPseudoFunctionalStyleMethod")
            // See readMultipleRecords(Iterable) for description.
        final Iterable<Message> keys = transform(ids, IdTransformer.direct()::convert);
        return delegate.readMultipleRecords(keys, fieldMask);
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
    protected void writeRecord(AggregateStateId id, EntityRecordWithColumns record) {
        final Message key = checkNotNull(IdTransformer.direct().convert(id));
        delegate.writeRecord(key, record);
    }

    @Override
    protected void writeRecords(Map<AggregateStateId, EntityRecordWithColumns> records) {
        final Map<Message, EntityRecordWithColumns> mapToWrite = new HashMap<>(records.size());
        final Function<AggregateStateId, Message> keyMapper = IdTransformer.direct()::convert;
        for (Map.Entry<AggregateStateId, EntityRecordWithColumns> entry : records.entrySet()) {
            final Message newKey = keyMapper.apply(entry.getKey());
            mapToWrite.put(newKey, entry.getValue());
        }
        delegate.writeRecords(mapToWrite);
    }

    @Override
    public Iterator<AggregateStateId> index() {
        return transform(delegate.index(), IdTransformer.reverseDirection()::convert);
    }

    private static class IdTransformer extends Converter<AggregateStateId, Message> {

        private static final Stringifier<AggregateStateId> stringifier;

        static {
            @SuppressWarnings("Guava") // Spine Java 7 API
            final Optional<Stringifier<AggregateStateId>> optStringifier =
                    StringifierRegistry.getInstance()
                                       .get(AggregateStateId.class);
            if (!optStringifier.isPresent()) {
                throw new IllegalStateException("AggregateStateId stringifier is absent.");
            }
            stringifier = optStringifier.get();
        }

        private IdTransformer() {
            // Prevent direct instantiation.
        }

        @Override
        protected Message doForward(AggregateStateId aggregateStateId) {
            final String stringId = stringifier.convert(aggregateStateId);
            checkNotNull(stringId);
            final Message messageId = StringValue.newBuilder()
                                                 .setValue(stringId)
                                                 .build();
            return messageId;
        }

        @Override
        protected AggregateStateId doBackward(Message message) {
            final StringValue stringValueId = (StringValue) message;
            final String stringId = stringValueId.getValue();
            final AggregateStateId result = stringifier.reverse().convert(stringId);
            return result;
        }

        private static Converter<AggregateStateId, Message> direct() {
            return Singleton.INSTANCE.value;
        }

        private static Converter<Message, AggregateStateId> reverseDirection() {
            return Singleton.INSTANCE.value.reverse();
        }

        private enum Singleton {
            INSTANCE;
            @SuppressWarnings("NonSerializableFieldInSerializableClass")
            private final IdTransformer value = new IdTransformer();
        }
    }
}
