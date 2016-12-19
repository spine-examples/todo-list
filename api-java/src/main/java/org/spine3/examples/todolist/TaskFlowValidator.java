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

package org.spine3.examples.todolist;

/**
 * Validates task commands and state transitions.
 *
 * @author Illia Shepilov
 */
public class TaskFlowValidator {

    /**
     * Check whether the transition from the current task status to the new status is allowed.
     *
     * @param currentStatus current task status
     * @param newStatus     new task status
     */
    public static void validateTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        final boolean isValid = TaskStatusTransition.isValid(currentStatus, newStatus);

        if (!isValid) {
            String message = String.format("Cannot make transition from: %s to: %s state",
                                           currentStatus, newStatus);
            throw new IllegalStateException(message);
        }
    }

    public static void validateUpdateTaskPriorityCommand(TaskStatus currentStatus) {
        ensureNeitherCompletedNorDeleted(currentStatus);
    }

    public static void validateUpdateTaskDueDateCommand(TaskStatus currentStatus) {
        ensureNeitherCompletedNorDeleted(currentStatus);
    }

    public static void validateRemoveLabelFromTaskCommand(TaskStatus currentStatus) {
        ensureNeitherCompletedNorDeleted(currentStatus);
    }

    public static void validateAssignLabelToTaskCommand(TaskStatus currentStatus) {
        ensureNeitherCompletedNorDeleted(currentStatus);
    }

    /**
     * Verifies status of current task.
     *
     * <p>If it is COMPLETED or DELETED {@link TaskStatus} state, throws {@link IllegalStateException}.
     *
     * @param currentStatus task's current state {@link TaskStatus}
     * @throws IllegalStateException if status, passed to the method,
     *                               {@code TaskStatus.COMPLETED} or {@code TaskStatus.DELETED}.
     */
    public static void ensureNeitherCompletedNorDeleted(TaskStatus currentStatus) {
        ensureNotDeleted(currentStatus);
        ensureNotCompleted(currentStatus);
    }

    private static void ensureNotCompleted(TaskStatus currentStatus) {
        if (currentStatus == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Command cannot be applied on completed task.");
        }
    }

    private static void ensureNotDeleted(TaskStatus currentStatus) {
        if (currentStatus == TaskStatus.DELETED) {
            throw new IllegalStateException("Command cannot be applied on deleted task.");
        }
    }
}
