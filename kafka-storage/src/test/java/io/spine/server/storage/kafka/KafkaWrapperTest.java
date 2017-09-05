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

import com.google.protobuf.Int32Value;
import com.google.protobuf.Message;
import io.spine.server.entity.AbstractEntity;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Properties;

import static io.spine.server.storage.kafka.Consistency.STRONG;
import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static io.spine.server.storage.kafka.given.KafkaStorageTestEnv.getConsumerConfig;
import static io.spine.server.storage.kafka.given.KafkaStorageTestEnv.getProducerConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Dashenkov
 */
@DisplayName("FafkaWrapper should")
class KafkaWrapperTest {

    private KafkaWrapper wrapper;

    @BeforeEach
    void setUp() {
        final Properties consumerConfig = getConsumerConfig();
        final Properties producerConfig = getProducerConfig();
        final KafkaConsumer<Message, Message> consumer = new KafkaConsumer<>(consumerConfig,
                                                                             deserializer(),
                                                                             deserializer());
        final KafkaProducer<Message, Message> producer = new KafkaProducer<>(producerConfig,
                                                                             serializer(),
                                                                             serializer());
        wrapper = new KafkaWrapper(producer, consumer, STRONG);
    }

    @Test
    @DisplayName("write and read data")
    void writeAndReadData() {
        final Message value = Int32Value.newBuilder()
                                        .setValue(42)
                                        .build();
        final Topic topic = Topic.ofValue("KafkaWrapperTest");
        wrapper.write(TestEntity.class, topic, value);
        final Optional<Int32Value> readResult = wrapper.readLast(topic);
        assertTrue(readResult.isPresent());
        final Int32Value readMessage = readResult.get();
        assertEquals(value, readMessage);
    }

    private static class TestEntity extends AbstractEntity<String, Int32Value> {
        protected TestEntity(String id) {
            super(id);
        }
    }
}
