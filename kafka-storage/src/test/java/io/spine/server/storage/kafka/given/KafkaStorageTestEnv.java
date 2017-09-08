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

package io.spine.server.storage.kafka.given;

import io.spine.server.storage.kafka.KafkaStorageFactory;
import io.spine.server.storage.kafka.MessageSerializer;

import java.time.Duration;
import java.util.Properties;

import static io.spine.server.storage.kafka.Consistency.STRONG;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

/**
 * @author Dmytro Dashenkov
 */
public class KafkaStorageTestEnv {

    private static final String TEST_SERVER_URL = "localhost:4545";
    private static final String SERIALIZER_NAME = MessageSerializer.class.getName();
    private static final String ALL = "all";
    private static final String CONSUMER_GROUP_ID = "0";

    private static final Duration TEST_POLL_AWAIT = Duration.of(1, SECONDS);

    private static final Properties producerConfig = new Properties();
    private static final Properties consumerConfig = new Properties();

    static {
        constructConfigs();
    }

    @SuppressWarnings("UseOfPropertiesAsHashtable") // Build test values (always String -> String)
    private static void constructConfigs() {
        producerConfig.put(BOOTSTRAP_SERVERS_CONFIG, TEST_SERVER_URL);
        producerConfig.put(KEY_SERIALIZER_CLASS_CONFIG, SERIALIZER_NAME);
        producerConfig.put(VALUE_SERIALIZER_CLASS_CONFIG, SERIALIZER_NAME);
        producerConfig.put(ACKS_CONFIG, ALL);

        consumerConfig.put(BOOTSTRAP_SERVERS_CONFIG, TEST_SERVER_URL);
        consumerConfig.put(KEY_DESERIALIZER_CLASS_CONFIG, SERIALIZER_NAME);
        consumerConfig.put(VALUE_DESERIALIZER_CLASS_CONFIG, SERIALIZER_NAME);
        consumerConfig.put(GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
    }

    private KafkaStorageTestEnv() {
        // Prevent utility class instantiation.
    }

    public static KafkaStorageFactory getStorageFactory() {
        return StorageFactorySingleton.INSTANCE.value;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField") // Ok for tests.
    public static Properties getProducerConfig() {
        return producerConfig;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField") // Ok for tests.
    public static Properties getConsumerConfig() {
        return consumerConfig;
    }

    public static Duration getPollAwait() {
        return TEST_POLL_AWAIT;
    }

    private enum StorageFactorySingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final KafkaStorageFactory value = new KafkaStorageFactory(producerConfig,
                                                                          consumerConfig,
                                                                          STRONG,
                                                                          TEST_POLL_AWAIT);
    }
}
