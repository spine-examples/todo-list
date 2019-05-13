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
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.rejection.Rejections;
import io.spine.examples.todolist.q.projection.TaskView;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.examples.todolist.repository.TaskViewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;

@DisplayName("CompleteTask command should be interpreted by TaskPart and")
class CompleteTaskTest extends TaskCommandTestBase {

    CompleteTaskTest() {
        super(new TaskRepository(), new TaskViewRepository());
    }

    @Test
    @DisplayName("produce TaskCompleted event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        boundedContext().receivesCommand(createTask)
                        .receivesCommand(completeTask(createTask.getId()))
                        .assertEmitted(TaskCompleted.class);
    }

    @Test
    @DisplayName("complete the task")
    void completeTheTask() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        CompleteTask completeTask = completeTask(taskId());

        TaskView expected = TaskView
                .vBuilder()
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

        boundedContext().receivesCommand(createTask)
                        .receivesCommand(deleteTask)
                        .receivesCommand(completeTask)
                        .assertRejectedWith(Rejections.CannotCompleteTask.class);
    }

    @Test
    @DisplayName("throw CannotCompleteTask rejection upon " +
            "an attempt to complete the task in draft state")
    void cannotCompleteDraft() {
        CreateDraft createDraft = createDraftInstance(taskId());
        CompleteTask completeTask = completeTask(createDraft.getId());

        boundedContext().receivesCommand(createDraft)
                        .receivesCommand(completeTask)
                        .assertRejectedWith(Rejections.CannotCompleteTask.class);
    }

    private static CompleteTask completeTask(TaskId taskId) {
        CompleteTask result = CompleteTask.vBuilder()
                                          .setId(taskId)
                                          .build();
        return result;
    }
}
