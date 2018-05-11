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

package io.spine.examples.todolist.testdata;

import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.change.StringChange;
import io.spine.change.TimestampChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.events.LabelledTaskRestored;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskCreated;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.events.TaskDraftFinalized;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;

import static io.spine.Identifier.newUuid;
import static io.spine.time.Time.getCurrentTime;

/**
 * A factory of the task events for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestTaskEventFactory {

    public static final String UPDATED_DESCRIPTION = "Updated description.";
    public static final String DESCRIPTION = "Task description.";
    public static final TaskPriority TASK_PRIORITY = TaskPriority.NORMAL;
    public static final TaskPriority UPDATED_TASK_PRIORITY = TaskPriority.HIGH;
    public static final Timestamp TASK_DUE_DATE = getCurrentTime();
    public static final Timestamp UPDATED_TASK_DUE_DATE = Timestamps.add(TASK_DUE_DATE,
                                                                         Duration.newBuilder()
                                                                                 .setSeconds(1000)
                                                                                 .build());
    public static final LabelId LABEL_ID = LabelId.newBuilder()
                                                  .setValue(newUuid())
                                                  .build();
    public static final TaskId TASK_ID = TaskId.newBuilder()
                                               .setValue(newUuid())
                                               .build();
    private static final Timestamp CREATION_TIME = getCurrentTime();

    private TestTaskEventFactory() {
    }

    public static class UpdateEvents {

        private UpdateEvents() {
        }

        /**
         * Provides a pre-configured {@link TaskDescriptionUpdated} event instance.
         *
         * @return the {@code TaskDescriptionUpdated} instance
         */
        public static TaskDescriptionUpdated taskDescriptionUpdatedInstance() {
            return taskDescriptionUpdatedInstance(TASK_ID, UPDATED_DESCRIPTION);
        }

        /**
         * Provides the {@link TaskDescriptionUpdated} event by task description and
         * task ID specified.
         *
         * @param description the description of the updated task
         * @return the {@code TaskDescriptionUpdated} instance
         */
        public static TaskDescriptionUpdated taskDescriptionUpdatedInstance(TaskId id,
                                                                            String description) {
            final StringChange descriptionChange = StringChange.newBuilder()
                                                               .setNewValue(description)
                                                               .build();
            final TaskDescriptionUpdated result =
                    TaskDescriptionUpdated.newBuilder()
                                          .setTaskId(id)
                                          .setDescriptionChange(descriptionChange)
                                          .build();
            return result;
        }

        /**
         * Provides a pre-configured {@link TaskPriorityUpdated} event instance.
         *
         * @return the {@code TaskPriorityUpdated} instance
         */
        public static TaskPriorityUpdated taskPriorityUpdatedInstance() {
            return taskPriorityUpdatedInstance(TASK_ID, UPDATED_TASK_PRIORITY);
        }

        /**
         * Provides the {@link TaskPriorityUpdated} event by specified task priority.
         *
         * @param priority the priority of the updated task
         * @return the {@code TaskPriorityUpdated} instance
         */
        public static TaskPriorityUpdated taskPriorityUpdatedInstance(TaskId id,
                                                                      TaskPriority priority) {
            final PriorityChange taskPriorityChange = PriorityChange.newBuilder()
                                                                    .setNewValue(priority)
                                                                    .build();
            final TaskPriorityUpdated result = TaskPriorityUpdated.newBuilder()
                                                                  .setTaskId(id)
                                                                  .setPriorityChange(
                                                                          taskPriorityChange)
                                                                  .build();
            return result;
        }

        /**
         * Provides a pre-configured {@link TaskDueDateUpdated} event instance.
         *
         * @return the {@code TaskDueDateUpdated} instance
         */
        public static TaskDueDateUpdated taskDueDateUpdatedInstance() {
            return taskDueDateUpdatedInstance(TASK_ID, UPDATED_TASK_DUE_DATE);
        }

        /**
         * Provides the {@link TaskDueDateUpdated} event by task due date and task ID specified.
         *
         * @param dueDate the due date of the updated task
         * @return the {@code TaskDueDateUpdated} instance
         */
        public static TaskDueDateUpdated taskDueDateUpdatedInstance(TaskId id, Timestamp dueDate) {
            final TimestampChange dueDateChange = TimestampChange.newBuilder()
                                                                 .setNewValue(dueDate)
                                                                 .build();
            final TaskDueDateUpdated result = TaskDueDateUpdated.newBuilder()
                                                                .setTaskId(id)
                                                                .setDueDateChange(dueDateChange)
                                                                .build();
            return result;
        }
    }

    public static class ChangeStatusEvents {

        private ChangeStatusEvents() {
        }

        /**
         * Provides a pre-configured {@link TaskCreated} event instance.
         *
         * @return the {@link TaskCreated} instance
         */
        public static TaskCreated taskCreatedInstance() {
            return taskCreatedInstance(DESCRIPTION, TASK_PRIORITY);
        }

        /**
         * Provides the {@link TaskCreated} event by specified description and task priority.
         *
         * @param description specified task description
         * @param priority    specified task priority
         * @return the {@code TaskCreated} instance
         */
        public static TaskCreated taskCreatedInstance(String description,
                                                      TaskPriority priority) {
            final TaskDetails details = newTaskDetails(description, priority);
            final TaskCreated result = TaskCreated.newBuilder()
                                                  .setId(TASK_ID)
                                                  .setDetails(details)
                                                  .build();
            return result;
        }

        /**
         * Provides the {@link TaskCreated} event by specified task ID.
         *
         * @param id the ID of the created task
         * @return the {@code TaskCreated} instance
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
         * @return the {@code TaskDraftCreated} instance
         */
        public static TaskDraftCreated taskDraftCreatedInstance() {
            return taskDraftCreatedInstance(DESCRIPTION, TASK_PRIORITY, CREATION_TIME);
        }

        /**
         * Provides the {@link TaskDraftCreated} event by specified task description,
         * task priority and draft creation time.
         *
         * @param description  the description of the created draft
         * @param priority     the priority of the created draft
         * @param creationTime the time creation of the created draft
         * @return the {@code TaskDraftCreated} instance
         */
        public static TaskDraftCreated taskDraftCreatedInstance(String description,
                                                                TaskPriority priority,
                                                                Timestamp creationTime) {
            final TaskDetails details = newTaskDetails(description, priority);
            final TaskDraftCreated result = TaskDraftCreated.newBuilder()
                                                            .setId(TASK_ID)
                                                            .setDetails(details)
                                                            .setDraftCreationTime(creationTime)
                                                            .build();
            return result;
        }

        /**
         * Provides a pre-configured {@link TaskDraftFinalized} event instance.
         *
         * @return the {@code TaskDraftFinalized} instance
         */
        public static TaskDraftFinalized taskDraftFinalizedInstance() {
            final TaskDraftFinalized result = TaskDraftFinalized.newBuilder()
                                                                .setTaskId(TASK_ID)
                                                                .build();
            return result;
        }

        /**
         * Provides a pre-configured {@link TaskCompleted} event instance.
         *
         * @return the {@code TaskCompleted} instance
         */
        public static TaskCompleted taskCompletedInstance() {
            return taskCompletedInstance(TASK_ID);
        }

        /**
         * Provides pre-configured {@link TaskCompleted} event instance.
         *
         * @return the {@code TaskCompleted} instance
         */
        public static TaskCompleted taskCompletedInstance(TaskId id) {
            final TaskCompleted result = TaskCompleted.newBuilder()
                                                      .setTaskId(id)
                                                      .build();
            return result;
        }

        /**
         * Provides a pre-configured {@link TaskReopened} event instance.
         *
         * @return the {@code TaskReopened} instance
         */
        public static TaskReopened taskReopenedInstance() {
            return taskReopenedInstance(TASK_ID);
        }

        /**
         * Provides pre-configured {@link TaskReopened} event instance.
         *
         * @return the {@code TaskReopened} instance
         */
        public static TaskReopened taskReopenedInstance(TaskId id) {
            final TaskReopened result = TaskReopened.newBuilder()
                                                    .setTaskId(id)
                                                    .build();
            return result;
        }

        /**
         * Provides a pre-configured {@link TaskDeleted} event instance.
         *
         * @return the {@code TaskDeleted} instance
         */
        public static TaskDeleted taskDeletedInstance() {
            final TaskDeleted result = TaskDeleted.newBuilder()
                                                  .setTaskId(TASK_ID)
                                                  .build();
            return result;
        }

        /**
         * Provides a pre-configured {@link LabelledTaskRestored} event instance.
         *
         * @return the {@code LabelledTaskRestored} instance
         */
        public static LabelledTaskRestored labelledTaskRestoredInstance() {
            final LabelledTaskRestored result = LabelledTaskRestored.newBuilder()
                                                                    .setTaskId(TASK_ID)
                                                                    .setLabelId(LABEL_ID)
                                                                    .build();
            return result;
        }

        private static TaskDetails newTaskDetails(String description, TaskPriority priority) {
            final TaskDescription taskDescription = TaskDescription.newBuilder()
                                                                   .setValue(description)
                                                                   .build();
            return TaskDetails.newBuilder()
                              .setDescription(taskDescription)
                              .setPriority(priority)
                              .build();
        }
    }
}
