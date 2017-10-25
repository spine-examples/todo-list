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

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import io.spine.core.Event;
import io.spine.type.TypeName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An internal utility for creating the {@link Topic} instances.
 *
 * @author Dmytro Dashenkov
 */
final class Topics {

    private static final char SEPARATOR = '_';
    private static final Pattern INVALID_TOPIC_CHARS = Pattern.compile("[$@]");

    static final Topic EVENT_TOPIC = new ValueTopic(TypeName.of(Event.class).value());

    private Topics() {
        // Prevent utility class instantiation.
    }

    /**
     * Escapes the given string deleting the characters which may appear in a Java class name, but
     * are not allowed for a topic name.
     */
    private static String escape(String topic) {
        final Matcher matcher = INVALID_TOPIC_CHARS.matcher(topic);
        final String result = matcher.replaceAll("");
        return result;
    }

    /**
     * A skeleton implementation for the {@link Topic} interface.
     */
    private abstract static class AbstractTopic implements Topic {

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Topic)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            final Topic other = (Topic) obj;
            return Objects.equal(getName(), other.getName());
        }
    }

    /**
     * A set of possible options of rules of creating a standard Spine type topic.
     */
    enum PrefixedTopicFactory {

        FOR_LIFECYCLE_FLAGS("_lifecycle"),
        FOR_LAST_EVENT_TIME("_lhet"),
        FOR_ENTITY_RECORD("entity"),
        FOR_AGGREGATE_RECORD("agg_record"),
        FOR_EVENT_COUNT_AFTER_SNAPSHOT("_event_count_als");

        private final String prefix;

        PrefixedTopicFactory(String prefix) {
            this.prefix = prefix;
        }

        Topic create(Class<?> forType) {
            final String value = Joiner.on(SEPARATOR)
                                       .join(prefix, forType.getName());
            final Topic result = new ValueTopic(value);
            return result;
        }
    }

    /**
     * The standard implantation of the {@link Topic} interface, returning the given {@code value}
     * on a call to {@link Topic#getName()}.
     */
    static final class ValueTopic extends AbstractTopic {

        private final String value;

        ValueTopic(String value) {
            this.value = escape(value);
        }

        @Override
        public String getName() {
            return value;
        }
    }
}
