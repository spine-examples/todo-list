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

package io.spine.examples.todolist.server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A utility for working with the Firebase client API.
 *
 * @author Dmytro Dashenkov
 */
final class FirebaseClients {

    private static final String FIREBASE_SERVICE_ACC_SECRET = "serviceAccount.json";
    private static final String DATABASE_URL = "https://spine-firestore-test.firebaseio.com";

    /**
     * The {@code private} constructor prevents the utility class instantiation.
     */
    private FirebaseClients() {}

    /**
     * Initializes the Cloud Firestore API with the service account credentials.
     *
     * <p>To performs the initialization successfully, the configuration file
     * {@code serviceAccount.json} should be present in the classpath.
     *
     * @return the initialized instance of {@link Firestore}
     */
    public static Firestore initializeFirestore() {
        final InputStream firebaseSecret = FirebaseClients.class
                .getClassLoader()
                .getResourceAsStream(FIREBASE_SERVICE_ACC_SECRET);
        checkNotNull(firebaseSecret,
                     "Required credentials file '%s' does not exist.", FIREBASE_SERVICE_ACC_SECRET);
        final GoogleCredentials credentials;
        try {
            credentials = GoogleCredentials.fromStream(firebaseSecret);
        } catch (IOException e) {
            log().error("Error while reading Firebase config file.", e);
            throw new IllegalStateException(e);
        }
        final FirebaseOptions options = new FirebaseOptions.Builder()
                .setDatabaseUrl(DATABASE_URL)
                .setCredentials(credentials)
                .build();
        FirebaseApp.initializeApp(options);
        final Firestore firestore = FirestoreClient.getFirestore();
        return firestore;
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(FirebaseClients.class);
    }
}
