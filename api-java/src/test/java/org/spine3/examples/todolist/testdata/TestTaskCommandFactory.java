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
import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
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

    private static final String ID = newUuid();
    private static final String DESCRIPTION = "Create command description.";
    private static final Timestamp DUE_DATE = Timestamps.getCurrentTime();

    /**
     * Prevent instantiation.
     */
    private TestTaskCommandFactory() {
        throw new UnsupportedOperationException("Cannot be instantiated.");
    }

    /**
     * Provides {@link CreateBasicTask} instance.
     *
     * @return {@link CreateBasicTask} instance
     */
    public static CreateBasicTask createTaskInstance() {
        return CreateBasicTask.newBuilder()
                              .setDescription(DESCRIPTION)
                              .build();
    }

    /**
     * Provides {@link UpdateTaskDescription} instance.
     *
     * @return {@link UpdateTaskDescription} instance
     */
    public static UpdateTaskDescription updateTaskDescriptionInstance() {
        return updateTaskDescriptionInstance(DESCRIPTION);
    }

    /**
     * Provides {@link UpdateTaskDescription} instance by specified description.
     *
     * @param description String value into updated description field
     * @return {@link UpdateTaskDescription} instance
     */
    public static UpdateTaskDescription updateTaskDescriptionInstance(String description) {
        final UpdateTaskDescription result = UpdateTaskDescription.newBuilder()
                                                                 .setUpdatedDescription(description)
                                                                 .build();
        return result;
    }

    /**
     * Provides {@link UpdateTaskDescription} instance by specified due date {@code DUE_DATE}.
     *
     * @return {@link UpdateTaskDueDate} instance
     */
    public static UpdateTaskDueDate updateTaskDueDateInstance() {
        return updateTaskDueDateInstance(DUE_DATE);
    }

    /**
     * Provides {@link UpdateTaskDueDate} instance with specified update due date field.
     *
     * @param updatedDueDate value into updated due datefield, present in seconds
     * @return {@link UpdateTaskDueDate} instance
     */
    public static UpdateTaskDueDate updateTaskDueDateInstance(Timestamp updatedDueDate) {
        final UpdateTaskDueDate result = UpdateTaskDueDate.newBuilder()
                                                         .setUpdatedDueDate(updatedDueDate)
                                                         .build();
        return result;
    }

    /**
     * Provides {@link UpdateTaskPriority} instance by specified priority {@code TaskPriority.HIGH}.
     *
     * @return {@link UpdateTaskPriority} instance
     */
    public static UpdateTaskPriority updateTaskPriorityInstance() {
        return updateTaskPriorityInstance(TaskPriority.HIGH);
    }

    /**
     * Provides {@link UpdateTaskPriority} instance by specified priority.
     *
     * @param priority {@link TaskPriority} enum value
     * @return {@link UpdateTaskPriority} instance
     */
    public static UpdateTaskPriority updateTaskPriorityInstance(TaskPriority priority) {
        final UpdateTaskPriority result = UpdateTaskPriority.newBuilder()
                                                           .setUpdatedPriority(priority)
                                                           .build();
        return result;
    }

    /**
     * Provides default {@link CompleteTask} command instance.
     *
     * @return {@link CompleteTask} instance
     */
    public static CompleteTask completeTaskInstance() {
        return CompleteTask.getDefaultInstance();
    }

    /**
     * Provides default {@link ReopenTask} command instance.
     *
     * @return {@link ReopenTask} instance
     */
    public static ReopenTask reopenTaskInstance() {
        return ReopenTask.getDefaultInstance();
    }

    /**
     * Provides {@link DeleteTask} command instance by specified {@link TaskLabelId}.
     * TaskLabelId constructed with {@code ID}.
     *
     * @return {@link DeleteTask} instance
     */
    public static DeleteTask deleteTaskInstance() {
        final DeleteTask result = DeleteTask.newBuilder()
                                           .setId(TaskId.newBuilder()
                                                        .setValue(ID))
                                           .build();
        return result;
    }

    /**
     * Provides default {@link RestoreDeletedTask} command instance.
     *
     * @return {@link RestoreDeletedTask} instance
     */
    public static RestoreDeletedTask restoreDeletedTaskInstance() {
        return RestoreDeletedTask.getDefaultInstance();
    }

    /**
     * Provides default {@link FinalizeDraft} command instance.
     *
     * @return {@link FinalizeDraft} instance
     */
    public static FinalizeDraft finalizeDraftInstance() {
        return FinalizeDraft.getDefaultInstance();
    }

    /**
     * Provides default {@link CreateDraft} command instance.
     *
     * @return {@link CreateDraft} instance
     */
    public static CreateDraft createDraftInstance() {
        return CreateDraft.getDefaultInstance();
    }

    /**
     * Provides {@link AssignLabelToTask} command instance by specified {@link TaskLabelId}
     * TaskLabelId constructed with {@code ID}.
     *
     * @return {@link AssignLabelToTask} instance
     */
    public static AssignLabelToTask assignLabelToTaskInstance() {
        final AssignLabelToTask result = AssignLabelToTask.newBuilder()
                                                         .setLabelId(TaskLabelId.newBuilder()
                                                                                .setValue(ID))
                                                         .build();
        return result;
    }

    /**
     * Provides {@link RemoveLabelFromTask} command instance by specified {@link TaskLabelId}.
     * TaskLabelId constructed with {@code ID}.
     *
     * @return {@link RemoveLabelFromTask} instance
     */
    public static RemoveLabelFromTask removeLabelFromTaskInstance() {
        final RemoveLabelFromTask result = RemoveLabelFromTask.newBuilder()
                                                             .setLabelId(TaskLabelId.newBuilder()
                                                                                    .setValue(ID))
                                                             .build();
        return result;
    }
}
