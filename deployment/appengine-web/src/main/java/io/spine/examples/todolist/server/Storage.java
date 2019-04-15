/*
 * Copyright (c) 2000-2019 TeamDev. All rights reserved.
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */

package io.spine.examples.todolist.server;

import com.google.auth.Credentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.common.annotations.VisibleForTesting;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.datastore.DatastoreStorageFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.server.GoogleAuth.serviceAccountCredentials;
import static io.spine.server.DeploymentType.APPENGINE_CLOUD;
import static io.spine.server.ServerEnvironment.getDeploymentType;

/**
 * Factory of {@link StorageFactory} instances.
 */
final class Storage {

    static final String LOCAL_PROJECT = "spine-dev";
    static final String LOCAL_DATASTORE_HOST = "localhost:8081";

    /**
     * Prevents the utility class instantiation.
     */
    private Storage() {
    }

    /**
     * Creates a new storage factory.
     *
     * @param multitenant
     *         {@code true} if the request context is multitenant, {@code false} otherwise
     * @return new storage factory
     */
    static StorageFactory createStorage(boolean multitenant) {
        Credentials credentials = serviceAccountCredentials();
        Datastore datastore = datastoreOptions(credentials).getService();
        return DatastoreStorageFactory
                .newBuilder()
                .setDatastore(datastore)
                .setMultitenant(multitenant)
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
        if (getDeploymentType() == APPENGINE_CLOUD) {
            return DatastoreOptions
                    .newBuilder()
                    .setCredentials(credentials)
                    .build();
        }
        return DatastoreOptions
                .newBuilder()
                .setProjectId(LOCAL_PROJECT)
                .setHost(LOCAL_DATASTORE_HOST)
                .build();
    }
}
