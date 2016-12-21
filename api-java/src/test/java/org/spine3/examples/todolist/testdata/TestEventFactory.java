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

package org.spine3.examples.todolist.testdata;

import com.google.protobuf.Timestamp;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.protobuf.Timestamps;

import static org.spine3.base.Identifiers.newUuid;

/**
 * A factory of events for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventFactory {

    public static final String DESCRIPTION = "task description";
    public static final TaskPriority TASK_PRIORITY = TaskPriority.NORMAL;
    private static final Timestamp TASK_DUE_DATE = Timestamps.getCurrentTime();
    public static final TaskLabelId LABEL_ID = TaskLabelId.newBuilder()
                                                          .setValue(newUuid())
                                                          .build();
    public static final TaskId TASK_ID = TaskId.newBuilder()
                                               .setValue(newUuid())
                                               .build();

    private static final Timestamp CREATION_TIME = Timestamps.getCurrentTime();

    /**
     * Prevent instantiation.
     */
    private TestEventFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Provides a pre-configured {@link TaskCreated} event instance.
     *
     * @return {@link TaskCreated} instance
     */
    public static TaskCreated taskCreatedInstance() {
        return taskCreatedInstance(DESCRIPTION, TASK_PRIORITY);
    }

    /**
     * Provides {@link TaskCreated} event by specified description and task priority.
     *
     * @param description specified task description
     * @param priority    specified task priority
     * @return {@link TaskCreated} instance
     */
    public static TaskCreated taskCreatedInstance(String description, TaskPriority priority) {
        final TaskDetails.Builder details = TaskDetails.newBuilder()
                                                       .setDescription(description)
                                                       .setPriority(priority);
        final TaskCreated result = TaskCreated.newBuilder()
                                              .setId(TASK_ID)
                                              .setDetails(details)
                                              .build();
        return result;
    }

    /**
     * Provides {@link TaskCreated} event by specified task ID.
     *
     * @param id task id
     * @return {@link TaskCreated} instance
     */
    public static TaskCreated taskCreatedInstance(TaskId id) {
        final TaskCreated result = TaskCreated.newBuilder()
                                              .setId(id)
                                              .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link TaskDraftCreated} event instance.
     *
     * @return {@link TaskDraftCreated} instance
     */
    public static TaskDraftCreated taskDraftCreatedInstance() {
        return taskDraftCreatedInstance(DESCRIPTION, TASK_PRIORITY, CREATION_TIME);
    }

    /**
     * Provides {@link TaskDraftCreated} event by specified task description,
     * task priority and draft creation time.
     *
     * @param description  task description
     * @param priority     task priority
     * @param creationTime time of draft creation
     * @return {@link TaskDraftCreated} instance
     */
    public static TaskDraftCreated taskDraftCreatedInstance(String description,
                                                            TaskPriority priority,
                                                            Timestamp creationTime) {
        final TaskDetails.Builder details = TaskDetails.newBuilder()
                                                       .setPriority(priority)
                                                       .setDescription(description);
        final TaskDraftCreated result = TaskDraftCreated.newBuilder()
                                                        .setId(TASK_ID)
                                                        .setDetails(details)
                                                        .setDraftCreationTime(creationTime)
                                                        .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link TaskDescriptionUpdated} event instance.
     *
     * @return {@link TaskDescriptionUpdated} instance
     */
    public static TaskDescriptionUpdated taskDescriptionUpdatedInstance() {
        return taskDescriptionUpdatedInstance(DESCRIPTION);
    }

    /**
     * Provides {@link TaskDescriptionUpdated} event by specified task description.
     *
     * @param description task description
     * @return {@link TaskDescriptionUpdated} instance
     */
    public static TaskDescriptionUpdated taskDescriptionUpdatedInstance(String description) {
        final TaskDescriptionUpdated result = TaskDescriptionUpdated.newBuilder()
                                                                    .setNewDescription(description)
                                                                    .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link TaskPriorityUpdated} event instance.
     *
     * @return {@link TaskPriorityUpdated} instance
     */
    public static TaskPriorityUpdated taskPriorityUpdatedInstance() {
        return taskPriorityUpdatedInstance(TASK_PRIORITY);
    }

    /**
     * Provides {@link TaskPriorityUpdated} event by specified task priority.
     *
     * @param priority task priority
     * @return {@link TaskPriorityUpdated} instance
     */
    public static TaskPriorityUpdated taskPriorityUpdatedInstance(TaskPriority priority) {
        final TaskPriorityUpdated result = TaskPriorityUpdated.newBuilder()
                                                              .setNewPriority(priority)
                                                              .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link TaskDueDateUpdated} event instance.
     *
     * @return {@link TaskDueDateUpdated} instance
     */
    public static TaskDueDateUpdated taskDueDateUpdatedInstance() {
        return taskDueDateUpdatedInstance(TASK_DUE_DATE);
    }

    /**
     * Provides {@link TaskDueDateUpdated} event by specified task due date.
     *
     * @param dueDate the due date value for the task
     * @return {@link TaskDueDateUpdated} instance
     */
    public static TaskDueDateUpdated taskDueDateUpdatedInstance(Timestamp dueDate) {
        final TaskDueDateUpdated result = TaskDueDateUpdated.newBuilder()
                                                            .setNewDueDate(dueDate)
                                                            .build();
        return result;
    }

    /**
     * Provides default {@link TaskDraftFinalized} event instance.
     *
     * @return {@link TaskDraftFinalized} instance
     */
    public static TaskDraftFinalized taskDraftFinalizedInstance() {
        final TaskDraftFinalized result = TaskDraftFinalized.newBuilder()
                                                           .setId(TASK_ID)
                                                           .build();
        return result;
    }

    /**
     * Provides default {@link TaskCompleted} event instance.
     *
     * @return {@link TaskCompleted} instance
     */
    public static TaskCompleted taskCompletedInstance() {
        return TaskCompleted.getDefaultInstance();
    }

    /**
     * Provides default {@link TaskReopened} event instance.
     *
     * @return {@link TaskCompleted} instance
     */
    public static TaskReopened taskReopenedInstance() {
        return TaskReopened.getDefaultInstance();
    }

    /**
     * Provides default {@link TaskDeleted} event instance.
     *
     * @return {@link TaskDeleted} instance
     */
    public static TaskDeleted taskDeletedInstance() {
        final TaskDeleted result = TaskDeleted.newBuilder()
                                             .setId(TASK_ID)
                                             .build();
        return result;
    }

    /**
     * Provides default {@link DeletedTaskRestored} event instance.
     *
     * @return {@link DeletedTaskRestored} instance
     */
    public static DeletedTaskRestored deletedTaskRestoredInstance() {
        final DeletedTaskRestored result = DeletedTaskRestored.newBuilder()
                                                             .setId(TASK_ID)
                                                             .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link LabelAssignedToTask} event instance.
     *
     * @return {@link LabelAssignedToTask} instance
     */
    public static LabelAssignedToTask labelAssignedToTaskInstance() {
        return labelAssignedToTaskInstance(LABEL_ID);
    }

    /**
     * Provides {@link LabelAssignedToTask} event by specified task label ID.
     *
     * @param labelId label id
     * @return {@link LabelAssignedToTask} instance
     */
    public static LabelAssignedToTask labelAssignedToTaskInstance(TaskLabelId labelId) {
        final LabelAssignedToTask result = LabelAssignedToTask.newBuilder()
                                                              .setId(TASK_ID)
                                                              .setLabelId(labelId)
                                                              .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link LabelRemovedFromTask} event instance.
     *
     * @return {@link LabelRemovedFromTask} instance
     */
    public static LabelRemovedFromTask labelRemovedFromTaskInstance() {
        final LabelRemovedFromTask result = LabelRemovedFromTask.newBuilder()
                                                                .setId(TASK_ID)
                                                                .setLabelId(LABEL_ID)
                                                                .build();
        return result;
    }
}
