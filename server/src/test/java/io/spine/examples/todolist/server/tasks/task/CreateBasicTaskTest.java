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

import io.spine.examples.todolist.tasks.command.CreateBasicTask;
import io.spine.examples.todolist.tasks.event.TaskCreated;
import io.spine.examples.todolist.tasks.view.TaskView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;

@DisplayName("CreateBasicTask command should be interpreted by TaskPart and")
class CreateBasicTaskTest extends TaskCommandTestBase {

    CreateBasicTaskTest() {
        super(new TaskRepository(), new TaskViewRepository());
    }

    @Test
    @DisplayName("produce TaskCreated event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        context().receivesCommand(createTask)
                 .assertEmitted(TaskCreated.class);
    }

    @Test
    @DisplayName("create the task")
    void createTask() {
        CreateBasicTask createBasicTask = createTaskInstance(taskId());
        TaskView expected = TaskView
                .newBuilder()
                .setId(taskId())
                .build();
        isEqualToExpectedAfterReceiving(expected, createBasicTask);
    }
}
