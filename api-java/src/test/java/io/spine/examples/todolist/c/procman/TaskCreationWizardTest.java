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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Timestamp;
import io.spine.base.CommandMessage;
import io.spine.base.Time;
import io.spine.change.TimestampChange;
import io.spine.client.ActorRequestFactory;
import io.spine.core.Command;
import io.spine.examples.todolist.DescriptionChange;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsVBuilder;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.LabelIdVBuilder;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskCreation;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskCreationIdVBuilder;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskDescriptionVBuilder;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskIdVBuilder;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.AddLabelsVBuilder;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CancelTaskCreation;
import io.spine.examples.todolist.c.commands.CancelTaskCreationVBuilder;
import io.spine.examples.todolist.c.commands.CompleteTaskCreation;
import io.spine.examples.todolist.c.commands.CompleteTaskCreationVBuilder;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraftVBuilder;
import io.spine.examples.todolist.c.commands.SkipLabels;
import io.spine.examples.todolist.c.commands.SkipLabelsVBuilder;
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.c.commands.StartTaskCreationVBuilder;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDescriptionVBuilder;
import io.spine.examples.todolist.c.commands.UpdateTaskDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDetailsVBuilder;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDateVBuilder;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.c.commands.UpdateTaskPriorityVBuilder;
import io.spine.examples.todolist.c.rejection.CannotAddLabels;
import io.spine.examples.todolist.c.rejection.CannotMoveToStage;
import io.spine.examples.todolist.c.rejection.CannotUpdateTaskDetails;
import io.spine.server.BoundedContext;
import io.spine.server.commandbus.CommandBus;
import io.spine.server.commandbus.CommandDispatcher;
import io.spine.server.procman.ProcessManager;
import io.spine.server.type.CommandClass;
import io.spine.server.type.CommandEnvelope;
import io.spine.testing.client.TestActorRequestFactory;
import io.spine.testing.server.procman.PmDispatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.TaskCreation.Stage.CANCELED;
import static io.spine.examples.todolist.TaskCreation.Stage.COMPLETED;
import static io.spine.examples.todolist.TaskCreation.Stage.CONFIRMATION;
import static io.spine.examples.todolist.TaskCreation.Stage.LABEL_ASSIGNMENT;
import static io.spine.examples.todolist.TaskCreation.Stage.TASK_DEFINITION;
import static io.spine.server.type.CommandClass.from;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Task creation wizard on ")
class TaskCreationWizardTest {

    private static final ActorRequestFactory requestFactory =
            new TestActorRequestFactory(TaskCreationWizardTest.class);

    @Nested
    @DisplayName("StartTaskCreation command should")
    class StartTaskCreationTest extends CommandTest {

        @BeforeEach
        @Override
        protected void setUp() {
            super.setUp();
        }

        @Test
        @DisplayName("start task creation process")
        void testStartWizard() {
            TaskId taskId = newTaskId();
            StartTaskCreation cmd = StartTaskCreationVBuilder
                    .newBuilder()
                    .setId(getId())
                    .setTaskId(taskId)
                    .build();
            dispatch(cmd);
            CommandMessage producedCommand = memoizingHandler().received.pop();
            assertThat(producedCommand, instanceOf(CreateDraft.class));
            CreateDraft createDraftCmd = (CreateDraft) producedCommand;
            assertEquals(taskId, createDraftCmd.getId());
            assertEquals(taskId, getTaskId());

            assertEquals(TASK_DEFINITION, getStage());
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
            TaskDescription description = TaskDescriptionVBuilder
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
            Timestamp dueDate = Time.getCurrentTime();
            TimestampChange dueDateChange = TimestampChange
                    .newBuilder()
                    .setNewValue(dueDate)
                    .build();
            UpdateTaskDetails cmd = UpdateTaskDetailsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .setDescriptionChange(descriptionChange)
                    .setPriorityChange(priorityChange)
                    .setDueDateChange(dueDateChange)
                    .build();

            dispatch(cmd);

            ArrayDeque<CommandMessage> producedCommands = memoizingHandler().received;

            assertThat(producedCommands, containsInAnyOrder(
                    UpdateTaskDescriptionVBuilder.newBuilder()
                                                 .setId(getTaskId())
                                                 .setDescriptionChange(descriptionChange)
                                                 .build(),
                    UpdateTaskPriorityVBuilder.newBuilder()
                                              .setId(getTaskId())
                                              .setPriorityChange(priorityChange)
                                              .build(),
                    UpdateTaskDueDateVBuilder.newBuilder()
                                             .setId(getTaskId())
                                             .setDueDateChange(dueDateChange)
                                             .build()));
            assertEquals(LABEL_ASSIGNMENT, getStage());
        }

