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
import io.spine.core.Command;
import io.spine.core.CommandEnvelope;
import io.spine.core.TenantId;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.aggregate.TaskPart;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.server.entity.Repository;
import io.spine.server.stand.Stand;
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
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Sets.newHashSet;
import static io.spine.Identifier.newUuid;
import static io.spine.server.FirestoreEntityStateUpdatePublisher.EntityStateField.bytes;
import static io.spine.server.FirestoreEntityStateUpdatePublisher.EntityStateField.id;
import static io.spine.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assume.assumeNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The {@link FirebaseEndpoint} tests.
 *
 * <p>These tests should be executed on CI only, as they rely on the {@code serviceAccount.json}
 * which is stored encrypted in the Git repository and is decrypted on CI with private environment
 * keys.
 *
 * <p>To run the tests locally, go to the Firebase console, create a new service account and save
 * the generated {@code .json} file as
 * {@code firebase-endpoint/src/test/resources/serviceAccount.json}. Then run the tests from IDE.
 *
 * @author Dmytro Dashenkov
 */
@Tag("CI")
@DisplayName("FirebaseEndpoint should")
class FirebaseEndpointTest {

    private static final String FIREBASE_SERVICE_ACC_SECRET = "serviceAccount.json";
    private static final String DATABASE_URL = "https://spine-firestore-test.firebaseio.com";

    /**
     * The {@link Firestore} instance to access from the endpoint.
     *
     * <p>This field is declared {@code static} to make it accessible in {@link AfterAll @AfterAll}
     * methods for the test data clean up.
     */
    private static Firestore firestore = null;

    private final ActorRequestFactory requestFactory =
            TestActorRequestFactory.newInstance(FirebaseEndpointTest.class);
    private FirebaseEndpoint endpoint;
    private BoundedContext boundedContext;

    /**
     * Stores all the {@link DocumentReference} instances used for the test suite.
     *
     * <p>It is required to clean up all the data in Cloud Firestore to avoid test failures.
     */
    private static final Collection<DocumentReference> documents = newHashSet();

    @BeforeAll
    static void beforeAll() throws IOException {
        final InputStream firebaseSecret =
                FirebaseEndpointTest.class.getClassLoader()
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
        boundedContext = BoundedContexts.create();
        final SubscriptionService subscriptionService = SubscriptionService.newBuilder()
                                                                           .add(boundedContext)
                                                                           .build();
        endpoint = FirebaseEndpoint.newBuilder()
                                   .setDatabase(firestore)
                                   .setSubscriptionService(subscriptionService)
                                   .build();
    }

    @Test
    @DisplayName("not allow nulls on construction")
    void testBuilderNotNull() {
        new NullPointerTester().testAllPublicInstanceMethods(FirebaseEndpoint.newBuilder());
    }

    @Test
    @DisplayName("not allow null arguments")
    void testNotNull() {
        new NullPointerTester().testAllPublicInstanceMethods(endpoint);
    }

    @Test
    @DisplayName("deliver the entity state updates")
    void testDeliver() throws ExecutionException, InterruptedException {
        final Topic topic = requestFactory.topic().allOf(Task.class);
        endpoint.subscribe(topic);
        final TaskId taskId = newId();
        final Task expectedState = createTask(taskId);
        waitForConsistency();
        final Task actualState = findTask(taskId);
        assertEquals(expectedState, actualState);
    }

    @Test
    @DisplayName("transform ID to string with the proper Stringifier")
    void testStringifyId() throws ExecutionException, InterruptedException {
        registerTaskIdStringifier();
        final Topic topic = requestFactory.topic().allOf(Task.class);
        endpoint.subscribe(topic);
        final TaskId taskId = newId();
        createTask(taskId);
        waitForConsistency();
        final DocumentSnapshot document = findTaskDocument(taskId);
        final String actualId = document.getString(id.toString());
        final Stringifier<TaskId> stringifier = StringifierRegistry.getInstance()
                                                                  .<TaskId>get(TaskId.class)
                                                                  .orNull();
        assertNotNull(stringifier);
        final TaskId readId = stringifier.reverse().convert(actualId);
        assertEquals(taskId, readId);
    }

    private static Task findTask(TaskId id) throws ExecutionException, InterruptedException {
        final DocumentSnapshot document = findTaskDocument(id);
        final Task task = deserialize(document);
        return task;
    }

    private static DocumentSnapshot findTaskDocument(TaskId taskId) throws ExecutionException,
                                                                    InterruptedException {
        final String collectionName = TypeName.of(Task.class)
                                              .value();
        final QuerySnapshot collection = firestore.collection(collectionName).get().get();
        final Collection<DocumentSnapshot> messages =
                collection.getDocuments()
                          .stream()
                          .peek(document -> documents.add(document.getReference()))
                          .filter(document -> idEquals(document, taskId))
                          .collect(toSet());
        assertEquals(1, messages.size());
        final DocumentSnapshot document = messages.iterator().next();
        return document;
    }

    private Task createTask(TaskId taskId) {
        @SuppressWarnings("unchecked")
        final Repository<TaskId, TaskPart> taskRepository =
                boundedContext.findRepository(Task.class).orNull();
        assertNotNull(taskRepository);
        final TaskPart aggregateInstance = taskRepository.create(taskId);
        final TaskDescription description = TaskDescription.newBuilder()
                                                           .setValue("Test description")
                                                           .build();
        final CreateBasicTask cmd = CreateBasicTask.newBuilder()
                                                   .setId(taskId)
                                                   .setDescription(description)
                                                   .build();
        final Command command = requestFactory.command()
                                              .create(cmd);
        dispatchCommand(aggregateInstance, CommandEnvelope.of(command));
        final Stand stand = boundedContext.getStand();
        stand.post(tenant(), aggregateInstance);
        return aggregateInstance.getState();
    }

    private static void registerTaskIdStringifier() {
        final Stringifier<TaskId> stringifier = new Stringifier<TaskId>() {
            @Override
            protected String toString(TaskId genericId) {
                return genericId.getValue();
            }

            @Override
            protected TaskId fromString(String stringId) {
                return TaskId.newBuilder()
                             .setValue(stringId)
                             .build();
            }
        };
        StringifierRegistry.getInstance().register(stringifier, TaskId.class);
    }

    private static boolean idEquals(DocumentSnapshot document, TaskId taskId) {
        final Object actualId = document.get(id.toString());
        final String expectedIdString = Stringifiers.toString(taskId);
        return expectedIdString.equals(actualId);
    }

    private static Task deserialize(DocumentSnapshot document) {
        final Blob blob = document.getBlob(bytes.toString());
        assertNotNull(blob);
        final byte[] bytes = blob.toBytes();
        try {
            final Task task = Task.parseFrom(bytes);
            return task;
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static TenantId tenant() {
        return TenantId.newBuilder()
                       .setValue(FirebaseEndpointTest.class.getSimpleName())
                       .build();
    }

    private static TaskId newId() {
        return TaskId.newBuilder()
                     .setValue(newUuid())
                     .build();
    }

    private static void waitForConsistency() {
        try {
            Thread.sleep(2500L);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}
