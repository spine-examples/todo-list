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
 * Provides methods for instantiation events for test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventFactory {

    private static final String DESCRIPTION = "task description";
    private static final TaskPriority TASK_PRIORITY = TaskPriority.NORMAL;
    private static final Timestamp TASK_DUE_DATE = Timestamps.getCurrentTime();
    public static final TaskLabelId LABEL_ID = TaskLabelId.newBuilder()
                                                          .setValue(newUuid())
                                                          .build();

    private static final Timestamp CREATION_TIME = Timestamps.getCurrentTime();

    /**
     * Prevent instantiation.
     */
    private TestEventFactory() {
        throw new UnsupportedOperationException("Cannot be instantiated");
    }

    /**
     * Provides {@link TaskCreated} event instance by specified description {@code DESCRIPTION}
     * and task's priority {@code TASK_PRIORITY}.
     *
     * @return {@link TaskCreated} instance
     */
    public static TaskCreated taskCreatedInstance() {
        return taskCreatedInstance(DESCRIPTION, TASK_PRIORITY);
    }

    /**
     * Provides {@link TaskCreated} event by specified description and task's priority.
     *
     * @param description specified task's description
     * @param priority    specified task's priority
     * @return {@link TaskCreated} instance
     */
    public static TaskCreated taskCreatedInstance(String description, TaskPriority priority) {
        return TaskCreated.newBuilder()
                          .setDetails(TaskDetails.newBuilder()
                                                 .setDescription(description)
                                                 .setPriority(priority))
                          .build();
    }

    /**
     * Provides {@link TaskCreated} event by specified task's id.
     *
     * @param id task's id
     * @return {@link TaskCreated} instance
     */
    public static TaskCreated taskCreatedInstance(TaskId id) {
        return TaskCreated.newBuilder()
                          .setId(id)
                          .build();
    }

    /**
     * Provides {@link TaskCreated} event by specified description {@see DESCRIPTION}
     * and task's priority {@code TASK_PRIORITY}.
     *
     * @return {@link TaskCreated} instance
     */
    public static TaskDraftCreated taskDraftCreatedInstance() {
        return taskDraftCreatedInstance(DESCRIPTION, TASK_PRIORITY, CREATION_TIME);
    }

    /**
     * Provides {@link TaskDraftCreated} event by specified task's description,
     * task's priority and draft's creation time.
     *
     * @param description  task's description
     * @param priority     task's priority
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
                                                        .setDetails(details)
                                                        .setDraftCreationTime(creationTime)
                                                        .build();
        return result;
    }

    /**
     * Provides {@link TaskDescriptionUpdated} event by specified task's description {@code DESCRIPTION}.
     *
     * @return {@link TaskDescriptionUpdated} instance
     */
    public static TaskDescriptionUpdated taskDescriptionUpdatedInstance() {
        return taskDescriptionUpdatedInstance(DESCRIPTION);
    }

    /**
     * Provides {@link TaskDescriptionUpdated} event by specified task's description.
     *
     * @param description task's description
     * @return {@link TaskDescriptionUpdated} instance
     */
    public static TaskDescriptionUpdated taskDescriptionUpdatedInstance(String description) {
        return TaskDescriptionUpdated.newBuilder()
                                     .setNewDescription(description)
                                     .build();
    }

    /**
     * Provides {@link TaskPriorityUpdated} event by specified task's priority {@code TASK_PRIORITY}.
     *
     * @return {@link TaskPriorityUpdated} instance
     */
    public static TaskPriorityUpdated taskPriorityUpdatedInstance() {
        return taskPriorityUpdatedInstance(TASK_PRIORITY);
    }

    /**
     * Provides {@link TaskPriorityUpdated} event by specified tasl's priority.
     *
     * @param priority task's priority
     * @return {@link TaskPriorityUpdated} instance
     */
    public static TaskPriorityUpdated taskPriorityUpdatedInstance(TaskPriority priority) {
        return TaskPriorityUpdated.newBuilder()
                                  .setNewPriority(priority)
                                  .build();
    }

    /**
     * Provides {@link TaskDueDateUpdated} event by specified task's due date value {@code TASK_DUE_DATE}.
     *
     * @return {@link TaskDueDateUpdated} instance
     */
    public static TaskDueDateUpdated taskDueDateUpdatedInstance() {
        return taskDueDateUpdatedInstance(TASK_DUE_DATE);
    }

    /**
     * Provides {@link TaskDueDateUpdated} event by specified task's due date.
     *
     * @param dueDate the due date value for the task
     * @return {@link TaskDueDateUpdated} instance
     */
    public static TaskDueDateUpdated taskDueDateUpdatedInstance(Timestamp dueDate) {
        return TaskDueDateUpdated.newBuilder()
                                 .setNewDueDate(dueDate)
                                 .build();
    }

    /**
     * Provides default {@link TaskDraftFinalized} event instance.
     *
     * @return {@link TaskDraftFinalized} instance
     */
    public static TaskDraftFinalized taskDraftFinalizedInstance() {
        return TaskDraftFinalized.getDefaultInstance();
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
        return TaskDeleted.getDefaultInstance();
    }

    /**
     * Provides default {@link DeletedTaskRestored} event instance.
     *
     * @return {@link DeletedTaskRestored} instance
     */
    public static DeletedTaskRestored deletedTaskRestoredInstance() {
        return DeletedTaskRestored.getDefaultInstance();
    }

    /**
     * Provides {@link LabelAssignedToTask} event by task's label id {@code LABEL_ID}.
     *
     * @return {@link LabelAssignedToTask} instance
     */
    public static LabelAssignedToTask labelAssignedToTaskInstance() {
        return labelAssignedToTaskInstance(LABEL_ID);
    }

    /**
     * Provides {@link LabelAssignedToTask} event by specified task's label id.
     *
     * @param labelId label's id
     * @return {@link LabelAssignedToTask} instance
     */
    public static LabelAssignedToTask labelAssignedToTaskInstance(TaskLabelId labelId) {
        return LabelAssignedToTask.newBuilder()
                                  .setLabelId(labelId)
                                  .build();
    }

    /**
     * Provides {@link LabelRemovedFromTask} event by task's label id {@code LABEL_ID}.
     *
     * @return {@link LabelRemovedFromTask} instance
     */
    public static LabelRemovedFromTask labelRemovedFromTaskInstance() {
        return LabelRemovedFromTask.newBuilder()
                                   .setLabelId(LABEL_ID)
                                   .build();
    }
}
