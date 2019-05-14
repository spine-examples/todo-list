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

import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.rejection.Rejections;
import io.spine.examples.todolist.q.projection.TaskView;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.examples.todolist.repository.TaskViewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.TaskStatus.FINALIZED;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;

@DisplayName("FinalizeDraft command should be interpreted by TaskPart and")
class FinalizeDraftTest extends TaskCommandTestBase {

    FinalizeDraftTest() {
        super(new TaskRepository(), new TaskViewRepository());
    }

    @Test
    @DisplayName("finalize the draft")
    void finalizeTask() {
        CreateDraft createDraft = createDraftInstance(taskId());
        FinalizeDraft finalizeDraft = finalizeDraftInstance(taskId());

        TaskView expected = TaskView
                .vBuilder()
                .setId(taskId())
                .setStatus(FINALIZED)
                .build();
        isEqualToExpectedAfterReceiving(expected, createDraft, finalizeDraft);
    }

    @Test
    @DisplayName("throw CannotFinalizeDraft rejection upon an attempt to finalize the deleted task")
    void cannotFinalizeDeletedTask() {
        CreateBasicTask createTask = createTaskInstance();
        DeleteTask deleteTask = deleteTaskInstance(taskId());
        FinalizeDraft finalizeDraft = finalizeDraftInstance(taskId());
        boundedContext().receivesCommand(createTask)
                        .receivesCommand(deleteTask)
                        .receivesCommand(finalizeDraft)
                        .assertRejectedWith(Rejections.CannotFinalizeDraft.class);
    }

    @Test
    @DisplayName("throw CannotFinalizeDraft rejection upon an attempt to finalize " +
            "the task which is not a draft")
    void cannotFinalizeNotDraftTask() {
        CreateBasicTask createTask = createTaskInstance();
        FinalizeDraft finalizeDraft = finalizeDraftInstance(taskId());
        boundedContext().receivesCommand(createTask)
                        .receivesCommand(finalizeDraft)
                        .assertRejectedWith(Rejections.CannotFinalizeDraft.class);
    }
}
