/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.auth.Credentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.common.annotations.VisibleForTesting;
import io.spine.server.ServerEnvironment;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.datastore.DatastoreStorageFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.server.appengine.GoogleAuth.serviceAccountCredentials;
import static io.spine.server.DeploymentType.APPENGINE_CLOUD;

/**
 * Factory of {@link StorageFactory} instances.
 */
final class Storage {

    @VisibleForTesting
    static final String LOCAL_DATASTORE_HOST = "localhost:8081";

    /**
     * Prevents the utility class instantiation.
     */
    private Storage() {
    }

    /**
     * Creates a new storage factory.
     *
     * @return new storage factory
     */
    static StorageFactory createStorage() {
        Credentials credentials = serviceAccountCredentials();
        Datastore datastore = datastoreOptions(credentials).getService();
        return DatastoreStorageFactory
                .newBuilder()
                .setDatastore(datastore)
                .build();
    }

    /**
     * Provides {@linkplain DatastoreOptions} for the current environment.
     *
     * <p>If the environment is local AppEngine, then provides options
     * to be used along with the local Datastore emulator.
     */
    @VisibleForTesting
    static DatastoreOptions datastoreOptions(Credentials credentials) {
        checkNotNull(credentials);
        String projectId = Configuration.instance()
                                        .projectId();
        if (ServerEnvironment.instance().deploymentType() == APPENGINE_CLOUD) {
            return DatastoreOptions
                    .newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();
        }

        return DatastoreOptions
                .newBuilder()
                .setProjectId(projectId)
                .setHost(LOCAL_DATASTORE_HOST)
                .build();
    }
}
