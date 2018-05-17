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

import java.io.IOException;
import java.io.InputStream;

import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;

/**
 * The factory of credentials for the Firebase integration.
 *
 * @author Dmytro Dashenkov
 */
final class FirebaseCredentials {

    /**
     * The name of the credentials file stored in the classpath.
     */
    private static final String CREDENTIALS_FILE = "spine-dev.json";

    /**
     * Prevents the utility class instantiation.
     */
    private FirebaseCredentials() {
    }

    /**
     * Reads the credentials from the service account key file in classpath.
     *
     * @return the credentials for the Firebase access
     */
    static GoogleCredentials read() {
        final InputStream in = FirebaseClient.class.getClassLoader()
                                                   .getResourceAsStream(CREDENTIALS_FILE);
        try {
            final GoogleCredentials credentials = GoogleCredentials.fromStream(in);
            return credentials;
        } catch (IOException e) {
            throw illegalArgumentWithCauseOf(e);
        }
    }
}
