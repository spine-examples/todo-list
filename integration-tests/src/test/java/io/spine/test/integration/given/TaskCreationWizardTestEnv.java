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

package io.spine.test.integration.given;

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.CancelTaskCreation;
import io.spine.examples.todolist.c.commands.CompleteTaskCreation;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.SetTaskDetails;
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.client.TodoClient;

import java.util.Optional;

import static io.spine.Identifier.newUuid;
import static io.spine.examples.todolist.TaskPriority.TP_UNDEFINED;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The task creation wizard test environment.
 *
 * <p>Provides values and routines for sending commands to the procman and verifying the results.
 *
 * @author Dmytro Dashenkov
 */
@SuppressWarnings("OverlyCoupledClass") // OK for this class.
public class TaskCreationWizardTestEnv {

    private final TodoClient client;

    private TaskCreationWizardTestEnv(TodoClient client) {
        this.client = client;
    }

    /**
     * Creates a new instance of {@code TaskCreationWizardTestEnv} with the given client.
     *
     * @param todoClient the {@code TodoClient} to connect to the TodoList server
     * @return new instance of {@code TaskCreationWizardTestEnv}
     */
    public static TaskCreationWizardTestEnv with(TodoClient todoClient) {
        return new TaskCreationWizardTestEnv(todoClient);
    }

    // Command sending methods
    // -----------------------

    public void createDraft(TaskCreationId pid, TaskId taskId) {
        final StartTaskCreation startCreation = StartTaskCreation.newBuilder()
                                                                 .setId(pid)
                                                                 .setTaskId(taskId)
                                                                 .build();
        client.postCommand(startCreation);
    }

    public void setDetails(TaskCreationId pid, String description) {
        setDetails(pid, description, TP_UNDEFINED, Timestamp.getDefaultInstance());
    }

    public void setDetails(TaskCreationId pid, String description,
                           TaskPriority priority, Timestamp dueDate) {
        final TaskDescription descValue = TaskDescription.newBuilder()
                                                         .setValue(description)
                                                         .build();
        final SetTaskDetails setDescription = SetTaskDetails.newBuilder()
                                                            .setId(pid)
                                                            .setDescription(descValue)
                                                            .setPriority(priority)
                                                            .setDueDate(dueDate)
                                                            .build();
        client.postCommand(setDescription);
    }

    public void addLabel(TaskCreationId pid, LabelId labelId) {
        final AddLabels addLabels = AddLabels.newBuilder()
                                             .setId(pid)
                                             .addExistingLabels(labelId)
                                             .build();
        client.postCommand(addLabels);
    }

    public void skipLabels(TaskCreationId pid) {
        final AddLabels addLabels = AddLabels.newBuilder()
                                             .setId(pid)
                                             .build();
        client.postCommand(addLabels);
    }

    public void complete(TaskCreationId pid) {
        final CompleteTaskCreation completeTaskCreation = CompleteTaskCreation.newBuilder()
                                                                              .setId(pid)
                                                                              .build();
        client.postCommand(completeTaskCreation);
    }

    public void cancel(TaskCreationId pid) {
        final CancelTaskCreation completeTaskCreation = CancelTaskCreation.newBuilder()
                                                                          .setId(pid)
                                                                          .build();
        client.postCommand(completeTaskCreation);
    }

    public LabelId createNewLabel(String title) {
        final LabelId id = LabelId.newBuilder()
                                  .setValue(newUuid())
                                  .build();
        final CreateBasicLabel cmd = CreateBasicLabel.newBuilder()
                                                     .setLabelId(id)
                                                     .setLabelTitle(title)
                                                     .build();
        client.postCommand(cmd);
        return id;
    }

    // Query methods
    // -------------

    public Task taskById(TaskId id) {
        final Optional<Task> taskOptional = client.getTasks()
                                                  .stream()
                                                  .filter(task -> id.equals(task.getId()))
                                                  .findAny();
        assertTrue(taskOptional.isPresent());
        final Task actualTask = taskOptional.get();
        return actualTask;
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
