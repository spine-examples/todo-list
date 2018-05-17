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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.change.StringChange;
import io.spine.change.TimestampChange;
import io.spine.client.ActorRequestFactory;
import io.spine.client.TestActorRequestFactory;
import io.spine.core.Command;
import io.spine.core.CommandClass;
import io.spine.core.CommandEnvelope;
import io.spine.core.Event;
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
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.c.commands.TodoCommand;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.protobuf.AnyPacker;
import io.spine.server.commandbus.CommandBus;
import io.spine.server.commandbus.CommandDispatcher;
import io.spine.server.commandstore.CommandStore;
import io.spine.server.procman.CommandRouted;
import io.spine.server.procman.ProcessManager;
import io.spine.server.procman.ProcessManagerDispatcher;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.StorageFactorySwitch;
import io.spine.server.tenant.TenantIndex;
import io.spine.base.Time;
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

import static io.spine.Identifier.newUuid;
import static io.spine.core.CommandClass.of;
import static io.spine.examples.todolist.TaskCreation.Stage.CANCELED;
import static io.spine.examples.todolist.TaskCreation.Stage.COMPLETED;
import static io.spine.examples.todolist.TaskCreation.Stage.CONFIRMATION;
import static io.spine.examples.todolist.TaskCreation.Stage.LABEL_ASSIGNMENT;
import static io.spine.examples.todolist.TaskCreation.Stage.TASK_DEFINITION;
import static io.spine.server.BoundedContext.newName;
import static io.spine.server.storage.StorageFactorySwitch.newInstance;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
            final List<? extends TodoCommand> commands = producesCommands(cmd);
            assertEquals(1, commands.size());
            final TodoCommand producedCommand = commands.get(0);
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
            final List<? extends TodoCommand> produced = producesCommands(cmd);
            assertEquals(3, produced.size());
            final StringChange descriptionChange = StringChange.newBuilder()
                                                               .setNewValue(descriptionValue)
                                                               .build();
            final PriorityChange priorityChange = PriorityChange.newBuilder()
                                                                .setNewValue(priority)
                                                                .build();
            final TimestampChange dueDateChange = TimestampChange.newBuilder()
                                                                 .setNewValue(dueDate)
                                                                 .build();
            assertThat(produced, containsInAnyOrder(
                    UpdateTaskDescription.newBuilder()
                                         .setId(getTaskId())
                                         .setDescriptionChange(descriptionChange)
                                         .build(),
                    UpdateTaskPriority.newBuilder()
                                      .setId(getTaskId())
                                      .setPriorityChange(priorityChange)
                                      .build(),
                    UpdateTaskDueDate.newBuilder()
                                     .setId(getTaskId())
                                     .setDueDateChange(dueDateChange)
                                     .build()
            ));
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
            final List<? extends TodoCommand> producedCommands = producesCommands(cmd);
            final Collection<Matcher<? super TodoCommand>> expectedCommands = ImmutableList.of(
                    instanceOf(AssignLabelToTask.class), // 3 label assignments
                    instanceOf(AssignLabelToTask.class),
                    instanceOf(AssignLabelToTask.class),
                    instanceOf(CreateBasicLabel.class),  // 1 label creation with details updates
                    instanceOf(UpdateLabelDetails.class)
            );
            assertThat(producedCommands, containsInAnyOrder(expectedCommands));
            assertEquals(CONFIRMATION, getStage());
        }

        @Test
        @DisplayName("do nothing if no labels specified")
        void testNoLabels() {
            final AddLabels cmd = AddLabels.newBuilder()
                                           .setId(getId())
                                           .build();
            final List<?> events = dispatch(cmd);
            assertTrue(events.isEmpty());
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
            final List<? extends TodoCommand> produced = producesCommands(cmd);
            assertEquals(1, produced.size());
            final FinalizeDraft expectedCmd = FinalizeDraft.newBuilder()
                                                           .setId(getTaskId())
                                                           .build();
            assertEquals(expectedCmd, produced.get(0));
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
            skipLabels();
        }

        @Test
        @DisplayName("cancel task creation process")
        void testCancelProc() {
            final CancelTaskCreation cmd = CancelTaskCreation.newBuilder()
                                                               .setId(getId())
                                                               .build();
            final List<?> events = dispatch(cmd);
            assertTrue(events.isEmpty());
            assertEquals(CANCELED, getStage());
            assertArchived();
        }
    }

    private abstract static class CommandTest {

        private TaskCreationWizard wizard;

        void setUp() {
            wizard = new TaskCreationWizard(newId());
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

        void assertArchived() {
            final boolean archived = wizard.isArchived();
            final boolean deleted = wizard.isDeleted();
            assertTrue(archived, "Should be archived");
            assertFalse(deleted, "Should not be deleted");
        }

        List<? extends Message> dispatch(TodoCommand command) {
            final Command cmd = requestFactory.command()
                                              .create(command);
            final CommandEnvelope envelope = CommandEnvelope.of(cmd);
            final List<Event> events = ProcessManagerDispatcher.dispatch(wizard, envelope);
            final List<? extends Message> result = events.stream()
                                                         .map(Event::getMessage)
                                                         .map(AnyPacker::<Message>unpack)
                                                         .collect(toList());
            return result;
        }

        List<? extends TodoCommand> producesCommands(TodoCommand source) {
            final List<? extends Message> events = dispatch(source);
            assertFalse(events.isEmpty());
            assertEquals(1, events.size());
            final Message event = events.get(0);
            assertThat(event, instanceOf(CommandRouted.class));
            final CommandRouted commandRouted = (CommandRouted) event;
            final List<Command> commands = commandRouted.getProducedList();
            final List<? extends TodoCommand> result =
                    commands.stream()
                            .map(cmd -> (TodoCommand) AnyPacker.unpack(cmd.getMessage()))
                            .collect(toList());
            return result;
        }

        void startWizard() {
            final StartTaskCreation cmd = StartTaskCreation.newBuilder()
                                                           .setId(getId())
                                                           .setTaskId(newTaskId())
                                                           .build();
            dispatch(cmd);
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
        }

        void skipLabels() {
            final AddLabels cmd = AddLabels.newBuilder()
                                           .setId(getId())
                                           .build();
            dispatch(cmd);
        }

        /**
         * Injects the fake CommandBus instance via Reflection.
         *
         * <p>This is suitable when tested handler posts subsequent commands to a command bus (which
         * doesn't exist at a test time) and you don't care about them.
         */
        private void prepareCommandBus() {
            try {
                final Method method = ProcessManager.class.getDeclaredMethod("setCommandBus",
                                                                             CommandBus.class);
                method.setAccessible(true);
                method.invoke(wizard, mockCommandBus());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw illegalStateWithCauseOf(e);
            }
        }

        private static CommandBus mockCommandBus() {
            final StorageFactorySwitch storageSwitch =
                    newInstance(newName(TaskCreationWizardTest.class.getSimpleName()), false);
            final StorageFactory storageFactory = storageSwitch.get();
            final TenantIndex tenantIndex = TenantIndex.Factory.singleTenant();
            final CommandStore commandStore = new CommandStore(storageFactory, tenantIndex);
            final CommandBus commandBus = CommandBus.newBuilder()
                                                    .setCommandStore(commandStore)
                                                    .build();
            commandBus.register(new MockCommandHandler());
            return commandBus;
        }

        private static final class MockCommandHandler implements CommandDispatcher<Object> {

            @Override
            public Set<CommandClass> getMessageClasses() {
                return ImmutableSet.of(
                        of(CreateDraft.class),
                        of(UpdateTaskDueDate.class),
                        of(UpdateTaskPriority.class),
                        of(UpdateTaskDescription.class),
                        of(CreateBasicLabel.class),
                        of(UpdateLabelDetails.class),
                        of(AssignLabelToTask.class),
                        of(FinalizeDraft.class)
                );
            }

            @Override
            public Object dispatch(CommandEnvelope envelope) {
                return MockCommandHandler.class.getName();
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
