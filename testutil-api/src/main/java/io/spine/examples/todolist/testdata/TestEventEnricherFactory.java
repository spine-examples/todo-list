/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.server.event.EventEnricher;

import java.util.function.Function;

/**
 * Provides event enricher for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventEnricherFactory {

    private static final TaskDescription DESCRIPTION =
            TaskDescription.newBuilder()
                           .setValue(TestTaskEventFactory.DESCRIPTION)
                           .build();
    private static final Timestamp TASK_DUE_DATE = TestTaskEventFactory.TASK_DUE_DATE;
    private static final TaskPriority TASK_PRIORITY = TestTaskEventFactory.TASK_PRIORITY;
    private static final TaskDetails TASK_DETAILS = TaskDetails.newBuilder()
                                                               .setDescription(DESCRIPTION)
                                                               .setPriority(TaskPriority.LOW)
                                                               .build();
    private static final Task TASK = Task.newBuilder()
                                         .setDescription(DESCRIPTION)
                                         .setDueDate(TASK_DUE_DATE)
                                         .setPriority(TASK_PRIORITY)
                                         .build();
    private static final Function<TaskId, TaskDetails> TASK_ID_TO_TASK_DETAILS =
            taskId -> TASK_DETAILS;

    private static final Function<TaskId, Task> TASK_ID_TO_TASK = taskId -> TASK;

    private TestEventEnricherFactory() {
    }

    /**
     * Provides a pre-configured {@link EventEnricher} event instance.
     *
     * @return {@code EventEnricher}
     */
    public static EventEnricher eventEnricherInstance() {
        final EventEnricher result = EventEnricher.newBuilder()
                                                  .add(TaskId.class,
                                                       TaskDetails.class,
                                                       TASK_ID_TO_TASK_DETAILS::apply)
                                                  .add(TaskId.class,
                                                       Task.class,
                                                       TASK_ID_TO_TASK::apply)
                                                  .build();
        return result;
    }
}
