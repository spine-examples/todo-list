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

package io.spine.server.storage.kafka;

import io.spine.server.entity.Entity;
import io.spine.server.projection.Projection;
import io.spine.server.projection.ProjectionStorage;
import io.spine.server.projection.ProjectionStorageShould;
import io.spine.test.storage.ProjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static io.spine.server.storage.kafka.given.KafkaStorageTestEnv.getStorageFactory;

/**
 * @author Dmytro Dashenkov
 */
public class KafkaProjectionStorageShould extends ProjectionStorageShould {

    private static final KafkaStorageFactory storageFactory = getStorageFactory();

    @BeforeEach
    void beforeEach() {
        TestRoutines.beforeEach();
    }

    @AfterEach
    void afterEach() {
        TestRoutines.afterEach();
    }

    @Override
    protected ProjectionStorage<ProjectId> getStorage(Class<? extends Entity> entityClass) {
        @SuppressWarnings("unchecked") // Test invariant.
        final Class<? extends Projection<ProjectId, ?, ?>> cls =
                (Class<? extends Projection<ProjectId, ?, ?>>) entityClass;
        return storageFactory.createProjectionStorage(cls);
    }
}
