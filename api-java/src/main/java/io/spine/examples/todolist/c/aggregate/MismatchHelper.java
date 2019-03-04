/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.c.aggregate;

import io.spine.change.ValueMismatch;
import io.spine.core.Version;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.TaskPriorityValue;
import io.spine.protobuf.AnyPacker;

/**
 * Utility class for working with mismatches.
 */
final class MismatchHelper {

    /** Prevents instantiation of this utility class. */
    private MismatchHelper() {
    }

    /**
     * Creates a new instance of the {@link ValueMismatch} with the passed values.
     *
     * @param expectedPriority
     *         the {@link TaskPriority} expected by the command
     * @param actualPriority
     *         the {@code TaskPriority} discovered instead of the expected
     * @param newPriority
     *         the new {@code TaskPriority} requested in the command
     * @param version
     *         the version of the entity in which the mismatch is discovered
     * @return new {@code ValueMismatch} instance
     */
    static ValueMismatch valueMismatch(TaskPriority expectedPriority,
                                       TaskPriority actualPriority,
                                       TaskPriority newPriority,
                                       Version version) {
        TaskPriorityValue actualPriorityValue = TaskPriorityValue
                .newBuilder()
                .setPriorityValue(actualPriority)
                .build();
        TaskPriorityValue expectedPriorityValue = TaskPriorityValue
                .newBuilder()
                .setPriorityValue(expectedPriority)
                .build();
        TaskPriorityValue newPriorityValue = TaskPriorityValue
                .newBuilder()
                .setPriorityValue(newPriority)
                .build();
        ValueMismatch result = ValueMismatch
                .newBuilder()
                .setExpected(AnyPacker.pack(expectedPriorityValue))
                .setActual(AnyPacker.pack(actualPriorityValue))
                .setNewValue(AnyPacker.pack(newPriorityValue))
                .setVersion(version.getNumber())
                .build();
        return result;
    }
}
