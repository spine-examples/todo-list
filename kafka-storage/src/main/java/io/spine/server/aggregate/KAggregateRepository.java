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
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.Identifier;
import io.spine.core.Command;
import io.spine.core.CommandEnvelope;
import io.spine.core.Event;
import io.spine.core.EventEnvelope;
import io.spine.core.MessageEnvelope;
import io.spine.core.Rejection;
import io.spine.core.RejectionEnvelope;
import io.spine.server.entity.EntityClass;
import io.spine.server.kafka.KafkaStreamsConfigs;
import io.spine.server.storage.kafka.KafkaWrapper;
import io.spine.server.storage.kafka.Topic;
import io.spine.string.Stringifiers;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static org.apache.kafka.common.serialization.Serdes.serdeFrom;
import static org.apache.kafka.streams.state.Stores.keyValueStoreBuilder;
import static org.apache.kafka.streams.state.Stores.persistentKeyValueStore;

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
 * {@code spine.server.aggregate.letters} and then consumed from the topic by the repository
 * itself. It's recommended that the {@code spine.server.aggregate.letters} topic exists before
 * the application start. It should have at least as many partitions as there are subtypes of
 * {@code KAggregateRepository} in the system. Also, consider having several replicas of the topic
 * (i.e. set {@code replication-factor} to a number greater than 1).
 *
 * <p>Unlike the {@linkplain AggregateRepository base implementation}, none of
 * the {@code dispatch*} methods of this class propagate exceptions thrown while handling
 * the dispatched message.
 *
 * @author Dmytro Dashenkov
 * @see AggregateRepository for the detailed description of the Aggregate Repositories, type params
 *                          and more
 */
