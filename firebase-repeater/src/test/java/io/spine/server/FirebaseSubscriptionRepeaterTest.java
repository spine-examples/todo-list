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

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Blob;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.common.testing.NullPointerTester;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.protobuf.InvalidProtocolBufferException;
import io.spine.client.ActorRequestFactory;
import io.spine.client.TestActorRequestFactory;
import io.spine.client.Topic;
import io.spine.core.BoundedContextName;
import io.spine.core.TenantId;
import io.spine.net.EmailAddress;
import io.spine.net.InternetDomain;
import io.spine.server.given.FirebaseRepeaterTestEnv;
import io.spine.server.storage.StorageFactory;
import io.spine.server.tenant.TenantAwareOperation;
import io.spine.string.Stringifier;
import io.spine.string.StringifierRegistry;
import io.spine.string.Stringifiers;
import io.spine.type.TypeName;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Sets.newHashSet;
import static io.spine.server.BoundedContext.newName;
import static io.spine.server.FirestoreEntityStateUpdatePublisher.EntityStateField.bytes;
import static io.spine.server.FirestoreEntityStateUpdatePublisher.EntityStateField.id;
import static io.spine.server.given.FirebaseRepeaterTestEnv.createTask;
import static io.spine.server.given.FirebaseRepeaterTestEnv.newId;
import static io.spine.server.given.FirebaseRepeaterTestEnv.registerTaskIdStringifier;
import static io.spine.server.storage.memory.InMemoryStorageFactory.newInstance;
import static org.junit.Assume.assumeNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The {@link FirebaseSubscriptionRepeater} tests.
 *
 * <p>These tests should be executed on CI only, as they rely on the {@code serviceAccount.json}
 * which is stored encrypted in the Git repository and is decrypted on CI with private environment
 * keys.
 *
 * <p>To run the tests locally, go to the Firebase console, create a new service account and save
 * the generated {@code .json} file as
 * {@code firebase-repeater/src/test/resources/serviceAccount.json}. Then run the tests from IDE.
 *
 * @author Dmytro Dashenkov
 */
@SuppressWarnings("ClassWithTooManyMethods")
@Tag("CI")
@DisplayName("FirebaseSubscriptionRepeater should")
class FirebaseSubscriptionRepeaterTest {

    private static final String FIREBASE_SERVICE_ACC_SECRET = "serviceAccount.json";
    private static final String DATABASE_URL = "https://spine-firestore-test.firebaseio.com";

    /**
     * The {@link Firestore} instance to access from the repeater.
     *
     * <p>This field is declared {@code static} to make it accessible in {@link AfterAll @AfterAll}
     * methods for the test data clean up.
     */
    private static Firestore firestore = null;

    private final ActorRequestFactory requestFactory =
            TestActorRequestFactory.newInstance(FirebaseSubscriptionRepeaterTest.class);
    private FirebaseSubscriptionRepeater repeater;
    private BoundedContext boundedContext;

    /**
     * Stores all the {@link DocumentReference} instances used for the test suite.
     *
     * <p>It is required to clean up all the data in Cloud Firestore to avoid test failures.
     */
    private static final Collection<DocumentReference> documents = newHashSet();

    @BeforeAll
    static void beforeAll() throws IOException {
        final InputStream firebaseSecret = FirebaseSubscriptionRepeaterTest.class
                .getClassLoader()
                .getResourceAsStream(FIREBASE_SERVICE_ACC_SECRET);
        // Check if `serviceAccount.json` file exists.
        assumeNotNull(firebaseSecret);
        final GoogleCredentials credentials = GoogleCredentials.fromStream(firebaseSecret);
        final FirebaseOptions options = new FirebaseOptions.Builder()
                .setDatabaseUrl(DATABASE_URL)
                .setCredentials(credentials)
                .build();
        FirebaseApp.initializeApp(options);
    }

    @AfterAll
    static void afterAll() throws ExecutionException, InterruptedException {
        final WriteBatch batch = firestore.batch();
        for (DocumentReference document : documents) {
            batch.delete(document);
        }
        // Submit the depletion operations and ensure execution.
        batch.commit().get();
        documents.clear();
    }

    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    @BeforeEach
    void beforeEach() {
        firestore = FirestoreClient.getFirestore();
        init(false);
    }

