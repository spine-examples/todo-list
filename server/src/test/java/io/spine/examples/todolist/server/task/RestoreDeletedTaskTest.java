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

package io.spine.examples.todolist.server.task;

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.command.AssignLabelToTask;
import io.spine.examples.todolist.command.CompleteTask;
import io.spine.examples.todolist.command.CreateBasicLabel;
import io.spine.examples.todolist.command.CreateBasicTask;
import io.spine.examples.todolist.command.CreateDraft;
import io.spine.examples.todolist.command.DeleteTask;
import io.spine.examples.todolist.command.FinalizeDraft;
import io.spine.examples.todolist.command.RestoreDeletedTask;
import io.spine.examples.todolist.event.LabelledTaskRestored;
import io.spine.examples.todolist.rejection.Rejections;
import io.spine.examples.todolist.view.TaskView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.TaskStatus.OPEN;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.createLabelInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;

@DisplayName("RestoreDeletedTask command should be interpreted by TaskPart and")
class RestoreDeletedTaskTest extends TaskCommandTestBase {

    RestoreDeletedTaskTest() {
        super(new TaskRepository(), new TaskViewRepository(), new TaskLabelsRepository());
    }

    @Test
    @DisplayName("produce LabelledTaskRestored event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        CreateBasicLabel createLabel = createLabelInstance();

        LabelId labelId = createLabel.getLabelId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId(), labelId);
        DeleteTask deleteTask = deleteTaskInstance(taskId());
        RestoreDeletedTask restoreTask = restoreDeletedTaskInstance(taskId());

        context().receivesCommand(createTask)
                 .receivesCommand(createLabel)
                 .receivesCommand(assignLabelToTask)
                 .receivesCommand(deleteTask)
                 .receivesCommand(restoreTask)
                 .assertEmitted(LabelledTaskRestored.class);
    }

    @Test
    @DisplayName("restore the deleted task")
    void restoreTask() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        CreateBasicLabel createLabel = createLabelInstance();

        LabelId labelId = createLabel.getLabelId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId(), labelId);
        DeleteTask deleteTask = deleteTaskInstance(taskId());
        RestoreDeletedTask restoreTask = restoreDeletedTaskInstance(taskId());

        TaskView expected = TaskView
                .newBuilder()
                .setId(taskId())
                .setStatus(OPEN)
                .build();
        isEqualToExpectedAfterReceiving(expected,
                                        createTask,
                                        createLabel,
                                        assignLabelToTask,
                                        deleteTask,
                                        restoreTask);
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask rejection upon an attempt to " +
            "restore the completed task")
    void cannotRestoreCompletedTask() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        CompleteTask completeTask = completeTaskInstance(taskId());
        RestoreDeletedTask restoreTask = restoreDeletedTaskInstance(taskId());

        context().receivesCommand(createTask)
                 .receivesCommand(completeTask)
                 .receivesCommand(restoreTask)
                 .assertRejectedWith(Rejections.CannotRestoreDeletedTask.class);
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask upon an attempt to restore the finalized task")
    void cannotRestoreFinalizedTask() {
        CreateDraft createDraft = createDraftInstance(taskId());
        FinalizeDraft finalizeDraft = finalizeDraftInstance(taskId());
        RestoreDeletedTask restoreTask = restoreDeletedTaskInstance(taskId());
        context().receivesCommand(createDraft)
                 .receivesCommand(finalizeDraft)
                 .receivesCommand(restoreTask)
                 .assertRejectedWith(Rejections.CannotRestoreDeletedTask.class);
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask upon an attempt to restore the draft")
    void cannotRestoreDraft() {
        CreateDraft createDraft = createDraftInstance(taskId());
        RestoreDeletedTask restoreTask = restoreDeletedTaskInstance(taskId());
        context().receivesCommand(createDraft)
                 .receivesCommand(restoreTask)
                 .assertRejectedWith(Rejections.CannotRestoreDeletedTask.class);
    }
}
