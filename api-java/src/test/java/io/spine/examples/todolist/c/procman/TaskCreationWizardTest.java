/*
 * Copyright 2018, TeamDev. All rights reserved.
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
import io.spine.change.StringChange;
import io.spine.change.TimestampChange;
import io.spine.client.ActorRequestFactory;
import io.spine.core.Command;
import io.spine.core.CommandClass;
import io.spine.core.CommandEnvelope;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskCreation;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CancelTaskCreation;
import io.spine.examples.todolist.c.commands.CompleteTaskCreation;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.SetTaskDetails;
import io.spine.examples.todolist.c.commands.SkipLabels;
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.c.rejection.CannotAddLabels;
import io.spine.server.BoundedContext;
import io.spine.server.commandbus.CommandBus;
import io.spine.server.commandbus.CommandDispatcher;
import io.spine.server.procman.ProcessManager;
import io.spine.testing.client.TestActorRequestFactory;
import io.spine.testing.server.procman.PmDispatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import static io.spine.base.Identifier.newUuid;
import static io.spine.core.CommandClass.from;
import static io.spine.examples.todolist.TaskCreation.Stage.CANCELED;
import static io.spine.examples.todolist.TaskCreation.Stage.COMPLETED;
import static io.spine.examples.todolist.TaskCreation.Stage.CONFIRMATION;
import static io.spine.examples.todolist.TaskCreation.Stage.LABEL_ASSIGNMENT;
import static io.spine.examples.todolist.TaskCreation.Stage.TASK_DEFINITION;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Dashenkov
 */
@DisplayName("Task creation wizard on ")
class TaskCreationWizardTest {

    private static final ActorRequestFactory requestFactory =
            TestActorRequestFactory.newInstance(TaskCreationWizardTest.class);

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
            final TaskId taskId = newTaskId();
            final StartTaskCreation cmd = StartTaskCreation.newBuilder()
                                                           .setId(getId())
                                                           .setTaskId(taskId)
                                                           .build();
            dispatch(cmd);
            CommandMessage producedCommand = memoizingHandler().received.pop();
            assertThat(producedCommand, instanceOf(CreateDraft.class));
            final CreateDraft createDraftCmd = (CreateDraft) producedCommand;
            assertEquals(taskId, createDraftCmd.getId());
            assertEquals(taskId, getTaskId());

