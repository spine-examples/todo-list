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

package io.spine.examples.todolist.c.aggregate.rejection;

import com.google.common.testing.NullPointerTester;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.RejectedTaskCommandDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.ChangeStatusRejections;
import io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.TaskCreationRejections;
import io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.UpdateRejections;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.c.rejection.CannotCreateDraft;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDescription;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDueDate;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.TaskCreationRejections.throwCannotCreateDraft;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.UpdateRejections.throwCannotUpdateTaskDescription;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.UpdateRejections.throwCannotUpdateTaskDueDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskPartRejectionsTest extends UtilityClassTest<TaskPartRejections> {

    private final TaskId taskId = TaskId.getDefaultInstance();

    TaskPartRejectionsTest() {
        super(TaskPartRejections.class);
    }

    @Nested
    @DisplayName("ChangeStatusRejections should")
    class ChangeStatusRejectionsTest extends UtilityClassTest<ChangeStatusRejections> {

        ChangeStatusRejectionsTest() {
            super(ChangeStatusRejections.class);
        }
    }

    @Nested
    @DisplayName("TaskCreationRejections should")
    class TaskCreationRejectionsTest extends UtilityClassTest<TaskCreationRejections> {

        TaskCreationRejectionsTest() {
            super(TaskCreationRejections.class);
        }

        @Test
        @DisplayName("throw CannotCreateDraft rejection")
        void throwCannotCreateDraftRejection() {
            CreateDraft cmd = CreateDraft
                    .newBuilder()
                    .setId(taskId)
                    .build();
            CannotCreateDraft rejection = assertThrows(CannotCreateDraft.class,
                                                       () -> throwCannotCreateDraft(cmd));
            TaskId actual = rejection.getMessageThrown()
                                     .getRejectionDetails()
                                     .getCommandDetails()
                                     .getTaskId();
            assertEquals(taskId, actual);
        }
    }

    @Nested
    @DisplayName("UpdateRejections should")
    class UpdateRejectionsTest extends UtilityClassTest<UpdateRejections> {

        UpdateRejectionsTest() {
            super(UpdateRejections.class);
        }

        @Override
        protected void configure(NullPointerTester tester) {
            tester.setDefault(UpdateTaskDueDate.class, UpdateTaskDueDate.getDefaultInstance());
            tester.setDefault(UpdateTaskDescription.class,
                              UpdateTaskDescription.getDefaultInstance());
            tester.setDefault(UpdateTaskPriority.class, UpdateTaskPriority.getDefaultInstance());
            tester.setDefault(ValueMismatch.class, ValueMismatch.getDefaultInstance());
        }

        @Test
        @DisplayName("throw CannotUpdateTaskDueDate rejection")
        void throwCannotUpdateTaskDueDateRejection() {
            UpdateTaskDueDate cmd = UpdateTaskDueDate
                    .newBuilder()
                    .setId(taskId)
                    .build();
            CannotUpdateTaskDueDate rejection =
                    assertThrows(CannotUpdateTaskDueDate.class,
                                 () -> throwCannotUpdateTaskDueDate(cmd));
            RejectedTaskCommandDetails commandDetails = rejection.getMessageThrown()
                                                                 .getRejectionDetails()
                                                                 .getCommandDetails();
            TaskId actualId = commandDetails.getTaskId();
            assertEquals(taskId, actualId);
        }

        @Test
        @DisplayName("throw CannotUpdateTaskDescription rejection")
        void throwCannotUpdateTaskDescriptionRejection() {
            UpdateTaskDescription cmd = UpdateTaskDescription
                    .newBuilder()
                    .setId(taskId)
                    .build();
            CannotUpdateTaskDescription rejection =
                    assertThrows(CannotUpdateTaskDescription.class,
                                 () -> throwCannotUpdateTaskDescription(cmd));
            RejectedTaskCommandDetails commandDetails = rejection.getMessageThrown()
                                                                 .getRejectionDetails()
                                                                 .getCommandDetails();

            TaskId actualId = commandDetails.getTaskId();
            assertEquals(taskId, actualId);
        }
    }
}
