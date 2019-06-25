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

package io.spine.test.integration.given;

import com.google.protobuf.Timestamp;
import io.spine.change.TimestampChange;
import io.spine.examples.todolist.DescriptionChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.CancelTaskCreation;
import io.spine.examples.todolist.c.commands.CompleteTaskCreation;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.SkipLabels;
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.c.commands.UpdateTaskDetails;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.TaskPriority.TP_UNDEFINED;

/**
 * The task creation wizard test environment.
 *
 * <p>Provides values and routines for sending commands to the procman and verifying the results.
 */
public class TaskCreationWizardTestEnv {

    /** Prevents instantiation of this utility class. */
    private TaskCreationWizardTestEnv() {
    }

    // Command sending methods
    // -----------------------

    public static StartTaskCreation createDraft(TaskCreationId pid, TaskId taskId) {
        StartTaskCreation startCreation = StartTaskCreation
                .newBuilder()
                .setId(pid)
                .setTaskId(taskId)
                .build();
        return startCreation;
    }

    public static UpdateTaskDetails setDetails(TaskCreationId pid, String description) {
        return setDetails(pid, description, TP_UNDEFINED, Timestamp.getDefaultInstance());
    }

    public static UpdateTaskDetails setDetails(TaskCreationId pid, String description,
                                               TaskPriority priority, Timestamp dueDate) {
        TaskDescription descValue = TaskDescription
                .newBuilder()
                .setValue(description)
                .build();
        DescriptionChange descriptionChange = DescriptionChange
                .newBuilder()
                .setNewValue(descValue)
                .build();
        PriorityChange priorityChange = PriorityChange
                .newBuilder()
                .setNewValue(priority)
                .build();
        TimestampChange dueDateChange = TimestampChange
                .newBuilder()
                .setNewValue(dueDate)
                .build();
        UpdateTaskDetails setDescription = UpdateTaskDetails
                .newBuilder()
                .setId(pid)
                .setDescriptionChange(descriptionChange)
                .setPriorityChange(priorityChange)
                .setDueDateChange(dueDateChange)
                .build();
        return setDescription;
    }

    public static AddLabels addLabel(TaskCreationId pid, LabelId labelId) {
        AddLabels addLabels = AddLabels
                .newBuilder()
                .setId(pid)
                .addExistingLabels(labelId)
                .build();
        return addLabels;
    }

    public static SkipLabels skipLabels(TaskCreationId pid) {
        SkipLabels skipLabels = SkipLabels
                .newBuilder()
                .setId(pid)
                .build();
        return skipLabels;
    }

    public static CompleteTaskCreation complete(TaskCreationId pid) {
        CompleteTaskCreation completeTaskCreation = CompleteTaskCreation
                .newBuilder()
                .setId(pid)
                .build();
        return completeTaskCreation;
    }

    public static CancelTaskCreation cancel(TaskCreationId pid) {
        CancelTaskCreation cancelTaskCreation = CancelTaskCreation
                .newBuilder()
                .setId(pid)
                .build();
        return cancelTaskCreation;
    }

    public static CreateBasicLabel createNewLabel(String title) {
        LabelId id = LabelId
                .newBuilder()
                .setValue(newUuid())
                .build();
        CreateBasicLabel cmd = CreateBasicLabel
                .newBuilder()
                .setLabelId(id)
                .setLabelTitle(title)
                .build();
        return cmd;
    }

    // Value generating static methods
    // -------------------------------

    public static TaskCreationId newPid() {
        return TaskCreationId.newBuilder()
                             .setValue(newUuid())
                             .build();
    }

    public static TaskId newTaskId() {
        return TaskId.newBuilder()
                     .setValue(newUuid())
                     .build();
    }
}
