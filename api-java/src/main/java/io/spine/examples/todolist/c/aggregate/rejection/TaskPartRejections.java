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

import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.CompleteTaskFailed;
import io.spine.examples.todolist.CreateDraftFailed;
import io.spine.examples.todolist.DeleteTaskFailed;
import io.spine.examples.todolist.DescriptionUpdateFailed;
import io.spine.examples.todolist.FailedTaskCommandDetails;
import io.spine.examples.todolist.FinalizeDraftFailed;
import io.spine.examples.todolist.PriorityUpdateFailed;
import io.spine.examples.todolist.ReopenTaskFailed;
import io.spine.examples.todolist.RestoreDeletedTaskFailed;
import io.spine.examples.todolist.TaskDueDateUpdateFailed;
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

/**
 * Utility class for working with {@link TaskPart} rejection.
 *
 * @author Illia Shepilov
 */
public class TaskPartRejections {

    private TaskPartRejections() {
        // Prevent instantiation of this utility class.
    }

    public static class UpdateRejections {

        private UpdateRejections() {
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDescription} rejection
         * according to the passed parameters.
         *
         * @param cmd the {@code UpdateTaskDescription} command which thrown the rejection
         * @throws CannotUpdateTaskDescription the rejection to throw
         */
        public static void throwCannotUpdateTaskDescription(UpdateTaskDescription cmd)
                throws CannotUpdateTaskDescription {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final DescriptionUpdateFailed descriptionUpdateFailed =
                    DescriptionUpdateFailed.newBuilder()
                                           .setRejectionDetails(commandFailed)
                                           .build();
            throw new CannotUpdateTaskDescription(descriptionUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDueDate} rejection
         * according to the passed parameters.
         *
         * @param cmd the {@code UpdateTaskDueDate} command which thrown the rejection
         * @throws CannotUpdateTaskDueDate the rejection to throw
         */
        public static void throwCannotUpdateTaskDueDate(UpdateTaskDueDate cmd)
                throws CannotUpdateTaskDueDate {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final TaskDueDateUpdateFailed dueDateUpdateFailed =
                    TaskDueDateUpdateFailed.newBuilder()
                                           .setRejectionDetails(commandFailed)
                                           .build();
            throw new CannotUpdateTaskDueDate(dueDateUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDueDate} rejection
         * according to the passed parameters.
         *
         * @param cmd      the {@code UpdateTaskDueDate} command which thrown the rejection
         * @param mismatch the {@link ValueMismatch}
         * @throws CannotUpdateTaskDueDate the rejection to throw
         */
        public static void throwCannotUpdateTaskDueDate(
                UpdateTaskDueDate cmd, ValueMismatch mismatch) throws CannotUpdateTaskDueDate {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final TaskDueDateUpdateFailed dueDateUpdateFailed =
                    TaskDueDateUpdateFailed.newBuilder()
                                           .setRejectionDetails(commandFailed)
                                           .setDueDateMismatch(mismatch)
                                           .build();
            throw new CannotUpdateTaskDueDate(dueDateUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDescription} rejection
         * according to the passed parameters.
         *
         * @param cmd      the {@code UpdateTaskDescription} command which thrown the rejection
         * @param mismatch the {@link ValueMismatch}
         * @throws CannotUpdateTaskDescription the rejection to throw
         */
        public static void throwCannotUpdateDescription(
                UpdateTaskDescription cmd, ValueMismatch mismatch)
                throws CannotUpdateTaskDescription {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final DescriptionUpdateFailed descriptionUpdateFailed =
                    DescriptionUpdateFailed.newBuilder()
                                           .setRejectionDetails(commandFailed)
                                           .setDescriptionMismatch(mismatch)
                                           .build();
            throw new CannotUpdateTaskDescription(descriptionUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskPriority} rejection
         * according to the passed parameters.
         *
         * @param cmd      the {@code UpdateTaskPriority} command which thrown the rejection
         * @param mismatch the {@link ValueMismatch}
         * @throws CannotUpdateTaskPriority the rejection to throw
         */
        public static void throwCannotUpdateTaskPriority(
                UpdateTaskPriority cmd, ValueMismatch mismatch) throws CannotUpdateTaskPriority {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final PriorityUpdateFailed priorityUpdateFailed =
                    PriorityUpdateFailed.newBuilder()
                                        .setRejectionDetails(commandFailed)
                                        .setPriorityMismatch(mismatch)
                                        .build();
            throw new CannotUpdateTaskPriority(priorityUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskPriority} rejection
         * according to the passed parameters.
         *
         * @param cmd the {@code UpdateTaskPriority} command which thrown the rejection
         * @throws CannotUpdateTaskPriority the rejection to throw
         */
        public static void throwCannotUpdateTaskPriority(UpdateTaskPriority cmd)
                throws CannotUpdateTaskPriority {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final PriorityUpdateFailed priorityUpdateFailed =
                    PriorityUpdateFailed.newBuilder()
                                        .setRejectionDetails(commandFailed)
                                        .build();
            throw new CannotUpdateTaskPriority(priorityUpdateFailed);
        }
    }

    public static class TaskCreationRejections {

        private TaskCreationRejections() {
        }

        /**
         * Constructs and throws the {@link CannotCreateDraft} rejection
         * according to the passed parameters.
         *
         * @param cmd the {@code CreateDraft} command which thrown the rejection
         * @throws CannotCreateDraft the rejection to throw
         */
        public static void throwCannotCreateDraft(CreateDraft cmd) throws CannotCreateDraft {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final CreateDraftFailed createDraftFailed =
                    CreateDraftFailed.newBuilder()
                                     .setRejectionDetails(commandFailed)
                                     .build();
            throw new CannotCreateDraft(createDraftFailed);
        }
    }

    public static class ChangeStatusRejections {

        private ChangeStatusRejections() {
        }

        /**
         * Constructs and throws the {@link CannotReopenTask} rejection
         * according to the passed parameters.
         *
         * @param cmd the {@code ReopenTask} command which thrown the rejection
         * @throws CannotReopenTask the rejection to throw
         */
        public static void throwCannotReopenTask(ReopenTask cmd) throws CannotReopenTask {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final ReopenTaskFailed reopenTaskFailed =
                    ReopenTaskFailed.newBuilder()
                                    .setRejectionDetails(commandFailed)
                                    .build();
            throw new CannotReopenTask(reopenTaskFailed);
        }

        /**
         * Constructs and throws the {@link CannotFinalizeDraft} rejection
         * according to the passed parameters.
         *
         * @param cmd the {@code FinalizeDraft} command which thrown the rejection
         * @throws CannotFinalizeDraft the rejection to throw
         */
        public static void throwCannotFinalizeDraft(FinalizeDraft cmd) throws CannotFinalizeDraft {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final FinalizeDraftFailed finalizeDraftFailed =
                    FinalizeDraftFailed.newBuilder()
                                       .setRejectionDetails(commandFailed)
                                       .build();
            throw new CannotFinalizeDraft(finalizeDraftFailed);
        }

        /**
         * Constructs and throws the {@link CannotDeleteTask} rejection
         * according to the passed parameters.
         *
         * @param cmd the {@code DeleteTask} command which thrown the rejection
         * @throws CannotDeleteTask the rejection to throw
         */
        public static void throwCannotDeleteTask(DeleteTask cmd) throws CannotDeleteTask {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final DeleteTaskFailed deleteTaskFailed =
                    DeleteTaskFailed.newBuilder()
                                    .setRejectionDetails(commandFailed)
                                    .build();
            throw new CannotDeleteTask(deleteTaskFailed);
        }

        /**
         * Constructs and throws the {@link CannotCompleteTask} rejection
         * according to the passed parameters.
         *
         * @param cmd the {@code CompleteTask} command which thrown the rejection
         * @throws CannotCompleteTask the rejection to throw
         */
        public static void throwCannotCompleteTask(CompleteTask cmd) throws CannotCompleteTask {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final CompleteTaskFailed completeTaskFailed =
                    CompleteTaskFailed.newBuilder()
                                      .setRejectionDetails(commandFailed)
                                      .build();
            throw new CannotCompleteTask(completeTaskFailed);
        }

        /**
         * Constructs and throws the {@link CannotRestoreDeletedTask} rejection
         * according to the passed parameters.
         *
         * @param cmd the {@code RestoreDeletedTask} command which thrown the rejection
         * @throws CannotRestoreDeletedTask the {@code CannotRestoreDeletedTask} rejection
         */
        public static void throwCannotRestoreDeletedTask(RestoreDeletedTask cmd)
                throws CannotRestoreDeletedTask {
            final FailedTaskCommandDetails commandFailed = newFailedTaskCommandDetails(cmd.getId());
            final RestoreDeletedTaskFailed restoreDeletedTaskFailed =
                    RestoreDeletedTaskFailed.newBuilder()
                                            .setRejectionDetails(commandFailed)
                                            .build();
            throw new CannotRestoreDeletedTask(restoreDeletedTaskFailed);
        }
    }

    private static FailedTaskCommandDetails newFailedTaskCommandDetails(TaskId taskId) {
        return FailedTaskCommandDetails.newBuilder()
                                       .setTaskId(taskId)
                                       .build();
    }
}
