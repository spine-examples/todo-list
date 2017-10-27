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
import io.spine.core.EventId;
import io.spine.server.entity.EntityClass;
import io.spine.server.storage.kafka.KafkaWrapper;
import io.spine.server.storage.kafka.Topic;
import io.spine.string.Stringifier;
import io.spine.string.StringifierRegistry;
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
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.Identifier.unpack;
import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static java.lang.String.format;
import static org.apache.kafka.common.serialization.Serdes.serdeFrom;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;

/**
 * @author Dmytro Dashenkov
 */
public abstract class KAggregateRepository<I, A extends Aggregate<I, ?, ?>>
        extends AggregateRepository<I, A> {

    private static final Topic EVENT_TOPIC = Topic.ofValue("W.spine.core.Event");
    private static final Topic AGGREGATE_EVENTS_TOPIC = Topic.ofValue("aggregate-events");

    private final AggregateAssembler assembler;
    private final Stringifier<I> idStringifier;
    private final KafkaWrapper kafka;

    @SuppressWarnings("ThisEscapedInObjectConstruction") // OK since the whole control
    protected KAggregateRepository(Properties streamConfig, KafkaWrapper kafka) {
        super();
        this.kafka = kafka;
        startKStream(streamConfig);
        @SuppressWarnings("unchecked") // Logically checked.
        final Class<I> idClass = (Class<I>) entityClass().getIdClass();
        @SuppressWarnings("Guava")
        final Optional<Stringifier<I>> stringifier = StringifierRegistry.getInstance()
                                                                        .get(idClass);
        checkState(stringifier.isPresent(),
                   "Stringifier for type {} is not registered.", idClass.getName());
        this.idStringifier = stringifier.get();
        this.assembler = this.new AggregateAssembler();
    }

    @Override
    public final Set<I> dispatchEvent(EventEnvelope envelope) {
        final EventId eventId = envelope.getId();
        final Event event = envelope.getOuterObject();
        kafka.write(EVENT_TOPIC, eventId, event);
        return Collections.emptySet();
    }

    @Override
    protected final void store(A aggregate) {
        assembler.writeAggregate(aggregate);
    }

    @SuppressWarnings("Guava") // Spine Java 7 API.
    @Override
    final Optional<A> load(I id) {
        return assembler.readAggregate(id);
    }

    private void startKStream(Properties config) {
        final String repositoryKey = repositoryKey();
        final Properties streamConfig = prepareConfig(config, repositoryKey);
        final KStreamBuilder builder = new KStreamBuilder();
        final Serde<Message> messageSerde = serdeFrom(serializer(), deserializer());
        final KStream<Message, Message> stream = builder.stream(messageSerde,
                                                                messageSerde,
                                                                EVENT_TOPIC.getName());
        buildTopology(stream, messageSerde);
        final KafkaStreams streams = new KafkaStreams(builder, streamConfig);
        streams.start();
    }

    private void buildTopology(KStream<Message, Message> stream,
                               Serde<? extends Message> messageSerde) {
        @SuppressWarnings("unchecked")
        final Serde<Event> eventServe = (Serde<Event>) messageSerde;
        stream.map((key, value) -> {
                  final Event event = (Event) value;
                  final String newKey = aggregateIdFromEvent(event);
                  return new KeyValue<>(newKey, event);
              })
              .through(Serdes.String(), eventServe, AGGREGATE_EVENTS_TOPIC.getName())
              .process(() -> assembler);
    }

    private String repositoryKey() {
        final EntityClass<?> cls = entityClass();
        final String typeName = cls.getStateType()
                                   .getTypeName();
        return typeName;
    }

    private String aggregateIdFromEvent(Event event) {
        final String producerId = producerIdString(event);
        final String typeKey = repositoryKey();
        return format("%s-%s", typeKey, producerId);
    }

    private String producerIdString(Event event) {
        final Any aggregateEvent = event.getContext().getProducerId();
        final String aggIdString = idStringifier.convert(unpack(aggregateEvent));
        return aggIdString;
    }

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

        @SuppressWarnings("Guava") // For consistency inside the class.
        private Optional<A> readAggregate(I id) {
            final Snapshot snapshot = aggregateStateStore.get(idStringifier.convert(id));
            if (snapshot == null) {
                return Optional.absent();
            }
            final A aggregate = create(id);
            inTx(aggregate, tx -> aggregate.restore(snapshot));
            return Optional.of(aggregate);
        }

        private void writeAggregate(A aggregate) {
            final I id = aggregate.getId();
            final String stringId = idStringifier.convert(id);
            final Snapshot snapshot = aggregate.toSnapshot();
            aggregateStateStore.put(stringId, snapshot);
        }

        private void inTx(A aggregate, Consumer<AggregateTransaction> transactionalOp) {
            final AggregateTransaction tx = AggregateTransaction.start(aggregate);
            transactionalOp.accept(tx);
            tx.commit();
        }
    }
}
