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
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.events.TaskReopened;
import io.spine.examples.todolist.c.rejection.Rejections;
import io.spine.examples.todolist.q.projection.TaskView;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.examples.todolist.repository.TaskViewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.TaskStatus.OPEN;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;

@DisplayName("ReopenTask command should be interpreted by TaskPart and")
class ReopenTaskCommandTest extends TaskCommandTestBase {

    ReopenTaskCommandTest() {
        super(new TaskRepository(), new TaskViewRepository());
    }

    @Test
    @DisplayName("produce TaskReopened event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        CompleteTask completeTask = completeTaskInstance(taskId());
        ReopenTask reopenTask = reopenTaskInstance(taskId());

        context().receivesCommand(createTask)
                 .receivesCommand(completeTask)
                 .receivesCommand(reopenTask)
                 .assertEmitted(TaskReopened.class);
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
                .build();
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
                 .assertRejectedWith(Rejections.CannotReopenTask.class);
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
                 .assertRejectedWith(Rejections.CannotReopenTask.class);
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
                .assertRejectedWith(Rejections.CannotReopenTask.class);
    }
}
