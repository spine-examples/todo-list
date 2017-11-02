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

import io.spine.Identifier;
import io.spine.annotation.Internal;
import io.spine.core.CommandEnvelope;
import io.spine.core.EventEnvelope;
import io.spine.core.MessageEnvelope;
import io.spine.core.RejectionEnvelope;
import io.spine.server.entity.EntityClass;
import io.spine.server.storage.kafka.KafkaWrapper;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

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
        extends AggregateRepository<I, A> implements KafkaAggregateRepository {

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
        KafkaAggregateLoading.start(this, streamConfig);
    }

    /**
     * {@inheritDoc}
     *
     * @return always empty set
     * @implSpec Publishes the given {@linkplain EventEnvelope event} into
     * the {@code spine.server.aggregate.messages} Kafka topic as a key-value pair:
     * <pre>
     * {@linkplain #key() Repository type} -> {@link EventEnvelope#getOuterObject()}.
     * </pre>
     */
    @Override
    public final Set<I> dispatchEvent(EventEnvelope envelope) {
        KafkaAggregateLoading.dispatchMessage(this, envelope);
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     *
     * @return always empty set
     * @implSpec Publishes the given {@linkplain RejectionEnvelope rejection} into
     * the {@code spine.server.aggregate.messages} Kafka topic as a key-value pair:
     * <pre>
     * {@linkplain #key() Repository type} -> {@link RejectionEnvelope#getOuterObject()}.
     * </pre>
     */
    @Override
    public Set<I> dispatchRejection(RejectionEnvelope envelope) {
        KafkaAggregateLoading.dispatchMessage(this, envelope);
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     *
     * @return the default value of the ID according to {@link Identifier#getDefaultValue(Class)}
     * @implSpec Publishes the given {@linkplain CommandEnvelope command} into
     * the {@code spine.server.aggregate.messages} Kafka topic as a key-value pair:
     * <pre>
     * {@linkplain #key() Repository type} -> {@link CommandEnvelope#getOuterObject()}.
     * </pre>
     */
    @Override
    public I dispatch(CommandEnvelope envelope) {
        KafkaAggregateLoading.dispatchMessage(this, envelope);
        return Identifier.getDefaultValue(getIdClass());
    }

    @Internal
    @Override
    public void dispatchEventNow(EventEnvelope envelope) {
        logErrors(super::dispatchEvent, envelope);
    }

    @Internal
    @Override
    public void dispatchRejectionNow(RejectionEnvelope envelope) {
        logErrors(super::dispatchRejection, envelope);
    }

    @Internal
    @Override
    public void dispatchCommandNow(CommandEnvelope envelope) {
        logErrors(super::dispatch, envelope);
    }

    @Internal
    @Override
    public String key() {
        final EntityClass<?> cls = entityClass();
        final String typeName = cls.getStateType()
                                   .getTypeName();
        return typeName;
    }

    @Internal
    @Override
    public KafkaWrapper kafka() {
        return kafka;
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
    @SuppressWarnings("DuplicateStringLiteralInspection")
        // OK for the log messages. Duplicate in `KAggregateRepository`.
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
}
