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

package io.spine.examples.todolist.testdata;

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;

/**
 * A factory of the task label events for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestTaskLabelsEventFactory {

    private static final LabelId LABEL_ID = TestTaskEventFactory.LABEL_ID;
    private static final TaskId TASK_ID = TestTaskEventFactory.TASK_ID;

    private TestTaskLabelsEventFactory() {
    }

    /**
     * Provides a pre-configured {@link LabelAssignedToTask} event instance.
     *
     * @return the {@code LabelAssignedToTask} instance
     */
    public static LabelAssignedToTask labelAssignedToTaskInstance() {
        return labelAssignedToTaskInstance(TASK_ID, LABEL_ID);
    }

    /**
     * Provides {@link LabelAssignedToTask} event by specified task label ID.
     *
     * @param labelId the ID of the assigned label
     * @return the {@code LabelAssignedToTask} instance
     */
    public static LabelAssignedToTask labelAssignedToTaskInstance(TaskId taskId, LabelId labelId) {
        final LabelAssignedToTask result = LabelAssignedToTask.newBuilder()
                                                              .setTaskId(taskId)
                                                              .setLabelId(labelId)
                                                              .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link LabelRemovedFromTask} event instance.
     *
     * @return the {@code LabelRemovedFromTask} instance
     */
    public static LabelRemovedFromTask labelRemovedFromTaskInstance() {
        final LabelRemovedFromTask result = LabelRemovedFromTask.newBuilder()
                                                                .setTaskId(TASK_ID)
                                                                .setLabelId(LABEL_ID)
                                                                .build();
        return result;
    }
}
