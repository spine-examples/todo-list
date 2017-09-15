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

import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.AggregateStorage;
import io.spine.server.aggregate.AggregateStorageShould;
import io.spine.server.entity.Entity;
import io.spine.test.aggregate.ProjectId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;

import static io.spine.server.storage.kafka.given.KafkaStorageTestEnv.getStorageFactory;

/**
 * @author Dmytro Dashenkov
 */
@DisplayName("KafkaAggregateStorage should")
public class KafkaAggregateStorageTest extends AggregateStorageShould {

    private static final KafkaStorageFactory storageFactory = getStorageFactory();

    @Override
    protected <I> AggregateStorage<I> getStorage(Class<? extends I> idClass,
                                                 Class<? extends Aggregate<I, ?, ?>> aggCls) {
        return storageFactory.createAggregateStorage(aggCls);
    }

    @Override
    protected AggregateStorage<ProjectId> getStorage(Class<? extends Entity> cls) {
        return getStorage();
    }

    @Disabled("tests unimplemented index() method")
    @Override
    public void index_all_IDs() {
        super.index_all_IDs();
    }

    @Disabled("tests unimplemented index() method")
    @Override
    public void have_immutable_index() {
        super.have_immutable_index();
    }

    @Disabled("tests unimplemented index() method")
    @Override
    public void have_index_on_ID() {
        super.have_index_on_ID();
    }
}
