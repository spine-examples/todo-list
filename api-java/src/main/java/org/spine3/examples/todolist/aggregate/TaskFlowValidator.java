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

package org.spine3.examples.todolist.aggregate;

import org.spine3.examples.todolist.TaskStatus;

/**
 * Validates task commands and state transitions.
 *
 * @author Illia Shepilov
 */
/* package */ class TaskFlowValidator {

    private TaskFlowValidator() {
    }

    /**
     * Check whether the transition from the current task status to the new status is allowed.
     *
     * @param currentStatus current task status
     * @param newStatus     new task status
     */
    /* package */ static boolean isValidTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        final boolean isValid = TaskStatusTransition.isValid(currentStatus, newStatus);
        return isValid;
    }

    /* package */ static boolean isValidUpdateTaskPriorityCommand(TaskStatus currentStatus) {
        final boolean isValid = ensureNeitherCompletedNorDeleted(currentStatus);
        return isValid;
    }

    /* package */ static boolean isValidUpdateTaskDueDateCommand(TaskStatus currentStatus) {
        final boolean isValid = ensureNeitherCompletedNorDeleted(currentStatus);
        return isValid;
    }

    /* package */ static boolean isValidRemoveLabelFromTaskCommand(TaskStatus currentStatus) {
        final boolean isValid = ensureNeitherCompletedNorDeleted(currentStatus);
        return isValid;
    }

    /* package */ static boolean isValidAssignLabelToTaskCommand(TaskStatus currentStatus) {
        final boolean isValid = ensureNeitherCompletedNorDeleted(currentStatus);
        return isValid;
    }

    /* package */ static boolean isValidCreateDraftCommand(TaskStatus currentStatus) {
        final boolean isValid = ensureNeitherCompletedNorDeleted(currentStatus);
        return isValid;
    }

    /**
     * Verifies status of current task.
     *
     * <p>If it is COMPLETED or DELETED {@link TaskStatus} state, throws {@link IllegalStateException}.
     *
     * @param currentStatus task current state {@link TaskStatus}
     * @throws IllegalStateException if status, passed to the method,
     *                               {@code TaskStatus.COMPLETED} or {@code TaskStatus.DELETED}.
     */
    /* package */ static boolean ensureNeitherCompletedNorDeleted(TaskStatus currentStatus) {
        boolean isDeleted = ensureNotDeleted(currentStatus);
        boolean isCompleted = ensureNotCompleted(currentStatus);
        final boolean result = !isDeleted && !isCompleted;
        return result;
    }

    private static boolean ensureNotCompleted(TaskStatus currentStatus) {
        final boolean isCompleted = currentStatus == TaskStatus.COMPLETED;
        return isCompleted;
    }

    private static boolean ensureNotDeleted(TaskStatus currentStatus) {
        final boolean isDeleted = currentStatus == TaskStatus.DELETED;
        return isDeleted;
    }
}
