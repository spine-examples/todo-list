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

package io.spine.server.kafka;

import io.spine.annotation.Internal;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;

/**
 * A utility for working with the Kafka Streams config {@link Properties}.
 *
 * @author Dmytro Dashenkov
 */
@Internal
public final class KafkaStreamsConfigs {

    private KafkaStreamsConfigs() {
        // Prevent utility class instantiation.
    }

    /**
     * Appends
     * the {@link org.apache.kafka.streams.StreamsConfig#APPLICATION_ID_CONFIG application.id}
     * configuration to the given properties.
     *
     * <p>Note: this method creates a defencive copy of the passed {@link Properties} instead of
     * modifying the passed one.
     *
     * @param config        the Kafka Streams configuration
     * @param applicationId the {@code application.id} property value to be set into the config
     * @return a copy of the given {@link Properties} with the {@code application.id} property set
     */
    public static Properties prepareConfig(Properties config, String applicationId) {
        final Properties result = copy(config);
        result.setProperty(APPLICATION_ID_CONFIG, applicationId);
        return result;
    }

    /**
     * Copies the given {@link Properties} into a new object.
     *
     * @param properties the object to copy
     * @return the copy of the input
     */
    @SuppressWarnings("UseOfPropertiesAsHashtable") // OK in this case.
    private static Properties copy(Properties properties) {
        checkNotNull(properties);
        final Properties result = new Properties();
        result.putAll(properties);
        return result;
    }
}
