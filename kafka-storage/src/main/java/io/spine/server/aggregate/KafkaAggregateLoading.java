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
import io.spine.core.Command;
import io.spine.core.CommandEnvelope;
import io.spine.core.Event;
import io.spine.core.EventEnvelope;
import io.spine.core.MessageEnvelope;
import io.spine.core.Rejection;
import io.spine.core.RejectionEnvelope;
import io.spine.server.storage.kafka.Topic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

import static io.spine.server.kafka.KafkaStreamsConfigs.prepareConfig;
import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static io.spine.server.storage.kafka.Topic.ofValue;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static org.apache.kafka.common.serialization.Serdes.serdeFrom;

/**
 * A utility for loading the {@link Aggregate} instances using Apache Kafka Streams.
 *
 * @author Dmytro Dashenkov
 */
final class KafkaAggregateLoading {

    /**
     * The Kafka topic for all the events, commands and rejections dispatched into
     * {@code KAggregateRepository}.
     */
    private static final Topic AGGREGATE_MESSAGES = ofValue("spine.server.aggregate.messages");

    private KafkaAggregateLoading() {
        // Prevent utility class instantiation.
    }

    /**
     * Starts the Aggregate loading for the given {@link KafkaAggregateRepository}.
     *
     * @param repository the repository to start the Aggregate loading for
     * @param config the Kafka Streams config
     */
    static void start(KafkaAggregateRepository repository, Properties config) {
        final StreamsBuilder builder = new StreamsBuilder();
        final Serde<Message> messageSerde = serdeFrom(serializer(), deserializer());
        final KStream<Message, Message> stream = builder.stream(AGGREGATE_MESSAGES.getName(),
                                                                Consumed.with(messageSerde,
                                                                              messageSerde));
        buildTopology(stream, repository);
        final Properties streamConfig = prepareConfig(config, repository.key());
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamConfig);
        streams.start();
    }

    /**
     * Publishes the given envelope into the Kafka {@code spine.server.aggregate.messages} topic.
     *
     * @param repository the repository which should handle the published message
     * @param msg the envelope to be dispatched
     */
    static void dispatchMessage(KafkaAggregateRepository repository,
                                MessageEnvelope<?, ? extends Message, ?> msg) {
        final String repositoryKey = repository.key();
        final Message message = msg.getOuterObject();
        repository.kafka().write(AGGREGATE_MESSAGES, repositoryKey, message);
    }

    private static void buildTopology(KStream<Message, Message> stream,
                                      KafkaAggregateRepository repository) {
        final String repositoryKey = repository.key();
        stream.filter((key, value) -> {
            final StringValue genericKey = (StringValue) key;
            final boolean result = repositoryKey.equals(genericKey.getValue());
            return result;
        }).foreach((key, value) -> doDispatch(repository, value));
    }

    @SuppressWarnings({"IfStatementWithTooManyBranches", "ChainOfInstanceofChecks"})
        // OK for this method as we want a *single* processing point for any kind of message.
    private static void doDispatch(KafkaAggregateRepository repo, Message message) {
        if (message instanceof Event) {
            final EventEnvelope envelope = EventEnvelope.of((Event) message);
            repo.dispatchEventNow(envelope);
        } else if (message instanceof Rejection) {
            final RejectionEnvelope envelope = RejectionEnvelope.of((Rejection) message);
            repo.dispatchRejectionNow(envelope);
        } else if (message instanceof Command) {
            final CommandEnvelope envelope = CommandEnvelope.of((Command) message);
            repo.dispatchCommandNow(envelope);
        } else {
            throw newIllegalArgumentException(
                    "Expected Command, Event or Rejection but encountered %s.",
                    message
            );
        }
    }
}
