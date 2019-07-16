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

package io.spine.examples.todolist.c.procman;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.change.TimestampChange;
import io.spine.examples.todolist.DescriptionChange;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskCreation;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CancelTaskCreation;
import io.spine.examples.todolist.c.commands.CompleteTaskCreation;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.SkipLabels;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.c.events.LabelAssignmentSkipped;
import io.spine.examples.todolist.c.rejection.Rejections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.TaskCreation.Stage.TASK_DEFINITION;
import static io.spine.testing.client.blackbox.Count.once;
import static io.spine.testing.client.blackbox.Count.thrice;
import static io.spine.testing.client.blackbox.Count.twice;
import static io.spine.testing.server.blackbox.VerifyCommands.emittedCommand;
import static io.spine.testing.server.blackbox.VerifyCommands.emittedCommands;

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
                    .build();
            context().assertThat(emittedCommand(CreateDraft.class, once()));
            context().assertEntity(TaskCreationWizard.class, processId())
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
                    .build();
            DescriptionChange descriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(description)
                    .build();
            TaskPriority priority = TaskPriority.HIGH;
            PriorityChange priorityChange = PriorityChange
                    .newBuilder()
                    .setNewValue(priority)
                    .build();
            Timestamp dueDate = Time.currentTime();
            TimestampChange dueDateChange = TimestampChange
                    .newBuilder()
                    .setNewValue(dueDate)
                    .build();
            UpdateTaskDetails cmd = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setDescriptionChange(descriptionChange)
                    .setPriorityChange(priorityChange)
                    .setDueDateChange(dueDateChange)
                    .build();

            context().receivesCommand(cmd)
                     .assertThat(emittedCommands(UpdateTaskDescription.class,
                                                 UpdateTaskPriority.class,
                                                 UpdateTaskDueDate.class));
        }

        @Test
        @DisplayName("throw CannotUpdateTaskDetails if description is not specified")
        void throwOnDescNotSet() {
            TaskPriority priority = TaskPriority.HIGH;
            PriorityChange priorityChange = PriorityChange
                    .newBuilder()
                    .setNewValue(priority)
                    .build();
            UpdateTaskDetails cmd = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setPriorityChange(priorityChange)
                    .build();
            context().receivesCommand(cmd)
                     .assertRejectedWith(Rejections.CannotUpdateTaskDetails.class);
        }

        @Test
        @DisplayName("allow omitting task description change on further updates")
        void allowOmitDescInFurtherUpdates() {
            String descriptionValue = "Task for test";
            TaskDescription description = TaskDescription
                    .newBuilder()
                    .setValue(descriptionValue)
                    .build();
            DescriptionChange descriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(description)
                    .build();
            UpdateTaskDetails cmd1 = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setDescriptionChange(descriptionChange)
                    .build();

            TaskPriority priority = TaskPriority.HIGH;
            PriorityChange priorityChange = PriorityChange
                    .newBuilder()
                    .setNewValue(priority)
                    .build();
            UpdateTaskDetails cmd2 = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setPriorityChange(priorityChange)
                    .build();

            context().receivesCommand(cmd1)
                     .receivesCommand(cmd2)
                     .assertThat(emittedCommands(UpdateTaskDescription.class,
                                                 UpdateTaskPriority.class));
        }

        @Test
        @DisplayName("modify task data even when on later stages")
        void modifyPreviousData() {
            String descriptionValue = "Description 1";
            TaskDescription description = TaskDescription
                    .newBuilder()
                    .setValue(descriptionValue)
                    .build();
            DescriptionChange descriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(description)
                    .build();
            UpdateTaskDetails cmd1 = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setDescriptionChange(descriptionChange)
                    .build();

            SkipLabels cmd2 = SkipLabels
                    .newBuilder()
                    .setId(processId())
                    .build();

            String newDescriptionValue = "Description 2";
            TaskDescription newDescription = TaskDescription
                    .newBuilder()
                    .setValue(newDescriptionValue)
                    .build();
            DescriptionChange newDescriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(newDescription)
                    .build();
            UpdateTaskDetails newUpdate = UpdateTaskDetails
                    .newBuilder()
                    .setId(processId())
                    .setDescriptionChange(newDescriptionChange)
                    .build();

            context().receivesCommand(cmd1)
                     .receivesCommand(cmd2)
                     .receivesCommand(newUpdate)
                     .assertThat(emittedCommand(UpdateTaskDescription.class, twice()));
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
            List<LabelId> existingLabelIds = ImmutableList.of(newLabelId(), newLabelId());
            LabelDetails newLabel = LabelDetails
                    .newBuilder()
                    .setTitle("testAddLabels")
                    .setColor(LabelColor.GREEN)
                    .build();
            AddLabels cmd = AddLabels
                    .newBuilder()
                    .setId(processId())
                    .addAllExistingLabels(existingLabelIds)
                    .addNewLabels(newLabel)
                    .build();
            context().receivesCommand(cmd);
            context().assertThat(emittedCommand(AssignLabelToTask.class, thrice()))
                     .assertThat(emittedCommand(CreateBasicLabel.class, once()))
                     .assertThat(emittedCommand(UpdateLabelDetails.class, once()));
        }

        @Test
        @DisplayName("throw CannotAddLabels if no labels specified")
        void testNoLabels() {
            AddLabels cmd = AddLabels
                    .newBuilder()
                    .setId(processId())
                    .build();
            context().receivesCommand(cmd)
                     .assertRejectedWith(Rejections.CannotAddLabels.class);
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
                    .build();
            context().receivesCommand(cmd)
                     .assertEmitted(LabelAssignmentSkipped.class);
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
                    .build();
            context().receivesCommand(cmd)
                     .assertThat(emittedCommand(FinalizeDraft.class, once()))
                     .assertEntity(TaskCreationWizard.class, processId())
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
                    .build();
            context().receivesCommand(cmd)
                     .assertEntity(TaskCreationWizard.class, processId())
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
                    .build();
            context().receivesCommand(cmd)
                     .assertRejectedWith(Rejections.CannotMoveToStage.class);
        }
    }
}