        @Test
        @DisplayName("throw CannotUpdateTaskDetails if description is not specified")
        void throwOnDescNotSet() {
            TaskPriority priority = TaskPriority.HIGH;
            PriorityChange priorityChange = PriorityChange
                    .newBuilder()
                    .setNewValue(priority)
                    .build();
            UpdateTaskDetails cmd = UpdateTaskDetailsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .setPriorityChange(priorityChange)
                    .build();
            Throwable t = assertThrows(Throwable.class, () -> dispatch(cmd));
            assertThat(Throwables.getRootCause(t), instanceOf(CannotUpdateTaskDetails.class));
            assertEquals(TASK_DEFINITION, getStage());
        }

        @Test
        @DisplayName("allow omitting task description change on further updates")
        void allowOmitDescInFurtherUpdates() {
            String descriptionValue = "Task for test";
            TaskDescription description = TaskDescriptionVBuilder
                    .newBuilder()
                    .setValue(descriptionValue)
                    .build();
            DescriptionChange descriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(description)
                    .build();
            UpdateTaskDetails cmd1 = UpdateTaskDetailsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .setDescriptionChange(descriptionChange)
                    .build();
            dispatch(cmd1);

            TaskPriority priority = TaskPriority.HIGH;
            PriorityChange priorityChange = PriorityChange
                    .newBuilder()
                    .setNewValue(priority)
                    .build();
            UpdateTaskDetails cmd2 = UpdateTaskDetailsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .setPriorityChange(priorityChange)
                    .build();
            dispatch(cmd2);
            ArrayDeque<CommandMessage> producedCommands = memoizingHandler().received;

            assertThat(producedCommands, containsInAnyOrder(
                    UpdateTaskDescriptionVBuilder.newBuilder()
                                                 .setId(getTaskId())
                                                 .setDescriptionChange(descriptionChange)
                                                 .build(),
                    UpdateTaskPriorityVBuilder.newBuilder()
                                              .setId(getTaskId())
                                              .setPriorityChange(priorityChange)
                                              .build()
            ));
            assertEquals(LABEL_ASSIGNMENT, getStage());
        }

