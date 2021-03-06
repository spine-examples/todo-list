/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.server.tasks.task;

import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.TaskStatus;
import io.spine.examples.todolist.tasks.command.CompleteTask;
import io.spine.examples.todolist.tasks.command.CreateBasicTask;
import io.spine.examples.todolist.tasks.command.CreateDraft;
import io.spine.examples.todolist.tasks.command.DeleteTask;
import io.spine.examples.todolist.tasks.event.TaskCompleted;
import io.spine.examples.todolist.tasks.rejection.Rejections;
import io.spine.examples.todolist.tasks.view.TaskView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;

@DisplayName("CompleteTask command should be interpreted by TaskPart and")
class CompleteTaskTest extends TaskCommandTestBase {

    @Test
    @DisplayName("produce TaskCompleted event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        context().receivesCommand(createTask)
                 .receivesCommand(completeTask(createTask.getId()))
                 .assertEvents()
                 .withType(TaskCompleted.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("complete the task")
    void completeTheTask() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        CompleteTask completeTask = completeTask(taskId());

        TaskView expected = TaskView
                .newBuilder()
                .setId(taskId())
                .setStatus(TaskStatus.COMPLETED)
                .build();

        isEqualToExpectedAfterReceiving(expected, createTask, completeTask);
    }

    @Test
    @DisplayName("throw CannotCompleteTask rejection upon an attempt to complete the deleted task")
    void cannotCompleteDeletedTask() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        CompleteTask completeTask = completeTask(taskId());
        DeleteTask deleteTask = deleteTaskInstance(taskId());

        context().receivesCommand(createTask)
                 .receivesCommand(deleteTask)
                 .receivesCommand(completeTask)
                 .assertEvents()
                 .withType(Rejections.CannotCompleteTask.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("throw CannotCompleteTask rejection upon " +
            "an attempt to complete the task in draft state")
    void cannotCompleteDraft() {
        CreateDraft createDraft = createDraftInstance(taskId());
        CompleteTask completeTask = completeTask(createDraft.getId());

        context().receivesCommand(createDraft)
                 .receivesCommand(completeTask)
                 .assertEvents()
                 .withType(Rejections.CannotCompleteTask.class)
                 .hasSize(1);
    }

    private static CompleteTask completeTask(TaskId taskId) {
        CompleteTask result = CompleteTask.newBuilder()
                                          .setId(taskId)
                                          .build();
        return result;
    }
}
