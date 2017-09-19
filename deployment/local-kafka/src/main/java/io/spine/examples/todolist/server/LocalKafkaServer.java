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

package io.spine.examples.todolist.server;

import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.kafka.KafkaStorageFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.context.BoundedContexts.create;
import static io.spine.examples.todolist.context.BoundedContexts.injectStorageFactory;
import static io.spine.server.storage.kafka.Consistency.STRONG;
import static java.time.temporal.ChronoUnit.MILLIS;

/**
 * A local {@link Server} using {@link KafkaStorageFactory}.
 *
 * <p>The server exposes its {@code gRPC API} at
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * @author Dmytro Dashenkov
 */
public class LocalKafkaServer {

    private static final String KAFKA_PRODUCER_PROPS_PATH = "config/kafka-producer.properties";
    private static final String KAFKA_CONSUMER_PROPS_PATH = "config/kafka-consumer.properties";
    private static final Duration POLL_AWAIT = Duration.of(50, MILLIS);

    static {
        final Properties producerConfig = loadProperties(KAFKA_PRODUCER_PROPS_PATH);
        final Properties consumerConfig = loadProperties(KAFKA_CONSUMER_PROPS_PATH);
        final StorageFactory defaultStorageFactory =
                KafkaStorageFactory.newBuilder()
                                   .setProducerConfig(producerConfig)
                                   .setConsumerConfig(consumerConfig)
                                   .setMaxPollAwait(POLL_AWAIT)
                                   .setConsistencyLevel(STRONG)
                                   .build();
        injectStorageFactory(defaultStorageFactory);
    }

    private LocalKafkaServer() {
        // Prevent utility class instantiation.
    }

    public static void main(String[] args) throws IOException {
        final BoundedContext boundedContext = create();
        final Server server = new Server(DEFAULT_CLIENT_SERVICE_PORT, boundedContext);
        server.start();
    }

    /**
     * Loads {@code .properties} file from the classpath by the given filename.
     *
     * <p>If the file is not found, an {@link NullPointerException} is thrown.
     */
    private static Properties loadProperties(String filename) {
        final ClassLoader loader = BoundedContexts.class.getClassLoader();
        final InputStream rawProperties = loader.getResourceAsStream(filename);
        checkNotNull(rawProperties, "Could not load properties file %s from classpath.", filename);

        final Properties result = new Properties();

        try (InputStream props = rawProperties) {
            result.load(props);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }
}