    @Test
    @DisplayName("not allow nulls on construction")
    void testBuilderNotNull() {
        new NullPointerTester()
                .testAllPublicInstanceMethods(FirebaseSubscriptionRepeater.newBuilder());
    }

    @Test
    @DisplayName("not allow null arguments")
    void testNotNull() {
        new NullPointerTester().testAllPublicInstanceMethods(repeater);
    }

    @Test
    @DisplayName("deliver the entity state updates")
    void testDeliver() throws ExecutionException, InterruptedException {
        final Topic topic = requestFactory.topic().allOf(FRCustomer.class);
        repeater.broadcast(topic);
        final FRCustomerId customerId = newId();
        final FRCustomer expectedState = createTask(customerId, boundedContext);
        waitForConsistency();
        final FRCustomer actualState = findTask(customerId);
        assertEquals(expectedState, actualState);
    }

    @Test
    @DisplayName("transform ID to string with the proper Stringifier")
    void testStringifyId() throws ExecutionException, InterruptedException {
        registerTaskIdStringifier();
        final Topic topic = requestFactory.topic().allOf(FRCustomer.class);
        repeater.broadcast(topic);
        final FRCustomerId customerId = newId();
        createTask(customerId, boundedContext);
        waitForConsistency();
        final DocumentSnapshot document = findTaskDocument(customerId);
        final String actualId = document.getString(id.toString());
        final Stringifier<FRCustomerId> stringifier =
                StringifierRegistry.getInstance()
                                   .<FRCustomerId>get(FRCustomerId.class)
                                   .orNull();
        assertNotNull(stringifier);
        final FRCustomerId readId = stringifier.reverse().convert(actualId);
        assertEquals(customerId, readId);
    }

    @Test
    @DisplayName("partition records of different tenants")
    void testMultitenancy() throws ExecutionException, InterruptedException {
        init(true);
        registerTaskIdStringifier();
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
        final Topic topic = requestFactory.topic().allOf(FRCustomer.class);
        new TenantAwareOperation(firstTenant) {
            @Override
            public void run() {
                repeater.broadcast(topic);
            }
        }.execute();
        final FRCustomerId customerId = newId();
        createTask(customerId, boundedContext, secondTenant);
        waitForConsistency();
        final Optional<?> document = tryFindTaskDocument(customerId);
        assertFalse(document.isPresent());
    }

    private void init(boolean multitenant) {
        final BoundedContextName contextName =
                newName(FirebaseSubscriptionRepeaterTest.class.getSimpleName());
        final StorageFactory storageFactory = newInstance(contextName, multitenant);
        boundedContext = BoundedContext.newBuilder()
                                       .setName(contextName.getValue())
                                       .setMultitenant(multitenant)
                                       .setStorageFactorySupplier(() -> storageFactory)
                                       .build();
        boundedContext.register(new FirebaseRepeaterTestEnv.CustomerRepository());
        final SubscriptionService subscriptionService = SubscriptionService.newBuilder()
                                                                           .add(boundedContext)
                                                                           .build();
        repeater = FirebaseSubscriptionRepeater.newBuilder()
                                               .setDatabase(firestore)
                                               .setSubscriptionService(subscriptionService)
                                               .build();
    }

    private static FRCustomer findTask(FRCustomerId id)
            throws ExecutionException, InterruptedException {
        final DocumentSnapshot document = findTaskDocument(id);
        final FRCustomer customer = deserialize(document);
        return customer;
    }

    private static DocumentSnapshot findTaskDocument(FRCustomerId customerId)
            throws ExecutionException, InterruptedException {
        return tryFindTaskDocument(customerId)
                .orElseThrow(NoSuchElementException::new);
    }

    private static Optional<DocumentSnapshot> tryFindTaskDocument(FRCustomerId customerId)
            throws ExecutionException, InterruptedException {
        final String collectionName = TypeName.of(FRCustomer.class).value();
        final QuerySnapshot collection = firestore.collection(collectionName).get().get();
        final Optional<DocumentSnapshot> result =
                collection.getDocuments()
                          .stream()
                          .peek(document -> documents.add(document.getReference()))
                          .filter(document -> idEquals(document, customerId))
                          .findAny();
        return result;
    }

    private static boolean idEquals(DocumentSnapshot document, FRCustomerId customerId) {
        final Object actualId = document.get(id.toString());
        final String expectedIdString = Stringifiers.toString(customerId);
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

    private static void waitForConsistency() {
        try {
            Thread.sleep(2500L);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}
