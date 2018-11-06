/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.c.aggregate.rejection;

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.rejection.CannotAddLabels;
import io.spine.examples.todolist.c.rejection.CannotAssignLabelToTask;
import io.spine.examples.todolist.c.rejection.CannotRemoveLabelFromTask;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.c.aggregate.rejection.TaskLabelsPartRejections.throwCannotAddLabelsToTask;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskLabelsPartRejections.throwCannotAssignLabelToTask;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskLabelsPartRejections.throwCannotRemoveLabelFromTask;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TaskLabelsPartRejections should")
class TaskLabelsPartRejectionsTest extends UtilityClassTest<TaskLabelsPartRejections> {

    private final TaskId taskId = TaskId.getDefaultInstance();
    private final LabelId labelId = LabelId.getDefaultInstance();

    TaskLabelsPartRejectionsTest() {
        super(TaskLabelsPartRejections.class);
    }

    @Test
    @DisplayName("throw CannotRemoveLabelFromTask rejection")
    void throwCannotRemoveLabelFromTaskRejection() {
        final RemoveLabelFromTask cmd = RemoveLabelFromTask.newBuilder()
                                                           .setId(taskId)
                                                           .setLabelId(labelId)
                                                           .build();
        final CannotRemoveLabelFromTask rejection =
                assertThrows(CannotRemoveLabelFromTask.class,
                             () -> throwCannotRemoveLabelFromTask(cmd));
        final TaskId actualId = rejection.getMessageThrown()
                                         .getRejectionDetails()
                                         .getCommandDetails()
                                         .getTaskId();
        assertEquals(taskId, actualId);
    }

    @Test
    @DisplayName("throw CannotAssignLabelToTask rejection")
    void throwCannotAssignLabelToTaskRejection() {
        final AssignLabelToTask cmd = AssignLabelToTask.newBuilder()
                                                       .setLabelId(labelId)
                                                       .setId(taskId)
                                                       .build();
        final CannotAssignLabelToTask rejection =
                assertThrows(CannotAssignLabelToTask.class,
                             () -> throwCannotAssignLabelToTask(cmd));
        final TaskId actualId = rejection.getMessageThrown()
                                         .getRejectionDetails()
                                         .getCommandDetails()
                                         .getTaskId();
        assertEquals(taskId, actualId);
    }

    @Test
    @DisplayName("throw CannotAddLabels rejection")
    void throwCannotAddLabelsToTaskRejection() {
        TaskCreationId taskCreationId = TaskCreationId.getDefaultInstance();
        final AddLabels cmd = AddLabels
                .newBuilder()
                .setId(taskCreationId)
                .build();
        final CannotAddLabels rejection =
                assertThrows(CannotAddLabels.class,
                             () -> throwCannotAddLabelsToTask(cmd));
        final TaskCreationId actualId = rejection.getMessageThrown()
                                                 .getRejectionDetails()
                                                 .getId();
        assertEquals(taskCreationId, actualId);
    }
}
