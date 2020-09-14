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

package io.spine.examples.todolist.server.tasks.task;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.change.TimestampChange;
import io.spine.examples.todolist.tasks.DescriptionChange;
import io.spine.examples.todolist.tasks.LabelColor;
import io.spine.examples.todolist.tasks.LabelDetails;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.PriorityChange;
import io.spine.examples.todolist.tasks.TaskCreation;
import io.spine.examples.todolist.tasks.TaskDescription;
import io.spine.examples.todolist.tasks.TaskPriority;
import io.spine.examples.todolist.tasks.command.AddLabels;
import io.spine.examples.todolist.tasks.command.AssignLabelToTask;
import io.spine.examples.todolist.tasks.command.CancelTaskCreation;
import io.spine.examples.todolist.tasks.command.CompleteTaskCreation;
import io.spine.examples.todolist.tasks.command.CreateBasicLabel;
import io.spine.examples.todolist.tasks.command.CreateDraft;
import io.spine.examples.todolist.tasks.command.FinalizeDraft;
import io.spine.examples.todolist.tasks.command.SkipLabels;
import io.spine.examples.todolist.tasks.command.UpdateLabelDetails;
import io.spine.examples.todolist.tasks.command.UpdateTaskDescription;
import io.spine.examples.todolist.tasks.command.UpdateTaskDetails;
import io.spine.examples.todolist.tasks.command.UpdateTaskDueDate;
import io.spine.examples.todolist.tasks.command.UpdateTaskPriority;
import io.spine.examples.todolist.tasks.event.LabelAssignmentSkipped;
import io.spine.examples.todolist.tasks.rejection.Rejections;
import io.spine.testing.server.CommandSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.tasks.TaskCreation.Stage.TASK_DEFINITION;

@DisplayName("Task creation wizard on ")
class TaskCreationWizardTest {

    @Nested
    @DisplayName("StartTaskCreation command should")
    class StartTaskCreationTest extends CommandTest {

        @Test
        @DisplayName("produce a command to create draft and change the stage")
        void testStartWizard() {
            startWizard();
            TaskCreation expectedWizardState = TaskCreation
                    .newBuilder()
                    .setId(processId())
                    .setTaskId(taskId())
                    .setStage(TASK_DEFINITION)
                    .vBuild();
            context().assertCommands()
                     .withType(CreateDraft.class)
                     .hasSize(1);
            context().assertEntity(processId(), TaskCreationWizard.class)
                     .hasStateThat()
                     .comparingExpectedFieldsOnly()
                     .isEqualTo(expectedWizardState);
        }
    }

    @Nested
    @DisplayName("UpdateTaskDetails command should")
    class UpdateTaskDetailsTest extends CommandTest {

        @BeforeEach
        @Override
        protected void setUp() {
            super.setUp();
            startWizard();
        }

        @Test
        @DisplayName("issue task mutation commands")
        void testSetDetails() {
            String descriptionValue = "Task for test";
            TaskDescription description = TaskDescription
                    .newBuilder()
                    .setValue(descriptionValue)
                    .vBuild();
            DescriptionChange descriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(description)
                    .vBuild();
            TaskPriority priority = TaskPriority.HIGH;
            PriorityChange priorityChange = PriorityChange
                    .newBuilder()
                    .setNewValue(priority)
                    .vBuild();
            Timestamp dueDate = Time.currentTime();
            TimestampChange dueDateChange = TimestampChange
                    .newBuilder()
                    .setNewValue(dueDate)
                    .vBuild();
            UpdateTaskDetails cmd = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setDescriptionChange(descriptionChange)
                    .setPriorityChange(priorityChange)
                    .setDueDateChange(dueDateChange)
                    .vBuild();

            CommandSubject assertCommands = context().receivesCommand(cmd)
                                                     .assertCommands();
            assertCommands.withType(UpdateTaskDescription.class)
                          .hasSize(1);
            assertCommands.withType(UpdateTaskPriority.class)
                          .hasSize(1);
            assertCommands.withType(UpdateTaskDueDate.class)
                          .hasSize(1);
        }

        @Test
        @DisplayName("throw CannotUpdateTaskDetails if description is not specified")
        void throwOnDescNotSet() {
            TaskPriority priority = TaskPriority.HIGH;
            PriorityChange priorityChange = PriorityChange
                    .newBuilder()
                    .setNewValue(priority)
                    .vBuild();
            UpdateTaskDetails cmd = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setPriorityChange(priorityChange)
                    .vBuild();
            context().receivesCommand(cmd)
                     .assertEvents()
                     .withType(Rejections.CannotUpdateTaskDetails.class)
                     .hasSize(1);
        }

        @Test
        @DisplayName("allow omitting task description change on further updates")
        void allowOmitDescInFurtherUpdates() {
            String descriptionValue = "Task for test";
            TaskDescription description = TaskDescription
                    .newBuilder()
                    .setValue(descriptionValue)
                    .vBuild();
            DescriptionChange descriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(description)
                    .vBuild();
            UpdateTaskDetails cmd1 = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setDescriptionChange(descriptionChange)
                    .vBuild();

            TaskPriority priority = TaskPriority.HIGH;
            PriorityChange priorityChange = PriorityChange
                    .newBuilder()
                    .setNewValue(priority)
                    .vBuild();
            UpdateTaskDetails cmd2 = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setPriorityChange(priorityChange)
                    .vBuild();

            CommandSubject assertCommands = context().receivesCommand(cmd1)
                                                     .receivesCommand(cmd2)
                                                     .assertCommands();
            assertCommands.withType(UpdateTaskDescription.class)
                          .hasSize(1);
            assertCommands.withType(UpdateTaskPriority.class)
                          .hasSize(1);
        }

