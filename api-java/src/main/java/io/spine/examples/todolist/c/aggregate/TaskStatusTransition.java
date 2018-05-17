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

package io.spine.examples.todolist.c.aggregate;

import io.spine.examples.todolist.TaskStatus;

/**
 * This class defines possible transitions of {@link TaskStatus}.
 *
 * @author Illia Shepilov
 */
enum TaskStatusTransition {

    /**
     * Finalize a task draft.
     */
    FINALIZE_DRAFT(TaskStatus.DRAFT, TaskStatus.FINALIZED),

    /**
     * Complete a task.
     */
    COMPLETE_TASK(TaskStatus.FINALIZED, TaskStatus.COMPLETED),

    /**
     * Reopen a completed task.
     */
    REOPEN_TASK(TaskStatus.COMPLETED, TaskStatus.OPEN),

    /**
     * Delete a task.
     */
    DELETE_FINALIZED_TASK(TaskStatus.FINALIZED, TaskStatus.DELETED),

    /**
     * Delete a draft.
     */
    DELETE_DRAFT_TASK(TaskStatus.DRAFT, TaskStatus.DELETED),

    /**
     * Restore a deleted task.
     */
    RESTORE_DELETED_TASK(TaskStatus.DELETED, TaskStatus.OPEN);

    /**
     * Current status of the task.
     */
    private final TaskStatus currentStatus;

    /**
     * New status of the task gets after transition.
     */
    private final TaskStatus newStatus;

    TaskStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        this.currentStatus = currentStatus;
        this.newStatus = newStatus;
    }

    /**
     * Checks if the current status and new suggested status are associated
     * with one of the described above transitions.
     *
     * @param currentStatus current status of the task
     * @param newStatus     suggested new status of the task
     * @return {@code true} if passed status matches transition, {@code false} otherwise
     */
    private boolean matches(TaskStatus currentStatus, TaskStatus newStatus) {
        final boolean isCurrentStatusMatches = this.currentStatus == currentStatus;
        final boolean isNewStatusMatches = this.newStatus == newStatus;
        final boolean result = isCurrentStatusMatches && isNewStatusMatches;
        return result;
    }

    /**
     * Verifies if the suggested status transition is valid.
     *
     * @param currentStatus current status of the task
     * @param newStatus     suggested new status of the task
     * @return {@code true} if passed statuses satisfy one of the transitions,
     *         {@code false} otherwise
     */
    static boolean isValid(TaskStatus currentStatus, TaskStatus newStatus) {
        for (TaskStatusTransition transition : TaskStatusTransition.values()) {
            if (transition.matches(currentStatus, newStatus)) {
                return true;
            }
        }
        return false;
    }
}
