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

package io.spine.server.storage.kafka.io.spine.server.catchup;

import com.google.common.collect.Multimap;
import com.google.protobuf.Message;
import io.spine.core.Event;
import io.spine.core.EventClass;
import io.spine.core.EventEnvelope;
import io.spine.core.Events;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.storage.kafka.Topic;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windows;
import org.apache.kafka.streams.kstream.internals.TimeWindow;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static java.util.stream.Collectors.toList;
import static org.apache.kafka.common.serialization.Serdes.serdeFrom;

/**
 * @author Dmytro Dashenkov
 */
public final class KafkaCatchUp {

    private static final long WINDOW_SIZE_MS = 2000;
    private static final Windows<TimeWindow> windows = TimeWindows.of(WINDOW_SIZE_MS);

    private KafkaCatchUp() {
        // Prevent utility class instantiation.
    }

    public static void start(Multimap<EventClass, ProjectionRepository<?, ?, ?>> repoRegistry,
                             Properties streamConfig) {
        final KStreamBuilder builder = new KStreamBuilder();
        final Serde<Message> messageSerde = serdeFrom(serializer(), deserializer());
        final String topicName = Topic.eventTopic().getName();
        final KStream<Message, Message> stream =
                builder.stream(messageSerde, messageSerde, topicName);
        stream.map((Message key, Message value) -> mapToRepositories(value, repoRegistry))
              .filter((repos, event) -> !repos.isEmpty())
              .flatMap((repos, event) -> repos.stream()
                                              .map(repo -> new KeyValue<>(repo, event))
                                              .collect(toList()))
              .groupByKey()
              .aggregate(KafkaCatchUp::voidInstance,
                         KafkaCatchUp::aggregateAndCount,
                         windows,
                         VoidSerde.INSTANCE);
        final KafkaStreams streams = new KafkaStreams(builder, streamConfig);
        streams.start();
    }

    private static KeyValue<Collection<ProjectionRepository<?, ?, ?>>, Event> mapToRepositories(
            Message eventValue,
            Multimap<EventClass, ProjectionRepository<?, ?, ?>> repoRegistry) {
        final Event event = (Event) eventValue;
        return new KeyValue<>(getRepos(event, repoRegistry), event);
    }

    private static Collection<ProjectionRepository<?, ?, ?>>
    getRepos(Event event, Multimap<EventClass, ProjectionRepository<?, ?, ?>> repoRegistry) {
        final EventClass eventCls = eventClass(event);
        final Collection<ProjectionRepository<?, ?, ?>> result = repoRegistry.get(eventCls);
        return result;
    }

    private static EventClass eventClass(Event of) {
        final Message eventMsg = Events.getMessage(of);
        final EventClass eventCls = EventClass.of(eventMsg);
        return eventCls;
    }

    private static Void aggregateAndCount(ProjectionRepository<?, ?, ?> repo,
                                          Event event,
                                          Void stub) {
        dispatch(repo, event);
        return stub;
    }

    private static Void voidInstance() {
        return null;
    }

    private static void dispatch(ProjectionRepository<?, ?, ?> repo, Event event) {
        final EventEnvelope envelope = EventEnvelope.of(event);
        repo.dispatch(envelope);
    }

    private enum VoidSerde implements Serde<Void>, Serializer<Void>, Deserializer<Void> {

        INSTANCE;

        private static final byte[] DATA = {};

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // NoOp
        }

        @Override
        public byte[] serialize(String topic, Void data) {
            return DATA;
        }

        @Override
        public Void deserialize(String topic, byte[] data) {
            return voidInstance();
        }

        @Override
        public void close() {
            // NoOp
        }

        @Override
        public Serializer<Void> serializer() {
            return this;
        }

        @Override
        public Deserializer<Void> deserializer() {
            return this;
        }
    }
}
