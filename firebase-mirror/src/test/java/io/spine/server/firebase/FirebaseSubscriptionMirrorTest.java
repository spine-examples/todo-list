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

package io.spine.server.firebase;

import com.google.cloud.firestore.Blob;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.spine.Identifier;
import io.spine.client.ActorRequestFactory;
import io.spine.client.Topic;
import io.spine.core.ActorContext;
import io.spine.core.TenantId;
import io.spine.net.EmailAddress;
import io.spine.net.InternetDomain;
import io.spine.server.BoundedContext;
import io.spine.server.SubscriptionService;
import io.spine.server.firebase.given.FirebaseMirrorTestEnv;
import io.spine.string.Stringifier;
import io.spine.string.StringifierRegistry;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static com.google.common.collect.Sets.newHashSet;
import static io.spine.client.TestActorRequestFactory.newInstance;
import static io.spine.server.firebase.FirestoreEntityStateUpdatePublisher.EntityStateField.bytes;
import static io.spine.server.firebase.FirestoreEntityStateUpdatePublisher.EntityStateField.id;
import static io.spine.server.firebase.given.FirebaseMirrorTestEnv.createBoundedContext;
import static io.spine.server.firebase.given.FirebaseMirrorTestEnv.createTask;
import static io.spine.server.firebase.given.FirebaseMirrorTestEnv.newId;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The {@link FirebaseSubscriptionMirror} tests.
 *
 * <p>These tests should be executed on CI only, as they rely on the {@code serviceAccount.json}
 * which is stored encrypted in the Git repository and is decrypted on CI with private environment
 * keys.
 *
 * <p>To run the tests locally, go to the Firebase console, create a new service account and save
 * the generated {@code .json} file as
 * {@code firebase-mirror/src/test/resources/serviceAccount.json}. Then run the tests from IDE.
 *
 * @author Dmytro Dashenkov
 */
@SuppressWarnings("ClassWithTooManyMethods")
@Tag("CI")
@DisplayName("FirebaseSubscriptionMirror should")
class FirebaseSubscriptionMirrorTest {

    /**
     * The {@link Firestore} instance to access from the mirror.
     *
     * <p>This field is declared {@code static} to make it accessible in {@link AfterAll @AfterAll}
     * methods for the test data clean up.
     */
    private static final Firestore firestore = FirebaseMirrorTestEnv.getFirestore();

    private final ActorRequestFactory requestFactory =
            newInstance(FirebaseSubscriptionMirrorTest.class);
    private FirebaseSubscriptionMirror mirror;
    private BoundedContext boundedContext;
    private SubscriptionService subscriptionService;

    /**
     * Stores all the {@link DocumentReference} instances used for the test suite.
     *
     * <p>It is required to clean up all the data in Cloud Firestore to avoid test failures.
     */
    private static final Collection<DocumentReference> documents = newHashSet();

    @AfterAll
    static void afterAll() throws ExecutionException, InterruptedException {
        final WriteBatch batch = firestore.batch();
        for (DocumentReference document : documents) {
            batch.delete(document);
        }
        // Submit the depletion operations and ensure execution.
        batch.commit()
             .get();
        documents.clear();
    }

    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    @BeforeEach
    void beforeEach() {
        initializeEnvironment(false);
    }

    @Test
    @DisplayName("not allow nulls on construction")
    void testBuilderNotNull() {
        new NullPointerTester()
                .testAllPublicInstanceMethods(FirebaseSubscriptionMirror.newBuilder());
    }

    @Test
    @DisplayName("not allow null arguments")
    void testNotNull() {
        new NullPointerTester()
                .setDefault(TenantId.class, TenantId.getDefaultInstance())
                .setDefault(Topic.class, Topic.getDefaultInstance())
                .testAllPublicInstanceMethods(mirror);
    }

