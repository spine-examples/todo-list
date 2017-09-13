/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

import io.spine.examples.todolist.RejectedTaskCommandDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.ChangeStatusRejections;
import io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.UpdateRejections;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.rejection.CannotCreateDraft;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDescription;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDueDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.TaskCreationRejections.throwCannotCreateDraft;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.UpdateRejections.throwCannotUpdateTaskDescription;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskPartRejections.UpdateRejections.throwCannotUpdateTaskDueDate;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Illia Shepilov
 */
@DisplayName("TaskPartRejections should")
class TaskPartRejectionsTest {

    private final TaskId taskId = TaskId.getDefaultInstance();

    @Test
    @DisplayName("have the private constructor")
    void havePrivateConstructor() {
        assertHasPrivateParameterlessCtor(TaskPartRejections.class);
    }

    @Nested
    @DisplayName("ChangeStatusRejections should")
    class ChangeStatusRejectionsTest {

        @Test
        @DisplayName("have the private constructor")
        void havePrivateConstructor() {
            assertHasPrivateParameterlessCtor(ChangeStatusRejections.class);
        }
    }

    @Nested
    @DisplayName("TaskCreationRejections should")
    class TaskCreationRejectionsTest {

        @Test
        @DisplayName("have the private constructor")
        void havePrivateConstructor() {
            assertHasPrivateParameterlessCtor(TaskPartRejections.TaskCreationRejections.class);
        }

        @Test
        @DisplayName("throw CannotCreateDraft rejection")
        void throwCannotCreateDraftRejection() {
            final CreateDraft cmd = CreateDraft.newBuilder()
                                               .setId(taskId)
                                               .build();
            final CannotCreateDraft rejection = assertThrows(CannotCreateDraft.class,
                                                             () -> throwCannotCreateDraft(cmd));
            final TaskId actual = rejection.getMessageThrown()
                                           .getCreateDraftRejected()
                                           .getRejectionDetails()
                                           .getTaskId();
            assertEquals(taskId, actual);
        }
    }

    @Nested
    @DisplayName("UpdateRejections should")
    class UpdateRejectionsTest {

        @Test
        @DisplayName("have the private constructor")
        void havePrivateConstructor() {
            assertHasPrivateParameterlessCtor(UpdateRejections.class);
        }

        @Test
        @DisplayName("throw CannotUpdateTaskDueDate rejection")
        void throwCannotUpdateTaskDueDateRejection() {
            final UpdateTaskDueDate cmd = UpdateTaskDueDate.newBuilder()
                                                           .setId(taskId)
                                                           .build();
            final CannotUpdateTaskDueDate rejection =
                    assertThrows(CannotUpdateTaskDueDate.class,
                                 () -> throwCannotUpdateTaskDueDate(cmd));
            final RejectedTaskCommandDetails rejectedCommand = rejection.getMessageThrown()
                                                                        .getUpdateRejected()
                                                                        .getRejectionDetails();
            final TaskId actualId = rejectedCommand.getTaskId();
            assertEquals(taskId, actualId);
        }

        @Test
        @DisplayName("throw CannotUpdateTaskDescription rejection")
        void throwCannotUpdateTaskDescriptionRejection() {
            final UpdateTaskDescription cmd = UpdateTaskDescription.newBuilder()
                                                                   .setId(taskId)
                                                                   .build();
            final CannotUpdateTaskDescription rejection =
                    assertThrows(CannotUpdateTaskDescription.class,
                                 () -> throwCannotUpdateTaskDescription(cmd));
            final RejectedTaskCommandDetails rejectedCommand = rejection.getMessageThrown()
                                                                        .getUpdateRejected()
                                                                        .getRejectionDetails();
            final TaskId actualId = rejectedCommand.getTaskId();
            assertEquals(taskId, actualId);
        }
    }
}
