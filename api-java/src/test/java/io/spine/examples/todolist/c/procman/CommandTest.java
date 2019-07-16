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

package io.spine.examples.todolist.c.procman;

import io.spine.examples.todolist.DescriptionChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.SkipLabels;
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.c.commands.UpdateTaskDetails;
import io.spine.examples.todolist.repository.LabelAggregateRepository;
import io.spine.examples.todolist.repository.TaskCreationWizardRepository;
import io.spine.examples.todolist.repository.TaskLabelsRepository;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.testing.server.blackbox.BlackBoxBoundedContext;
import org.junit.jupiter.api.BeforeEach;

import static io.spine.base.Identifier.newUuid;

/**
 * Abstract base for tests testing command execution.
 */
abstract class CommandTest {

    private BlackBoxBoundedContext<?> context;
    private TaskId taskId;
    private TaskCreationId processId;

    @BeforeEach
    void setUp() {
        context = BlackBoxBoundedContext
                .singleTenant()
                .with(new TaskCreationWizardRepository(), new TaskRepository(),
                      new LabelAggregateRepository(), new TaskLabelsRepository());
        taskId = newTaskId();
        processId = newId();
    }

    void startWizard() {
        StartTaskCreation cmd = StartTaskCreation
                .newBuilder()
                .setId(processId)
                .setTaskId(taskId)
                .build();
        context.receivesCommand(cmd);
    }

    void addDescription() {
        TaskDescription description = TaskDescription
                .newBuilder()
                .setValue("task for test")
                .build();
        DescriptionChange descriptionChange = DescriptionChange
                .newBuilder()
                .setNewValue(description)
                .build();
        UpdateTaskDetails cmd = UpdateTaskDetails
                .newBuilder()
                .setId(processId)
                .setDescriptionChange(descriptionChange)
                .build();
        context.receivesCommand(cmd);
    }

    void skipLabels() {
        SkipLabels cmd = SkipLabels
                .newBuilder()
                .setId(processId)
                .build();
        context.receivesCommand(cmd);
    }

    TaskCreationId newId() {
        return TaskCreationId.newBuilder()
                                     .setValue(newUuid())
                                     .build();
    }

    TaskId newTaskId() {
        return TaskId.newBuilder()
                             .setValue(newUuid())
                             .build();
    }

    LabelId newLabelId() {
        return LabelId.newBuilder()
                              .setValue(newUuid())
                              .build();
    }

    BlackBoxBoundedContext<?> context() {
        return context;
    }

    TaskCreationId processId() {
        return processId;
    }

    TaskId taskId() {
        return taskId;
    }
}
