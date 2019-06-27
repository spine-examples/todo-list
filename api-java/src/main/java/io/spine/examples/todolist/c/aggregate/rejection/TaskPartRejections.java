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

import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.CompleteTaskRejected;
import io.spine.examples.todolist.CreateDraftRejected;
import io.spine.examples.todolist.DeleteTaskRejected;
import io.spine.examples.todolist.DescriptionUpdateRejected;
import io.spine.examples.todolist.FinalizeDraftRejected;
import io.spine.examples.todolist.PriorityUpdateRejected;
import io.spine.examples.todolist.RejectedTaskCommandDetails;
import io.spine.examples.todolist.ReopenTaskRejected;
import io.spine.examples.todolist.RestoreDeletedTaskRejected;
import io.spine.examples.todolist.TaskDueDateUpdateRejected;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.aggregate.TaskPart;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.c.rejection.CannotCompleteTask;
import io.spine.examples.todolist.c.rejection.CannotCreateDraft;
import io.spine.examples.todolist.c.rejection.CannotDeleteTask;
import io.spine.examples.todolist.c.rejection.CannotFinalizeDraft;
import io.spine.examples.todolist.c.rejection.CannotReopenTask;
import io.spine.examples.todolist.c.rejection.CannotRestoreDeletedTask;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDescription;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDueDate;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskPriority;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for working with {@link TaskPart} rejection.
 */
public final class TaskPartRejections {

    private TaskPartRejections() {
    }

    public static final class UpdateRejections {