    @Test
    @DisplayName("accept only one of Firestore of DocumentReference on construction")
    void testAcceptOnlyOneLocation() {
        final DocumentReference location = firestore.collection("test_collection")
                                                    .document("test_document");
        final FirebaseSubscriptionMirror.Builder builder =
                FirebaseSubscriptionMirror.newBuilder()
                                          .setSubscriptionService(subscriptionService)
                                          .setFirestore(firestore)
                                          .addBoundedContext(boundedContext)
                                          .setFirestoreDocument(location);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("require at least one BoundedContext on construction")
    void testRequireBCInstance() {
        final FirebaseSubscriptionMirror.Builder builder =
                FirebaseSubscriptionMirror.newBuilder()
                                          .setSubscriptionService(subscriptionService)
                                          .setFirestore(firestore);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("deliver the entity state updates")
    void testDeliver() throws ExecutionException, InterruptedException {
        final Topic topic = requestFactory.topic().allOf(FRCustomer.class);
        mirror.reflect(topic);
        final FRCustomerId customerId = newId();
        final FRCustomer expectedState = createTask(customerId, boundedContext);
        FirebaseMirrorTestEnv.waitForConsistency();
        final FRCustomer actualState = findCustomer(customerId, firestore::collection);
        assertEquals(expectedState, actualState);
    }

    @Test
    @DisplayName("transform ID to string with the proper Stringifier")
    void testStringifyId() throws ExecutionException, InterruptedException {
        FirebaseMirrorTestEnv.registerSessionIdStringifier();
        final Topic topic = requestFactory.topic().allOf(FRSession.class);
        mirror.reflect(topic);
        final FRSessionId sessionId = FirebaseMirrorTestEnv.newSessionId();
        FirebaseMirrorTestEnv.createSession(sessionId, boundedContext);
        FirebaseMirrorTestEnv.waitForConsistency();
        final DocumentSnapshot document = findDocument(FRSession.class,
                                                       sessionId,
                                                       firestore::collection);
        final String actualId = document.getString(id.toString());
        final Stringifier<FRSessionId> stringifier =
                StringifierRegistry.getInstance()
                                   .<FRSessionId>get(FRSessionId.class)
                                   .orNull();
        assertNotNull(stringifier);
        final FRSessionId readId = stringifier.reverse().convert(actualId);
        assertEquals(sessionId, readId);
    }

    @Test
    @DisplayName("partition records of different tenants")
    void testMultitenancy() throws ExecutionException, InterruptedException {
        initializeEnvironment(true);
        final InternetDomain tenantDomain = InternetDomain.newBuilder()
                                                          .setValue("example.org")
                                                          .build();
        final EmailAddress tenantEmail = EmailAddress.newBuilder()
                                                     .setValue("user@example.org")
                                                     .build();
        final TenantId firstTenant = TenantId.newBuilder()
                                             .setDomain(tenantDomain)
                                             .build();
        final TenantId secondTenant = TenantId.newBuilder()
                                              .setEmail(tenantEmail)
                                              .build();
        boundedContext.getTenantIndex()
                      .keep(firstTenant);
        final Topic topic = requestFactory.topic().allOf(FRCustomer.class);
        mirror.reflect(topic);
        final FRCustomerId customerId = newId();
        createTask(customerId, boundedContext, secondTenant);
        FirebaseMirrorTestEnv.waitForConsistency();
        final Optional<?> document = tryFindDocument(FRCustomer.class,
                                                     customerId,
                                                     firestore::collection);
        assertFalse(document.isPresent());
    }

    @Test
    @DisplayName("allow to specify a custom document to work with")
    void testDeliverWithCustomLocation() throws ExecutionException, InterruptedException {
        final DocumentReference customLocation = firestore.document("custom/location");
        final FirebaseSubscriptionMirror mirror =
                FirebaseSubscriptionMirror.newBuilder()
                                          .setSubscriptionService(subscriptionService)
                                          .setFirestoreDocument(customLocation)
                                          .addBoundedContext(boundedContext)
                                          .build();
        final Topic topic = requestFactory.topic().allOf(FRCustomer.class);
        mirror.reflect(topic);
        final FRCustomerId customerId = newId();
        final FRCustomer expectedState = createTask(customerId, boundedContext);
        FirebaseMirrorTestEnv.waitForConsistency();
        final FRCustomer actualState = findCustomer(customerId, customLocation::collection);
        assertEquals(expectedState, actualState);
    }

    @Test
    @DisplayName("allow to specify a custom document per topic")
    void testCustomLocator() throws ExecutionException, InterruptedException {
        final Function<Topic, DocumentReference> locator = topic -> {
            final ActorContext context = topic.getContext();
            final String userId = context.getActor().getValue();
            return firestore.collection("user_subscriptions").document(userId);
        };
        final FirebaseSubscriptionMirror mirror =
                FirebaseSubscriptionMirror.newBuilder()
                                          .setSubscriptionService(subscriptionService)
                                          .setLocatorFunction(locator)
                                          .addBoundedContext(boundedContext)
                                          .build();
        final Topic topic = requestFactory.topic().allOf(FRCustomer.class);
        mirror.reflect(topic);
        final FRCustomerId customerId = newId();
        final FRCustomer expectedState = createTask(customerId, boundedContext);
        FirebaseMirrorTestEnv.waitForConsistency();
        final DocumentReference expectedDocument = locator.apply(topic);
        final FRCustomer actualState = findCustomer(customerId, expectedDocument::collection);
        assertEquals(expectedState, actualState);

    }

    private void initializeEnvironment(boolean multitenant) {
        final String name = FirebaseSubscriptionMirrorTest.class.getSimpleName();
        boundedContext = createBoundedContext(name, multitenant);
        subscriptionService = SubscriptionService.newBuilder()
                                                 .add(boundedContext)
                                                 .build();
        mirror = FirebaseSubscriptionMirror.newBuilder()
                                           .setFirestore(firestore)
                                           .setSubscriptionService(subscriptionService)
                                           .addBoundedContext(boundedContext)
                                           .build();
    }

    /**
     * Finds a {@code FRCustomer} with the given ID.
     *
     * <p>The collection of {@code FRCustomer} records is retrieved with the given
     * {@code collectionAccess} function.
     *
     * <p>Note that the {@code collectionAccess} accepts a short name of the collection (not
     * the whole path).
     *
     * @param id               the {@code FRCustomer} ID to search by
     * @param collectionAccess a function retrieving
     *                         the {@linkplain CollectionReference collection} which holds the
     *                         {@code FRCustomer}
     * @return the found {@code FRCustomer}
     */
    private static FRCustomer findCustomer(FRCustomerId id,
                                           Function<String, CollectionReference> collectionAccess)
            throws ExecutionException,
                   InterruptedException {
        final DocumentSnapshot document = findDocument(FRCustomer.class, id, collectionAccess);
        final FRCustomer customer = deserialize(document);
        return customer;
    }

    /**
     * Finds a {@link DocumentReference} containing the given ID.
     *
     * <p>Unlike {@link #tryFindDocument(Class, Message, Function)}, this method throws
     * a {@link NoSuchElementException} if the searched document is not found.
     *
     * @see #tryFindDocument(Class, Message, Function)
     */
    private static DocumentSnapshot
    findDocument(Class<? extends Message> msgClass, Message id,
                 Function<String, CollectionReference> collectionAccess)
            throws ExecutionException,
                   InterruptedException {
        return tryFindDocument(msgClass, id, collectionAccess)
                .orElseThrow(() -> new NoSuchElementException(
                        format("Record with ID %s not found", id)));
    }

    /**
     * Finds a {@link DocumentReference} containing the given ID.
     *
     * <p>The document is looked up in the {@linkplain CollectionReference collection} returned by
     * the given {@code collectionAccess} function. The collection should have the Protobuf type
     * name of the message of the specified {@code msgClass}.
     *
     * @param msgClass         the type of the message stored in the searched document
     * @param id               the ID of the message stored in the searched document
     * @param collectionAccess a function retrieving
     *                         the {@linkplain CollectionReference collection} which holds the
     *                         document
     * @return the searched document or {@code Optional.empty()} if no such document is found
     */
    private static Optional<DocumentSnapshot>
    tryFindDocument(Class<? extends Message> msgClass, Message id,
                    Function<String, CollectionReference> collectionAccess)
            throws ExecutionException,
                   InterruptedException {
        final TypeUrl typeUrl = TypeUrl.of(msgClass);
        final String collectionName = typeUrl.getPrefix() + '_' + typeUrl.getTypeName();
        final QuerySnapshot collection = collectionAccess.apply(collectionName)
                                                         .get().get();
        final Optional<DocumentSnapshot> result =
                collection.getDocuments()
                          .stream()
                          .peek(document -> documents.add(document.getReference()))
                          .filter(document -> idEquals(document, id))
                          .findAny();
        return result;
    }

    private static boolean idEquals(DocumentSnapshot document, Message customerId) {
        final Object actualId = document.get(id.toString());
        final String expectedIdString = Identifier.toString(customerId);
        return expectedIdString.equals(actualId);
    }

    private static FRCustomer deserialize(DocumentSnapshot document) {
        final Blob blob = document.getBlob(bytes.toString());
        assertNotNull(blob);
        final byte[] bytes = blob.toBytes();
        try {
            final FRCustomer result = FRCustomer.parseFrom(bytes);
            return result;
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
