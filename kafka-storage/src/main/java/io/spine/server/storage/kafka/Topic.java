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

import io.spine.annotation.SPI;
import io.spine.server.storage.kafka.Topics.ValueTopic;

import static io.spine.server.storage.kafka.Topics.PrefixedTopicFactory.FOR_AGGREGATE_RECORD;
import static io.spine.server.storage.kafka.Topics.PrefixedTopicFactory.FOR_ENTITY_RECORD;
import static io.spine.server.storage.kafka.Topics.PrefixedTopicFactory.FOR_EVENT_COUNT_AFTER_SNAPSHOT;
import static io.spine.server.storage.kafka.Topics.PrefixedTopicFactory.FOR_LAST_EVENT_TIME;
import static io.spine.server.storage.kafka.Topics.PrefixedTopicFactory.FOR_LIFECYCLE_FLAGS;

/**
 * An Apache Kafka <a href="https://kafka.apache.org/documentation/#intro_topics">topic</a>.
 *
 * <p>A topic is the main category in the Kafka type system. A topic typically represents a single
 * type of data, equivalent to a table in a relational database.
 *
 * <p>It's not recommended to implement this interface. Use {@link Topic#ofValue(String)}
 * to instantiate this type. All the static methods of this type return a carefully implemented
 * {@code Topic} (i.e. with {@code equals()}, {@code hashCode()} and {@code toString()} overridden,
 * etc.).
 *
 * @author Dmytro Dashenkov
 */
@SPI
public interface Topic {

    /**
     * @return the name of the represented topic
     */
    String getName();

    /**
     * Creates an instance of {@link Topic} for the records of entity of the given type.
     *
     * @param ofType the type of record
     * @return new instance of {@link Topic}
     */
    static Topic forRecordOfType(Class<?> ofType) {
        return FOR_ENTITY_RECORD.create(ofType);
    }

    /**
     * Creates an instance of {@link Topic} for the records of aggregate of the given type.
     *
     * @param ofType the type of record
     * @return new instance of {@link Topic}
     */
    static Topic forAggregateRecord(Class<?> ofType) {
        return FOR_AGGREGATE_RECORD.create(ofType);
    }

    /**
     * Creates an instance of {@link Topic} for the records of entity of given type.
     *
     * @param ofType the type of record
     * @return new instance of {@link Topic}
     */
    static Topic forLifecycleFlags(Class<?> ofType) {
        return FOR_LIFECYCLE_FLAGS.create(ofType);
    }

    /**
     * Creates an instance of {@link Topic} for storing the event count after last snapshot for
     * the entities of the given type.
     *
     * @param ofType the type of record
     * @return new instance of {@link Topic}
     */
    static Topic forEventCountAfterSnapshot(Class<?> ofType) {
        return FOR_EVENT_COUNT_AFTER_SNAPSHOT.create(ofType);
    }

    /**
     * Creates an instance of {@link Topic} for the timestamp of the last handled by the repository
     * of the Projections of that type event.
     *
     * @param ofType the type of record
     * @return new instance of {@link Topic}
     */
    static Topic forLastHandledEventTime(Class<?> ofType) {
        return FOR_LAST_EVENT_TIME.create(ofType);
    }

    /**
     * Creates an instance of {@link Topic} with the given name.
     *
     * @param topicValue the name of the topic
     * @return new instance of {@link Topic}
     */
    static Topic ofValue(String topicValue) {
        return new ValueTopic(topicValue);
    }

    /**
     * Creates a topic for all the events in the system.
     *
     * @return topic with name {@code spine.core.Event}
     */
    static Topic eventTopic() {
        return Topics.EVENT_TOPIC;
    }
}
