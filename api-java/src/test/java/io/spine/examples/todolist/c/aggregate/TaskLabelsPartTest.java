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

package io.spine.examples.todolist.c.aggregate;

import com.google.protobuf.Message;
import io.spine.base.CommandMessage;
import io.spine.core.Ack;
import io.spine.core.Command;
import io.spine.core.CommandEnvelope;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabels;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.rejection.CannotAssignLabelToTask;
import io.spine.examples.todolist.c.rejection.CannotRemoveLabelFromTask;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.examples.todolist.repository.TaskLabelsRepository;
import io.spine.grpc.MemoizingObserver;
import io.spine.grpc.StreamObservers;
import io.spine.server.BoundedContext;
import io.spine.server.commandbus.CommandBus;
import io.spine.server.entity.Repository;
import io.spine.testing.client.TestActorRequestFactory;
import io.spine.testing.server.ShardingReset;
import io.spine.testing.server.aggregate.AggregatePartCommandTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.createLabelInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.LABEL_ID;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.TASK_ID;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.removeLabelFromTaskInstance;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
@ExtendWith(ShardingReset.class)
class TaskLabelsPartTest {

    @Nested
    @DisplayName("AssignLabelToTask command should be interpreted by TaskLabelsPart and")
    class AssignLabelToTaskCommand extends TaskLabelsCommandTest<AssignLabelToTask> {

        protected AssignLabelToTaskCommand() {
            super(assignLabelToTaskInstance(TASK_ID, LABEL_ID));
        }

        @Test
        @DisplayName("produce LabelAssignedToTask event")
        void produceEvent() {
            final List<? extends Message> messageList =
                    dispatchCommand(taskLabelsPart, commandEnvelope());

            assertEquals(1, messageList.size());
            assertEquals(LabelAssignedToTask.class, messageList.get(0)
                                                               .getClass());
            final LabelAssignedToTask labelAssignedToTask =
                    (LabelAssignedToTask) messageList.get(0);

            assertEquals(entityId(), labelAssignedToTask.getTaskId());
            assertEquals(labelId, labelAssignedToTask.getLabelId());
        }

        @Test
        @DisplayName("assign a label to the task")
        void testAssignLabelToTask() {
            dispatchCommand(taskLabelsPart, commandEnvelope());

            final TaskLabels state = taskLabelsPart.getState();
            final List<LabelId> labelIds = state.getLabelIdsList()
                                                .getIdsList();
            assertEquals(entityId(), state.getTaskId());
            assertTrue(labelIds.contains(labelId));
        }

        @Test
        @DisplayName("throw CannotAssignLabelToTask rejection " +
                "upon an attempt to assign the label to the deleted task")
        void cannotAssignLabelToDeletedTask() {
            createBasicTask();
            deleteTask();

            assertThrows(CannotAssignLabelToTask.class,
                         () -> taskLabelsPart.handle(message()));
        }

        @Test
        @DisplayName("throw CannotAssignLabelToTask rejection " +
                "upon an attempt to assign the label to the completed task")
        void cannotAssignLabelToCompletedTask() {
            createBasicTask();
            completeTask();

            assertThrows(CannotAssignLabelToTask.class,
                         () -> taskLabelsPart.handle(message()));
        }
    }

    @Nested
    @DisplayName("RemoveLabelFromTask command should be interpreted by TaskLabelsPart and")
    class RemoveLabelFromTaskCommand extends TaskLabelsCommandTest<RemoveLabelFromTask> {

        protected RemoveLabelFromTaskCommand() {
            super(removeLabelFromTaskInstance(TASK_ID, LABEL_ID));
        }

        @Test
        @DisplayName("produce LabelRemovedFromTask event")
        void produceEvent() throws CannotRemoveLabelFromTask {
            createBasicTask();
            assignLabelToTask();

            final List<? extends Message> messageList = taskLabelsPart.handle(message());

            assertEquals(1, messageList.size());
            assertEquals(LabelRemovedFromTask.class, messageList.get(0)
                                                                .getClass());
            final LabelRemovedFromTask labelRemovedFromTask =
                    (LabelRemovedFromTask) messageList.get(0);

            assertEquals(entityId(), labelRemovedFromTask.getTaskId());
            assertEquals(labelId, labelRemovedFromTask.getLabelId());
        }

        @Test
        @DisplayName("remove a label from the task")
        void removeLabelFromTask() {
            createBasicTask();
            assignLabelToTask();
            final List<LabelId> labelIdsBeforeRemove = taskLabelsPart.getState()
                                                                     .getLabelIdsList()
                                                                     .getIdsList();
            assertTrue(labelIdsBeforeRemove.contains(labelId));

            dispatchCommand(taskLabelsPart, commandEnvelope());
            final List<LabelId> labelIdsAfterRemove = taskLabelsPart.getState()
                                                                    .getLabelIdsList()
                                                                    .getIdsList();
            assertTrue(labelIdsAfterRemove.isEmpty());
        }

        @Test
        @DisplayName("throw CannotRemoveLabelFromTask rejection " +
                "upon an attempt to remove the not assigned label")
        void cannotRemoveNotExistingLabel() {
            assertThrows(CannotRemoveLabelFromTask.class,
                         () -> taskLabelsPart.handle(message()));
        }

