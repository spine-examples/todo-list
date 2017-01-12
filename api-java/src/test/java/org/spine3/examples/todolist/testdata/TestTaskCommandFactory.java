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
import org.spine3.change.StringChange;
import org.spine3.change.TimestampChange;
import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsChange;
import org.spine3.examples.todolist.PriorityChange;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.UpdateLabelDetails;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;
import org.spine3.protobuf.Timestamps;

import static org.spine3.base.Identifiers.newUuid;

/**
 * A factory of the task commands for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestTaskCommandFactory {

    public static final TaskId TASK_ID = TaskId.newBuilder()
                                               .setValue(newUuid())
                                               .build();
    public static final TaskLabelId LABEL_ID = TaskLabelId.newBuilder()
                                                          .setValue(newUuid())
                                                          .build();
    public static final String DESCRIPTION = "Create command description.";
    public static final Timestamp DUE_DATE = Timestamps.getCurrentTime();
    public static final String UPDATED_LABEL_TITLE = "labelTitle";

    private TestTaskCommandFactory() {
    }

    /**
     * Provides {@link CreateBasicTask} instance.
     *
     * @return {@link CreateBasicTask} instance
     */
    public static CreateBasicTask createTaskInstance() {
        final CreateBasicTask result = createTaskInstance(TASK_ID, DESCRIPTION);
        return result;
    }

    public static CreateBasicTask createTaskInstance(TaskId id, String description) {
        final CreateBasicTask result = CreateBasicTask.newBuilder()
                                                      .setId(id)
                                                      .setDescription(description)
                                                      .build();
        return result;
    }

    public static CreateBasicTask createUniqueTask() {
        final TaskId.Builder id = TaskId.newBuilder()
                                        .setValue(newUuid());
        final CreateBasicTask result = CreateBasicTask.newBuilder()
                                                      .setId(id)
                                                      .setDescription(DESCRIPTION)
                                                      .build();
        return result;
    }

    /**
     * Provides {@link UpdateTaskDescription} instance.
     *
     * @return {@link UpdateTaskDescription} instance
     */
    public static UpdateTaskDescription updateTaskDescriptionInstance() {
        return updateTaskDescriptionInstance(TASK_ID, DESCRIPTION, DESCRIPTION);
    }

    /**
     * Provides {@link UpdateTaskDescription} instance by description and task id specified.
     *
     * @param description the description of the updated task
     * @return {@link UpdateTaskDescription} instance
     */
    public static UpdateTaskDescription updateTaskDescriptionInstance(TaskId id, String previousDescription, String newDescription) {
        final StringChange descriptionChange = StringChange.newBuilder()
                                                           .setPreviousValue(previousDescription)
                                                           .setNewValue(newDescription)
                                                           .build();
        final UpdateTaskDescription result = UpdateTaskDescription.newBuilder()
                                                                  .setId(id)
                                                                  .setDescriptionChange(descriptionChange)
                                                                  .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateTaskDueDate} command instance.
     *
     * @return {@link UpdateTaskDueDate} instance
     */
    public static UpdateTaskDueDate updateTaskDueDateInstance() {
        return updateTaskDueDateInstance(TASK_ID, DUE_DATE);
    }

    /**
     * Provides {@link UpdateTaskDueDate} instance with specified update due date and {@link TaskId} fields.
     *
     * @param updatedDueDate the due date of the updated task
     * @return {@link UpdateTaskDueDate} instance
     */
    public static UpdateTaskDueDate updateTaskDueDateInstance(TaskId id, Timestamp updatedDueDate) {
        final TimestampChange dueDateChange = TimestampChange.newBuilder()
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
     * @return {@link UpdateTaskPriority} instance
     */
    public static UpdateTaskPriority updateTaskPriorityInstance() {
        return updateTaskPriorityInstance(TASK_ID, TaskPriority.HIGH);
    }

    /**
     * Provides a pre-configured {@link UpdateTaskPriority} command instance.
     *
     * @param priority the priority of the updated task
     * @return {@link UpdateTaskPriority} instance
     */
    public static UpdateTaskPriority updateTaskPriorityInstance(TaskId id, TaskPriority priority) {
        final PriorityChange taskPriorityChange = PriorityChange.newBuilder()
                                                                .setNewValue(priority)
                                                                .build();
        final UpdateTaskPriority result = UpdateTaskPriority.newBuilder()
                                                            .setId(id)
                                                            .setPriorityChange(taskPriorityChange)
                                                            .build();
        return result;
    }

    /**
     * Provides default {@link CompleteTask} command instance.
     *
     * @return {@link CompleteTask} instance
     */
    public static CompleteTask completeTaskInstance() {
        return completeTaskInstance(TASK_ID);
    }

    /**
     * Provides a pre-configured {@link CompleteTask} command instance.
     *
     * @return {@link CompleteTask} instance
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
     * @return {@link ReopenTask} instance
     */
    public static ReopenTask reopenTaskInstance() {
        final ReopenTask result = ReopenTask.newBuilder()
                                            .setId(TASK_ID)
                                            .build();
        return reopenTaskInstance(TASK_ID);
    }

    /**
     * Provides a pre-configured {@link ReopenTask} command instance.
     *
     * @return {@link ReopenTask} instance
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
     * @return {@link DeleteTask} instance
     */
    public static DeleteTask deleteTaskInstance() {
        return deleteTaskInstance(TASK_ID);
    }

    /**
     * Provides a pre-configured {@link DeleteTask} command instance.
     *
     * @return {@link DeleteTask} instance
     */
    public static DeleteTask deleteTaskInstance(TaskId id) {
        final DeleteTask result = DeleteTask.newBuilder()
                                            .setId(id)
                                            .build();
        return result;
    }

    /**
     * Provides pre-configured {@link RestoreDeletedTask} command instance.
     *
     * @return {@link RestoreDeletedTask} instance
     */
    public static RestoreDeletedTask restoreDeletedTaskInstance() {
        final RestoreDeletedTask result = RestoreDeletedTask.newBuilder()
                                                            .setId(TASK_ID)
                                                            .build();
        return restoreDeletedTaskInstance(TASK_ID);
    }

    /**
     * Provides a pre-configured {@link RestoreDeletedTask} command instance.
     *
     * @return {@link RestoreDeletedTask} instance
     */
    public static RestoreDeletedTask restoreDeletedTaskInstance(TaskId id) {
        final RestoreDeletedTask result = RestoreDeletedTask.newBuilder()
                                                            .setId(id)
                                                            .build();
        return result;
    }

    /**
     * Provides pre-configured {@link FinalizeDraft} command instance.
     *
     * @return {@link FinalizeDraft} instance
     */
    public static FinalizeDraft finalizeDraftInstance() {
        final FinalizeDraft result = FinalizeDraft.newBuilder()
                                                  .setId(TASK_ID)
                                                  .build();
        return finalizeDraftInstance(TASK_ID);
    }

    /**
     * Provides default {@link FinalizeDraft} command instance.
     *
     * @return {@link FinalizeDraft} instance
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
     * @return {@link CreateDraft} instance
     */
    public static CreateDraft createDraftInstance() {
        final CreateDraft result = CreateDraft.newBuilder()
                                              .setId(TASK_ID)
                                              .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link AssignLabelToTask} command instance.
     *
     * @return {@link AssignLabelToTask} instance
     */
    public static AssignLabelToTask assignLabelToTaskInstance(TaskId taskId, TaskLabelId labelId) {
        final AssignLabelToTask result = AssignLabelToTask.newBuilder()
                                                          .setId(taskId)
                                                          .setLabelId(labelId)
                                                          .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link AssignLabelToTask} command instance.
     *
     * @return {@link AssignLabelToTask} instance
     */
    public static AssignLabelToTask assignLabelToTaskInstance() {
        return assignLabelToTaskInstance(TASK_ID, LABEL_ID);
    }

    /**
     * Provides a pre-configured {@link RemoveLabelFromTask} command instance.
     *
     * @return {@link RemoveLabelFromTask} instance
     */
    public static RemoveLabelFromTask removeLabelFromTaskInstance() {
        return removeLabelFromTaskInstance(TASK_ID, LABEL_ID);
    }

    /**
     * Provides a pre-configured {@link RemoveLabelFromTask} command instance.
     *
     * @return {@link RemoveLabelFromTask} instance
     */
    public static RemoveLabelFromTask removeLabelFromTaskInstance(TaskId taskId, TaskLabelId labelId) {
        final RemoveLabelFromTask result = RemoveLabelFromTask.newBuilder()
                                                              .setId(taskId)
                                                              .setLabelId(labelId)
                                                              .build();
        return result;
    }
}