public abstract class KAggregateRepository<I, A extends Aggregate<I, ?, ?>>
        extends AggregateRepository<I, A> {

    /**
     * The Kafka topic for all the events, commands and rejections dispatched into 
     * {@code KAggregateRepository}.
     */
    private static final Topic AGGREGATE_LETTERS = Topic.ofValue("spine.server.aggregate.letters");

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
     * @implSpec Publishes the given {@linkplain EventEnvelope event} into 
     * the {@code spine.server.aggregate.letters} Kafka topic as a key-value pair:
     * <pre>
     * {@linkplain #repositoryKey() Repository type} -> {@link EventEnvelope#getOuterObject()}.
     * </pre>
     */
    @Override
    public final Set<I> dispatchEvent(EventEnvelope envelope) {
        dispatchLetter(envelope);
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     * 
     * @return always empty set
     * @implSpec Publishes the given {@linkplain RejectionEnvelope rejection} into 
     * the {@code spine.server.aggregate.letters} Kafka topic as a key-value pair:
     * <pre>
     * {@linkplain #repositoryKey() Repository type} -> {@link RejectionEnvelope#getOuterObject()}.
     * </pre>
     */
    @Override
    public Set<I> dispatchRejection(RejectionEnvelope envelope) {
        dispatchLetter(envelope);
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     *
     * @return the default value of the ID according to {@link Identifier#getDefaultValue(Class)}
     * @implSpec Publishes the given {@linkplain CommandEnvelope command} into
     * the {@code spine.server.aggregate.letters} Kafka topic as a key-value pair:
     * <pre>
     * {@linkplain #repositoryKey() Repository type} -> {@link CommandEnvelope#getOuterObject()}.
     * </pre>
     */
    @Override
    public I dispatch(CommandEnvelope envelope) {
        dispatchLetter(envelope);
        return Identifier.getDefaultValue(getIdClass());
    }

    /**
     * Publishes the given envelope into the Kafka {@code spine.server.aggregate.letters} topic.
     *
     * @param msg the envelope to be dispatched
     */
    private void dispatchLetter(MessageEnvelope<?, ? extends Message, ?> msg) {
        final String repositoryKey = repositoryKey();
        final Message letter = msg.getOuterObject();
        kafka.write(AGGREGATE_LETTERS, repositoryKey, letter);
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
    protected final Optional<AggregateStateRecord> fetchHistory(I id) {
        return assembler.readAggregate(id);
    }

    private void startKStream(Properties config) {
        final StreamsBuilder builder = new StreamsBuilder();
        final Serde<Message> messageSerde = serdeFrom(serializer(), deserializer());
        final KeyValueBytesStoreSupplier supplier =
                persistentKeyValueStore(AggregateAssembler.STATE_STORE_NAME);
        builder.addStateStore(keyValueStoreBuilder(supplier, messageSerde, messageSerde));
        final KStream<Message, Message> stream = builder.stream(AGGREGATE_LETTERS.getName(),
                                                                Consumed.with(messageSerde,
                                                                              messageSerde));
        final String repositoryKey = repositoryKey();
        buildTopology(stream, repositoryKey);
        final Properties streamConfig = KafkaStreamsConfigs.prepareConfig(config, repositoryKey);
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamConfig);
        streams.start();
    }

    private void buildTopology(KStream<Message, Message> stream, String repositoryKey) {
        stream.filter((key, value) -> {
            final StringValue genericKey = (StringValue) key;
            final boolean result = repositoryKey.equals(genericKey.getValue());
            return result;
        }).process(() -> assembler, AggregateAssembler.STATE_STORE_NAME);
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
        logErrors(super::dispatchEvent, envelope);
    }

    /**
     * Dispatches the given {@link RejectionEnvelope}.
     *
     * <p>This method uses the {@linkplain AggregateRepository#dispatchRejection super}
     * implementation to dispatch the rejection.
     *
     * <p>Acts if
     * <pre>
     * {@code
     *     private void doDispatchRejection(RejectionEnvelope envelope) {
     *         super.dispatchRejection(envelope);
     *     }
     * }
     * </pre>
     *
     * @param envelope the rejection to be dispatched by this repository
     */
    private void doDispatchRejection(RejectionEnvelope envelope) {
        logErrors(super::dispatchRejection, envelope);
    }

    /**
     * Dispatches the given {@link CommandEnvelope}.
     *
     * <p>This method uses the {@linkplain AggregateRepository#dispatch super} implementation
     * to dispatch the command.
     *
     * <p>Acts if
     * <pre>
     * {@code
     *     private void doDispatchEvent(CommandEnvelope envelope) {
     *         super.dispatch(envelope);
     *     }
     * }
     * </pre>
     *
     * @param envelope the command to be dispatched by this repository
     */
    private void doDispatchCommand(CommandEnvelope envelope) {
        logErrors(super::dispatch, envelope);
    }

    /**
     * Executes the given {@linkplain Consumer dispatching task} with the given
     * {@linkplain MessageEnvelope message argument} and catches and logs all the runtime
     * exceptions thrown by the {@code task}.
     *
     * @param task the {@link Consumer} to execute
     * @param argument the {@code task} argument
     * @param <E> the type of the {@code argument} for the {@code task}
     */
    private <E extends MessageEnvelope<?, ? ,?>> void logErrors(Consumer<E> task, E argument) {
        try {
            task.accept(argument);
            log().info("Acknowledged {} (ID: {}).",
                       argument.getMessageClass(),
                       Identifier.toString(argument.getId()));
        } catch (RuntimeException e) {
            logError("Error while processing %s (ID: %s).", argument, e);
        }
    }

    /**
     * The Kafka Stream {@link org.apache.kafka.streams.processor.Processor Processor} storing
     * the Aggregate states and applying the new events to the aggregates.
     */
    private class AggregateAssembler extends AbstractProcessor<Message, Message> {

        private static final String STATE_STORE_NAME = "aggregate-state-store";

        private KeyValueStore<String, Snapshot> aggregateStateStore;

        @SuppressWarnings("unchecked") // Internal invariant of KAggregateRepository
        @Override
        public void init(ProcessorContext context) {
            super.init(context);
            this.aggregateStateStore =
                    (KeyValueStore<String, Snapshot>) context.getStateStore(STATE_STORE_NAME);
            checkNotNull(aggregateStateStore);
        }

        @SuppressWarnings({"IfStatementWithTooManyBranches", "ChainOfInstanceofChecks"})
            // OK for this method, since we should have a *single* processor for all the state
            // changing messages.
        @Override
        public void process(Message ignoredKey, Message value) {
            if (value instanceof Event) {
                final EventEnvelope envelope = EventEnvelope.of((Event) value);
                KAggregateRepository.this.doDispatchEvent(envelope);
            } else if (value instanceof Rejection) {
                final RejectionEnvelope envelope = RejectionEnvelope.of((Rejection) value);
                KAggregateRepository.this.doDispatchRejection(envelope);
            } else if (value instanceof Command) {
                final CommandEnvelope envelope = CommandEnvelope.of((Command) value);
                KAggregateRepository.this.doDispatchCommand(envelope);
            } else {
                throw newIllegalArgumentException(
                        "Expected Command, Event or Rejection but encountered %s.",
                        value
                );
            }
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