        @Test
        @DisplayName("throw CannotRemoveLabelFromTask rejection " +
                "upon an attempt to remove the label from the completed task")
        void cannotRemoveLabelFromCompletedTask() {
            createBasicTask();
            assignLabelToTask();
            completeTask();

            assertThrows(CannotRemoveLabelFromTask.class,
                         () -> taskLabelsPart.handle(message()));
        }

        @Test
        @DisplayName("throw CannotRemoveLabelFromTask rejection " +
                "upon an attempt to remove the label from the deleted task")
        void cannotRemoveLabelFromDeletedTask() {
            createBasicTask();
            assignLabelToTask();
            deleteTask();

            assertThrows(CannotRemoveLabelFromTask.class,
                         () -> taskLabelsPart.handle(message()));
        }
    }

    @Nested
    @DisplayName("Multiple AssignLabelToTask commands should")
    class AssignMultipleLabelsTest extends TaskLabelsCommandTest<CreateBasicTask> {

        private final List<LabelId> labelIds = of(
                createLabelId(), createLabelId(), createLabelId(), createLabelId()
        );

        AssignMultipleLabelsTest() {
            super(createTaskInstance());
        }

        @BeforeEach
        @Override
        public void setUp() {
            super.setUp();
            labelIds.forEach(this::createLabel);
            createBasicTask();
            labelIds.forEach(this::assignLabelToTask);
        }

        @Test
        @DisplayName("assign all labels to the task")
        void testContainsAllLabels() {
            final Collection<LabelId> actualLabels = taskLabelsPart.getState()
                                                                   .getLabelIdsList()
                                                                   .getIdsList();
            assertThat(actualLabels, containsInAnyOrder(labelIds.toArray()));
        }
    }

    @SuppressWarnings("PackageVisibleField") // for brevity of descendants.
    private abstract static class TaskLabelsCommandTest<C extends CommandMessage>
            extends AggregatePartCommandTest<TaskId,
                                             C,
                                             TaskLabels,
                                             TaskLabelsPart,
                                             TaskAggregateRoot> {

        private final TestActorRequestFactory requestFactory =
                TestActorRequestFactory.newInstance(getClass());

        MemoizingObserver<Ack> responseObserver;
        CommandBus commandBus;
        TaskLabelsPart taskLabelsPart;
        LabelId labelId;

        protected TaskLabelsCommandTest(C commandMessage) {
            super(TASK_ID, commandMessage);
        }

        @BeforeEach
        @Override
        public void setUp() {
            super.setUp();
            taskLabelsPart = newPart(entityId());
            createLabel();
        }

        @Override
        protected TaskAggregateRoot newRoot(TaskId id) {
            final BoundedContext boundedContext = BoundedContexts.create();
            commandBus = boundedContext.getCommandBus();
            responseObserver = StreamObservers.memoizingObserver();
            labelId = LABEL_ID;
            TaskAggregateRoot root = new TaskAggregateRoot(boundedContext, id);
            return root;
        }

        @Override
        protected TaskLabelsPart newPart(TaskAggregateRoot root) {
            return new TaskLabelsPart(root);
        }

        @Override
        protected Repository<TaskId, TaskLabelsPart> createEntityRepository() {
            return new TaskLabelsRepository();
        }

        private void createLabel() {
            createLabel(labelId);
        }

        protected void createLabel(LabelId labelId) {
            final CreateBasicLabel createLabel = createLabelInstance(labelId);
            final Command createLabelCmd = createNewCommand(createLabel);
            commandBus.post(createLabelCmd, responseObserver);
        }

        void createBasicTask() {
            final CreateBasicTask createTask = createTaskInstance(entityId(), DESCRIPTION);
            final Command createTaskCmd = createNewCommand(createTask);
            commandBus.post(createTaskCmd, responseObserver);
        }

        void deleteTask() {
            final DeleteTask deleteTask = deleteTaskInstance(entityId());
            final Command deleteTaskCmd = createNewCommand(deleteTask);
            commandBus.post(deleteTaskCmd, responseObserver);
        }

        void completeTask() {
            final CompleteTask completeTask = completeTaskInstance(entityId());
            final Command completeTaskCmd = createNewCommand(completeTask);
            commandBus.post(completeTaskCmd, responseObserver);
        }

        void assignLabelToTask() {
            assignLabelToTask(labelId);
        }

        void assignLabelToTask(LabelId labelId) {
            final AssignLabelToTask assignLabelToTask =
                    assignLabelToTaskInstance(entityId(), labelId);
            final Command assignLabelToTaskCmd = createNewCommand(assignLabelToTask);
            dispatchCommand(taskLabelsPart, CommandEnvelope.of(assignLabelToTaskCmd));
        }

        CommandEnvelope commandEnvelope() {
            return envelopeOf(message());
        }

        CommandEnvelope envelopeOf(CommandMessage commandMessage) {
            Command command = createNewCommand(commandMessage);
            CommandEnvelope envelope = CommandEnvelope.of(command);
            return envelope;
        }

        Command createNewCommand(CommandMessage commandMessage) {
            Command command = requestFactory.command()
                                            .create(commandMessage);
            return command;
        }

        static LabelId createLabelId() {
            return LabelId.newBuilder()
                          .setValue(newUuid())
                          .build();
        }
    }
}
