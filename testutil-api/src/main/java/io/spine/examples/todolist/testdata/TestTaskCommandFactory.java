/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.protobuf.Timestamp;
import io.spine.change.TimestampChange;
import io.spine.examples.todolist.tasks.DescriptionChange;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.PriorityChange;
import io.spine.examples.todolist.tasks.TaskDescription;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.TaskPriority;
import io.spine.examples.todolist.tasks.command.CompleteTask;
import io.spine.examples.todolist.tasks.command.CreateBasicTask;
import io.spine.examples.todolist.tasks.command.CreateDraft;
import io.spine.examples.todolist.tasks.command.DeleteTask;
import io.spine.examples.todolist.tasks.command.FinalizeDraft;
import io.spine.examples.todolist.tasks.command.ReopenTask;
import io.spine.examples.todolist.tasks.command.RestoreDeletedTask;
import io.spine.examples.todolist.tasks.command.UpdateTaskDescription;
import io.spine.examples.todolist.tasks.command.UpdateTaskDueDate;
import io.spine.examples.todolist.tasks.command.UpdateTaskPriority;

import static io.spine.base.Time.currentTime;

/**
 * A factory of the task commands for the test needs.
 */
@SuppressWarnings("OverlyCoupledClass")
// It's necessary to provide all task-related commands.
public class TestTaskCommandFactory {

    public static final TaskId TASK_ID = TaskId.generate();
    public static final LabelId LABEL_ID = LabelId.generate();
    public static final String DESCRIPTION = "Task description.";
    private static final Timestamp DUE_DATE = currentTime();

    private TestTaskCommandFactory() {
    }

    /**
     * Provides a pre-configured {@link CreateBasicTask} instance.
     *
     * @return the {@code CreateBasicTask} instance
     */
    public static CreateBasicTask createTaskInstance() {
        CreateBasicTask result = createTaskInstance(TASK_ID, DESCRIPTION);
        return result;
    }

    public static CreateBasicTask createTaskInstance(TaskId taskId) {
        CreateBasicTask result = createTaskInstance(taskId, DESCRIPTION);
        return result;
    }

    /**
     * Provides a pre-configured {@link CreateBasicTask} instance.
     *
     * @param id
     *         an identifier of the created task
     * @param description
     *         a description of the updated task
     * @return the {@code CreateBasicTask} instance
     */
    public static CreateBasicTask createTaskInstance(TaskId id, String description) {
        TaskDescription taskDescription = TaskDescription
                .newBuilder()
                .setValue(description)
                .vBuild();
        CreateBasicTask result = CreateBasicTask
                .newBuilder()
                .setId(id)
                .setDescription(taskDescription)
                .vBuild();
        return result;
    }

    /**
     * Provides the {@link UpdateTaskDescription} instance by description and task ID specified.
     *
     * @param id
     *         an identifier of the updated task
     * @param previousDescription
     *         the previous description of the task
     * @param newDescription
     *         the description of the updated task
     * @return the {@code UpdateTaskDescription} instance
     */
    public static UpdateTaskDescription updateTaskDescriptionInstance(TaskId id,
                                                                      String previousDescription,
                                                                      String newDescription) {
        TaskDescription newValue = TaskDescription
                .newBuilder()
                .setValue(newDescription)
                .vBuild();
        DescriptionChange.Builder change = DescriptionChange
                .newBuilder()
                .setNewValue(newValue);
        if (!previousDescription.isEmpty()) {
            TaskDescription previousValue = TaskDescription
                    .newBuilder()
                    .setValue(previousDescription)
                    .vBuild();
            change.setPreviousValue(previousValue);
        }
        UpdateTaskDescription result = UpdateTaskDescription
                .newBuilder()
                .setId(id)
                .setDescriptionChange(change.buildPartial())
                .vBuild();
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateTaskDueDate} command instance.
     *
     * @return the {@code UpdateTaskDueDate} instance
     */
    public static UpdateTaskDueDate updateTaskDueDateInstance(TaskId taskId) {
        UpdateTaskDueDate result = updateTaskDueDateInstance(taskId,
                                                             Timestamp.getDefaultInstance(),
                                                             DUE_DATE);
        return result;
    }

