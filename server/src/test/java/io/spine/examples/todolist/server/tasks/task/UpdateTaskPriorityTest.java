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
import io.spine.examples.todolist.tasks.command.CreateBasicTask;
import io.spine.examples.todolist.tasks.command.DeleteTask;
import io.spine.examples.todolist.tasks.command.UpdateTaskPriority;
import io.spine.examples.todolist.tasks.event.TaskPriorityUpdated;
import io.spine.examples.todolist.tasks.rejection.Rejections;
import io.spine.examples.todolist.tasks.view.TaskView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.tasks.TaskPriority.HIGH;
import static io.spine.examples.todolist.tasks.TaskPriority.LOW;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;

@DisplayName("UpdateTaskPriority command should be interpreted by TaskPart and")
class UpdateTaskPriorityTest extends TaskCommandTestBase {

    @Test
    @DisplayName("produce TaskPriorityUpdated event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        UpdateTaskPriority updateTaskPriority = updateTaskPriorityInstance(taskId());
        context().receivesCommand(createTask)
                 .receivesCommand(updateTaskPriority)
                 .assertEvents()
                 .withType(TaskPriorityUpdated.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("update the task priority")
    void updatePriority() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        UpdateTaskPriority updateTaskPriority = updateTaskPriorityInstance(taskId());
        TaskView expected = TaskView
                .newBuilder()
                .setId(taskId())
                .setPriority(updateTaskPriority.getPriorityChange()
                                               .getNewValue())
                .build();
        isEqualToExpectedAfterReceiving(expected, createTask, updateTaskPriority);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskPriority rejection upon an attempt to " +
            "update the priority of the deleted task")
    void cannotUpdateDeletedTaskPriority() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        DeleteTask deleteTask = deleteTaskInstance(taskId());

        UpdateTaskPriority updatePriority = updateTaskPriorityInstance(taskId());

        context().receivesCommand(createTask)
                 .receivesCommand(deleteTask)
                 .receivesCommand(updatePriority)
                 .assertEvents()
                 .withType(Rejections.CannotUpdateTaskPriority.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskPriority rejection " +
            "upon an attempt to update the priority of the completed task")
    void cannotUpdateCompletedTaskPriority() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        DeleteTask deleteTask = deleteTaskInstance(taskId());

        UpdateTaskPriority updatePriority = updateTaskPriorityInstance(taskId());

        context().receivesCommand(createTask)
                 .receivesCommand(deleteTask)
                 .receivesCommand(updatePriority)
                 .assertEvents()
                 .withType(Rejections.CannotUpdateTaskPriority.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("produce CannotUpdateTaskPriority rejection")
    void produceRejection() {
        CreateBasicTask createTask = createTaskInstance();
        TaskId taskId = createTask.getId();
        UpdateTaskPriority updateTaskPriority = updateTaskPriorityInstance(taskId, HIGH, LOW);
        context().receivesCommand(createTask)
                 .receivesCommand(updateTaskPriority)
                 .assertEvents()
                 .withType(Rejections.CannotUpdateTaskPriority.class)
                 .hasSize(1);
    }
}
