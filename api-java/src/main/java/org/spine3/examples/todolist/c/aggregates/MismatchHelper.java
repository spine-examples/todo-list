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

package org.spine3.examples.todolist.c.aggregates;

import com.google.protobuf.Timestamp;
import org.spine3.change.StringMismatch;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskPriorityValue;
import org.spine3.protobuf.AnyPacker;

import static com.google.protobuf.Any.pack;

/**
 * Utility class for working with mismatches.
 *
 * @author Illia Shepilov
 */
public class MismatchHelper {

    private MismatchHelper() {
    }

    /**
     * Creates a new instance of the {@link ValueMismatch} with the passed values.
     *
     * @param expectedPriority the {@link TaskPriority} expected by the command
     * @param actualPriority   the {@code TaskPriority} discovered instead of the expected
     * @param newPriority      the new {@code TaskPriority} requested in the command
     * @param version          the version of the entity in which the mismatch is discovered
     * @return new {@code ValueMismatch} instance
     */
    public static ValueMismatch of(TaskPriority expectedPriority, TaskPriority actualPriority,
                                   TaskPriority newPriority, int version) {
        final TaskPriorityValue actualPriorityValue = TaskPriorityValue.newBuilder()
                                                                       .setPriorityValue(actualPriority)
                                                                       .build();
        final TaskPriorityValue expectedPriorityValue = TaskPriorityValue.newBuilder()
                                                                         .setPriorityValue(expectedPriority)
                                                                         .build();
        final TaskPriorityValue newPriorityValue = TaskPriorityValue.newBuilder()
                                                                    .setPriorityValue(newPriority)
                                                                    .build();
        final ValueMismatch result = ValueMismatch.newBuilder()
                                                  .setExpected(AnyPacker.pack(expectedPriorityValue))
                                                  .setActual(AnyPacker.pack(actualPriorityValue))
                                                  .setNewValue(AnyPacker.pack(newPriorityValue))
                                                  .setVersion(version)
                                                  .build();
        return result;
    }

    /**
     * Creates a new instance of the {@link ValueMismatch} with the passed values.
     *
     * @param expectedValue the value expected by the command
     * @param actualValue   the value discovered instead of the expected
     * @param newValue      the new value requested in the command
     * @param version       the version of the entity in which the mismatch is discovered
     * @return new {@code ValueMismatch} instance
     */
    public static ValueMismatch of(String expectedValue, String actualValue, String newValue, int version) {
        final ValueMismatch result = StringMismatch.unexpectedValue(expectedValue,
                                                                    actualValue,
                                                                    newValue,
                                                                    version);
        return result;
    }

    /**
     * Creates a new instance of the {@link ValueMismatch} with the passed values.
     *
     * @param expectedTime the value expected by the command
     * @param actualTime   the value discovered instead of the expected
     * @param newTime      the new value requested in the command
     * @param version      the version of the entity in which the mismatch is discovered
     * @return new {@code ValueMismatch} instance
     */
    public static ValueMismatch of(Timestamp expectedTime, Timestamp actualTime, Timestamp newTime, int version) {
        final ValueMismatch result = ValueMismatch.newBuilder()
                                                  .setExpected(AnyPacker.pack(expectedTime))
                                                  .setActual(AnyPacker.pack(actualTime))
                                                  .setNewValue(AnyPacker.pack(newTime))
                                                  .setVersion(version)
                                                  .build();
        return result;
    }

    /**
     * Creates a new instance of the {@link ValueMismatch} with the passed values.
     *
     * @param expectedLabelDetails the {@link LabelDetails} expected by the command
     * @param actualLabelDetails   the {@code LabelDetails} discovered instead of the expected
     * @param newLabelDetails      the new {@code LabelDetails} requested in the command
     * @param version              the version of the entity in which the mismatch is discovered
     * @return new {@code ValueMismatch} instance
     */
    public static ValueMismatch of(LabelDetails expectedLabelDetails, LabelDetails actualLabelDetails,
                                   LabelDetails newLabelDetails, int version) {
        final ValueMismatch result = ValueMismatch.newBuilder()
                                                  .setActual(pack(actualLabelDetails))
                                                  .setExpected(pack(expectedLabelDetails))
                                                  .setNewValue(pack(newLabelDetails))
                                                  .setVersion(version)
                                                  .build();
        return result;
    }
}
