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

package org.spine3.examples.todolist.c.aggregates;

import com.google.common.base.Throwables;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.base.Commands;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsUpdateFailed;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskLabels;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelCreated;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.failures.CannotAssignLabelToTask;
import org.spine3.examples.todolist.c.failures.CannotRemoveLabelFromTask;
import org.spine3.examples.todolist.c.failures.CannotUpdateLabelDetails;
import org.spine3.examples.todolist.c.failures.Failures;
import org.spine3.examples.todolist.context.TodoListBoundedContext;
import org.spine3.examples.todolist.testdata.TestResponseObserver;
import org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory;
import org.spine3.server.command.CommandBus;
import org.spine3.test.CommandTest;

import java.util.List;

import static com.google.protobuf.Any.pack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Commands.create;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.LABEL_ID;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.TASK_ID;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.removeLabelFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.UPDATED_LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.createLabelInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.updateLabelDetailsInstance;

/**
 * @author Illia Shepilov
 */
public class TaskLabelsPartTest {

    private static final CommandContext COMMAND_CONTEXT = createCommandContext();

    private TestResponseObserver responseObserver;
    private CommandBus commandBus;
    private TaskLabelsPart taskLabelsPart;

    @BeforeEach
    public void setUp() {
        commandBus = TodoListBoundedContext.getCommandBus();
        responseObserver = new TestResponseObserver();
        taskLabelsPart = createTaskLabelsPart(TASK_ID);
    }

    private static TaskId createTaskId() {
        final TaskId result = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        return result;
    }

    private static TaskLabelsPart createTaskLabelsPart(TaskId taskId) {
        return new TaskLabelsPart(taskId);
    }

    @Nested
    @DisplayName("CreateBasicLabel command")
    class CreateBasicLabelCommand extends CommandTest<CreateBasicLabel> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("produces LabelCreated event")
        public void producesLabel() {
            final CreateBasicLabel createLabelCmd = createLabelInstance();
            final List<? extends Message> messageList =
                    taskLabelsPart.dispatchForTest(createLabelCmd, COMMAND_CONTEXT);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(LabelCreated.class, messageList.get(0)
                                                        .getClass());

            final LabelCreated labelCreated = (LabelCreated) messageList.get(0);

            assertEquals(TestTaskLabelCommandFactory.LABEL_ID, labelCreated.getId());
            assertEquals(LABEL_TITLE, labelCreated.getDetails()
                                                  .getTitle());
        }