            assertEquals(TASK_DEFINITION, getStage());
        }
    }

    @Nested
    @DisplayName("SetTaskDetails command should")
    class SetTaskDetailsTest extends CommandTest {

        @BeforeEach
        @Override
        protected void setUp() {
            super.setUp();
            startWizard();
        }

        @Test
        @DisplayName("issue task mutation commands")
        void testSetDetails() {
            final String descriptionValue = "Task for test";
            final TaskDescription description = TaskDescription.newBuilder()
                                                               .setValue(descriptionValue)
                                                               .build();
            final TaskPriority priority = TaskPriority.HIGH;
            final Timestamp dueDate = Time.getCurrentTime();
            final SetTaskDetails cmd = SetTaskDetails.newBuilder()
                                                     .setId(getId())
                                                     .setDescription(description)
                                                     .setPriority(priority)
                                                     .setDueDate(dueDate)
                                                     .build();

            dispatch(cmd);

            final StringChange descriptionChange = StringChange.newBuilder()
                                                               .setNewValue(descriptionValue)
                                                               .build();
            final PriorityChange priorityChange = PriorityChange.newBuilder()
                                                                .setNewValue(priority)
                                                                .build();
            final TimestampChange dueDateChange = TimestampChange.newBuilder()
                                                                 .setNewValue(dueDate)
                                                                 .build();
            Stack<CommandMessage> producedCommands = memoizingHandler().received;

            assertThat(producedCommands, containsInAnyOrder(
                    UpdateTaskDescription.newBuilder()
                                         .setId(getTaskId())
                                         .setDescriptionChange(
                                                 descriptionChange)
                                         .build(),
                    UpdateTaskPriority.newBuilder()
                                      .setId(getTaskId())
                                      .setPriorityChange(
                                              priorityChange)
                                      .build(),
                    UpdateTaskDueDate.newBuilder()
                                     .setId(getTaskId())
                                     .setDueDateChange(
                                             dueDateChange)
                                     .build()));
            assertEquals(LABEL_ASSIGNMENT, getStage());
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
            final List<LabelId> existingLabelIds = ImmutableList.of(newLabelId(), newLabelId());
            final LabelDetails newLabel = LabelDetails.newBuilder()
                                                      .setTitle("testAddLabels")
                                                      .setColor(LabelColor.GREEN)
                                                      .build();
            final AddLabels cmd = AddLabels.newBuilder()
                                           .setId(getId())
                                           .addAllExistingLabels(existingLabelIds)
                                           .addNewLabels(newLabel)
                                           .build();

            dispatch(cmd);

            final Collection<Matcher<? super CommandMessage>> expectedCommands = ImmutableList.of(
                    instanceOf(AssignLabelToTask.class), // 3 label assignments
                    instanceOf(AssignLabelToTask.class),
                    instanceOf(AssignLabelToTask.class),
                    instanceOf(CreateBasicLabel.class),  // 1 label creation with details updates
                    instanceOf(UpdateLabelDetails.class)
            );
            Stack<CommandMessage> producedCommands = memoizingHandler().received;
            assertThat(producedCommands, containsInAnyOrder(expectedCommands));
            assertEquals(CONFIRMATION, getStage());
        }

        @Test
        @DisplayName("throw CannotAddLabels if no labels specified")
        void testNoLabels() {
            final AddLabels cmd = AddLabels.newBuilder()
                                           .setId(getId())
                                           .build();
            final Throwable t = assertThrows(Throwable.class, () -> dispatch(cmd));
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
            final SkipLabels cmd = SkipLabels.newBuilder()
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
            final CompleteTaskCreation cmd = CompleteTaskCreation.newBuilder()
                                                                 .setId(getId())
                                                                 .build();
            dispatch(cmd);
            Stack<CommandMessage> producedCommands = memoizingHandler().received;
            CommandMessage producedCommand = producedCommands.pop();
            final FinalizeDraft expectedCmd = FinalizeDraft.newBuilder()
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
            final CancelTaskCreation cmd = CancelTaskCreation.newBuilder()
                                                               .setId(getId())
                                                               .build();
            dispatch(cmd);
            assertEquals(CANCELED, getStage());
            assertArchived();
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
            return wizard.getId();
        }

        TaskId getTaskId() {
            return wizard.getState().getTaskId();
        }

        TaskCreation.Stage getStage() {
            return wizard.getState().getStage();
        }

        MemoizingCommandHandler memoizingHandler() {
            return memoizingHandler;
        }

        void assertArchived() {
            final boolean archived = wizard.isArchived();
            final boolean deleted = wizard.isDeleted();
            assertTrue(archived, "Should be archived");
            assertFalse(deleted, "Should not be deleted");
        }

        void dispatch(CommandMessage command) {
            final Command cmd = requestFactory.command()
                                              .create(command);
            final CommandEnvelope envelope = CommandEnvelope.of(cmd);
            PmDispatcher.dispatch(wizard, envelope);
         }

        void startWizard() {
            final StartTaskCreation cmd = StartTaskCreation.newBuilder()
                                                           .setId(getId())
                                                           .setTaskId(newTaskId())
                                                           .build();
            dispatch(cmd);
            clearReceivedCommands();
        }

        void addDescription() {
            final TaskDescription description = TaskDescription.newBuilder()
                                                               .setValue("task for test")
                                                               .build();
            final SetTaskDetails cmd = SetTaskDetails.newBuilder()
                                                     .setId(getId())
                                                     .setDescription(description)
                                                     .build();
            dispatch(cmd);
            clearReceivedCommands();
        }

        void skipLabels() {
            final SkipLabels cmd = SkipLabels.newBuilder()
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
                final Method method = ProcessManager.class.getDeclaredMethod("setCommandBus",
                                                                             CommandBus.class);
                method.setAccessible(true);
                method.invoke(wizard, commandBus);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw illegalStateWithCauseOf(e);
            }
        }

        private CommandBus createCommandBus() {
            BoundedContext emptyContext = BoundedContext.newBuilder()
                                                        .setCommandBus(CommandBus.newBuilder())
                                                        .build();
            CommandBus commandBus = emptyContext.getCommandBus();
            commandBus.register(memoizingHandler);
            return commandBus;
        }

        private static final class MemoizingCommandHandler implements CommandDispatcher<Object> {

            private final Stack<CommandMessage> received = new Stack<>();

            @Override
            public Set<CommandClass> getMessageClasses() {
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
                received.push(envelope.getMessage());
                return MemoizingCommandHandler.class.getName();
            }

            @Override
            public void onError(CommandEnvelope envelope, RuntimeException exception) {
                // NoOp for test.
            }
        }
    }

    private static TaskCreationId newId() {
        return TaskCreationId.newBuilder()
                             .setValue(newUuid())
                             .build();
    }

    private static TaskId newTaskId() {
        return TaskId.newBuilder()
                     .setValue(newUuid())
                     .build();
    }

    private static LabelId newLabelId() {
        return LabelId.newBuilder()
                      .setValue(newUuid())
                      .build();
    }
}
