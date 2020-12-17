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

import io.spine.examples.todolist.tasks.command.CompleteTask;
import io.spine.examples.todolist.tasks.command.CreateBasicTask;
import io.spine.examples.todolist.tasks.command.DeleteTask;
import io.spine.examples.todolist.tasks.command.UpdateTaskDescription;
import io.spine.examples.todolist.tasks.event.TaskDescriptionUpdated;
import io.spine.examples.todolist.tasks.rejection.Rejections;
import io.spine.examples.todolist.tasks.view.TaskView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;

@DisplayName("UpdateTaskDescription command should be interpreted by TaskPart and")
class UpdateTaskDescriptionTest extends TaskCommandTestBase {

    private static final String NEW_DESCRIPTION = "Wash my dog.";

    @Test
    @DisplayName("produce TaskDescriptionUpdated event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        String previousDescription = createTask.getDescription()
                                               .getValue();
        UpdateTaskDescription updateDescription =
                updateTaskDescriptionInstance(taskId(), previousDescription, NEW_DESCRIPTION);
        context().receivesCommand(createTask)
                 .receivesCommand(updateDescription)
                 .assertEvents()
                 .withType(TaskDescriptionUpdated.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("update the task description")
    void updateDescription() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        String previousDescription = createTask.getDescription()
                                               .getValue();
        UpdateTaskDescription updateDescription =
                updateTaskDescriptionInstance(taskId(), previousDescription, NEW_DESCRIPTION);
        TaskView expected = TaskView
                .newBuilder()
                .setId(taskId())
                .setDescription(updateDescription.getDescriptionChange()
                                                 .getNewValue())
                .build();
        isEqualToExpectedAfterReceiving(expected, createTask, updateDescription);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription rejection " +
            "upon an attempt to update the description of the deleted task")
    void cannotUpdateDeletedTaskDescription() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        String previousDescription = createTask.getDescription()
                                               .getValue();

        DeleteTask deleteTask = deleteTaskInstance(taskId());
        UpdateTaskDescription updateDescription =
                updateTaskDescriptionInstance(taskId(), previousDescription, NEW_DESCRIPTION);

        context().receivesCommand(createTask)
                 .receivesCommand(deleteTask)
                 .receivesCommand(updateDescription)
                 .assertEvents()
                 .withType(Rejections.CannotUpdateTaskDescription.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription rejection " +
            "upon an attempt to update the description of the completed task")
    void cannotUpdateCompletedTaskDescription() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        String previousDescription = createTask.getDescription()
                                               .getValue();

        CompleteTask completeTask = completeTaskInstance(taskId());
        UpdateTaskDescription updateDescription =
                updateTaskDescriptionInstance(taskId(), previousDescription, NEW_DESCRIPTION);

        context().receivesCommand(createTask)
                 .receivesCommand(completeTask)
                 .receivesCommand(updateDescription)
                 .assertEvents()
                 .withType(Rejections.CannotUpdateTaskDescription.class)
                 .hasSize(1);
    }

    @Test
    @DisplayName("produce CannotUpdateTaskDescription rejection")
    void produceRejection() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        String incorrectDescription = createTask.getDescription()
                                                .getValue() + "random suffix";

        UpdateTaskDescription updateDescription =
                updateTaskDescriptionInstance(taskId(), incorrectDescription, NEW_DESCRIPTION);

        context().receivesCommand(createTask)
                 .receivesCommand(updateDescription)
                 .assertEvents()
                 .withType(Rejections.CannotUpdateTaskDescription.class)
                 .hasSize(1);
    }
}
