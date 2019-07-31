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

package io.spine.test.integration;

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabels;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.command.AddLabels;
import io.spine.examples.todolist.command.CancelTaskCreation;
import io.spine.examples.todolist.command.CompleteTaskCreation;
import io.spine.examples.todolist.command.CreateBasicLabel;
import io.spine.examples.todolist.command.SkipLabels;
import io.spine.examples.todolist.command.StartTaskCreation;
import io.spine.examples.todolist.command.UpdateTaskDetails;
import io.spine.examples.todolist.server.label.LabelAggregateRepository;
import io.spine.examples.todolist.server.task.TaskCreationWizardRepository;
import io.spine.examples.todolist.server.task.TaskLabelsPart;
import io.spine.examples.todolist.server.task.TaskLabelsRepository;
import io.spine.examples.todolist.server.task.TaskPart;
import io.spine.examples.todolist.server.task.TaskRepository;
import io.spine.examples.todolist.server.task.TaskViewRepository;
import io.spine.testing.server.blackbox.BlackBoxBoundedContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Timestamps.add;
import static io.spine.base.Time.currentTime;
import static io.spine.examples.todolist.LabelColor.BLUE;
import static io.spine.examples.todolist.LabelColor.GREEN;
import static io.spine.examples.todolist.LabelColor.RED;
import static io.spine.examples.todolist.TaskPriority.LOW;
import static io.spine.examples.todolist.TaskStatus.DRAFT;
import static io.spine.examples.todolist.TaskStatus.FINALIZED;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.addLabel;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.cancel;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.complete;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.createDraft;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.createNewLabel;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.setDetails;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.skipLabels;

@DisplayName("TaskCreationWizard should")
class TaskCreationWizardTest {

    private BlackBoxBoundedContext<?> context;

    @BeforeEach
    void before() {
        context = BlackBoxBoundedContext
                .singleTenant()
                .with(new TaskCreationWizardRepository(),
                      new TaskRepository(),
                      new LabelAggregateRepository(),
                      new TaskLabelsRepository(),
                      new TaskViewRepository());
    }

    @Test
    @DisplayName("supervise task creation")
    void firstCase() {
        TaskCreationId pid = TaskCreationId.generate();
        TaskId taskId = TaskId.generate();
        StartTaskCreation createDraft = createDraft(pid, taskId);
        String description = "firstCase";
        UpdateTaskDetails updateDetails = setDetails(pid, description);
        SkipLabels skipLabels = skipLabels(pid);
        CompleteTaskCreation completeCreation = complete(pid);

        Task expected = Task
                .newBuilder()
                .setId(taskId)
                .setDescription(updateDetails.getDescriptionChange()
                                             .getNewValue())
                .setTaskStatus(FINALIZED)
                .build();

        context.receivesCommand(createDraft)
               .receivesCommand(updateDetails)
               .receivesCommand(skipLabels)
               .receivesCommand(completeCreation)
               .assertEntity(TaskPart.class, taskId)
               .hasStateThat()
               .comparingExpectedFieldsOnly()
               .isEqualTo(expected);

    }

    @Test
    @DisplayName("create and assign new labels")
    void secondCase() {
        TaskCreationId pid = TaskCreationId.generate();
        TaskId taskId = TaskId.generate();
        StartTaskCreation createDraft = createDraft(pid, taskId);
        UpdateTaskDetails setDetails = setDetails(pid, "secondCase");
        LabelDetails redLabel = LabelDetails
                .newBuilder()
                .setTitle("red label")
                .setColor(RED)
                .build();
        LabelDetails greenLabel = LabelDetails
                .newBuilder()
                .setTitle("green label")
                .setColor(GREEN)
                .build();
        LabelDetails blueLabel = LabelDetails
                .newBuilder()
                .setTitle("blue label")
                .setColor(BLUE)
                .build();
        AddLabels addLabels = AddLabels
                .newBuilder()
                .setId(pid)
                .addNewLabels(redLabel)
                .addNewLabels(greenLabel)
                .addNewLabels(blueLabel)
                .build();

        TaskLabels expected = TaskLabels
                .newBuilder()
                .setTaskId(taskId)
                .build();

        context.receivesCommand(createDraft)
               .receivesCommand(setDetails)
               .receivesCommand(addLabels)
               .assertEntity(TaskLabelsPart.class, taskId)
               .hasStateThat()
               .comparingExpectedFieldsOnly()
               .isEqualTo(expected);
    }

    @Test
    @DisplayName("set all the optional fields")
    void thirdCase() {
        TaskCreationId pid = TaskCreationId.generate();
        TaskId taskId = TaskId.generate();
        StartTaskCreation createDraft = createDraft(pid, taskId);

        String description = "thirdCase";
        Timestamp dueDate = add(currentTime(), fromSeconds(100));
        TaskPriority priority = LOW;
        UpdateTaskDetails updateDetails = setDetails(pid, description, priority, dueDate);
        String labelTitle = "thirdCase-label";
        CreateBasicLabel createBasicLabel = createNewLabel(labelTitle);
        AddLabels addLabels = addLabel(pid, createBasicLabel.getLabelId());
        CompleteTaskCreation completeTaskCreation = complete(pid);

        Task expected = Task
                .newBuilder()
                .setId(taskId)
                .setTaskStatus(FINALIZED)
                .setPriority(priority)
                .setDueDate(dueDate)
                .build();

        context.receivesCommand(createDraft)
               .receivesCommand(updateDetails)
               .receivesCommand(addLabels)
               .receivesCommand(completeTaskCreation)
               .assertEntity(TaskPart.class, taskId)
               .hasStateThat()
               .comparingExpectedFieldsOnly()
               .isEqualTo(expected);
    }

    @Test
    @DisplayName("cancel the process")
    void forthCase() {
        TaskCreationId pid = TaskCreationId.generate();
        TaskId taskId = TaskId.generate();
        StartTaskCreation createDraft = createDraft(pid, taskId);
        String description = "fourthCase";
        UpdateTaskDetails setDetails = setDetails(pid, description);
        CancelTaskCreation cancel = cancel(pid);

        Task expected = Task
                .newBuilder()
                .setId(taskId)
                .setDescription(setDetails.getDescriptionChange()
                                          .getNewValue())
                .setTaskStatus(DRAFT)
                .build();

        context.receivesCommand(createDraft)
               .receivesCommand(setDetails)
               .receivesCommand(cancel)
               .assertEntity(TaskPart.class, taskId)
               .hasStateThat()
               .comparingExpectedFieldsOnly()
               .isEqualTo(expected);
    }
}
