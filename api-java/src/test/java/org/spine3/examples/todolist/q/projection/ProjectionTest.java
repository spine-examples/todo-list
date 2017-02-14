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

package org.spine3.examples.todolist.q.projection;

import org.spine3.examples.todolist.TaskListId;

import static org.spine3.base.Identifiers.newUuid;

/**
 * @author Illia Shepilov
 */
abstract class ProjectionTest {

    static final String DOES_NOT_UPDATE_TASK_DUE_DATE = "does not update task due date";
    static final String TASK_COMPLETED_EVENT = "TaskCompleted event";
    static final String TASK_REOPENED_EVENT = "TaskReopened event";
    static final String UPDATES_LABEL_DETAILS = "updates label details";
    static final String DOES_NOT_UPDATE_LABEL_DETAILS = "does not update label details";
    static final String LABEL_REMOVED_FROM_TASK_EVENT = "LabelRemovedFromTask event";
    static final String LABEL_DETAILS_UPDATED_EVENT = "LabelDetailsUpdated event";
    static final String TASK_PRIORITY_UPDATED_EVENT = "TaskPriorityUpdated event";
    static final String TASK_DUE_DATE_UPDATED_EVENT = "TaskDueDateUpdated event";
    static final String TASK_DESCRIPTION_UPDATED_EVENT = "TaskDescriptionUpdated event";
    static final String TASK_DELETED_EVENT = "TaskDeleted event";
    static final String REMOVES_TASK_VIEW_FROM_STATE = "removes TaskView form state";
    static final String ADDS_TASK_VIEW_TO_STATE = "adds TaskView to state";
    static final String UPDATES_TASK_DUE_DATE = "updates task due date";
    static final String UPDATES_TASK_DESCRIPTION = "updates task description";
    static final String UPDATES_TASK_PRIORITY = "updates the task priority";
    static final String DOES_NOT_UPDATE_TASK_PRIORITY_BY_WRONG_TASK_ID =
            "does not update the task priority by wrong task ID";
    static final String DOES_NOT_UPDATE_TASK_DESCRIPTION_BY_WRONG_TASK_ID =
            "does not update task description by wrong task ID";

    TaskListId createTaskListId() {
        final TaskListId taskListId = TaskListId.newBuilder()
                                                .setValue(newUuid())
                                                .build();
        return taskListId;
    }
}