        @Test
        @DisplayName("modify task data even when on later stages")
        void modifyPreviousData() {
            String descriptionValue = "Description 1";
            TaskDescription description = TaskDescription
                    .newBuilder()
                    .setValue(descriptionValue)
                    .vBuild();
            DescriptionChange descriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(description)
                    .vBuild();
            UpdateTaskDetails cmd1 = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setDescriptionChange(descriptionChange)
                    .vBuild();

            SkipLabels cmd2 = SkipLabels
                    .newBuilder()
                    .setId(processId())
                    .vBuild();

            String newDescriptionValue = "Description 2";
            TaskDescription newDescription = TaskDescription
                    .newBuilder()
                    .setValue(newDescriptionValue)
                    .vBuild();
            DescriptionChange newDescriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(newDescription)
                    .vBuild();
            UpdateTaskDetails newUpdate = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setDescriptionChange(newDescriptionChange)
                    .vBuild();

            context().receivesCommand(cmd1)
                     .receivesCommand(cmd2)
                     .receivesCommand(newUpdate)
                     .assertCommands()
                     .withType(UpdateTaskDescription.class)
                     .hasSize(2);
        }
    }

    @Nested
    @DisplayName("AddLabels command should")
    class AddLabelsTest extends CommandTest {

        @BeforeEach
        @Override
        protected void setUp() {
            super.setUp();
            startWizard();
            addDescription();
        }

        @Test
        @DisplayName("create and assign requested labels")
        void testAddLabels() {
            List<LabelId> existingLabelIds = ImmutableList.of(LabelId.generate(),
                                                              LabelId.generate());
            LabelDetails newLabel = LabelDetails
                    .newBuilder()
                    .setTitle("testAddLabels")
                    .setColor(LabelColor.GREEN)
                    .vBuild();
            AddLabels cmd = AddLabels
                    .newBuilder()
                    .setId(processId())
                    .addAllExistingLabels(existingLabelIds)
                    .addNewLabels(newLabel)
                    .vBuild();
            context().receivesCommand(cmd);
            CommandSubject assertCommands = context().assertCommands();
            assertCommands.withType(AssignLabelToTask.class)
                          .hasSize(3);
            assertCommands.withType(CreateBasicLabel.class)
                          .hasSize(1);
            assertCommands.withType(UpdateLabelDetails.class)
                          .hasSize(1);
        }

        @Test
        @DisplayName("throw CannotAddLabels if no labels specified")
        void testNoLabels() {
            AddLabels cmd = AddLabels
                    .newBuilder()
                    .setId(processId())
                    .vBuild();
            context().receivesCommand(cmd)
                     .assertEvents()
                     .withType(Rejections.CannotAddLabels.class)
                     .hasSize(1);
        }
    }

    @Nested
    @DisplayName("SkipLabels command should")
    class SkipLabelsTest extends CommandTest {

        @BeforeEach
        @Override
        protected void setUp() {
            super.setUp();
            startWizard();
            addDescription();
        }

        @Test
        @DisplayName("skip labels creation and proceed to the next stage")
        void testSkipLabels() {
            SkipLabels cmd = SkipLabels
                    .newBuilder()
                    .setId(processId())
                    .vBuild();
            context().receivesCommand(cmd)
                     .assertEvents()
                     .withType(LabelAssignmentSkipped.class)
                     .hasSize(1);
        }
    }

    @Nested
    @DisplayName("CompleteTaskCreation command should")
    class CompleteTaskCreationTest extends CommandTest {

        @BeforeEach
        @Override
        protected void setUp() {
            super.setUp();
            startWizard();
            addDescription();
            skipLabels();
        }

        @Test
        @DisplayName("complete task creation process")
        void testCompleteWizard() {
            CompleteTaskCreation cmd = CompleteTaskCreation
                    .newBuilder()
                    .setId(processId())
                    .vBuild();
            context().receivesCommand(cmd);
            context().assertCommands()
                     .withType(FinalizeDraft.class)
                     .hasSize(1);
            context().assertEntity(processId(), TaskCreationWizard.class)
                     .archivedFlag()
                     .isTrue();
        }
    }

    @Nested
    @DisplayName("CancelTaskCreation command should")
    class CancelTaskCreationTest extends CommandTest {

        @BeforeEach
        @Override
        protected void setUp() {
            super.setUp();
            startWizard();
            addDescription();
        }

        @Test
        @DisplayName("cancel task creation process")
        void testCancelProc() {
            CancelTaskCreation cmd = CancelTaskCreation
                    .newBuilder()
                    .setId(processId())
                    .vBuild();
            context().receivesCommand(cmd)
                     .assertEntity(processId(), TaskCreationWizard.class)
                     .archivedFlag()
                     .isTrue();
        }
    }

    @Nested
    @DisplayName("any command should")
    class AnyCommandShould extends CommandTest {

        @BeforeEach
        @Override
        protected void setUp() {
            super.setUp();
            startWizard();
        }

        @Test
        @DisplayName("throw CannotMoveToStage rejection if trying to move on incorrect stage")
        void throwOnIncorrectStage() {
            CompleteTaskCreation cmd = CompleteTaskCreation
                    .newBuilder()
                    .setId(processId())
                    .vBuild();
            context().receivesCommand(cmd)
                     .assertEvents()
                     .withType(Rejections.CannotMoveToStage.class)
                     .hasSize(1);
        }
    }
}
