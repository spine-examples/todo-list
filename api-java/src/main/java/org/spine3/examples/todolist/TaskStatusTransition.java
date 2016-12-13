//
// Copyright 2016, TeamDev Ltd. All rights reserved.
//
// Redistribution and use in source and/or binary forms, with or without
// modification, must retain the above copyright notice and the following
// disclaimer.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
package org.spine3.examples.todolist;

/**
 * This class defines possible transitions of {@link TaskStatus}.
 *
 * @author Illia Shepilov
 */
public enum TaskStatusTransition {

    /**
     * Finalize task's DRAFT.
     */
    FINALIZE_DRAFT(TaskStatus.DRAFT, TaskStatus.FINALIZED),
    /**
     * Complete FINALIZED draft.
     */
    COMPLETE_TASK(TaskStatus.FINALIZED, TaskStatus.COMPLETED),
    /**
     * Reopen already COMPLETED task.
     */
    REOPEN_TASK(TaskStatus.COMPLETED, TaskStatus.OPEN),
    /**
     * Delete task in FINALIZED_DRAFT state.
     */
    DELETE_FINALIZED_TASK(TaskStatus.FINALIZED, TaskStatus.DELETED),
    /**
     * Delete task into DRAFT state.
     */
    DELETE_DRAFT_TASK(TaskStatus.DRAFT, TaskStatus.DELETED),
    /**
     * Restore already DELETED task.
     */
    RESTORE_DELETED_TASK(TaskStatus.DELETED, TaskStatus.OPEN);

    TaskStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        this.currentStatus = currentStatus;
        this.newStatus = newStatus;
    }

    /**
     * Current status of the task.
     */
    private final TaskStatus currentStatus;
    /**
     * New status of the task gets after transition.
     */
    private final TaskStatus newStatus;

    /**
     * Verifies if the current status and new suggested status are associated
     * with one of the described above transitions.
     *
     * @param currentStatus current status of the task
     * @param newStatus     suggested new status of the task
     * @return {@code true} if passed status matches transition, otherwise {@code false}
     */
    private boolean matches(TaskStatus currentStatus, TaskStatus newStatus) {
        final boolean isCurrentStatusMatches = this.currentStatus == currentStatus;
        final boolean isNewStatusMatches = this.newStatus == newStatus;
        return isCurrentStatusMatches && isNewStatusMatches;
    }

    /**
     * Verifies if the suggested status transition is valid.
     *
     * @param currentStatus current status of the task
     * @param newStatus     suggested new status of the task
     * @return {@code true} if passed statuses satisfy one of the transitions, {@code false} otherwise
     */
    public static boolean isValid(TaskStatus currentStatus, TaskStatus newStatus) {
        for (TaskStatusTransition transition : TaskStatusTransition.values()) {
            if (transition.matches(currentStatus, newStatus)) {
                return true;
            }
        }
        return false;
    }

}
