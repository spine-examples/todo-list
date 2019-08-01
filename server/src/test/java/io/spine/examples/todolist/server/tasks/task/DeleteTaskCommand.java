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
import io.spine.examples.todolist.tasks.command.CreateDraft;
import io.spine.examples.todolist.tasks.command.DeleteTask;
import io.spine.examples.todolist.tasks.event.TaskDeleted;
import io.spine.examples.todolist.tasks.rejection.Rejections;
import io.spine.examples.todolist.tasks.view.TaskView;
import io.spine.testing.server.entity.EntitySubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.tasks.TaskStatus.DELETED;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;

@DisplayName("DeleteTask command should be interpreted by TaskPart and")
class DeleteTaskCommand extends TaskCommandTestBase {

    @Test
    @DisplayName("produce TaskDeleted event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        DeleteTask deleteTask = deleteTaskInstance(taskId());

        context().receivesCommand(createTask)
                 .receivesCommand(deleteTask)
                 .assertEmitted(TaskDeleted.class);
    }

    @Test
    @DisplayName("delete the task")
    void deleteTask() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        DeleteTask deleteTask = deleteTaskInstance(taskId());

        TaskView expected = TaskView
                .newBuilder()
                .setId(taskId())
                .setStatus(DELETED)
                .build();
        isEqualToExpectedAfterReceiving(expected, createTask, deleteTask);
    }

    @Test
    @DisplayName("set both archived and deleted flags to true if sent to the task in `Draft` state")
    void deleteDraft() {
        CreateDraft createDraft = createDraftInstance(taskId());
        DeleteTask deleteTask = deleteTaskInstance(taskId());
        EntitySubject projectionSubject = context()
                .receivesCommand(createDraft)
                .receivesCommand(deleteTask)
                .assertEntity(TaskViewProjection.class, taskId());
        projectionSubject.deletedFlag()
                         .isTrue();
        projectionSubject.archivedFlag()
                         .isTrue();
    }

    @Test
    @DisplayName("throw CannotDeleteTask rejection upon an attempt to " +
            "delete the already deleted task")
    void cannotDeleteAlreadyDeletedTask() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        DeleteTask deleteTask = deleteTaskInstance(taskId());
        context().receivesCommand(createTask)
                 .receivesCommand(deleteTask)
                 .receivesCommand(deleteTask)
                 .assertRejectedWith(Rejections.CannotDeleteTask.class);
    }
}
