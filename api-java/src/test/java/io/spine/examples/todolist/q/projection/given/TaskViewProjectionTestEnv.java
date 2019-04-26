/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.q.projection.given;

import com.google.protobuf.Timestamp;
import io.spine.base.Identifier;
import io.spine.change.TimestampChange;
import io.spine.examples.todolist.DescriptionChange;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskCreated;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;
import io.spine.protobuf.Timestamps2;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Environment for testing the {@link io.spine.examples.todolist.q.projection.TaskViewProjection}.
 *
 * <p>This environment works with 1 task at a time, the ID of which is supplied via its constructor.
 *
 * <p>All of the methods that produce task related events use the above mentioned {@code TaskId}.
 */
public final class TaskViewProjectionTestEnv {

    private final TaskId taskId;
    private static final String DEFAULT_DESCRIPTION = "Wash my car";

    public TaskViewProjectionTestEnv(TaskId taskId) {
        this.taskId = taskId;
    }

    /** Obtains a task ID with a value of a new random UUID. */
    public static TaskId randomTaskId() {
        String value = Identifier.newUuid();
        TaskId result = TaskId
                .newBuilder()
                .setValue(value)
                .build();
        return result;
    }

    /** Obtains a {@code Timestamp} that corresponds to a day after tomorrow. */
    public static Timestamp theDayAfterTomorrow() {
        Instant today = Instant.now();
        Instant theDayAfterTomorrow = today.plus(2, DAYS);
        Timestamp result = Timestamps2.fromInstant(theDayAfterTomorrow);
        return result;
    }

    /** Obtains an event that signifies that a new task got created. */
    public TaskCreated taskCreated() {
        return taskCreated(DEFAULT_DESCRIPTION);
    }

    /** Obtains an event that signifies that a task got its description updated to the specified one. */
    public TaskDescriptionUpdated descriptionUpdated(String newDescription) {
        DescriptionChange change = DescriptionChange
                .newBuilder()
                .setPreviousValue(description(DEFAULT_DESCRIPTION))
                .setNewValue(description(newDescription))
                .build();
        TaskDescriptionUpdated result = TaskDescriptionUpdated
                .newBuilder()
                .setTaskId(taskId)
                .setDescriptionChange(change)
                .build();
        return result;
    }

    /** Obtains an event that signifies that a completed task got reopened. */
    public TaskReopened taskReopened() {
        TaskReopened result = TaskReopened
                .newBuilder()
                .setTaskId(taskId)
                .build();
        return result;
    }

    /** Obtains an event that signifies that a task got deleted. */
    public TaskDeleted taskDeleted() {
        TaskDeleted deleted = TaskDeleted
                .newBuilder()
                .setTaskId(taskId)
                .build();
        return deleted;
    }

    /** Obtains an event that signifies that a task got completed. */
    public TaskCompleted taskCompleted() {
        TaskCompleted taskCompleted = TaskCompleted
                .newBuilder()
                .setTaskId(taskId)
                .build();
        return taskCompleted;
    }

    /** Obtains an event that signifies that a task got its priority updated to the specified one. */
    public TaskPriorityUpdated priorityUpdated(TaskPriority newPriority) {
        PriorityChange priorityChange = PriorityChange
                .newBuilder()
                .setPreviousValue(TaskPriority.NORMAL)
                .setNewValue(newPriority)
                .build();
        TaskPriorityUpdated result = TaskPriorityUpdated
                .newBuilder()
                .setTaskId(taskId)
                .setPriorityChange(priorityChange)
                .build();
        return result;
    }

    /** Obtains an event that signifies that a task got its due date updated to the specified one. */
    public TaskDueDateUpdated dueDateUpdated(Timestamp newDate) {
        TimestampChange timestampChange = TimestampChange
                .newBuilder()
                .setPreviousValue(tomorrow())
                .setNewValue(newDate)
                .build();
        TaskDueDateUpdated result = TaskDueDateUpdated
                .newBuilder()
                .setTaskId(taskId)
                .setDueDateChange(timestampChange)
                .build();
        return result;
    }

    /** Obtains an event that signifies that a task with {@code Draft} status got created. */
    public TaskDraftCreated draftCreated() {
        TaskDetails details = details(DEFAULT_DESCRIPTION);
        Instant today = Instant.now();
        Timestamp creationTime = Timestamps2.fromInstant(today);
        TaskDraftCreated result = TaskDraftCreated
                .newBuilder()
                .setId(taskId)
                .setDetails(details)
                .setDraftCreationTime(creationTime)
                .build();
        return result;
    }

    private TaskCreated taskCreated(String description) {
        TaskDetails details = details(description);
        TaskCreated result = TaskCreated
                .newBuilder()
                .setId(taskId)
                .setDetails(details)
                .build();
        return result;
    }

    private static TaskDetails details(String description) {
        return TaskDetails
                .newBuilder()
                .setDescription(description(description))
                .setDueDate(tomorrow())
                .setPriority(TaskPriority.NORMAL)
                .build();
    }

    private static Timestamp tomorrow() {
        Instant result = Instant.now();
        result.plus(1, DAYS);
        return Timestamps2.fromInstant(result);
    }

    private static TaskDescription description(String value) {
        TaskDescription result = TaskDescription
                .newBuilder()
                .setValue(value)
                .build();
        return result;
    }
}
