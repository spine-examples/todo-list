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

package io.spine.examples.todolist.c.aggregate.definition;

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.rejection.Rejections;
import io.spine.examples.todolist.q.projection.TaskView;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.examples.todolist.repository.TaskViewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.TaskStatus.DRAFT;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;

@DisplayName("CreateDraft command should be interpreted by TaskPart and")
class CreateDraftTest extends TaskCommandTestBase {

    CreateDraftTest() {
        super(new TaskRepository(), new TaskViewRepository());
    }

    @Test
    @DisplayName("produce TaskDraftCreated event")
    void produceEvent() {
        CreateDraft createDraftCmd = createDraftInstance();
        context().receivesCommand(createDraftCmd)
                 .assertEmitted(TaskDraftCreated.class);
    }

    @Test
    @DisplayName("create the draft")
    void createDraft() {
        CreateDraft createDraft = createDraftInstance(taskId());
        context().receivesCommand(createDraft);

        TaskView expected = TaskView
                .newBuilder()
                .setId(taskId())
                .setStatus(DRAFT)
                .build();
        isEqualToExpectedAfterReceiving(expected, createDraft);
    }

    @Test
    @DisplayName("throw CannotCreateDraft rejection upon " +
            "an attempt to create draft with deleted task ID")
    void notCreateDraft() {
        CreateBasicTask createTask = createTaskInstance();
        TaskId taskId = createTask.getId();
        DeleteTask deleteTask = deleteTaskInstance(taskId);
        CreateDraft createDraft = createDraftInstance(taskId);

        context().receivesCommand(createTask)
                 .receivesCommand(deleteTask)
                 .receivesCommand(createDraft)
                 .assertRejectedWith(Rejections.CannotCreateDraft.class);
    }
}
