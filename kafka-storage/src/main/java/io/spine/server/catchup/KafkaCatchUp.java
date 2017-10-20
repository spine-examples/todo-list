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

package io.spine.server.catchup;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import io.spine.core.Event;
import io.spine.core.EventClass;
import io.spine.core.EventEnvelope;
import io.spine.server.event.EventDispatcher;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.storage.kafka.KafkaWrapper;
import io.spine.server.storage.kafka.Topic;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windows;
import org.apache.kafka.streams.kstream.internals.TimeWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static org.apache.kafka.common.serialization.Serdes.serdeFrom;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;

/**
 * @author Dmytro Dashenkov
 */
public final class KafkaCatchUp {

    private static final long WINDOW_SIZE_MS = 2000;
    private static final Windows<TimeWindow> windows = TimeWindows.of(WINDOW_SIZE_MS);

    private KafkaCatchUp() {
        // Prevent utility class instantiation.
    }

    public static void start(ProjectionRepository<?, ?, ?> repository, Properties streamConfig) {
        checkNotNull(repository);
        final Properties config = copy(streamConfig);
        final String repositoryKey = repositoryKey(repository);
        config.setProperty(APPLICATION_ID_CONFIG, repositoryKey);
        doStart(repository, repositoryKey, config);
    }

    private static void doStart(ProjectionRepository<?, ?, ?> repository,
                                String repositoryKey,
                                Properties streamConfig) {
        final KStreamBuilder builder = new KStreamBuilder();
        final Serde<Message> messageSerde = serdeFrom(serializer(), deserializer());
        final KStream<Message, Message> stream =
                builder.stream(messageSerde, messageSerde, Topic.eventTopic().getName());
        final Set<EventClass> handledClasses = repository.getMessageClasses();
        stream.filter((key, value) -> handledClasses.contains(EventClass.of(value)))
              .map((key, value) -> new KeyValue<>(repositoryKey, value))
              .groupByKey(Serdes.String(), messageSerde)
              .aggregate(KafkaCatchUp::voidInstance,
                         (key, value, aggregate) -> dispatchEvent(repository, (Event) value),
                         windows,
                         VoidSerde.INSTANCE.cast());
        final KafkaStreams streams = new KafkaStreams(builder, streamConfig);
        streams.cleanUp();
        streams.start();
        log().info("Starting catch up for {} projection.", repositoryKey);
    }

    @SuppressWarnings("UseOfPropertiesAsHashtable") // OK in this case.
    private static Properties copy(Properties properties) {
        checkNotNull(properties);
        final Properties result = new Properties();
        result.putAll(properties);
        return result;
    }

    private static Void dispatchEvent(ProjectionRepository<?, ?, ?> repo, Event event) {
        final EventEnvelope envelope = EventEnvelope.of(event);
        repo.dispatch(envelope);
        log().info("Dispatched event {} with {}.",
                   envelope.getOuterObject(),
                   repo.getClass().getName());
        return voidInstance();
    }

    private static String repositoryKey(ProjectionRepository<?, ?, ?> repository) {
        final String typeName = repository.getEntityStateType().getTypeName();
        return typeName;
    }

    private static Void voidInstance() {
        return null;
    }

    @SuppressWarnings("unchecked") // OK since the dispatcher never produces any IDs.
    public static <I> EventDispatcher<I> dispatcher(Set<EventClass> messageClasses,
                                                    KafkaWrapper kafka) {
        return (EventDispatcher<I>) new KafkaEventDispatcher(messageClasses, kafka);
    }

    private static class KafkaEventDispatcher implements EventDispatcher<Object> {

        private final ImmutableSet<EventClass> messageClasses;
        private final KafkaWrapper kafka;

        private KafkaEventDispatcher(Set<EventClass> messageClasses,
                                     KafkaWrapper kafka) {
            this.messageClasses = ImmutableSet.copyOf(messageClasses);
            this.kafka = kafka;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField") // OK for an immutable collection.
        @Override
        public Set<EventClass> getMessageClasses() {
            return messageClasses;
        }

        @Override
        public Set<Object> dispatch(EventEnvelope envelope) {
            final Message id = envelope.getId();
            final Message event = envelope.getOuterObject();
            kafka.write(Topic.eventTopic(), id, event);
            return Collections.emptySet();
        }

        @Override
        public void onError(EventEnvelope envelope, RuntimeException exception) {
            // TODO:2017-10-19:dmytro.dashenkov: Think of an error scenario.
        }
    }

    private enum VoidSerde implements Serde<Object>, Serializer<Object>, Deserializer<Object> {

        INSTANCE;

        private static final byte[] DATA = {};

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // NoOp
        }

        @Override
        public byte[] serialize(String topic, Object data) {
            return DATA;
        }

        @Override
        public Object deserialize(String topic, byte[] data) {
            return voidInstance();
        }

        @Override
        public void close() {
            // NoOp
        }

        @Override
        public Serializer<Object> serializer() {
            return this;
        }

        @Override
        public Deserializer<Object> deserializer() {
            return this;
        }

        @SuppressWarnings("unchecked")
        private <T> Serde<T> cast() {
            return (Serde<T>) this;
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(KafkaCatchUp.class);
    }
}
