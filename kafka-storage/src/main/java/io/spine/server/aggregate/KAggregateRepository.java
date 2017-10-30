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

package io.spine.server.aggregate;

import com.google.common.base.Optional;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.core.Event;
import io.spine.core.EventEnvelope;
import io.spine.server.entity.EntityClass;
import io.spine.server.storage.kafka.KafkaWrapper;
import io.spine.server.storage.kafka.Topic;
import io.spine.string.Stringifiers;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static org.apache.kafka.common.serialization.Serdes.serdeFrom;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;

/**
 * An {@link AggregateRepository} which applies the dispatched events through a Kafka Streams
 * topology.
 *
 * <p>Each subclass of {@code KAggregateRepository} starts a Kafka Streams processor on
 * the constructor invocation.
 *
 * <p>To enable the Kafka-based Aggregate loading, extend {@code KAggregateRepository} instead of
 * extending {@code AggregateRepository} directly.
 *
 * <p>Unlike the base implementation of {@link AggregateRepository}, {@code KAggregateRepository}
 * never replays the existing events upon an {@link Aggregate} loading. Instead, it creates
 * a {@link Snapshot} upon each event dispatched to a given instance of {@code Aggregate} and
 * stores it in the {@linkplain org.apache.kafka.streams.processor.StateStore Kafka Streams
 * topology state store). The {@code Aggregate} instances are loaded by reading the
 * {@link Snapshot} from the store.
 *
 * <p>All the events dispatched to the repository are published into a single topic with name
 * {@code W.spine.core.Event} and then consumed from the topic by the repository itself,
 * redistributed by the repository type and event
 * {@link io.spine.core.EventContext#getProducerId() producerId}.
 *
 * <p>It's recommended that the {@code W.spine.core.Event} topic exists before the application
 * start. It should have at least as many partitions as there are instances of
 * {@code KAggregateRepository} in the system. Also, consider having several replicas of the topic
 * (i.e. set {@code replication-factor} to a number greater than 1).
 *
 * <p>The Streams topology may also create auxiliary topics.
 *
 *
 * @author Dmytro Dashenkov
 * @see AggregateRepository for the detailed description of the Aggregate Repositories, type params
 *                          and more
 */
