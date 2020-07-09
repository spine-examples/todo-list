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

import com.google.cloud.datastore.DatastoreOptions;
import io.spine.examples.todolist.server.appengine.given.StorageTestEnv.EmptyCredentials;
import io.spine.server.ServerEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.server.DeploymentType.APPENGINE_CLOUD;
import static io.spine.server.DeploymentType.APPENGINE_EMULATOR;
import static io.spine.server.DeploymentType.STANDALONE;

@DisplayName("Storage should")
class StorageTest {

    protected static final ServerEnvironment serverEnvironment = ServerEnvironment.instance();

    @Nested
    @DisplayName("provide DatastoreOptions for")
    class ProvideDatastoreOptions {

        @AfterEach
        void tearDown() {
            serverEnvironment.reset();
        }

        @Test
        @DisplayName("on App Engine cloud infrastructure")
        void onGaeCloud() {
            serverEnvironment.configureDeployment(() -> APPENGINE_CLOUD);
            DatastoreOptions options = Storage.datastoreOptions(new EmptyCredentials());
            assertThat(options.getHost())
                    .isEqualTo("https://datastore.googleapis.com");
        }

        @Test
        @DisplayName("on App Engine emulator")
        void onGaeEmulator() {
            serverEnvironment.configureDeployment(() -> APPENGINE_EMULATOR);
            DatastoreOptions options = Storage.datastoreOptions(new EmptyCredentials());

            assertThat(options.getHost())
                    .isEqualTo(Storage.LOCAL_DATASTORE_HOST);
            assertThat(options.getProjectId())
                    .isEqualTo(Configuration.instance().projectId());
        }

        @Test
        @DisplayName("in standalone mode")
        void inStandaloneMode() {
            serverEnvironment.configureDeployment(() -> STANDALONE);
            DatastoreOptions options = Storage.datastoreOptions(new EmptyCredentials());

            assertThat(options.getHost())
                    .isEqualTo(Storage.LOCAL_DATASTORE_HOST);
            assertThat(options.getProjectId())
                    .isEqualTo(Configuration.instance().projectId());
        }
    }
}