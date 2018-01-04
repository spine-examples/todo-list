/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.server.firebase;

import com.google.cloud.firestore.Blob;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.spine.Identifier;
import io.spine.client.EntityStateUpdate;
import io.spine.server.firebase.FirestoreSubscriptionPublisher.EntityStateField;
import io.spine.server.firebase.given.FirebaseMirrorTestEnv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static io.spine.protobuf.AnyPacker.pack;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dmytro Dashenkov
 */
@DisplayName("Firestore publisher should")
@Tag("CI")
class FirebaseSubscriptionPublisherTest {

    private static final CollectionReference targetCollection =
            FirebaseMirrorTestEnv.getFirestore().collection("test_records");

    @Test
    @DisplayName("escape illegal chars in a document key")
    void testEscapeKey() throws ExecutionException,
                                InterruptedException,
                                InvalidProtocolBufferException {
        final FirestoreSubscriptionPublisher publisher =
                new FirestoreSubscriptionPublisher(targetCollection);
        final String rawId = "___&$id001%-_foobar";
        final String expectedId = "id001_foobar";
        final Any id = Identifier.pack(rawId);
        final FMCustomer expectedState = FMCustomer.newBuilder()
                                                   .setId(FirebaseMirrorTestEnv.newId())
                                                   .build();
        final Any state = pack(expectedState);
        final EntityStateUpdate update = EntityStateUpdate.newBuilder()
                                                          .setId(id)
                                                          .setState(state)
                                                          .build();
        publisher.publish(singleton(update));
        final DocumentSnapshot document = targetCollection.document(expectedId)
                                                          .get().get();
        final String entityStateId = document.getString(EntityStateField.id.toString());
        assertEquals(rawId, entityStateId);

        final Blob stateBlob = document.getBlob(EntityStateField.bytes.toString());
        assertNotNull(state);
        final FMCustomer actualState = FMCustomer.parseFrom(stateBlob.toBytes());
        assertEquals(expectedState, actualState);

        document.getReference().delete();
    }
}