        @Test
        @DisplayName("mofify task data even when on later stages")
        void modifyPreviousData() {
            String descriptionValue = "Description 1";
            TaskDescription description = TaskDescriptionVBuilder
                    .newBuilder()
                    .setValue(descriptionValue)
                    .build();
            DescriptionChange descriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(description)
                    .build();
            UpdateTaskDetails cmd1 = UpdateTaskDetailsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .setDescriptionChange(descriptionChange)
                    .build();
            dispatch(cmd1);

            SkipLabels cmd2 = SkipLabelsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .build();
            dispatch(cmd2);
            assertEquals(CONFIRMATION, getStage());

            String newDescriptionValue = "Description 2";
            TaskDescription newDescription = TaskDescriptionVBuilder
                    .newBuilder()
                    .setValue(newDescriptionValue)
                    .build();
            DescriptionChange newDescriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(newDescription)
                    .build();
            UpdateTaskDetails newUpdate = UpdateTaskDetailsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .setDescriptionChange(newDescriptionChange)
                    .build();
            dispatch(newUpdate);

            ArrayDeque<CommandMessage> producedCommands = memoizingHandler().received;
            assertThat(producedCommands, hasItem(
                    UpdateTaskDescriptionVBuilder.newBuilder()
                                                 .setId(getTaskId())
                                                 .setDescriptionChange(newDescriptionChange)
                                                 .build()
            ));
            assertEquals(CONFIRMATION, getStage());
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
            LabelDetails newLabel = LabelDetailsVBuilder
                    .newBuilder()
                    .setTitle("testAddLabels")
                    .setColor(LabelColor.GREEN)
                    .build();
            AddLabels cmd = AddLabelsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .addAllExistingLabels(existingLabelIds)
                    .addNewLabels(newLabel)
                    .build();
            dispatch(cmd);

            Collection<Matcher<? super CommandMessage>> expectedCommands = ImmutableList.of(
                    instanceOf(AssignLabelToTask.class), // 3 label assignments
                    instanceOf(AssignLabelToTask.class),
                    instanceOf(AssignLabelToTask.class),
                    instanceOf(CreateBasicLabel.class),  // 1 label creation with details updates
                    instanceOf(UpdateLabelDetails.class)
            );
            ArrayDeque<CommandMessage> producedCommands = memoizingHandler().received;
            assertThat(producedCommands, containsInAnyOrder(expectedCommands));
            assertEquals(CONFIRMATION, getStage());
        }

        @Test
        @DisplayName("throw CannotAddLabels if no labels specified")
        void testNoLabels() {
            AddLabels cmd = AddLabelsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .build();
            Throwable t = assertThrows(Throwable.class, () -> dispatch(cmd));
            assertThat(Throwables.getRootCause(t), instanceOf(CannotAddLabels.class));
            assertEquals(LABEL_ASSIGNMENT, getStage());
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
        @DisplayName("skip labels creation and procede to the next stage")
        void testSkipLabels() {
            SkipLabels cmd = SkipLabelsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .build();
            dispatch(cmd);
            assertEquals(CONFIRMATION, getStage());
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
            CompleteTaskCreation cmd = CompleteTaskCreationVBuilder
                    .newBuilder()
                    .setId(getId())
                    .build();
            dispatch(cmd);
            ArrayDeque<CommandMessage> producedCommands = memoizingHandler().received;
            CommandMessage producedCommand = producedCommands.pop();
            FinalizeDraft expectedCmd = FinalizeDraftVBuilder
                    .newBuilder()
                    .setId(getTaskId())
                    .build();
            assertEquals(expectedCmd, producedCommand);
            assertEquals(COMPLETED, getStage());
            assertArchived();
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
            CancelTaskCreation cmd = CancelTaskCreationVBuilder
                    .newBuilder()
                    .setId(getId())
                    .build();
            dispatch(cmd);
            assertEquals(CANCELED, getStage());
            assertArchived();
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
            CompleteTaskCreation cmd = CompleteTaskCreationVBuilder
                    .newBuilder()
                    .setId(getId())
                    .build();
            Throwable t = assertThrows(Throwable.class, () -> dispatch(cmd));
            assertThat(Throwables.getRootCause(t), instanceOf(CannotMoveToStage.class));
        }
    }

    private abstract static class CommandTest {

        private TaskCreationWizard wizard;

        private MemoizingCommandHandler memoizingHandler;

        void setUp() {
            wizard = new TaskCreationWizard(newId());
            memoizingHandler = new MemoizingCommandHandler();
            prepareCommandBus();
        }

        TaskCreationId getId() {
            return wizard.id();
        }

        TaskId getTaskId() {
            return wizard.state()
                         .getTaskId();
        }

        TaskCreation.Stage getStage() {
            return wizard.state()
                         .getStage();
        }