        @Test
        @DisplayName("creates the basic label")
        public void createsLabel() {
            final CreateBasicLabel createLabelCmd = createLabelInstance();
            taskLabelsPart.dispatchForTest(createLabelCmd, COMMAND_CONTEXT);

            final TaskLabels state = taskLabelsPart.getState();
            final List<TaskLabel> labels = state.getLabelsList();
            assertEquals(1, labels.size());

            final TaskLabel label = labels.get(0);
            assertEquals(TestTaskLabelCommandFactory.LABEL_ID, label.getId());
            assertEquals(LabelColor.GRAY, label.getColor());
            assertEquals(LABEL_TITLE, label.getTitle());
        }
    }

    @Nested
    @DisplayName("AssignLabelToTask command")
    class AssignLabelToTaskCommand extends CommandTest<AssignLabelToTask> {

        private TaskId taskId;

        @BeforeEach
        @Override
        public void setUp() {
            taskId = createTaskId();
            commandBus = TodoListBoundedContext.getCommandBus();
            responseObserver = new TestResponseObserver();
            taskLabelsPart = createTaskLabelsPart(taskId);
        }

        @Test
        @DisplayName("produces LabelAssignedToTask event")
        public void producesEvent() {
            final AssignLabelToTask assignLabelToTaskCmd = assignLabelToTaskInstance(taskId, LABEL_ID);
            final List<? extends Message> messageList =
                    taskLabelsPart.dispatchForTest(assignLabelToTaskCmd, COMMAND_CONTEXT);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(LabelAssignedToTask.class, messageList.get(0)
                                                               .getClass());
            final LabelAssignedToTask labelAssignedToTask = (LabelAssignedToTask) messageList.get(0);

            assertEquals(taskId, labelAssignedToTask.getTaskId());
            assertEquals(LABEL_ID, labelAssignedToTask.getLabelId());
        }

        @Test
        @DisplayName("cannot assign label to deleted task")
        public void cannotAssignLabelToDeletedTask() {
            try {
                final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
                final Command createTaskCmd = create(createTask, COMMAND_CONTEXT);
                commandBus.post(createTaskCmd, responseObserver);

                final DeleteTask deleteTask = deleteTaskInstance(taskId);
                final Command deleteTaskCmd = create(deleteTask, COMMAND_CONTEXT);
                commandBus.post(deleteTaskCmd, responseObserver);

                final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, LABEL_ID);
                final Command assignLabelToTaskCmd = create(assignLabelToTask, COMMAND_CONTEXT);
                commandBus.post(assignLabelToTaskCmd, responseObserver);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotAssignLabelToTask);
            }
        }

        @Test
        @DisplayName("cannot assign label to completed task")
        public void cannotAssignLabelToCompletedTask() {
            try {
                final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
                final Command createTaskCmd = create(createTask, COMMAND_CONTEXT);
                commandBus.post(createTaskCmd, responseObserver);

                final CompleteTask completeTask = completeTaskInstance(taskId);
                final Command completeTaskCmd = create(completeTask, COMMAND_CONTEXT);
                commandBus.post(completeTaskCmd, responseObserver);

                final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, LABEL_ID);
                final Command assignLabelToTaskCmd = Commands.create(assignLabelToTask, COMMAND_CONTEXT);
                commandBus.post(assignLabelToTaskCmd, responseObserver);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotAssignLabelToTask);
            }
        }
    }

    @Nested
    @DisplayName("RemoveLabelFromTask command")
    class RemoveLabelFromTaskCommand extends CommandTest<RemoveLabelFromTask> {

        private TaskId taskId;

        @BeforeEach
        @Override
        public void setUp() {
            taskId = createTaskId();
            commandBus = TodoListBoundedContext.getCommandBus();
            responseObserver = new TestResponseObserver();
            taskLabelsPart = createTaskLabelsPart(taskId);
        }

        @Test
        @DisplayName("produces LabelRemovedFromTask event")
        public void producesEvent() {
            final RemoveLabelFromTask removeLabelFromTaskCmd = removeLabelFromTaskInstance();
            final List<? extends Message> messageList =
                    taskLabelsPart.dispatchForTest(removeLabelFromTaskCmd, COMMAND_CONTEXT);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(LabelRemovedFromTask.class, messageList.get(0)
                                                                .getClass());
            final LabelRemovedFromTask labelRemovedFromTask = (LabelRemovedFromTask) messageList.get(0);

            assertEquals(TASK_ID, labelRemovedFromTask.getTaskId());
            assertEquals(LABEL_ID, labelRemovedFromTask.getLabelId());
        }

        @Test
        @DisplayName("cannot remove label from completed task")
        public void cannotRemoveLabelFromCompletedTask() {
            try {
                final CreateBasicTask createTask = createTaskInstance();
                final Command createTaskCmd = create(createTask, COMMAND_CONTEXT);
                commandBus.post(createTaskCmd, responseObserver);

                final CompleteTask completeTask = completeTaskInstance();
                final Command completeTaskCmd = create(completeTask, COMMAND_CONTEXT);
                commandBus.post(completeTaskCmd, responseObserver);

                final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance();
                final Command removeLabelFromTaskCmd = create(removeLabelFromTask, COMMAND_CONTEXT);
                commandBus.post(removeLabelFromTaskCmd, responseObserver);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotRemoveLabelFromTask);
            }
        }

        @Test
        @DisplayName("cannot remove label from deleted task")
        public void cannotRemoveLabelFromDeletedTask() {
            try {
                final CreateBasicTask createTask = createTaskInstance();
                final Command createTaskCmd = create(createTask, COMMAND_CONTEXT);
                commandBus.post(createTaskCmd, responseObserver);

                final DeleteTask deleteTask = deleteTaskInstance();
                final Command deleteTaskCmd = create(deleteTask, COMMAND_CONTEXT);
                commandBus.post(deleteTaskCmd, responseObserver);

                final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance();
                final Command removeLabelFromTaskCmd = create(removeLabelFromTask, COMMAND_CONTEXT);
                commandBus.post(removeLabelFromTaskCmd, responseObserver);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotRemoveLabelFromTask);
            }
        }
    }

    @Nested
    @DisplayName("UpdateLabelDetails command")
    class UpdateLabelDetailsCommand extends CommandTest<UpdateLabelDetails> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("produces LabelDetailsUpdated event")
        public void producesEvent() {
            final CreateBasicLabel createBasicLabel = createLabelInstance();
            taskLabelsPart.dispatchForTest(createBasicLabel, COMMAND_CONTEXT);

            final UpdateLabelDetails updateLabelDetailsCmd = updateLabelDetailsInstance();
            final List<? extends Message> messageList =
                    taskLabelsPart.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(LabelDetailsUpdated.class, messageList.get(0)
                                                               .getClass());

            final LabelDetailsUpdated labelDetailsUpdated = (LabelDetailsUpdated) messageList.get(0);
            final LabelDetails details = labelDetailsUpdated.getLabelDetailsChange()
                                                            .getNewDetails();
            assertEquals(LABEL_ID, labelDetailsUpdated.getLabelId());
            assertEquals(LabelColor.GREEN, details.getColor());
            assertEquals(UPDATED_LABEL_TITLE, details.getTitle());
        }

        @Test
        @DisplayName("updates label details twice")
        public void updatesLabelDetailsTwice() {
            final CreateBasicLabel createBasicLabel = createLabelInstance();
            taskLabelsPart.dispatchForTest(createBasicLabel, COMMAND_CONTEXT);

            UpdateLabelDetails updateLabelDetailsCmd = updateLabelDetailsInstance();
            taskLabelsPart.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

            TaskLabels state = taskLabelsPart.getState();
            List<TaskLabel> labels = state.getLabelsList();
            assertEquals(1, labels.size());

            TaskLabel label = labels.get(0);
            assertEquals(TestTaskLabelCommandFactory.LABEL_ID, label.getId());
            assertEquals(LabelColor.GREEN, label.getColor());
            assertEquals(UPDATED_LABEL_TITLE, label.getTitle());

            final LabelColor previousLabelColor = LabelColor.GREEN;
            final LabelDetails previousLabelDetails = LabelDetails.newBuilder()
                                                                  .setTitle(UPDATED_LABEL_TITLE)
                                                                  .setColor(previousLabelColor)
                                                                  .build();
            final LabelColor updatedLabelColor = LabelColor.BLUE;
            final String updatedTitle = "updated title";
            final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                             .setColor(updatedLabelColor)
                                                             .setTitle(updatedTitle)
                                                             .build();
            updateLabelDetailsCmd = updateLabelDetailsInstance(LABEL_ID, previousLabelDetails, newLabelDetails);
            taskLabelsPart.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

            state = taskLabelsPart.getState();
            labels = state.getLabelsList();
            assertEquals(1, labels.size());

            label = labels.get(0);
            assertEquals(TestTaskLabelCommandFactory.LABEL_ID, label.getId());
            assertEquals(updatedLabelColor, label.getColor());
            assertEquals(updatedTitle, label.getTitle());
        }

        @Test
        @DisplayName("cannot update label details when label details does not match expected")
        public void cannotUpdateLabelDetails() {
            final CreateBasicLabel createBasicLabel = createLabelInstance();
            taskLabelsPart.dispatchForTest(createBasicLabel, COMMAND_CONTEXT);

            final LabelDetails expectedLabelDetails = LabelDetails.newBuilder()
                                                                  .setColor(LabelColor.BLUE)
                                                                  .setTitle(LABEL_TITLE)
                                                                  .build();
            final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                             .setColor(LabelColor.RED)
                                                             .setTitle(UPDATED_LABEL_TITLE)
                                                             .build();
            try {
                final UpdateLabelDetails updateLabelDetailsCmd =
                        updateLabelDetailsInstance(LABEL_ID, expectedLabelDetails, newLabelDetails);
                taskLabelsPart.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateLabelDetails);

                @SuppressWarnings("ConstantConditions")
                final Failures.CannotUpdateLabelDetails cannotUpdateLabelDetails =
                        ((CannotUpdateLabelDetails) cause).getFailure();
                final LabelDetailsUpdateFailed labelDetailsUpdateFailed = cannotUpdateLabelDetails.getUpdateFailed();
                final TaskLabelId actualLabelId = labelDetailsUpdateFailed.getFailedCommand()
                                                                          .getLabelId();
                assertEquals(TestTaskLabelCommandFactory.LABEL_ID, actualLabelId);

                final ValueMismatch mismatch = labelDetailsUpdateFailed.getLabelDetailsMismatch();
                assertEquals(pack(expectedLabelDetails), mismatch.getExpected());
                assertEquals(pack(newLabelDetails), mismatch.getNewValue());

                final LabelDetails actualLabelDetails = LabelDetails.newBuilder()
                                                                    .setColor(LabelColor.GRAY)
                                                                    .setTitle(LABEL_TITLE)
                                                                    .build();
                assertEquals(pack(actualLabelDetails), mismatch.getActual());
            }
        }
    }
}
