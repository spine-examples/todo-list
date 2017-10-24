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
import com.google.protobuf.UInt32Value;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.AggregateEventRecord;
import io.spine.server.aggregate.AggregateReadRequest;
import io.spine.server.aggregate.AggregateStorage;
import io.spine.server.entity.LifecycleFlags;

import java.util.Iterator;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.server.aggregate.AggregateEventRecord.KindCase.EVENT;
import static io.spine.server.aggregate.AggregateEventRecord.KindCase.SNAPSHOT;

/**
 * A Kafka based implementation of {@link AggregateStorage}.
 *
 * @author Dmytro Dashenkov
 */
public class KafkaAggregateStorage<I> extends AggregateStorage<I> {

    private static final String DEFAULT_RECORD_ERROR_TEMPLATE =
            "Passed AggregateEventRecord is neither an EVENT nor a SNAPSHOT. Value is: %s";

    private final Class<? extends Aggregate<I, ?, ?>> aggregateClass;
    private final KafkaWrapper storage;

    protected KafkaAggregateStorage(Class<? extends Aggregate<I, ?, ?>> cls,
                                    KafkaWrapper storage,
                                    boolean multitenant) {
        super(multitenant);
        this.aggregateClass = cls;
        this.storage = storage;
    }

    @Override
    protected int readEventCountAfterLastSnapshot(I id) {
        checkNotClosed();
        checkNotNull(id);
        final Topic topic = Topic.forEventCountAfterSnapshot(aggregateClass);
        final int eventCount = storage.<UInt32Value>read(topic, id)
                                      .map(UInt32Value::getValue)
                                      .orElse(0);
        return eventCount;
    }

    @Override
    protected void writeEventCountAfterLastSnapshot(I id, int eventCount) {
        checkNotClosed();
        checkNotNull(id);
        final Topic topic = Topic.forEventCountAfterSnapshot(aggregateClass);
        final UInt32Value msg = UInt32Value.newBuilder()
                                           .setValue(eventCount)
                                           .build();
        storage.write(topic, id, msg);
    }

    @Override
    protected void writeRecord(I id, AggregateEventRecord record) {
        checkNotClosed();
        checkNotNull(id);
        checkNotNull(record);
        final Topic topic = Topic.forAggregateRecord(aggregateClass);
        Object key = null;
        if (record.getKindCase() == EVENT) {
            key = record.getEvent().getId();
        } else if (record.getKindCase() == SNAPSHOT) {
            key = topic.getName();
        }
        checkArgument(key != null,
                      DEFAULT_RECORD_ERROR_TEMPLATE,
                      record);
        storage.write(topic, key, record);
    }

    @Override
    protected Iterator<AggregateEventRecord> historyBackward(AggregateReadRequest<I> request) {
        checkNotClosed();
        checkNotNull(request);
        // TODO:2017-10-24:dmytro.dashenkov: Fix.
        final Topic topic = Topic.forAggregateRecord(aggregateClass);
        return storage.read(topic);
    }

    @SuppressWarnings("Guava") // Spine API for Java 7.
    @Override
    public Optional<LifecycleFlags> readLifecycleFlags(I id) {
        checkNotClosed();
        checkNotNull(id);
        final Topic topic = Topic.forLifecycleFlags(aggregateClass);
        final LifecycleFlags result = storage.<LifecycleFlags>read(topic, id)
                                             .orElse(null);
        return fromNullable(result);
    }

    @Override
    public void writeLifecycleFlags(I id, LifecycleFlags flags) {
        checkNotClosed();
        checkNotNull(id);
        final Topic topic = Topic.forLifecycleFlags(aggregateClass);
        storage.write(topic, id, flags);
    }

    @Override
    public Iterator<I> index() {
        throw new UnsupportedOperationException("Method index is unimplemented!");
    }
}
