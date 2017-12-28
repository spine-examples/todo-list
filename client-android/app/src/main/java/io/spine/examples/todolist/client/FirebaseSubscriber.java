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

package io.spine.examples.todolist.client;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import io.spine.protobuf.Messages;
import io.spine.type.TypeUrl;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.firebase.firestore.DocumentChange.Type.ADDED;
import static com.google.firebase.firestore.DocumentChange.Type.MODIFIED;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.String.format;

public class FirebaseSubscriber {

    private static final String ID_KEY = "id";
    private static final String BYTES_KEY = "bytes";

    private static final String TAG = FirebaseSubscriber.class.getSimpleName();

    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    private static final FirebaseSubscriber instance = new FirebaseSubscriber();

    public static FirebaseSubscriber instance() {
        return instance;
    }

    private FirebaseSubscriber() {
    }

    public <T extends Message> LiveData<Map<String, T>> subscribeTo(Class<T> targetType) {
        checkNotNull(targetType);
        final CollectionReference targetCollection = collectionFor(targetType);
        final MutableLiveData<Map<String, T>> result = new MutableLiveData<>();
        targetCollection.addSnapshotListener((documentSnapshots, error) -> {
            if (error != null) {
                final String errorMsg = format(
                        "Error encountered while listening for the %s state updates.",
                        targetType
                );
                Log.e(TAG, errorMsg, error);
            } else {
                final Parser<T> parser = getParserFor(targetType);
                for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
                    deliverUpdate(change, result, parser);
                }
            }
        });
        return result;
    }

    public <T extends Message> LiveData<T> subscribeToSingle(Class<T> targetType) {
        final MutableLiveData<T> liveData = new MutableLiveData<>();
        final LiveData<Map<String, T>> allRecordsData = subscribeTo(targetType);
        allRecordsData.observeForever(map -> {
            if (map == null || map.isEmpty()) {
                return;
            } else if (map.size() > 1) {
                throw newIllegalArgumentException("Type %s has multiple instances.", targetType);
            } else {
                final Map.Entry<?, T> singleEntry = map.entrySet()
                                                       .iterator()
                                                       .next();
                final T singleData = singleEntry.getValue();
                liveData.postValue(singleData);
            }
        });
        return liveData;
    }

    private CollectionReference collectionFor(Class<? extends Message> type) {
        final TypeUrl typeUrl = TypeUrl.of(type);
        final String targetCollectionName = typeUrl.value()
                                                   .replace('/', '_');
        final CollectionReference result = database.collection(targetCollectionName);
        return result;
    }

    private static <T extends Message>
    void deliverUpdate(DocumentChange change,
                       MutableLiveData<Map<String, T>> destination,
                       Parser<T> parser) {
        final DocumentChange.Type type = change.getType();
        final Map<String, T> currentData = destination.getValue();
        final Map<String, T> newData = currentData == null
                                      ? newHashMap()
                                      : newHashMap(currentData);
        final DocumentSnapshot doc = change.getDocument();
        final String id = parseMessageId(doc);
        final T newMessage = parseMessage(doc, parser);

        if (type == ADDED || type == MODIFIED) {
            newData.put(id, newMessage);
        } else {
            throw new UnsupportedOperationException(type.toString());
        }

        destination.postValue(newData);
    }

    private static <T extends Message> T parseMessage(DocumentSnapshot doc, Parser<T> parser) {
        final Blob blob = doc.getBlob(BYTES_KEY);
        final byte[] bytes = blob.toBytes();
        try {
            final T message = parser.parseFrom(bytes);
            return message;
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T extends Message> Parser<T> getParserFor(Class<T> type) {
        final T mockInstance = Messages.newInstance(type);
        @SuppressWarnings("unchecked")
        final Parser<T> result = (Parser<T>) mockInstance.getParserForType();
        return result;
    }

    private static String parseMessageId(DocumentSnapshot doc) {
        final String id = doc.getString(ID_KEY);
        return id;
    }
}
