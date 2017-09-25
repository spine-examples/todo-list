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

import com.google.protobuf.Message;
import io.spine.Identifier;
import io.spine.server.entity.Entity;
import io.spine.server.storage.RecordStorageShould;
import io.spine.test.storage.Project;
import io.spine.test.storage.ProjectId;
import io.spine.test.storage.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static io.spine.server.storage.kafka.given.KafkaStorageTestEnv.getStorageFactory;

/**
 * @author Dmytro Dashenkov
 */
public class KafkaRecordStorageShould extends RecordStorageShould<ProjectId, KafkaRecordStorage<ProjectId>> {

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
    protected ProjectId newId() {
        final ProjectId projectId = ProjectId.newBuilder()
                                             .setId(Identifier.newUuid())
                                             .build();
        return projectId;
    }

    @Override
    protected Message newState(ProjectId projectId) {
        final Project project = Project.newBuilder()
                                       .setId(projectId)
                                       .setName("Some test name")
                                       .addTask(Task.getDefaultInstance())
                                       .setStatus(Project.Status.CREATED)
                                       .build();
        return project;
    }

    @Override
    protected KafkaRecordStorage<ProjectId> getStorage(Class<? extends Entity> entityClass) {
        @SuppressWarnings("unchecked") // Test invariant.
        final Class<? extends Entity<ProjectId, ?>> cls =
                (Class<? extends Entity<ProjectId, ?>>) entityClass;
        return storageFactory.createRecordStorage(cls);
    }
}
