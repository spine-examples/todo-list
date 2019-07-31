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

package io.spine.examples.todolist.server.tasks.task;

import io.spine.examples.todolist.server.tasks.label.LabelAggregateRepository;
import io.spine.examples.todolist.tasks.DescriptionChange;
import io.spine.examples.todolist.tasks.TaskCreationId;
import io.spine.examples.todolist.tasks.TaskDescription;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.command.SkipLabels;
import io.spine.examples.todolist.tasks.command.StartTaskCreation;
import io.spine.examples.todolist.tasks.command.UpdateTaskDetails;
import io.spine.testing.server.blackbox.BlackBoxBoundedContext;
import org.junit.jupiter.api.BeforeEach;

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
                .with(new TaskCreationWizardRepository(),
                      new TaskRepository(),
                      new LabelAggregateRepository(),
                      new TaskLabelsRepository());
        taskId = TaskId.generate();
        processId = TaskCreationId.generate();
    }

    void startWizard() {
        StartTaskCreation cmd = StartTaskCreation
                .newBuilder()
                .setId(processId)
                .setTaskId(taskId)
                .vBuild();
        context.receivesCommand(cmd);
    }

    void addDescription() {
        TaskDescription description = TaskDescription
                .newBuilder()
                .setValue("task for test")
                .vBuild();
        DescriptionChange descriptionChange = DescriptionChange
                .newBuilder()
                .setNewValue(description)
                .vBuild();
        UpdateTaskDetails cmd = UpdateTaskDetails
                .newBuilder()
                .setId(processId)
                .setDescriptionChange(descriptionChange)
                .vBuild();
        context.receivesCommand(cmd);
    }

    void skipLabels() {
        SkipLabels cmd = SkipLabels
                .newBuilder()
                .setId(processId)
                .vBuild();
        context.receivesCommand(cmd);
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
