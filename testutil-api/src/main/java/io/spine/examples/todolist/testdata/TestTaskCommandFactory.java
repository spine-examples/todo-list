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

package io.spine.examples.todolist.testdata;

import com.google.protobuf.Timestamp;
import io.spine.change.TimestampChange;
import io.spine.change.TimestampChangeVBuilder;
import io.spine.examples.todolist.DescriptionChangeVBuilder;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.LabelIdVBuilder;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.PriorityChangeVBuilder;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskDescriptionVBuilder;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskIdVBuilder;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CompleteTaskVBuilder;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.CreateDraftVBuilder;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.DeleteTaskVBuilder;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraftVBuilder;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.commands.ReopenTaskVBuilder;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTaskVBuilder;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDescriptionVBuilder;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDateVBuilder;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.c.commands.UpdateTaskPriorityVBuilder;

import static io.spine.base.Identifier.newUuid;
import static io.spine.base.Time.currentTime;

/**
 * A factory of the task commands for the test needs.
 */
@SuppressWarnings({"ClassWithTooManyMethods", "OverlyCoupledClass"})
// It's necessary to provide all task-related commands.
public class TestTaskCommandFactory {

    public static final TaskId TASK_ID = TaskIdVBuilder
            .newBuilder()
            .setValue(newUuid())
            .build();
    public static final LabelId LABEL_ID = LabelIdVBuilder
            .newBuilder()
            .setValue(newUuid())
            .build();
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
        TaskDescription taskDescription = TaskDescriptionVBuilder
                .newBuilder()
                .setValue(description)
                .build();
        CreateBasicTask result = CreateBasicTaskVBuilder
                .newBuilder()
                .setId(id)
                .setDescription(taskDescription)
                .build();
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
        TaskDescription newValue = TaskDescriptionVBuilder
                .newBuilder()
                .setValue(newDescription)
                .build();
        DescriptionChangeVBuilder change = DescriptionChangeVBuilder
                .newBuilder()
                .setNewValue(newValue);
        if (!previousDescription.isEmpty()) {
            TaskDescription previousValue = TaskDescriptionVBuilder
                    .newBuilder()
                    .setValue(previousDescription)
                    .build();
            change.setPreviousValue(previousValue);
        }
        UpdateTaskDescription result = UpdateTaskDescriptionVBuilder
                .newBuilder()
                .setId(id)
                .setDescriptionChange(change.build())
                .build();
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
        TimestampChange dueDateChange = TimestampChangeVBuilder
                .newBuilder()
                .setPreviousValue(previousDueDate)
                .setNewValue(updatedDueDate)
                .build();
        UpdateTaskDueDate result = UpdateTaskDueDateVBuilder
                .newBuilder()
                .setId(id)
                .setDueDateChange(dueDateChange)
                .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateTaskPriority} command instance.
     *
     * @return the {@code UpdateTaskPriority} instance
     */
    public static UpdateTaskPriority updateTaskPriorityInstance(TaskId taskId) {
        UpdateTaskPriority result = updateTaskPriorityInstance(taskId,
                                                               TaskPriority.TP_UNDEFINED,
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
        PriorityChange taskPriorityChange = PriorityChangeVBuilder
                .newBuilder()
                .setPreviousValue(previousPriority)
                .setNewValue(newPriority)
                .build();
        UpdateTaskPriority result = UpdateTaskPriorityVBuilder
                .newBuilder()
                .setId(id)
                .setPriorityChange(taskPriorityChange)
                .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link CompleteTask} command instance.
     *
     * @return the {@code CompleteTask} instance
     */
    public static CompleteTask completeTaskInstance() {
        return completeTaskInstance(TASK_ID);
    }

    /**
     * Provides a pre-configured {@link CompleteTask} command instance.
     *
     * @return the {@code CompleteTask} instance
     */
    public static CompleteTask completeTaskInstance(TaskId id) {
        CompleteTask result = CompleteTaskVBuilder
                .newBuilder()
                .setId(id)
                .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link ReopenTask} command instance.
     *
     * @return the {@code ReopenTask} instance
     */
    public static ReopenTask reopenTaskInstance(TaskId id) {
        ReopenTask result = ReopenTaskVBuilder
                .newBuilder()
                .setId(id)
                .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link DeleteTask} command instance.
     *
     * @return the {@code DeleteTask} instance
     */
    public static DeleteTask deleteTaskInstance(TaskId id) {
        DeleteTask result = DeleteTaskVBuilder
                .newBuilder()
                .setId(id)
                .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link RestoreDeletedTask} command instance.
     *
     * @return the {@code RestoreDeletedTask} instance
     */
    public static RestoreDeletedTask restoreDeletedTaskInstance(TaskId id) {
        RestoreDeletedTask result = RestoreDeletedTaskVBuilder
                .newBuilder()
                .setId(id)
                .build();
        return result;
    }

    /**
     * Provides the {@link FinalizeDraft} command instance according to the passed task ID.
     *
     * @return the {@code FinalizeDraft} instance
     */
    public static FinalizeDraft finalizeDraftInstance(TaskId id) {
        FinalizeDraft result = FinalizeDraftVBuilder
                .newBuilder()
                .setId(id)
                .build();
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
        CreateDraft result = CreateDraftVBuilder
                .newBuilder()
                .setId(taskId)
                .build();
        return result;
    }
}