        MemoizingCommandHandler memoizingHandler() {
            return memoizingHandler;
        }

        void assertArchived() {
            boolean archived = wizard.isArchived();
            boolean deleted = wizard.isDeleted();
            assertTrue(archived, "Should be archived");
            assertFalse(deleted, "Should not be deleted");
        }

        void dispatch(CommandMessage command) {
            Command cmd = requestFactory.command()
                                        .create(command);
            CommandEnvelope envelope = CommandEnvelope.of(cmd);
            PmDispatcher.dispatch(wizard, envelope);
        }

        void startWizard() {
            StartTaskCreation cmd = StartTaskCreationVBuilder
                    .newBuilder()
                    .setId(getId())
                    .setTaskId(newTaskId())
                    .build();
            dispatch(cmd);
            clearReceivedCommands();
        }

        void addDescription() {
            TaskDescription description = TaskDescriptionVBuilder
                    .newBuilder()
                    .setValue("task for test")
                    .build();
            DescriptionChange descriptionChange = DescriptionChange
                    .newBuilder()
                    .setNewValue(description)
                    .build();
            UpdateTaskDetails cmd = UpdateTaskDetailsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .setDescriptionChange(descriptionChange)
                    .build();
            dispatch(cmd);
            clearReceivedCommands();
        }

        void skipLabels() {
            SkipLabels cmd = SkipLabelsVBuilder
                    .newBuilder()
                    .setId(getId())
                    .build();
            dispatch(cmd);
            clearReceivedCommands();
        }

        private void clearReceivedCommands() {
            memoizingHandler().received.clear();
        }

        /**
         * Injects the fake CommandBus instance via Reflection.
         *
         * <p>This is suitable when tested handler posts subsequent commands to a command bus (which
         * doesn't exist at a test time) and you don't care about them.
         */
        private void prepareCommandBus() {
            CommandBus commandBus = createCommandBus();
            try {
                Method method = ProcessManager.class.getDeclaredMethod("setCommandBus",
                                                                       CommandBus.class);
                method.setAccessible(true);
                method.invoke(wizard, commandBus);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw illegalStateWithCauseOf(e);
            }
        }

        private CommandBus createCommandBus() {
            BoundedContext emptyContext = BoundedContext
                    .newBuilder()
                    .setCommandBus(CommandBus.newBuilder())
                    .build();
            CommandBus commandBus = emptyContext.commandBus();
            commandBus.register(memoizingHandler);
            return commandBus;
        }

        private static final class MemoizingCommandHandler implements CommandDispatcher<Object> {

            private final ArrayDeque<CommandMessage> received = new ArrayDeque<>();

            @SuppressWarnings("BadImport") // Actually enhances readability.
            @Override
            public Set<CommandClass> messageClasses() {
                return ImmutableSet.of(
                        from(CreateDraft.class),
                        from(UpdateTaskDueDate.class),
                        from(UpdateTaskPriority.class),
                        from(UpdateTaskDescription.class),
                        from(CreateBasicLabel.class),
                        from(UpdateLabelDetails.class),
                        from(AssignLabelToTask.class),
                        from(FinalizeDraft.class)
                );
            }

            @Override
            public Object dispatch(CommandEnvelope envelope) {
                received.push(envelope.message());
                return MemoizingCommandHandler.class.getName();
            }

            @Override
            public void onError(CommandEnvelope envelope, RuntimeException exception) {
                // NoOp for test.
            }
        }

        private static TaskCreationId newId() {
            return TaskCreationIdVBuilder.newBuilder()
                                         .setValue(newUuid())
                                         .build();
        }
    }

    private static TaskId newTaskId() {
        return TaskIdVBuilder.newBuilder()
                             .setValue(newUuid())
                             .build();
    }

    private static LabelId newLabelId() {
        return LabelIdVBuilder.newBuilder()
                              .setValue(newUuid())
                              .build();
    }
}