    /**
     * Provides the {@link UpdateTaskDueDate} instance with specified update due date and
     * {@link TaskId} fields.
     *
     * @param previousDueDate
     *         the previous due date of the task
     * @param updatedDueDate
     *         the due date of the updated task
     * @return {@link UpdateTaskDueDate} instance
     */
    public static UpdateTaskDueDate updateTaskDueDateInstance(TaskId id,
                                                              Timestamp previousDueDate,
                                                              Timestamp updatedDueDate) {
        TimestampChange dueDateChange = TimestampChange
                .newBuilder()
                .setPreviousValue(previousDueDate)
                .setNewValue(updatedDueDate)
                .vBuild();
        UpdateTaskDueDate result = UpdateTaskDueDate
                .newBuilder()
                .setId(id)
                .setDueDateChange(dueDateChange)
                .vBuild();
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateTaskPriority} command instance.
     *
     * @return the {@code UpdateTaskPriority} instance
     */
    public static UpdateTaskPriority updateTaskPriorityInstance(TaskId taskId) {
        UpdateTaskPriority result = updateTaskPriorityInstance(taskId,
                                                               TaskPriority.NORMAL,
                                                               TaskPriority.HIGH);
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateTaskPriority} command instance.
     *
     * @param previousPriority
     *         the previous task priority
     * @param newPriority
     *         the priority of the updated task
     * @return the {@code UpdateTaskPriority} instance
     */
    public static UpdateTaskPriority updateTaskPriorityInstance(TaskId id,
                                                                TaskPriority previousPriority,
                                                                TaskPriority newPriority) {
        PriorityChange taskPriorityChange = PriorityChange
                .newBuilder()
                .setPreviousValue(previousPriority)
                .setNewValue(newPriority)
                .buildPartial();
        UpdateTaskPriority result = UpdateTaskPriority
                .newBuilder()
                .setId(id)
                .setPriorityChange(taskPriorityChange)
                .vBuild();
        return result;
    }

    /**
     * Provides a pre-configured {@link CompleteTask} command instance.
     *
     * @return the {@code CompleteTask} instance
     */
    public static CompleteTask completeTaskInstance(TaskId id) {
        CompleteTask result = CompleteTask
                .newBuilder()
                .setId(id)
                .vBuild();
        return result;
    }

    /**
     * Provides a pre-configured {@link ReopenTask} command instance.
     *
     * @return the {@code ReopenTask} instance
     */
    public static ReopenTask reopenTaskInstance(TaskId id) {
        ReopenTask result = ReopenTask
                .newBuilder()
                .setId(id)
                .vBuild();
        return result;
    }

    /**
     * Provides a pre-configured {@link DeleteTask} command instance.
     *
     * @return the {@code DeleteTask} instance
     */
    public static DeleteTask deleteTaskInstance(TaskId id) {
        DeleteTask result = DeleteTask
                .newBuilder()
                .setId(id)
                .vBuild();
        return result;
    }

    /**
     * Provides a pre-configured {@link RestoreDeletedTask} command instance.
     *
     * @return the {@code RestoreDeletedTask} instance
     */
    public static RestoreDeletedTask restoreDeletedTaskInstance(TaskId id) {
        RestoreDeletedTask result = RestoreDeletedTask
                .newBuilder()
                .setId(id)
                .vBuild();
        return result;
    }

    /**
     * Provides the {@link FinalizeDraft} command instance according to the passed task ID.
     *
     * @return the {@code FinalizeDraft} instance
     */
    public static FinalizeDraft finalizeDraftInstance(TaskId id) {
        FinalizeDraft result = FinalizeDraft
                .newBuilder()
                .setId(id)
                .vBuild();
        return result;
    }

    /**
     * Provides a pre-configured {@link CreateDraft} command instance.
     *
     * @return the {@code CreateDraft} instance
     */
    public static CreateDraft createDraftInstance() {
        CreateDraft result = createDraftInstance(TASK_ID);
        return result;
    }

    /**
     * Provides a pre-configured {@link CreateDraft} command instance.
     *
     * @return the {@code CreateDraft} instance
     */
    public static CreateDraft createDraftInstance(TaskId taskId) {
        CreateDraft result = CreateDraft
                .newBuilder()
                .setId(taskId)
                .vBuild();
        return result;
    }
}
