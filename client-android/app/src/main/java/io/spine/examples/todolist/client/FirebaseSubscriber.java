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
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;

/**
 * The Firebase client for subscribing to the data in Cloud Firestore.
 *
 * @author Dmytro Dashenkov
 */
public class FirebaseSubscriber {

    private static final String ID_KEY = "id";
    private static final String BYTES_KEY = "bytes";

    private static final String TAG = FirebaseSubscriber.class.getSimpleName();

    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    private static final FirebaseSubscriber instance = new FirebaseSubscriber();

    /**
     * Retrieves an instance of {@code FirebaseSubscriber}.
     */
    public static FirebaseSubscriber instance() {
        return instance;
    }

    /** Prevent direct instantiation. */
    private FirebaseSubscriber() {}

    /**
     * Subscribes to the entity states of the given type.
     *
     * <p>The method returns a {@link LiveData} of map (string ID -> entity state). The ID is
     * the {@linkplain io.spine.Identifier#toString(Object) string representation} of
     * the corresponding entity ID.
     *
     * <p>Currently, the element removal is not supported. If a {@link DocumentChange} of type other
     * than {@link DocumentChange.Type#ADDED ADDED} of {@link DocumentChange.Type#MODIFIED MODIFIED}
     * is encountered, an {@link UnsupportedOperationException} is thrown.
     *
     * @param targetType the class of the entity to subscribe to
     * @param <T>        the type of the entity to subscribe to
     * @return an instance of {@link LiveData} for the observers to subscribe to
     */
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

    /**
     * Subscribes to the single entity state of the given type.
     *
     * <p>If multiple records of the given type are found in Firestore,
     * an {@link IllegalStateException} is thrown.
     *
     * <p>If no records of the given type are found in Firestore, the update is ignored (i.e.
     * the resulting {@link LiveData} is not triggered).
     *
     * <p>Currently, the element removal is not supported. If a {@link DocumentChange} of type other
     * than {@link DocumentChange.Type#ADDED ADDED} of {@link DocumentChange.Type#MODIFIED MODIFIED}
     * is encountered, an {@link UnsupportedOperationException} is thrown.
     *
     * @param targetType the class of the entity state to subscribe to
     * @param <T>        the type of the entity state to subscribe to
     * @return a instance of {@link LiveData} for the observers to subscribe to
     */
    @SuppressWarnings("UnnecessaryReturnStatement") // OK for a fast exit on invalid data.
    public <T extends Message> LiveData<T> subscribeToSingle(Class<T> targetType) {
        final MutableLiveData<T> liveData = new MutableLiveData<>();
        final LiveData<Map<String, T>> allRecordsData = subscribeTo(targetType);
        allRecordsData.observeForever(map -> {
            if (map == null || map.isEmpty()) {
                return;
            } else if (map.size() > 1) {
                throw newIllegalStateException("Type %s has multiple instances.", targetType);
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

    /**
     * Delivers the entity state update represented by the given {@link DocumentChange} to
     * the observers of the given {@link LiveData}.
     *
     * @param change      the Firestore document change
     * @param destination the update publishing target
     * @param parser      the {@link Parser} for the target entity state type
     * @param <T>         the entity state type
     */
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

    private static String parseMessageId(DocumentSnapshot doc) {
        final String id = doc.getString(ID_KEY);
        return id;
    }

    private static <T extends Message> Parser<T> getParserFor(Class<T> type) {
        final T mockInstance = Messages.newInstance(type);
        @SuppressWarnings("unchecked") final Parser<T> result = (Parser<T>) mockInstance.getParserForType();
        return result;
    }
}
