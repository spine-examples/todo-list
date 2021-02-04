/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.server.appengine;

import com.google.auth.oauth2.GoogleCredentials;
import io.spine.server.DeploymentType;
import io.spine.server.ServerEnvironment;

import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.server.DeploymentType.APPENGINE_CLOUD;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * A factory of various forms of Google API credentials.
 */
final class GoogleAuth {

    /**
     * Prevents the utility class instantiation.
     */
    private GoogleAuth() {
    }

    /**
     * Obtains the service account credential.
     *
     * <p>When running under AppEngine, returns the default service account of this application.
     * Otherwise, reads and returns the service account credential from
     * the {@code service-account.json} resource.
     *
     * <p>This credential is used for accessing the GCP APIs, such as the Could Datastore API.
     */
    static GoogleCredentials serviceAccountCredentials() {
        if (deploymentType() == APPENGINE_CLOUD) {
            return propagateIoErrors(GoogleCredentials::getApplicationDefault);
        } else {
            String credentialsResource = Configuration.instance()
                                                      .serviceAccCredentialsResource();
            InputStream inputStream =
                    readResource(credentialsResource);
            GoogleCredentials credential = propagateIoErrors(
                    () -> GoogleCredentials.fromStream(inputStream)
            );
            return credential;
        }
    }

    private static DeploymentType deploymentType() {
        return ServerEnvironment.instance()
                                .deploymentType();
    }

    private static InputStream readResource(String name) {
        InputStream secret = GoogleAuth.class.getClassLoader()
                                             .getResourceAsStream(name);
        checkNotNull(secret, "%s resource is missing.", name);
        return secret;
    }

    private static <T> T propagateIoErrors(IoOperation<T> operation) {
        try {
            return operation.perform();
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * An I/O operation.
     *
     * <p>This operation may produce an {@link IOException} or return a result. An example is
     * reading a file from classpath.
     *
     * @param <T>
     *         the type of the operation result
     */
    @FunctionalInterface
    private interface IoOperation<T> {

        /**
         * Performs the operation.
         *
         * @return result of the operation
         * @throws IOException
         *         if an I/O error occurs
         */
        T perform() throws IOException;
    }
}