        private UpdateRejections() {
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDescription} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code UpdateTaskDescription} command which thrown the rejection
         * @throws CannotUpdateTaskDescription
         *         the rejection to throw
         */
        public static void throwCannotUpdateTaskDescription(UpdateTaskDescription cmd)
                throws CannotUpdateTaskDescription {
            checkNotNull(cmd);

            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            DescriptionUpdateRejected descriptionUpdateRejected = DescriptionUpdateRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .vBuild();
            CannotUpdateTaskDescription rejection = CannotUpdateTaskDescription
                    .newBuilder()
                    .setRejectionDetails(descriptionUpdateRejected)
                    .build();
            throw rejection;
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDueDate} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code UpdateTaskDueDate} command which thrown the rejection
         * @throws CannotUpdateTaskDueDate
         *         the rejection to throw
         */
        public static void throwCannotUpdateTaskDueDate(UpdateTaskDueDate cmd)
                throws CannotUpdateTaskDueDate {
            checkNotNull(cmd);

            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            TaskDueDateUpdateRejected dueDateUpdateRejected = TaskDueDateUpdateRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .vBuild();
            CannotUpdateTaskDueDate rejection = CannotUpdateTaskDueDate
                    .newBuilder()
                    .setRejectionDetails(dueDateUpdateRejected)
                    .build();
            throw rejection;
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDueDate} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code UpdateTaskDueDate} command which thrown the rejection
         * @param mismatch
         *         the {@link ValueMismatch}
         * @throws CannotUpdateTaskDueDate
         *         the rejection to throw
         */
        public static void throwCannotUpdateTaskDueDate(UpdateTaskDueDate cmd,
                                                        ValueMismatch mismatch)
                throws CannotUpdateTaskDueDate {
            checkNotNull(cmd);
            checkNotNull(mismatch);

            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            TaskDueDateUpdateRejected dueDateUpdateRejected = TaskDueDateUpdateRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .setDueDateMismatch(mismatch)
                    .vBuild();
            CannotUpdateTaskDueDate rejection = CannotUpdateTaskDueDate
                    .newBuilder()
                    .setRejectionDetails(dueDateUpdateRejected)
                    .build();
            throw rejection;
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDescription} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code UpdateTaskDescription} command which thrown the rejection
         * @param mismatch
         *         the {@link ValueMismatch}
         * @throws CannotUpdateTaskDescription
         *         the rejection to throw
         */
        public static void throwCannotUpdateDescription(UpdateTaskDescription cmd,
                                                        ValueMismatch mismatch)
                throws CannotUpdateTaskDescription {
            checkNotNull(cmd);
            checkNotNull(mismatch);

            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            DescriptionUpdateRejected descriptionUpdateRejected = DescriptionUpdateRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .setDescriptionMismatch(mismatch)
                    .vBuild();
            CannotUpdateTaskDescription rejection = CannotUpdateTaskDescription
                    .newBuilder()
                    .setRejectionDetails(descriptionUpdateRejected)
                    .build();
            throw rejection;
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskPriority} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code UpdateTaskPriority} command which thrown the rejection
         * @param mismatch
         *         the {@link ValueMismatch}
         * @throws CannotUpdateTaskPriority
         *         the rejection to throw
         */
        public static void throwCannotUpdateTaskPriority(UpdateTaskPriority cmd,
                                                         ValueMismatch mismatch)
                throws CannotUpdateTaskPriority {
            checkNotNull(cmd);
            checkNotNull(mismatch);

            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            PriorityUpdateRejected priorityUpdateRejected = PriorityUpdateRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .setPriorityMismatch(mismatch)
                    .vBuild();
            CannotUpdateTaskPriority rejection = CannotUpdateTaskPriority
                    .newBuilder()
                    .setRejectionDetails(priorityUpdateRejected)
                    .build();
            throw rejection;
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskPriority} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code UpdateTaskPriority} command which thrown the rejection
         * @throws CannotUpdateTaskPriority
         *         the rejection to throw
         */
        public static void throwCannotUpdateTaskPriority(UpdateTaskPriority cmd)
                throws CannotUpdateTaskPriority {
            checkNotNull(cmd);

            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            PriorityUpdateRejected priorityUpdateRejected = PriorityUpdateRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .vBuild();
            CannotUpdateTaskPriority rejection = CannotUpdateTaskPriority
                    .newBuilder()
                    .setRejectionDetails(priorityUpdateRejected)
                    .build();
            throw rejection;
        }
    }

    public static final class TaskCreationRejections {

        private TaskCreationRejections() {
        }

        /**
         * Constructs and throws the {@link CannotCreateDraft} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code CreateDraft} command which thrown the rejection
         * @throws CannotCreateDraft
         *         the rejection to throw
         */
        public static void throwCannotCreateDraft(CreateDraft cmd) throws CannotCreateDraft {
            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            CreateDraftRejected createDraftRejected = CreateDraftRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .vBuild();
            CannotCreateDraft rejection = CannotCreateDraft
                    .newBuilder()
                    .setRejectionDetails(createDraftRejected)
                    .build();
            throw rejection;
        }
    }

    @SuppressWarnings("OverlyCoupledClass") // A lot of rejection types.
    public static final class ChangeStatusRejections {

        private ChangeStatusRejections() {
        }

        /**
         * Constructs and throws the {@link CannotReopenTask} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code ReopenTask} command which thrown the rejection
         * @throws CannotReopenTask
         *         the rejection to throw
         */
        public static void throwCannotReopenTask(ReopenTask cmd) throws CannotReopenTask {
            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            ReopenTaskRejected reopenTaskRejected = ReopenTaskRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .vBuild();
            CannotReopenTask rejection = CannotReopenTask
                    .newBuilder()
                    .setRejectionDetails(reopenTaskRejected)
                    .build();
            throw rejection;
        }

        /**
         * Constructs and throws the {@link CannotFinalizeDraft} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code FinalizeDraft} command which thrown the rejection
         * @throws CannotFinalizeDraft
         *         the rejection to throw
         */
        public static void throwCannotFinalizeDraft(FinalizeDraft cmd) throws CannotFinalizeDraft {
            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            FinalizeDraftRejected finalizeDraftRejected = FinalizeDraftRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .vBuild();
            CannotFinalizeDraft rejection = CannotFinalizeDraft
                    .newBuilder()
                    .setRejectionDetails(finalizeDraftRejected)
                    .build();
            throw rejection;
        }

        /**
         * Constructs and throws the {@link CannotDeleteTask} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code DeleteTask} command which thrown the rejection
         * @throws CannotDeleteTask
         *         the rejection to throw
         */
        public static void throwCannotDeleteTask(DeleteTask cmd) throws CannotDeleteTask {
            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            DeleteTaskRejected deleteTaskRejected = DeleteTaskRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .vBuild();
            CannotDeleteTask rejection = CannotDeleteTask
                    .newBuilder()
                    .setRejectionDetails(deleteTaskRejected)
                    .build();
            throw rejection;
        }

        /**
         * Constructs and throws the {@link CannotCompleteTask} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code CompleteTask} command which thrown the rejection
         * @throws CannotCompleteTask
         *         the rejection to throw
         */
        public static void throwCannotCompleteTask(CompleteTask cmd) throws CannotCompleteTask {
            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            CompleteTaskRejected completeTaskRejected = CompleteTaskRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .vBuild();
            CannotCompleteTask rejection = CannotCompleteTask
                    .newBuilder()
                    .setRejectionDetails(completeTaskRejected)
                    .build();
            throw rejection;
        }

        /**
         * Constructs and throws the {@link CannotRestoreDeletedTask} rejection
         * according to the passed parameters.
         *
         * @param cmd
         *         the {@code RestoreDeletedTask} command which thrown the rejection
         * @throws CannotRestoreDeletedTask
         *         the {@code CannotRestoreDeletedTask} rejection
         */
        public static void throwCannotRestoreDeletedTask(RestoreDeletedTask cmd)
                throws CannotRestoreDeletedTask {
            RejectedTaskCommandDetails commandDetails = newRejectedTaskCommandDetails(cmd.getId());
            RestoreDeletedTaskRejected restoreTaskRejected = RestoreDeletedTaskRejected
                    .newBuilder()
                    .setCommandDetails(commandDetails)
                    .vBuild();
            CannotRestoreDeletedTask rejection = CannotRestoreDeletedTask
                    .newBuilder()
                    .setRejectionDetails(restoreTaskRejected)
                    .build();
            throw rejection;
        }
    }

    private static RejectedTaskCommandDetails newRejectedTaskCommandDetails(TaskId taskId) {
        return RejectedTaskCommandDetails.newBuilder()
                                         .setTaskId(taskId)
                                         .vBuild();
    }
}
