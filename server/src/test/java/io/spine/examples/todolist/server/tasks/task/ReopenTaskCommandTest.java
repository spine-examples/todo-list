/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import io.spine.examples.todolist.tasks.command.CompleteTask;
import io.spine.examples.todolist.tasks.command.CreateBasicTask;
import io.spine.examples.todolist.tasks.command.CreateDraft;
import io.spine.examples.todolist.tasks.command.DeleteTask;
import io.spine.examples.todolist.tasks.command.ReopenTask;
import io.spine.examples.todolist.tasks.event.TaskReopened;
import io.spine.examples.todolist.tasks.rejection.Rejections;
import io.spine.examples.todolist.tasks.view.TaskView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.tasks.TaskStatus.OPEN;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;

@DisplayName("ReopenTask command should be interpreted by TaskPart and")
class ReopenTaskCommandTest extends TaskCommandTestBase {

    @Test
    @DisplayName("produce TaskReopened event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        CompleteTask completeTask = completeTaskInstance(taskId());
        ReopenTask reopenTask = reopenTaskInstance(taskId());

        context().receivesCommand(createTask)
                 .receivesCommand(completeTask)
                 .receivesCommand(reopenTask)
                 .assertEvents()
                 .withType(TaskReopened.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("reopen completed task")
    void reopenTask() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        CompleteTask completeTask = completeTaskInstance(taskId());
        ReopenTask reopenTask = reopenTaskInstance(taskId());

        TaskView expected = TaskView
                .newBuilder()
                .setId(taskId())
                .setStatus(OPEN)
                .vBuild();
        isEqualToExpectedAfterReceiving(expected, createTask, completeTask, reopenTask);
    }

    @Test
    @DisplayName("throw CannotReopenTask rejection upon an attempt to reopen not completed task")
    void cannotReopenNotCompletedTask() {
        CreateBasicTask createTask = createTaskInstance();
        TaskId taskId = createTask.getId();
        ReopenTask reopenTask = reopenTaskInstance(taskId);

        context().receivesCommand(createTask)
                 .receivesCommand(reopenTask)
                 .assertEvents()
                 .withType(Rejections.CannotReopenTask.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("throw CannotReopenTask upon an attempt to reopen the deleted task")
    void cannotReopenDeletedTask() {
        CreateBasicTask createTask = createTaskInstance();
        TaskId taskId = createTask.getId();
        DeleteTask deleteTask = deleteTaskInstance(taskId);
        ReopenTask reopenTask = reopenTaskInstance(taskId);

        context().receivesCommand(createTask)
                 .receivesCommand(deleteTask)
                 .receivesCommand(reopenTask)
                 .assertEvents()
                 .withType(Rejections.CannotReopenTask.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("throw CannotReopenTask upon an attempt to reopen the task in draft state")
    void cannotReopenDraft() {
        CreateDraft createDraft = createDraftInstance();
        TaskId taskId = createDraft.getId();
        ReopenTask reopenTask = reopenTaskInstance(taskId);
        context()
                .receivesCommand(createDraft)
                .receivesCommand(reopenTask)
                .assertEvents()
                .withType(Rejections.CannotReopenTask.class)
                .hasSize(1);
    }
}
