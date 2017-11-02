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
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static io.spine.server.storage.kafka.Topic.ofValue;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static org.apache.kafka.common.serialization.Serdes.serdeFrom;

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
 * <p>All the commands, events and rejections dispatched to the repository are published into
 * a single Kafka topic with name {@code spine.server.aggregate.messages} and then consumed from
 * the topic by the repository itself. It's recommended that
 * the {@code spine.server.aggregate.messages} topic exists before the application start. It should
 * have at least as many partitions as there are subtypes of {@code KAggregateRepository} in the
 * system. Also, consider having several replicas of the topic
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
    private static final Topic AGGREGATE_MESSAGES = ofValue("spine.server.aggregate.messages");

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
    }

    /**
     * {@inheritDoc}
     *
     * @return always empty set
     * @implSpec Publishes the given {@linkplain EventEnvelope event} into 
     * the {@code spine.server.aggregate.messages} Kafka topic as a key-value pair:
     * <pre>
     * {@linkplain #repositoryKey() Repository type} -> {@link EventEnvelope#getOuterObject()}.
     * </pre>
     */
    @Override
    public final Set<I> dispatchEvent(EventEnvelope envelope) {
        dispatchMessage(envelope);
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     * 
     * @return always empty set
     * @implSpec Publishes the given {@linkplain RejectionEnvelope rejection} into 
     * the {@code spine.server.aggregate.messages} Kafka topic as a key-value pair:
     * <pre>
     * {@linkplain #repositoryKey() Repository type} -> {@link RejectionEnvelope#getOuterObject()}.
     * </pre>
     */
    @Override
    public Set<I> dispatchRejection(RejectionEnvelope envelope) {
        dispatchMessage(envelope);
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     *
     * @return the default value of the ID according to {@link Identifier#getDefaultValue(Class)}
     * @implSpec Publishes the given {@linkplain CommandEnvelope command} into
     * the {@code spine.server.aggregate.messages} Kafka topic as a key-value pair:
     * <pre>
     * {@linkplain #repositoryKey() Repository type} -> {@link CommandEnvelope#getOuterObject()}.
     * </pre>
     */
    @Override
    public I dispatch(CommandEnvelope envelope) {
        dispatchMessage(envelope);
        return Identifier.getDefaultValue(getIdClass());
    }

    /**
     * Publishes the given envelope into the Kafka {@code spine.server.aggregate.messages} topic.
     *
     * @param msg the envelope to be dispatched
     */
    private void dispatchMessage(MessageEnvelope<?, ? extends Message, ?> msg) {
        final String repositoryKey = repositoryKey();
        final Message message = msg.getOuterObject();
        kafka.write(AGGREGATE_MESSAGES, repositoryKey, message);
    }

    private void startKStream(Properties config) {
        final StreamsBuilder builder = new StreamsBuilder();
        final Serde<Message> messageSerde = serdeFrom(serializer(), deserializer());
        final KStream<Message, Message> stream = builder.stream(AGGREGATE_MESSAGES.getName(),
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
        }).foreach((key, value) -> doDispatch(value));
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
     * Executes the given dispatching task with the given
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

    @SuppressWarnings({"IfStatementWithTooManyBranches", "ChainOfInstanceofChecks"})
        // OK for this method as we want a *single* processing point for any kind of message.
    private void doDispatch(Message message) {
        if (message instanceof Event) {
            final EventEnvelope envelope = EventEnvelope.of((Event) message);
            doDispatchEvent(envelope);
        } else if (message instanceof Rejection) {
            final RejectionEnvelope envelope = RejectionEnvelope.of((Rejection) message);
            doDispatchRejection(envelope);
        } else if (message instanceof Command) {
            final CommandEnvelope envelope = CommandEnvelope.of((Command) message);
            doDispatchCommand(envelope);
        } else {
            throw newIllegalArgumentException(
                    "Expected Command, Event or Rejection but encountered %s.",
                    message
            );
        }
    }
}
