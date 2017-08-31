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

package io.spine.examples.todolist.c.aggregate.failures;

import io.spine.examples.todolist.FailedTaskCommandDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.ChangeStatusFailures;
import io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.TaskCreationFailures;
import io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.UpdateFailures;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.failures.CannotCreateDraft;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskDescription;
import io.spine.examples.todolist.c.failures.CannotUpdateTaskDueDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.TaskCreationFailures.throwCannotCreateDraftFailure;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.UpdateFailures.throwCannotUpdateTaskDescription;
import static io.spine.examples.todolist.c.aggregate.failures.TaskPartFailures.UpdateFailures.throwCannotUpdateTaskDueDate;
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
        assertHasPrivateParameterlessCtor(TaskPartFailures.class);
    }

    @Nested
    @DisplayName("ChangeStatusFailures should")
    class ChangeStatusFailuresTest {

        @Test
        @DisplayName("have the private constructor")
        void havePrivateConstructor() {
            assertHasPrivateParameterlessCtor(ChangeStatusFailures.class);
        }
    }

    @Nested
    @DisplayName("TaskCreationFailures should")
    class TaskCreationFailuresTest {

        @Test
        @DisplayName("have the private constructor")
        void havePrivateConstructor() {
            assertHasPrivateParameterlessCtor(TaskCreationFailures.class);
        }

        @Test
        @DisplayName("throw CannotCreateDraft failure")
        void throwCannotCreateDraft() {
            final CreateDraft cmd = CreateDraft.newBuilder()
                                               .setId(taskId)
                                               .build();
            final CannotCreateDraft failure = assertThrows(CannotCreateDraft.class,
                                                           () -> throwCannotCreateDraftFailure(
                                                                   cmd));
            final TaskId actual = failure.getMessageThrown()
                                         .getCreateDraftFailed()
                                         .getFailureDetails()
                                         .getTaskId();
            assertEquals(taskId, actual);
        }
    }

    @Nested
    @DisplayName("UpdateFailures should")
    class UpdateFailuresTest {

        @Test
        @DisplayName("have the private constructor")
        void havePrivateConstructor() {
            assertHasPrivateParameterlessCtor(UpdateFailures.class);
        }

        @Test
        @DisplayName("throw CannotUpdateTaskDueDate failure")
        void throwCannotUpdateTaskDueDateFailure() {
            final UpdateTaskDueDate cmd = UpdateTaskDueDate.newBuilder()
                                                           .setId(taskId)
                                                           .build();
            final CannotUpdateTaskDueDate failure =
                    assertThrows(CannotUpdateTaskDueDate.class,
                                 () -> throwCannotUpdateTaskDueDate(cmd));
            final FailedTaskCommandDetails failedCommand = failure.getMessageThrown()
                                                                  .getUpdateFailed()
                                                                  .getFailureDetails();
            final TaskId actualId = failedCommand.getTaskId();
            assertEquals(taskId, actualId);
        }

        @Test
        @DisplayName("throw CannotUpdateTaskDescription failure")
        void throwCannotUpdateTaskDescriptionFailure() {
            final UpdateTaskDescription cmd = UpdateTaskDescription.newBuilder()
                                                                   .setId(taskId)
                                                                   .build();
            final CannotUpdateTaskDescription failure =
                    assertThrows(CannotUpdateTaskDescription.class,
                                 () -> throwCannotUpdateTaskDescription(cmd));
            final FailedTaskCommandDetails failedCommand = failure.getMessageThrown()
                                                                  .getUpdateFailed()
                                                                  .getFailureDetails();
            final TaskId actualId = failedCommand.getTaskId();
            assertEquals(taskId, actualId);
        }
    }
}
