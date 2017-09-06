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
import io.spine.server.entity.Entity;
import io.spine.server.storage.kafka.Topics.TypeTopic;
import io.spine.server.storage.kafka.Topics.ValueTopic;
import io.spine.type.TypeName;

import static io.spine.server.entity.Entity.GenericParameter.STATE;
import static io.spine.server.storage.kafka.Topics.PrefixedIdTopicFactory.FOR_AGGREGATE_RECORD;
import static io.spine.server.storage.kafka.Topics.PrefixedIdTopicFactory.FOR_ENTITY_RECORD;
import static io.spine.server.storage.kafka.Topics.PrefixedIdTopicFactory.FOR_EVENT_COUNT_AFTER_SNAPSHOT;
import static io.spine.server.storage.kafka.Topics.PrefixedTopicFactory.FOR_LAST_EVENT_TIME;
import static io.spine.server.storage.kafka.Topics.PrefixedTopicFactory.FOR_LIFECYCLE_FLAGS;

/**
 * @author Dmytro Dashenkov
 */
public interface Topic {

    String getName();

    static Topic forRecord(Class<?> ofType, Object withId) {
        return FOR_ENTITY_RECORD.create(ofType, withId);
    }

    static Topic forAggregateRecord(Class<?> ofType, Object withId) {
        return FOR_AGGREGATE_RECORD.create(ofType, withId);
    }

    static Topic forLifecycleFlags(Class<?> ofType) {
        return FOR_LIFECYCLE_FLAGS.create(ofType);
    }

    static Topic forEventCountAfterSnapshot(Class<?> ofType, Object withId) {
        return FOR_EVENT_COUNT_AFTER_SNAPSHOT.create(ofType, withId);
    }

    static Topic forLastHandledEventTime(Class<?> ofType) {
        return FOR_LAST_EVENT_TIME.create(ofType);
    }

    static Topic ofValue(String topicValue) {
        return new ValueTopic(topicValue);
    }

    static Topic forType(Class<? extends Entity> entityClass) {
        @SuppressWarnings("unchecked") // Guaranteed by the `STATE` contract.
        final Class<? extends Message> stateClass =
                (Class<? extends Message>) STATE.getArgumentIn(entityClass);
        final TypeName typeName = TypeName.of(stateClass);
        final Topic topic = new TypeTopic(typeName);
        return topic;
    }
}
