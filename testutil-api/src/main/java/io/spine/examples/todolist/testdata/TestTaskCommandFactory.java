/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
import io.spine.change.StringChange;
import io.spine.change.TimestampChange;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.ReopenTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;

import static io.spine.Identifier.newUuid;
import static io.spine.time.Time.getCurrentTime;

/**
 * A factory of the task commands for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestTaskCommandFactory {

    public static final TaskId TASK_ID = TaskId.newBuilder()
                                               .setValue(newUuid())
                                               .build();
    public static final String DESCRIPTION = "Task description.";
    public static final Timestamp DUE_DATE = getCurrentTime();
    public static final String UPDATED_LABEL_TITLE = "labelTitle";

    private TestTaskCommandFactory() {
    }

    /**
     * Provides a pre-configured {@link CreateBasicTask} instance.
     *
     * @return the {@code CreateBasicTask} instance
     */
    public static CreateBasicTask createTaskInstance() {
        final CreateBasicTask result = createTaskInstance(TASK_ID, DESCRIPTION);
        return result;
    }

    /**
     * Provides a pre-configured {@link CreateBasicTask} instance.
     *
     * @param id          an identifier of the created task
     * @param description a description of the updated task
     * @return the {@code CreateBasicTask} instance
     */
    public static CreateBasicTask createTaskInstance(TaskId id, String description) {
        final TaskDescription taskDescription = TaskDescription.newBuilder()
                                                               .setValue(description)
                                                               .build();
        final CreateBasicTask result = CreateBasicTask.newBuilder()
                                                      .setId(id)
                                                      .setDescription(taskDescription)
                                                      .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateTaskDescription} instance.
     *
     * @return the {@code UpdateTaskDescription} instance
     */
    public static UpdateTaskDescription updateTaskDescriptionInstance() {
        final UpdateTaskDescription result = updateTaskDescriptionInstance(TASK_ID,
                                                                           DESCRIPTION,
                                                                           DESCRIPTION);
        return result;
    }

    /**
     * Provides the {@link UpdateTaskDescription} instance by description and task ID specified.
     *
     * @param taskId an identifier of the updated task
     * @return the {@code UpdateTaskDescription} instance
     */
    public static UpdateTaskDescription updateTaskDescriptionInstance(TaskId taskId) {
        final UpdateTaskDescription result = updateTaskDescriptionInstance(taskId,
                                                                           DESCRIPTION,
                                                                           DESCRIPTION);
        return result;
    }

    /**
     * Provides the {@link UpdateTaskDescription} instance by description and task ID specified.
     *
     * @param id                  an identifier of the updated task
     * @param previousDescription the previous description of the task
     * @param newDescription      the description of the updated task
     * @return the {@code UpdateTaskDescription} instance
     */
    public static UpdateTaskDescription updateTaskDescriptionInstance(TaskId id,
                                                                      String previousDescription,
                                                                      String newDescription) {
        final StringChange change = StringChange.newBuilder()
                                                .setPreviousValue(previousDescription)
                                                .setNewValue(newDescription)
                                                .build();
        final UpdateTaskDescription result = UpdateTaskDescription.newBuilder()
                                                                  .setId(id)
                                                                  .setDescriptionChange(change)
                                                                  .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateTaskDueDate} command instance.
     *
     * @return the {@code UpdateTaskDueDate} instance
     */
    public static UpdateTaskDueDate updateTaskDueDateInstance() {
        final UpdateTaskDueDate result = updateTaskDueDateInstance(TASK_ID,
                                                                   Timestamp.getDefaultInstance(),
                                                                   DUE_DATE);
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateTaskDueDate} command instance.
     *
     * @return the {@code UpdateTaskDueDate} instance
     */
    public static UpdateTaskDueDate updateTaskDueDateInstance(TaskId taskId) {
        final UpdateTaskDueDate result = updateTaskDueDateInstance(taskId,
                                                                   Timestamp.getDefaultInstance(),
                                                                   DUE_DATE);
        return result;
    }

    /**
     * Provides the {@link UpdateTaskDueDate} instance with specified update due date and
     * {@link TaskId} fields.
     *
     * @param previousDueDate the previous due date of the task
     * @param updatedDueDate  the due date of the updated task
     * @return {@link UpdateTaskDueDate} instance
     */
    public static UpdateTaskDueDate updateTaskDueDateInstance(TaskId id,
                                                              Timestamp previousDueDate,
                                                              Timestamp updatedDueDate) {
        final TimestampChange dueDateChange = TimestampChange.newBuilder()
                                                             .setPreviousValue(previousDueDate)
                                                             .setNewValue(updatedDueDate)
                                                             .build();
        final UpdateTaskDueDate result = UpdateTaskDueDate.newBuilder()
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
    public static UpdateTaskPriority updateTaskPriorityInstance() {
        final UpdateTaskPriority result = updateTaskPriorityInstance(TASK_ID,
                                                                     TaskPriority.TP_UNDEFINED,
                                                                     TaskPriority.HIGH);
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateTaskPriority} command instance.
     *
     * @return the {@code UpdateTaskPriority} instance
     */
    public static UpdateTaskPriority updateTaskPriorityInstance(TaskId taskId) {
        final UpdateTaskPriority result = updateTaskPriorityInstance(taskId,
                                                                     TaskPriority.TP_UNDEFINED,
                                                                     TaskPriority.HIGH);
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateTaskPriority} command instance.
     *
     * @param previousPriority the previous task priority
     * @param newPriority      the priority of the updated task
     * @return the {@code UpdateTaskPriority} instance
     */
    public static UpdateTaskPriority updateTaskPriorityInstance(TaskId id,
                                                                TaskPriority previousPriority,
                                                                TaskPriority newPriority) {
        final PriorityChange taskPriorityChange = PriorityChange.newBuilder()
                                                                .setPreviousValue(previousPriority)
                                                                .setNewValue(newPriority)
                                                                .build();
        final UpdateTaskPriority result = UpdateTaskPriority.newBuilder()
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
        final CompleteTask result = CompleteTask.newBuilder()
                                                .setId(id)
                                                .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link ReopenTask} command instance.
     *
     * @return the {@code ReopenTask} instance
     */
    public static ReopenTask reopenTaskInstance() {
        final ReopenTask result = reopenTaskInstance(TASK_ID);
        return result;
    }

    /**
     * Provides a pre-configured {@link ReopenTask} command instance.
     *
     * @return the {@code ReopenTask} instance
     */
    public static ReopenTask reopenTaskInstance(TaskId id) {
        final ReopenTask result = ReopenTask.newBuilder()
                                            .setId(id)
                                            .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link DeleteTask} command instance.
     *
     * @return the {@code DeleteTask} instance
     */
    public static DeleteTask deleteTaskInstance() {
        final DeleteTask result = deleteTaskInstance(TASK_ID);
        return result;
    }

    /**
     * Provides a pre-configured {@link DeleteTask} command instance.
     *
     * @return the {@code DeleteTask} instance
     */
    public static DeleteTask deleteTaskInstance(TaskId id) {
        final DeleteTask result = DeleteTask.newBuilder()
                                            .setId(id)
                                            .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link RestoreDeletedTask} command instance.
     *
     * @return the {@code RestoreDeletedTask} instance
     */
    public static RestoreDeletedTask restoreDeletedTaskInstance() {
        final RestoreDeletedTask result = restoreDeletedTaskInstance(TASK_ID);
        return result;
    }

    /**
     * Provides a pre-configured {@link RestoreDeletedTask} command instance.
     *
     * @return the {@code RestoreDeletedTask} instance
     */
    public static RestoreDeletedTask restoreDeletedTaskInstance(TaskId id) {
        final RestoreDeletedTask result = RestoreDeletedTask.newBuilder()
                                                            .setId(id)
                                                            .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link FinalizeDraft} command instance.
     *
     * @return the {@code FinalizeDraft} instance
     */
    public static FinalizeDraft finalizeDraftInstance() {
        final FinalizeDraft result = finalizeDraftInstance(TASK_ID);
        return result;
    }

    /**
     * Provides the {@link FinalizeDraft} command instance according to the passed task ID.
     *
     * @return the {@code FinalizeDraft} instance
     */
    public static FinalizeDraft finalizeDraftInstance(TaskId id) {
        final FinalizeDraft result = FinalizeDraft.newBuilder()
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
        final CreateDraft result = createDraftInstance(TASK_ID);
        return result;
    }

    /**
     * Provides a pre-configured {@link CreateDraft} command instance.
     *
     * @return the {@code CreateDraft} instance
     */
    public static CreateDraft createDraftInstance(TaskId taskId) {
        final CreateDraft result = CreateDraft.newBuilder()
                                              .setId(taskId)
                                              .build();
        return result;
    }
}
