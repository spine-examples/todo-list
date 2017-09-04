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

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.spine.protobuf.AnyPacker;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Dmytro Dashenkov
 */
public class MessageSerializer implements Serializer<Message>, Deserializer<Message> {

    private MessageSerializer() {
        // Prevent direct instantiation and insure no parameter ctor (required by Kafka)
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // NoOp
    }

    @Override
    public Message deserialize(String topic, byte[] data) {
        checkNotNull(data);
        final Any packed;
        try {
            packed = Any.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException(e);
        }
        final Message result = AnyPacker.unpack(packed);
        return result;
    }

    @Override
    public byte[] serialize(String topic, Message data) {
        checkNotNull(data);
        final Any packed = AnyPacker.pack(data);
        final byte[] result = packed.toByteArray();
        return result;
    }

    @Override
    public void close() {
        // NoOp
    }

    public static Serializer<Message> serializer() {
        return Default.INSTANCE.value;
    }

    public static Deserializer<Message> deserializer() {
        return Default.INSTANCE.value;
    }

    private enum Default {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final MessageSerializer value = new MessageSerializer();
    }
}
