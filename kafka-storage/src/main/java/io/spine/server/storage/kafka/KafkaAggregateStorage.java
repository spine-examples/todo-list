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

import com.google.common.base.Optional;
import io.spine.server.aggregate.AggregateEventRecord;
import io.spine.server.aggregate.AggregateStorage;
import io.spine.server.entity.LifecycleFlags;

import java.util.Iterator;

/**
 * @author Dmytro Dashenkov
 */
public class KafkaAggregateStorage<I> extends AggregateStorage<I> {

    protected KafkaAggregateStorage(boolean multitenant) {
        super(multitenant);
    }

    @Override
    protected int readEventCountAfterLastSnapshot(I id) {
        return 0;
    }

    @Override
    protected void writeEventCountAfterLastSnapshot(I id, int eventCount) {

    }

    @Override
    protected void writeRecord(I id, AggregateEventRecord record) {

    }

    @Override
    protected Iterator<AggregateEventRecord> historyBackward(I id) {
        return null;
    }

    @Override
    public Optional<LifecycleFlags> readLifecycleFlags(I id) {
        return null;
    }

    @Override
    public void writeLifecycleFlags(I id, LifecycleFlags flags) {

    }

    @Override
    public Iterator<I> index() {
        return null;
    }
}
