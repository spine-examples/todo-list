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

package io.spine.server;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteBatch;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.Identifier;
import io.spine.client.EntityStateUpdate;
import io.spine.server.storage.StorageField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

import static com.google.cloud.firestore.Blob.fromBytes;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.of;
import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.server.FirestoreEntityStateUpdatePublisher.EntityStateField.bytes;
import static io.spine.server.FirestoreEntityStateUpdatePublisher.EntityStateField.id;
import static java.util.regex.Pattern.compile;

/**
 * Publishes {@link EntityStateUpdate}s to Cloud Firestore.
 *
 * @author Dmytro Dashenkov
 */
final class FirestoreEntityStateUpdatePublisher {

    private static final Pattern INVALID_KEY_CHARS = compile("[^\\w\\d]");

    private final CollectionReference databaseSlice;

    FirestoreEntityStateUpdatePublisher(CollectionReference databaseSlice) {
        this.databaseSlice = checkNotNull(databaseSlice);
    }

    /**
     * Publishes the given {@link EntityStateUpdate}s into the Cloud Firestore.
     *
     * <p>Each {@code EntityStateUpdate} causes either a new document creation or an existent
     * document update under the given {@code CollectionReference}.
     *
     * @param updates updates to publish to the Firestore
     */
    void publish(Iterable<EntityStateUpdate> updates) {
        checkNotNull(updates);
        final WriteBatch batch = databaseSlice.getFirestore().batch();
        for (EntityStateUpdate update : updates) {
            write(batch, update);
        }
        batch.commit();
    }

    private void write(WriteBatch batch, EntityStateUpdate update) {
        final Any updateId = update.getId();
        final Any updateState = update.getState();
        final Message entityId = unpack(updateId);
        final Message message = unpack(updateState);
        final String stringId = Identifier.toString(entityId);
        final byte[] stateBytes = message.toByteArray();
        final Map<String, Object> data = of(bytes.toString(), fromBytes(stateBytes),
                                            id.toString(), stringId);
        final DocumentReference targetDocument = documentFor(stringId);
        log().info("Writing state update of type {} (id: {}) into Firestore location {}.",
                   updateState.getTypeUrl(), stringId, targetDocument.getPath());
        batch.set(targetDocument, data);
    }

    private DocumentReference documentFor(String entityId) {
        final String documentKey = escapeKey(entityId);
        final DocumentReference result = databaseSlice.document(documentKey);
        return result;
    }

    private static String escapeKey(String dirtyKey) {
        final String trimmedKey = trimUnderscore(dirtyKey);
        final String result = INVALID_KEY_CHARS.matcher(trimmedKey)
                                               .replaceAll("");
        return result;
    }

    @SuppressWarnings("BreakStatement") // Natural in this case.
    private static String trimUnderscore(String key) {
        int underscoreCounter = 0;
        for (char character : key.toCharArray()) {
            if (character == '_') {
                underscoreCounter++;
            } else {
                break;
            }
        }
        final String result = underscoreCounter > 0
                              ? key.substring(underscoreCounter)
                              : key;
        return result;
    }

    /**
     * The list of fields of the entity state as it is stored to Firestore.
     */
    @VisibleForTesting
    enum EntityStateField implements StorageField {

        /**
         * The string field for the entity ID.
         *
         * <p>The ID is converted to {@code String} by the rules of
         * {@link io.spine.Identifier#toString(Object) Identifier.toString(id)}.
         */
        id,

        /**
         * The byte array representation of the entity state.
         *
         * @see Message#toByteArray()
         */
        bytes
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value =
                LoggerFactory.getLogger(FirestoreEntityStateUpdatePublisher.class);
    }
}
