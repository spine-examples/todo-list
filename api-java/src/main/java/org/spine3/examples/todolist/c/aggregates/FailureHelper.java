/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.c.aggregates;

import com.google.protobuf.Message;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.AssignLabelToTaskFailed;
import org.spine3.examples.todolist.CompleteTaskFailed;
import org.spine3.examples.todolist.CreateBasicTaskFailed;
import org.spine3.examples.todolist.CreateDraftFailed;
import org.spine3.examples.todolist.DeleteTaskFailed;
import org.spine3.examples.todolist.DescriptionUpdateFailed;
import org.spine3.examples.todolist.FinalizeDraftFailed;
import org.spine3.examples.todolist.LabelDetailsUpdateFailed;
import org.spine3.examples.todolist.PriorityUpdateFailed;
import org.spine3.examples.todolist.RemoveLabelFromTaskFailed;
import org.spine3.examples.todolist.ReopenTaskFailed;
import org.spine3.examples.todolist.RestoreDeletedTaskFailed;
import org.spine3.examples.todolist.TaskDueDateUpdateFailed;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.UnsuccessfulLabelCommand;
import org.spine3.examples.todolist.UnsuccessfulTaskCommand;
import org.spine3.examples.todolist.c.failures.CannotAssignLabelToTask;
import org.spine3.examples.todolist.c.failures.CannotCompleteTask;
import org.spine3.examples.todolist.c.failures.CannotCreateDraft;
import org.spine3.examples.todolist.c.failures.CannotCreateTaskWithInappropriateDescription;
import org.spine3.examples.todolist.c.failures.CannotDeleteTask;
import org.spine3.examples.todolist.c.failures.CannotFinalizeDraft;
import org.spine3.examples.todolist.c.failures.CannotRemoveLabelFromTask;
import org.spine3.examples.todolist.c.failures.CannotReopenTask;
import org.spine3.examples.todolist.c.failures.CannotRestoreDeletedTask;
import org.spine3.examples.todolist.c.failures.CannotUpdateLabelDetails;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDescription;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDueDate;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskPriority;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskWithInappropriateDescription;

import java.util.List;

/**
 * Utility class for working with failures.
 *
 * @author Illia Shepilov
 */