public abstract class KAggregateRepository<I, A extends Aggregate<I, ?, ?>>
        extends AggregateRepository<I, A> {

    /**
     * The Kafka topic for all the write-side events.
     *
     * <p>The events arrive into the topic unordered and are partitioned randomly.
     */
    private static final Topic EVENT_TOPIC = Topic.ofValue("W.spine.core.Event");

    /**
     * The Kafka topic for the aggregate events.
     *
     * <p>The events are partitioned by the Aggregate ID, so the events of a single aggregate are
     * processed sequentially.
     */
    private static final Topic AGGREGATE_EVENTS_TOPIC = Topic.ofValue("aggregate-events");

    private final AggregateAssembler assembler;
    private final KafkaWrapper kafka;

    /**
     * Creates a new instance of {@code KAggregateRepository}.
     *
     * <p>Also, starts the Kafka Streams processing topology.
     *
     * @param streamConfig the Kafka Streams configuration containing {@code bootstrap.servers}
     *                     property and (optionally) other Streams configs
     * @param kafka        the {@link KafkaWrapper} instance used to publish the events
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction") // OK since the whole control
    protected KAggregateRepository(Properties streamConfig, KafkaWrapper kafka) {
        super();
        this.kafka = kafka;
        startKStream(streamConfig);
        this.assembler = this.new AggregateAssembler();
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@code 1}
     * @implSpec It is obligatory for {@code KAggregateRepository} to create {@linkplain Snapshot snapshots}
     * upon each aggregate event, so the method is never used in practice.
     * @deprecated does not make sense for the {@code KAggregateRepository} implementation
     */
    @Deprecated
    @Override
    protected final int getSnapshotTrigger() {
        return 1;
    }

    /**
     * {@inheritDoc}
     *
     * @return always empty set
     * @implSpec Publishes the given {@linkplain EventEnvelope event} into the {@code W.spine.core.Event}
     * Kafka topic as a key-value pair:
     * <pre>
     * {@linkplain #repositoryKey() Repository type} -> {@link EventEnvelope#getOuterObject()}.
     * </pre>
     */
    @Override
    public final Set<I> dispatchEvent(EventEnvelope envelope) {
        final String repoKey = repositoryKey();
        final Event event = envelope.getOuterObject();
        kafka.write(EVENT_TOPIC, repoKey, event);
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec Stores the given {@link Aggregate} into the Kafka topology state store.
     */
    @Override
    protected final void store(A aggregate) {
        assembler.writeAggregate(aggregate);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec
     * Reads the aggregate state {@linkplain Snapshot snapshot} from the Kafka Streams state store.
     */
    @SuppressWarnings("Guava") // Spine Java 7 API.
    @Override
    protected final Optional<AggregateStateRecord> loadState(I id) {
        return assembler.readAggregate(id);
    }

    private void startKStream(Properties config) {
        final String repositoryKey = repositoryKey();
        final Properties streamConfig = prepareConfig(config, repositoryKey);
        final KStreamBuilder builder = new KStreamBuilder();
        final Serde<Message> messageSerde = serdeFrom(serializer(), deserializer());
        final KStream<String, Message> stream = builder.stream(Serdes.String(),
                                                               messageSerde,
                                                               EVENT_TOPIC.getName());
        buildTopology(stream, messageSerde);
        final KafkaStreams streams = new KafkaStreams(builder, streamConfig);
        streams.start();
    }

    private void buildTopology(KStream<String, Message> stream,
                               Serde<? extends Message> messageSerde) {
        @SuppressWarnings("unchecked")
        final Serde<Event> eventServe = (Serde<Event>) messageSerde;
        final String repoKey = repositoryKey();
        stream.filter((key, value) -> repoKey.equals(key))
              .map((key, value) -> {
                  final Event event = (Event) value;
                  final String newKey = producerIdString(event);
                  return new KeyValue<>(newKey, event);
              })
              .through(Serdes.String(), eventServe, AGGREGATE_EVENTS_TOPIC.getName())
              .process(() -> assembler);
    }

    /**
     * @return a string identifier of this {@code AggregateRepository} type (defined by the type of
     *         the {@code Aggregate})
     */
    private String repositoryKey() {
        final EntityClass<?> cls = entityClass();
        final String typeName = cls.getStateType()
                                   .getTypeName();
        return typeName;
    }

    private static String producerIdString(Event event) {
        final Any aggregateEvent = event.getContext().getProducerId();
        final String aggIdString = Stringifiers.toString(aggregateEvent);
        return aggIdString;
    }

    /**
     * Dispatches the given {@link EventEnvelope}.
     *
     * <p>This method uses the {@linkplain AggregateRepository#dispatchEvent super} implementation
     * to dispatch the event.
     *
     * <p>Acts if
     * <pre>
     * {@code
     *     private void doDispatchEvent(EventEnvelope envelope) {
     *         super.dispatchEvent(envelope);
     *     }
     * }
     * </pre>
     *
     * @param envelope the event to be dispatched by this repository
     */
    private void doDispatchEvent(EventEnvelope envelope) {
        super.dispatchEvent(envelope);
    }

    private static Properties prepareConfig(Properties configTemplate,
                                            String applicationId) {
        final Properties result = copy(configTemplate);
        result.setProperty(APPLICATION_ID_CONFIG, applicationId);
        return result;
    }

    @SuppressWarnings("UseOfPropertiesAsHashtable") // OK in this case.
    private static Properties copy(Properties properties) {
        checkNotNull(properties);
        final Properties result = new Properties();
        result.putAll(properties);
        return result;
    }

    /**
     * The Kafka Stream {@link org.apache.kafka.streams.processor.Processor Processor} storing
     * the Aggregate states and applying the new events to the aggregates.
     */
    private class AggregateAssembler extends AbstractProcessor<String, Event> {

        private static final String STATE_STORE_NAME = "aggregate-state-store";

        private KeyValueStore<String, Snapshot> aggregateStateStore;

        @SuppressWarnings("unchecked") // Internal invariant of KAggregateRepository
        @Override
        public void init(ProcessorContext context) {
            super.init(context);
            this.aggregateStateStore =
                    (KeyValueStore<String, Snapshot>) context.getStateStore(STATE_STORE_NAME);
        }

        @Override
        public void process(String key, Event value) {
            final EventEnvelope envelope = EventEnvelope.of(value);
            KAggregateRepository.this.doDispatchEvent(envelope);
        }

        @SuppressWarnings("Guava") // For consistency within the class.
        private Optional<AggregateStateRecord> readAggregate(I id) {
            final String stringKey = Stringifiers.toString(id);
            final Snapshot snapshot = aggregateStateStore.get(stringKey);
            if (snapshot == null) {
                return Optional.absent();
            }
            final AggregateStateRecord result = AggregateStateRecord.newBuilder()
                                                                    .setSnapshot(snapshot)
                                                                    .build();
            return Optional.of(result);
        }

        private void writeAggregate(A aggregate) {
            final I id = aggregate.getId();
            final String stringId = Stringifiers.toString(id);
            final Snapshot snapshot = aggregate.toSnapshot();
            aggregateStateStore.put(stringId, snapshot);
        }
    }
}
