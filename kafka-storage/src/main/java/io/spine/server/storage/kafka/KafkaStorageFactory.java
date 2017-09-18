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

import com.google.protobuf.Message;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.AggregateStorage;
import io.spine.server.entity.AbstractEntity;
import io.spine.server.entity.Entity;
import io.spine.server.entity.EntityRecord;
import io.spine.server.entity.storage.ColumnTypeRegistry;
import io.spine.server.projection.Projection;
import io.spine.server.projection.ProjectionStorage;
import io.spine.server.stand.StandStorage;
import io.spine.server.storage.StorageFactory;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.time.Duration;
import java.util.Properties;

import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;

/**
 * The Apache Kafka based {@link StorageFactory} implementation.
 *
 * <p>The storage does not support multitenancy.
 *
 * @author Dmytro Dashenkov
 */
public class KafkaStorageFactory implements StorageFactory {

    private final KafkaWrapper storage;

    // TODO:2017-09-04:dmytro.dashenkov: Replace with builder.
    public KafkaStorageFactory(Properties producerConfig,
                               Properties consumerConfig,
                               Consistency consistencyLevel,
                               Duration maxPollAwait) {
        this.storage = createStorage(producerConfig,
                                     consumerConfig,
                                     consistencyLevel,
                                     maxPollAwait);
    }

    private static KafkaWrapper createStorage(Properties producerConfig,
                                              Properties consumerConfig,
                                              Consistency consistencyLevel,
                                              Duration maxPollAwait) {
        final KafkaProducer<Message, Message> producer = new KafkaProducer<>(producerConfig,
                                                                             serializer(),
                                                                             serializer());
        final KafkaConsumer<Message, Message> consumer = new KafkaConsumer<>(consumerConfig,
                                                                             deserializer(),
                                                                             deserializer());
        final KafkaWrapper kafkaWrapper = new KafkaWrapper(producer,
                                                           consumer,
                                                           consistencyLevel,
                                                           maxPollAwait);
        return kafkaWrapper;
    }

    @Override
    public boolean isMultitenant() {
        return false;
    }

    @Override
    public ColumnTypeRegistry getTypeRegistry() {
        return ColumnTypeRegistry.newBuilder()
                                 .build();
    }

    @Override
    public StandStorage createStandStorage() {
        final KafkaRecordStorage<Message> delegate = createRecordStorage(StandStorageRecord.class);
        return new KafkaStandStorage(delegate, isMultitenant());
    }

    @Override
    public <I> AggregateStorage<I>
    createAggregateStorage(Class<? extends Aggregate<I, ?, ?>> aggregateClass) {
        return new KafkaAggregateStorage<>(aggregateClass, storage, isMultitenant());
    }

    @Override
    public <I> KafkaRecordStorage<I>
    createRecordStorage(Class<? extends Entity<I, ?>> entityClass) {
        return new KafkaRecordStorage<>(entityClass, storage, isMultitenant());
    }

    @Override
    public <I> ProjectionStorage<I>
    createProjectionStorage(Class<? extends Projection<I, ?, ?>> projectionClass) {
        final KafkaRecordStorage<I> recordStorage = createRecordStorage(projectionClass);
        return new KafkaProjectionStorage<>(recordStorage, isMultitenant());
    }

    @Override
    public StorageFactory toSingleTenant() {
        return this;
    }

    @Override
    public void close() {
        // NOP
    }

    /**
     * The {@link Entity} type stored in {@link KafkaStandStorage}.
     */
    private static class StandStorageRecord extends AbstractEntity<Message, EntityRecord> {

        protected StandStorageRecord(Message id) {
            super(id);
        }
    }
}