/* package */ class FailureHelper {

    /* package */ static final String TASK_DELETED_OR_COMPLETED_EXCEPTION_MESSAGE = "Command cannot be applied " +
            "to the deleted or completed task.";
    private static final String TOO_SHORT_TASK_DESCRIPTION_EXCEPTION_MESSAGE = "Description should contain " +
            "at least 3 alphanumeric symbols.";

    private FailureHelper() {
    }

    static void throwCannotReopenTaskFailure(TaskId taskId, String message) throws CannotReopenTask {
        final UnsuccessfulTaskCommand commandFailed = UnsuccessfulTaskCommand.newBuilder()
                                                                             .setTaskId(taskId)
                                                                             .setMessage(message)
                                                                             .build();
        final ReopenTaskFailed reopenTaskFailed = ReopenTaskFailed.newBuilder()
                                                                  .setFailedCommand(commandFailed)
                                                                  .build();
        throw new CannotReopenTask(reopenTaskFailed);
    }

    static void throwCannotFinalizeDraftFailure(TaskId taskId, String message)
            throws CannotFinalizeDraft {
        final UnsuccessfulTaskCommand commandFailed = UnsuccessfulTaskCommand.newBuilder()
                                                                             .setTaskId(taskId)
                                                                             .setMessage(message)
                                                                             .build();
        final FinalizeDraftFailed finalizeDraftFailed = FinalizeDraftFailed.newBuilder()
                                                                           .setFailedCommand(commandFailed)
                                                                           .build();
        throw new CannotFinalizeDraft(finalizeDraftFailed);
    }

    static void throwCannotCreateDraftFailure(TaskId taskId) throws CannotCreateDraft {
        final UnsuccessfulTaskCommand commandFailed =
                UnsuccessfulTaskCommand.newBuilder()
                                       .setTaskId(taskId)
                                       .setMessage(TASK_DELETED_OR_COMPLETED_EXCEPTION_MESSAGE)
                                       .build();
        final CreateDraftFailed createDraftFailed = CreateDraftFailed.newBuilder()
                                                                     .setFailedCommand(commandFailed)
                                                                     .build();
        throw new CannotCreateDraft(createDraftFailed);
    }

    static List<? extends Message> throwCannotDeleteTask(TaskId taskId, String message) throws CannotDeleteTask {

        final UnsuccessfulTaskCommand commandFailed = UnsuccessfulTaskCommand.newBuilder()
                                                                             .setTaskId(taskId)
                                                                             .setMessage(message)
                                                                             .build();
        final DeleteTaskFailed deleteTaskFailed = DeleteTaskFailed.newBuilder()
                                                                  .setFailedCommand(commandFailed)
                                                                  .build();
        throw new CannotDeleteTask(deleteTaskFailed);
    }

    static void throwCannotRemoveLabelFromTaskFailure(TaskLabelId labelId, TaskId taskId)
            throws CannotRemoveLabelFromTask {
        final UnsuccessfulTaskCommand commandFailed =
                UnsuccessfulTaskCommand.newBuilder()
                                       .setTaskId(taskId)
                                       .setMessage(TASK_DELETED_OR_COMPLETED_EXCEPTION_MESSAGE)
                                       .build();
        final RemoveLabelFromTaskFailed removeLabelFromTaskFailed =
                RemoveLabelFromTaskFailed.newBuilder()
                                         .setLabelId(labelId)
                                         .setFailedCommand(commandFailed)
                                         .build();
        throw new CannotRemoveLabelFromTask(removeLabelFromTaskFailed);
    }

    static void throwCannotCompleteTaskFailure(TaskId taskId, String message) throws CannotCompleteTask {
        final UnsuccessfulTaskCommand commandFailed = UnsuccessfulTaskCommand.newBuilder()
                                                                             .setTaskId(taskId)
                                                                             .setMessage(message)
                                                                             .build();
        final CompleteTaskFailed completeTaskFailed = CompleteTaskFailed.newBuilder()
                                                                        .setFailedCommand(commandFailed)
                                                                        .build();
        throw new CannotCompleteTask(completeTaskFailed);
    }

    static void throwCannotRestoreDeletedTaskFailure(TaskId taskId, String message)
            throws CannotRestoreDeletedTask {
        final UnsuccessfulTaskCommand commandFailed = UnsuccessfulTaskCommand.newBuilder()
                                                                             .setTaskId(taskId)
                                                                             .setMessage(message)
                                                                             .build();
        final RestoreDeletedTaskFailed restoreDeletedTaskFailed =
                RestoreDeletedTaskFailed.newBuilder()
                                        .setFailedCommand(commandFailed)
                                        .build();
        throw new CannotRestoreDeletedTask(restoreDeletedTaskFailed);
    }

    static void throwCannotUpdateTaskDueDateFailure(TaskId taskId) throws CannotUpdateTaskDueDate {
        final UnsuccessfulTaskCommand commandFailed =
                UnsuccessfulTaskCommand.newBuilder()
                                       .setTaskId(taskId)
                                       .setMessage(TASK_DELETED_OR_COMPLETED_EXCEPTION_MESSAGE)
                                       .build();
        final TaskDueDateUpdateFailed dueDateUpdateFailed =
                TaskDueDateUpdateFailed.newBuilder()
                                       .setFailedCommand(commandFailed)
                                       .build();
        throw new CannotUpdateTaskDueDate(dueDateUpdateFailed);
    }

    static void throwCannotUpdateTaskDescriptionFailure(TaskId taskId, String message, ValueMismatch mismatch)
            throws CannotUpdateTaskDescription {
        final UnsuccessfulTaskCommand commandFailed = UnsuccessfulTaskCommand.newBuilder()
                                                                             .setTaskId(taskId)
                                                                             .setMessage(message)
                                                                             .build();
        final DescriptionUpdateFailed descriptionUpdateFailed =
                DescriptionUpdateFailed.newBuilder()
                                       .setFailedCommand(commandFailed)
                                       .setDescriptionMismatch(mismatch)
                                       .build();
        throw new CannotUpdateTaskDescription(descriptionUpdateFailed);
    }

    static void throwCannotUpdateTaskDescriptionFailure(TaskId taskId, String message)
            throws CannotUpdateTaskDescription {
        final UnsuccessfulTaskCommand commandFailed = UnsuccessfulTaskCommand.newBuilder()
                                                                             .setTaskId(taskId)
                                                                             .setMessage(message)
                                                                             .build();
        final DescriptionUpdateFailed descriptionUpdateFailed =
                DescriptionUpdateFailed.newBuilder()
                                       .setFailedCommand(commandFailed)
                                       .build();
        throw new CannotUpdateTaskDescription(descriptionUpdateFailed);
    }

    static void throwCannotAssignLabelToTaskFailure(TaskId taskId, TaskLabelId labelId) throws CannotAssignLabelToTask {
        final UnsuccessfulTaskCommand commandFailed =
                UnsuccessfulTaskCommand.newBuilder()
                                       .setTaskId(taskId)
                                       .setMessage(TASK_DELETED_OR_COMPLETED_EXCEPTION_MESSAGE)
                                       .build();
        final AssignLabelToTaskFailed assignLabelToTaskFailed =
                AssignLabelToTaskFailed.newBuilder()
                                       .setAssignLabelFailed(commandFailed)
                                       .setLabelId(labelId)
                                       .build();
        throw new CannotAssignLabelToTask(assignLabelToTaskFailed);
    }

    static void throwCannotUpdateTaskDueDateFailure(TaskId taskId, ValueMismatch mismatch)
            throws CannotUpdateTaskDueDate {
        final UnsuccessfulTaskCommand commandFailed = UnsuccessfulTaskCommand.newBuilder()
                                                                             .setTaskId(taskId)
                                                                             .build();
        final TaskDueDateUpdateFailed dueDateUpdateFailed =
                TaskDueDateUpdateFailed.newBuilder()
                                       .setFailedCommand(commandFailed)
                                       .setDueDateMismatch(mismatch)
                                       .build();
        throw new CannotUpdateTaskDueDate(dueDateUpdateFailed);
    }

    static void throwCannotUpdateDescriptionFailure(TaskId taskId, ValueMismatch mismatch)
            throws CannotUpdateTaskDescription {
        final UnsuccessfulTaskCommand commandFailed = UnsuccessfulTaskCommand.newBuilder()
                                                                             .setTaskId(taskId)
                                                                             .build();
        final DescriptionUpdateFailed descriptionUpdateFailed =
                DescriptionUpdateFailed.newBuilder()
                                       .setFailedCommand(commandFailed)
                                       .setDescriptionMismatch(mismatch)
                                       .build();
        throw new CannotUpdateTaskDescription(descriptionUpdateFailed);
    }

    static void throwCannotUpdateTaskPriorityFailure(TaskId taskId, ValueMismatch mismatch)
            throws CannotUpdateTaskPriority {
        final UnsuccessfulTaskCommand commandFailed = UnsuccessfulTaskCommand.newBuilder()
                                                                             .setTaskId(taskId)
                                                                             .build();
        final PriorityUpdateFailed priorityUpdateFailed = PriorityUpdateFailed.newBuilder()
                                                                              .setFailedCommand(commandFailed)
                                                                              .setPriorityMismatch(mismatch)
                                                                              .build();
        throw new CannotUpdateTaskPriority(priorityUpdateFailed);
    }

    static void throwCannotUpdateTaskPriorityFailure(TaskId taskId) throws CannotUpdateTaskPriority {
        final UnsuccessfulTaskCommand commandFailed =
                UnsuccessfulTaskCommand.newBuilder()
                                       .setTaskId(taskId)
                                       .setMessage(TASK_DELETED_OR_COMPLETED_EXCEPTION_MESSAGE)
                                       .build();
        final PriorityUpdateFailed priorityUpdateFailed = PriorityUpdateFailed.newBuilder()
                                                                              .setFailedCommand(commandFailed)
                                                                              .build();
        throw new CannotUpdateTaskPriority(priorityUpdateFailed);
    }

    static void throwCannotUpdateLabelDetailsFailure(TaskLabelId labelId, ValueMismatch mismatch)
            throws CannotUpdateLabelDetails {
        final UnsuccessfulLabelCommand labelCommandFailed = UnsuccessfulLabelCommand.newBuilder()
                                                                                    .setLabelId(labelId)
                                                                                    .build();
        final LabelDetailsUpdateFailed labelDetailsUpdateFailed =
                LabelDetailsUpdateFailed.newBuilder()
                                        .setFailedCommand(labelCommandFailed)
                                        .setLabelDetailsMismatch(mismatch)
                                        .build();
        throw new CannotUpdateLabelDetails(labelDetailsUpdateFailed);
    }

    static void throwCannotUpdateTooShortDescriptionFailure(TaskId taskId)
            throws CannotUpdateTaskWithInappropriateDescription {
        final UnsuccessfulTaskCommand commandFailed =
                UnsuccessfulTaskCommand.newBuilder()
                                       .setMessage(TOO_SHORT_TASK_DESCRIPTION_EXCEPTION_MESSAGE)
                                       .setTaskId(taskId)
                                       .build();
        final DescriptionUpdateFailed updateFailed = DescriptionUpdateFailed.newBuilder()
                                                                            .setFailedCommand(commandFailed)
                                                                            .build();
        throw new CannotUpdateTaskWithInappropriateDescription(updateFailed);
    }

    static void throwCannotCreateTaskWithInappropriateDescription(TaskId taskId)
            throws CannotCreateTaskWithInappropriateDescription {
        final UnsuccessfulTaskCommand commandFailed =
                UnsuccessfulTaskCommand.newBuilder()
                                       .setMessage(TOO_SHORT_TASK_DESCRIPTION_EXCEPTION_MESSAGE)
                                       .setTaskId(taskId)
                                       .build();
        final CreateBasicTaskFailed createBasicTaskFailed = CreateBasicTaskFailed.newBuilder()
                                                                                 .setFailedCommand(commandFailed)
                                                                                 .build();
        throw new CannotCreateTaskWithInappropriateDescription(createBasicTaskFailed);
    }
}
