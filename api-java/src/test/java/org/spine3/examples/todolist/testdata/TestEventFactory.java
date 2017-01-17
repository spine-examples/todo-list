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

package org.spine3.examples.todolist.testdata;

import com.google.protobuf.Timestamp;
import org.spine3.change.StringChange;
import org.spine3.change.TimestampChange;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsChange;
import org.spine3.examples.todolist.PriorityChange;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.events.DeletedTaskRestored;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelCreated;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.events.LabelledTaskRestored;
import org.spine3.examples.todolist.c.events.TaskCompleted;
import org.spine3.examples.todolist.c.events.TaskCreated;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDraftCreated;
import org.spine3.examples.todolist.c.events.TaskDraftFinalized;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.examples.todolist.c.events.TaskReopened;
import org.spine3.protobuf.Timestamps;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.LABEL_TITLE;

/**
 * A factory of events for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventFactory {

    public static final String UPDATED_DESCRIPTION = "Description updated.";
    public static final String DESCRIPTION = "task description";
    public static final TaskPriority TASK_PRIORITY = TaskPriority.NORMAL;
    public static final TaskPriority UPDATED_TASK_PRIORITY = TaskPriority.NORMAL;
    public static final Timestamp UPDATED_TASK_DUE_DATE = Timestamps.getCurrentTime();
    public static final TaskLabelId LABEL_ID = TaskLabelId.newBuilder()
                                                          .setValue(newUuid())
                                                          .build();
    public static final TaskId TASK_ID = TaskId.newBuilder()
                                               .setValue(newUuid())
                                               .build();
    private static final Timestamp CREATION_TIME = Timestamps.getCurrentTime();

    private TestEventFactory() {
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
     * @param id the ID of the created task
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
     * @param description  the description of the created draft
     * @param priority     the priority of the created draft
     * @param creationTime the time creation of the created draft
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
        return taskDescriptionUpdatedInstance(TASK_ID, UPDATED_DESCRIPTION);
    }

    /**
     * Provides {@link TaskDescriptionUpdated} event by task description and task ID specified.
     *
     * @param description the description of the updated task
     * @return {@link TaskDescriptionUpdated} instance
     */
    public static TaskDescriptionUpdated taskDescriptionUpdatedInstance(TaskId id, String description) {
        final StringChange descriptionChange = StringChange.newBuilder()
                                                           .setNewValue(description)
                                                           .build();
        final TaskDescriptionUpdated result = TaskDescriptionUpdated.newBuilder()
                                                                    .setId(id)
                                                                    .setDescriptionChange(descriptionChange)
                                                                    .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link TaskPriorityUpdated} event instance.
     *
     * @return {@link TaskPriorityUpdated} instance
     */
    public static TaskPriorityUpdated taskPriorityUpdatedInstance() {
        return taskPriorityUpdatedInstance(TASK_ID, UPDATED_TASK_PRIORITY);
    }

    /**
     * Provides {@link TaskPriorityUpdated} event by specified task priority.
     *
     * @param priority the priority of the updated task
     * @return {@link TaskPriorityUpdated} instance
     */
    public static TaskPriorityUpdated taskPriorityUpdatedInstance(TaskId id, TaskPriority priority) {
        final PriorityChange taskPriorityChange = PriorityChange.newBuilder()
                                                                .setNewValue(priority)
                                                                .build();
        final TaskPriorityUpdated result = TaskPriorityUpdated.newBuilder()
                                                              .setId(id)
                                                              .setPriorityChange(taskPriorityChange)
                                                              .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link TaskDueDateUpdated} event instance.
     *
     * @return {@link TaskDueDateUpdated} instance
     */
    public static TaskDueDateUpdated taskDueDateUpdatedInstance() {
        return taskDueDateUpdatedInstance(TASK_ID, UPDATED_TASK_DUE_DATE);
    }

    /**
     * Provides {@link TaskDueDateUpdated} event by task due date and task ID specified.
     *
     * @param dueDate the due date of the updated task
     * @return {@link TaskDueDateUpdated} instance
     */
    public static TaskDueDateUpdated taskDueDateUpdatedInstance(TaskId id, Timestamp dueDate) {
        final TimestampChange dueDateChange = TimestampChange.newBuilder()
                                                             .setNewValue(dueDate)
                                                             .build();
        final TaskDueDateUpdated result = TaskDueDateUpdated.newBuilder()
                                                            .setId(id)
                                                            .setDueDateChange(dueDateChange)
                                                            .build();
        return result;
    }

    /**
     * Provides pre-configured {@link TaskDraftFinalized} event instance.
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
     * Provides pre-configured {@link TaskCompleted} event instance.
     *
     * @return {@link TaskCompleted} instance
     */
    public static TaskCompleted taskCompletedInstance() {
        return taskCompletedInstance(TASK_ID);
    }

    /**
     * Provides pre-configured {@link TaskCompleted} event instance.
     *
     * @return {@link TaskCompleted} instance
     */
    public static TaskCompleted taskCompletedInstance(TaskId id) {
        final TaskCompleted result = TaskCompleted.newBuilder()
                                                  .setId(id)
                                                  .build();
        return result;
    }

    /**
     * Provides pre-configured {@link TaskReopened} event instance.
     *
     * @return {@link TaskReopened} instance
     */
    public static TaskReopened taskReopenedInstance() {
        return taskReopenedInstance(TASK_ID);
    }

    /**
     * Provides pre-configured {@link TaskReopened} event instance.
     *
     * @return {@link TaskReopened} instance
     */
    public static TaskReopened taskReopenedInstance(TaskId id) {
        final TaskReopened result = TaskReopened.newBuilder()
                                                .setId(id)
                                                .build();
        return result;
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
     * Provides default {@link LabelledTaskRestored} event instance.
     *
     * @return {@link LabelledTaskRestored} instance
     */
    public static LabelledTaskRestored labelledTaskRestoredInstance() {
        final LabelledTaskRestored result = LabelledTaskRestored.newBuilder()
                                                                .setTaskId(TASK_ID)
                                                                .setLabelId(LABEL_ID)
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
        return labelAssignedToTaskInstance(TASK_ID, LABEL_ID);
    }

    /**
     * Provides {@link LabelAssignedToTask} event by specified task label ID.
     *
     * @param labelId the ID of the assigned label
     * @return {@link LabelAssignedToTask} instance
     */
    public static LabelAssignedToTask labelAssignedToTaskInstance(TaskId taskId, TaskLabelId labelId) {
        final LabelAssignedToTask result = LabelAssignedToTask.newBuilder()
                                                              .setTaskId(taskId)
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

    /**
     * Provides a pre-configured {@link LabelCreated} event instance.
     *
     * @return {@link LabelCreated} instance
     */
    public static LabelCreated labelCreatedInstance() {
        return labelCreatedInstance(LabelColor.GRAY, LABEL_TITLE);
    }

    /**
     * Provides {@link LabelCreated} event by specified label color and title.
     *
     * @param color the color of the created label
     * @param title the title of the created label
     * @return {@link LabelCreated} instance
     */
    public static LabelCreated labelCreatedInstance(LabelColor color, String title) {
        final LabelDetails.Builder labelDetailsBuilder = LabelDetails.newBuilder()
                                                                     .setColor(color)
                                                                     .setTitle(title);
        final LabelCreated result = LabelCreated.newBuilder()
                                                .setDetails(labelDetailsBuilder)
                                                .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link LabelDetailsUpdated} event instance.
     *
     * @return {@link LabelDetailsUpdated} instance.
     */
    public static LabelDetailsUpdated labelDetailsUpdatedInstance() {
        return labelDetailsUpdatedInstance(LABEL_ID, LabelColor.GRAY, LABEL_TITLE);
    }

    /**
     * Provides {@link LabelDetailsUpdated} event by specified label color and title.
     *
     * @param color the color of the updated label details
     * @param title the title of the updated label details
     * @return {@link LabelDetailsUpdated} instance
     */
    public static LabelDetailsUpdated labelDetailsUpdatedInstance(TaskLabelId labelId, LabelColor color, String title) {
        final LabelDetails.Builder labelDetailsBuilder = LabelDetails.newBuilder()
                                                                     .setColor(color)
                                                                     .setTitle(title);
        final LabelDetailsChange labelDetailsChange = LabelDetailsChange.newBuilder()
                                                                        .setNewDetails(labelDetailsBuilder)
                                                                        .build();
        final LabelDetailsUpdated result = LabelDetailsUpdated.newBuilder()
                                                              .setLabelId(labelId)
                                                              .setLabelDetailsChange(labelDetailsChange)
                                                              .build();
        return result;
    }
}
