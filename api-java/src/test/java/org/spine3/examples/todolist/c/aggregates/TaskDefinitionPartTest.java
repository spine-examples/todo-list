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
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.DescriptionUpdateFailed;
import org.spine3.examples.todolist.PriorityUpdateFailed;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskDueDateUpdateFailed;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskPriorityValue;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.c.commands.ReopenTask;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.c.events.TaskCompleted;
import org.spine3.examples.todolist.c.events.TaskCreated;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDraftCreated;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.examples.todolist.c.failures.CannotCompleteTask;
import org.spine3.examples.todolist.c.failures.CannotDeleteTask;
import org.spine3.examples.todolist.c.failures.CannotFinalizeDraft;
import org.spine3.examples.todolist.c.failures.CannotReopenTask;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDescription;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDueDate;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskPriority;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskWithInappropriateDescription;
import org.spine3.examples.todolist.c.failures.Failures;
import org.spine3.protobuf.Timestamps;
import org.spine3.test.CommandTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.TaskStatus.COMPLETED;
import static org.spine3.examples.todolist.TaskStatus.DELETED;
import static org.spine3.examples.todolist.TaskStatus.DRAFT;
import static org.spine3.examples.todolist.TaskStatus.FINALIZED;
import static org.spine3.examples.todolist.TaskStatus.OPEN;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DUE_DATE;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.TASK_ID;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static org.spine3.protobuf.AnyPacker.unpack;

/**
 * @author Illia Shepilov
 */
public class TaskDefinitionPartTest {

    private static final CommandContext COMMAND_CONTEXT = createCommandContext();
    private TaskDefinitionPart aggregate;
    private static final String EXPECTED_DESCRIPTION = "description";

    @BeforeEach
    public void setUp() {
        aggregate = new TaskDefinitionPart(TASK_ID);
    }

    @Nested
    @DisplayName("CreateBasicTask command")
    class CreateBasicTaskCommand extends CommandTest<CreateBasicTask> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("produces TaskCreated event")
        public void producesEvent() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            final List<? extends com.google.protobuf.Message> messageList =
                    aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            assertNotNull(aggregate.getState()
                                   .getCreated());
            assertNotNull(aggregate.getId());

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(TaskCreated.class, messageList.get(0)
                                                       .getClass());
            final TaskCreated taskCreated = (TaskCreated) messageList.get(0);

            assertEquals(TASK_ID, taskCreated.getId());
            assertEquals(DESCRIPTION, taskCreated.getDetails()
                                                 .getDescription());
        }

        @Test
        @DisplayName("")
        public void record_modification_timestamp() throws InterruptedException {
            CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            TaskDefinition state = aggregate.getState();
            final Timestamp firstStateCreationTime = state.getCreated();

            assertEquals(TASK_ID, state.getId());

            Thread.sleep(1000);

            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            state = aggregate.getState();
            final Timestamp secondStateCreationTime = state.getCreated();

            assertEquals(TASK_ID, state.getId());
            assertTrue(Timestamps.isLaterThan(secondStateCreationTime, firstStateCreationTime));
        }
    }

    @Nested
    @DisplayName("UpdateTaskDescription command")
    class UpdateTaskDescriptionCommand extends CommandTest<UpdateTaskDescription> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("produces TaskDescriptionUpdated event")
        public void producesEvent() {
            final CreateBasicTask createBasicTask = createTaskInstance();
            aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);
            final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance();
            final List<? extends Message> messageList =
                    aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(TaskDescriptionUpdated.class, messageList.get(0)
                                                                  .getClass());
            final TaskDescriptionUpdated taskDescriptionUpdated = (TaskDescriptionUpdated) messageList.get(0);

            assertEquals(TASK_ID, taskDescriptionUpdated.getTaskId());
            final String newDescription = taskDescriptionUpdated.getDescriptionChange()
                                                                .getNewValue();
            assertEquals(DESCRIPTION, newDescription);
        }

        @Test
        @DisplayName("cannot update task with inappropriate description")
        public void cannotUpdateTaskDescription() {
            try {
                final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance(TASK_ID, "", ".");
                aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateTaskWithInappropriateDescription);
            }
        }

        @Test
        @DisplayName("cannot update description for deleted task")
        public void cannotUpdateDeletedTaskDescription() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            try {
                final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance();
                assertThrows(CannotUpdateTaskDescription.class, () ->
                        aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT));
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateTaskDescription);
            }
        }

        @Test
        @DisplayName("cannot update description for the completed task")
        public void cannotUpdateCompletedTaskDescription() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            try {
                final UpdateTaskDescription updateTaskDescriptionCmd = updateTaskDescriptionInstance();
                aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateTaskDescription);
            }
        }

        @Test
        @DisplayName("updates task description")
        public void updatesDescription() {
            final String newDescription = "new description.";
            final CreateBasicTask createBasicTask = createTaskInstance();
            aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

            final UpdateTaskDescription updateTaskDescriptionCmd =
                    updateTaskDescriptionInstance(TASK_ID, DESCRIPTION, newDescription);
            aggregate.dispatchForTest(updateTaskDescriptionCmd, COMMAND_CONTEXT);
            final TaskDefinition state = aggregate.getState();

            assertEquals(TASK_ID, state.getId());
            assertEquals(newDescription, state.getDescription());
        }

        @Test
        @DisplayName("produces throwing CannotUpdateTaskDescription failure")
        public void producesFailure() {
            final CreateBasicTask createBasicTask = createTaskInstance();
            aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

            final String expectedValue = EXPECTED_DESCRIPTION;
            final String newValue = "update description";
            final String actualValue = createBasicTask.getDescription();

            try {
                final UpdateTaskDescription updateTaskDescription =
                        updateTaskDescriptionInstance(TASK_ID, expectedValue, newValue);
                aggregate.dispatchForTest(updateTaskDescription, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateTaskDescription);

                @SuppressWarnings("ConstantConditions")
                final Failures.CannotUpdateTaskDescription failure = ((CannotUpdateTaskDescription) cause).getFailure();
                final DescriptionUpdateFailed descriptionUpdateFailed = failure.getUpdateFailed();
                final TaskId actualTaskId = descriptionUpdateFailed.getFailedCommand()
                                                                   .getTaskId();
                assertEquals(TASK_ID, actualTaskId);

                final StringValue expectedStringValue = StringValue.newBuilder()
                                                                   .setValue(expectedValue)
                                                                   .build();
                final StringValue actualStringValue = StringValue.newBuilder()
                                                                 .setValue(actualValue)
                                                                 .build();
                final StringValue newStringValue = StringValue.newBuilder()
                                                              .setValue(newValue)
                                                              .build();

                final ValueMismatch mismatch = descriptionUpdateFailed.getDescriptionMismatch();
                assertEquals(expectedStringValue, unpack(mismatch.getExpected()));
                assertEquals(actualStringValue, unpack(mismatch.getActual()));
                assertEquals(newStringValue, unpack(mismatch.getNewValue()));
            }
        }
    }

    @Nested
    @DisplayName("UpdateTaskDueDate commamnd")
    class UpdateTaskDueDateCommand extends CommandTest<UpdateTaskDueDate> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("cannot update due date for the completed task")
        public void cannotUpdateCompletedTaskDueDate() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            try {
                final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance();
                aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateTaskDueDate);
            }
        }

        @Test
        @DisplayName("cannot update due date for the deleted task")
        public void cannotUpdateDeletedTaskDueDate() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            try {
                final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance();
                aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateTaskDueDate);
            }
        }

        @Test
        @DisplayName("produces TaskDueDateUpdated event")
        public void producesEvent() {
            final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance();
            final List<? extends com.google.protobuf.Message> messageList =
                    aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(TaskDueDateUpdated.class, messageList.get(0)
                                                              .getClass());
            final TaskDueDateUpdated taskDueDateUpdated = (TaskDueDateUpdated) messageList.get(0);

            assertEquals(TASK_ID, taskDueDateUpdated.getTaskId());
            final Timestamp newDueDate = taskDueDateUpdated.getDueDateChange()
                                                           .getNewValue();
            assertEquals(DUE_DATE, newDueDate);
        }

        @Test
        @DisplayName("updates task due date")
        public void updatesDueDate() {
            final Timestamp updatedDueDate = Timestamps.getCurrentTime();
            final CreateBasicTask createBasicTask = createTaskInstance();
            aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

            final UpdateTaskDueDate updateTaskDueDateCmd =
                    updateTaskDueDateInstance(TASK_ID, Timestamp.getDefaultInstance(), updatedDueDate);
            aggregate.dispatchForTest(updateTaskDueDateCmd, COMMAND_CONTEXT);
            final TaskDefinition state = aggregate.getState();

            assertEquals(TASK_ID, state.getId());
            assertEquals(updatedDueDate, state.getDueDate());
        }

        @Test
        @DisplayName("produces throwing CannotUpdateTaskDueDate failure")
        public void producesFailure() {
            final CreateBasicTask createBasicTask = createTaskInstance();
            aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

            final Timestamp expectedDueDate = Timestamps.getCurrentTime();
            final Timestamp newDueDate = Timestamps.getCurrentTime();

            try {
                final UpdateTaskDueDate updateTaskDueDate =
                        updateTaskDueDateInstance(TASK_ID, expectedDueDate, newDueDate);
                final List<? extends com.google.protobuf.Message> messageList =
                        aggregate.dispatchForTest(updateTaskDueDate, COMMAND_CONTEXT);

                final int expectedListSize = 1;
                assertEquals(expectedListSize, messageList.size());
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateTaskDueDate);

                @SuppressWarnings("ConstantConditions")
                final Failures.CannotUpdateTaskDueDate cannotUpdateTaskDueDate =
                        ((CannotUpdateTaskDueDate) cause).getFailure();

                final TaskDueDateUpdateFailed dueDateUpdateFailed = cannotUpdateTaskDueDate.getUpdateFailed();
                final TaskId actualTaskId = dueDateUpdateFailed.getFailedCommand()
                                                               .getTaskId();
                assertEquals(TASK_ID, actualTaskId);

                final ValueMismatch mismatch = dueDateUpdateFailed.getDueDateMismatch();

                assertEquals(newDueDate, unpack(mismatch.getNewValue()));
                assertEquals(expectedDueDate, unpack(mismatch.getExpected()));

                final Timestamp actualDueDate = Timestamp.getDefaultInstance();
                assertEquals(actualDueDate, unpack(mismatch.getActual()));
            }
        }
    }

    @Nested
    @DisplayName("UpdateTaskPriority command")
    class UpdateTaskPriorityCommand extends CommandTest<UpdateTaskPriority> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("cannot update priority for the deleted task")
        public void cannotUpdateDeletedTaskPriority() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            try {
                final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance();
                aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateTaskPriority);
            }
        }

        @Test
        @DisplayName("cannot update priority for the completed task")
        public void cannotUpdateCompletedTaskPriority() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            try {
                final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance();
                aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateTaskPriority);
            }
        }

        @Test
        @DisplayName("produces TaskPriorityUpdated event")
        public void producesEvent() {
            final CreateBasicTask createBasicTask = createTaskInstance();
            aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

            final UpdateTaskPriority updateTaskPriorityCmd = updateTaskPriorityInstance();
            final List<? extends com.google.protobuf.Message> messageList =
                    aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(TaskPriorityUpdated.class, messageList.get(0)
                                                               .getClass());
            final TaskPriorityUpdated taskPriorityUpdated = (TaskPriorityUpdated) messageList.get(0);

            assertEquals(TASK_ID, taskPriorityUpdated.getTaskId());
            final TaskPriority newPriority = taskPriorityUpdated.getPriorityChange()
                                                                .getNewValue();
            assertEquals(TaskPriority.HIGH, newPriority);
        }

        @Test
        @DisplayName("updates task priority")
        public void updatesPriority() {
            final TaskPriority updatedPriority = TaskPriority.HIGH;
            final CreateBasicTask createBasicTask = createTaskInstance();
            aggregate.dispatchForTest(createBasicTask, COMMAND_CONTEXT);

            final UpdateTaskPriority updateTaskPriorityCmd =
                    updateTaskPriorityInstance(TASK_ID, TaskPriority.TP_UNDEFINED, updatedPriority);
            aggregate.dispatchForTest(updateTaskPriorityCmd, COMMAND_CONTEXT);
            final TaskDefinition state = aggregate.getState();

            assertEquals(TASK_ID, state.getId());
            assertEquals(updatedPriority, state.getPriority());
        }

        @Test
        @DisplayName("produces throwing CannotUpdateTaskPriority failure")
        public void producesFailure() {
            try {
                final UpdateTaskPriority updateTaskPriority =
                        updateTaskPriorityInstance(TASK_ID, TaskPriority.LOW, TaskPriority.HIGH);
                aggregate.dispatchForTest(updateTaskPriority, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateTaskPriority);

                @SuppressWarnings("ConstantConditions")
                final Failures.CannotUpdateTaskPriority cannotUpdateTaskPriority =
                        ((CannotUpdateTaskPriority) cause).getFailure();
                final PriorityUpdateFailed priorityUpdateFailed = cannotUpdateTaskPriority.getUpdateFailed();
                final TaskId actualTaskId = priorityUpdateFailed.getFailedCommand()
                                                                .getTaskId();
                assertEquals(TASK_ID, actualTaskId);

                final ValueMismatch mismatch = priorityUpdateFailed.getPriorityMismatch();
                final TaskPriorityValue expectedValue = TaskPriorityValue.newBuilder()
                                                                         .setPriorityValue(TaskPriority.LOW)
                                                                         .build();
                final TaskPriorityValue actualValue = TaskPriorityValue.newBuilder()
                                                                       .setPriorityValue(TaskPriority.TP_UNDEFINED)
                                                                       .build();
                final TaskPriorityValue newValue = TaskPriorityValue.newBuilder()
                                                                    .setPriorityValue(TaskPriority.HIGH)
                                                                    .build();
                assertEquals(actualValue, unpack(mismatch.getActual()));
                assertEquals(expectedValue, unpack(mismatch.getExpected()));
                assertEquals(newValue, unpack(mismatch.getNewValue()));
            }
        }
    }

    @Nested
    @DisplayName("CompleteTask command")
    class CompleteTaskCommand extends CommandTest<CompleteTask> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("produces TaskCompleted event")
        public void producesEvent() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            final List<? extends com.google.protobuf.Message> messageList =
                    aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(TaskCompleted.class, messageList.get(0)
                                                         .getClass());
            final TaskCompleted taskCompleted = (TaskCompleted) messageList.get(0);

            assertEquals(TASK_ID, taskCompleted.getTaskId());
        }

        @Test
        @DisplayName("completes the task")
        public void completesTheTask() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
            final TaskDefinition state = aggregate.getState();

            assertEquals(TASK_ID, state.getId());
            assertEquals(COMPLETED, state.getTaskStatus());
        }

        @Test
        @DisplayName("cannot complete deleted task")
        public void cannotCompleteDeletedTask() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            try {
                final CompleteTask completeTaskCmd = completeTaskInstance();
                aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotCompleteTask);
            }
        }

        @Test
        @DisplayName("cannot complete task in draft state")
        public void cannotCompleteDraft() {
            final CreateDraft createDraftCmd = createDraftInstance();
            aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

            try {
                final CompleteTask completeTaskCmd = completeTaskInstance();
                aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotCompleteTask);
            }
        }
    }

    @Nested
    @DisplayName("DeleteTask command")
    class DeleteTaskCommand extends CommandTest<DeleteTask> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("deletes task")
        public void deletesTask() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
            final TaskDefinition state = aggregate.getState();

            assertEquals(TASK_ID, state.getId());
            assertEquals(DELETED, state.getTaskStatus());
        }

        @Test
        @DisplayName("cannot delete already deleted task")
        public void cannotDeleteAlreadyDeletedTask() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            try {
                aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotDeleteTask);
            }
        }

        @Test
        @DisplayName("produces TaskDeleted event")
        public void producesEvent() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            final List<? extends com.google.protobuf.Message> messageList =
                    aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(TaskDeleted.class, messageList.get(0)
                                                       .getClass());
            final TaskDeleted taskDeleted = (TaskDeleted) messageList.get(0);

            assertEquals(TASK_ID, taskDeleted.getTaskId());
        }
    }

    @Nested
    @DisplayName("FinalizeDraft command")
    class FinalizeDraftCommand extends CommandTest<FinalizeDraft> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("finalizes task")
        public void finalizesTask() {
            final CreateDraft createDraftCmd = createDraftInstance();
            aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

            final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance();
            TaskDefinition state = aggregate.getState();

            assertEquals(TASK_ID, state.getId());
            assertEquals(DRAFT, state.getTaskStatus());

            aggregate.dispatchForTest(finalizeDraftCmd, COMMAND_CONTEXT);
            state = aggregate.getState();

            assertEquals(TASK_ID, state.getId());
            assertEquals(FINALIZED, state.getTaskStatus());
        }

        @Test
        @DisplayName("cannot finalize deleted task")
        public void cannotFinalizeDeletedTask() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            try {
                final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance();
                aggregate.dispatchForTest(finalizeDraftCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotFinalizeDraft);
            }
        }

        @Test
        @DisplayName("cannot finalize task when task state is not draft")
        public void cannotFinalizeNotDraftTask() {
            try {
                final FinalizeDraft finalizeDraftCmd = finalizeDraftInstance();
                aggregate.dispatchForTest(finalizeDraftCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotFinalizeDraft);
            }
        }
    }

    @Nested
    @DisplayName("CreateDraft command")
    class CreateDraftCommand extends CommandTest<CreateDraft> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("produces TaskDraftCreated event")
        public void producesEvent() {
            final CreateDraft createDraftCmd = createDraftInstance();
            final List<? extends com.google.protobuf.Message> messageList =
                    aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

            final int expectedListSize = 1;
            assertEquals(expectedListSize, messageList.size());
            assertEquals(TaskDraftCreated.class, messageList.get(0)
                                                            .getClass());
            final TaskDraftCreated taskDraftCreated = (TaskDraftCreated) messageList.get(0);

            assertEquals(TASK_ID, taskDraftCreated.getId());
        }

        @Test
        @DisplayName("creates draft")
        public void createsDraft() {
            final CreateDraft createDraftCmd = createDraftInstance();
            aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);
            final TaskDefinition state = aggregate.getState();

            assertEquals(TASK_ID, state.getId());
            assertEquals(DRAFT, state.getTaskStatus());
        }
    }

    @Nested
    @DisplayName("ReopenTask command")
    class ReopenTaskCommand extends CommandTest<ReopenTask> {

        @Override
        protected void setUp() {

        }

        @Test
        @DisplayName("cannot reopen not completed task")
        public void cannotReopenNotCompletedTask() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            try {
                final ReopenTask reopenTaskCmd = reopenTaskInstance();
                aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotReopenTask);
            }
        }

        @Test
        @DisplayName("reopens completed task")
        public void reopensTask() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final CompleteTask completeTaskCmd = completeTaskInstance();
            aggregate.dispatchForTest(completeTaskCmd, COMMAND_CONTEXT);

            TaskDefinition state = aggregate.getState();
            assertEquals(TASK_ID, state.getId());
            assertEquals(COMPLETED, state.getTaskStatus());

            final ReopenTask reopenTaskCmd = reopenTaskInstance();
            aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);

            state = aggregate.getState();
            assertEquals(TASK_ID, state.getId());
            assertEquals(OPEN, state.getTaskStatus());
        }

        @Test
        @DisplayName("cannot reopen deleted task")
        public void cannotReopenDeletedTask() {
            final CreateBasicTask createTaskCmd = createTaskInstance();
            aggregate.dispatchForTest(createTaskCmd, COMMAND_CONTEXT);

            final DeleteTask deleteTaskCmd = deleteTaskInstance();
            aggregate.dispatchForTest(deleteTaskCmd, COMMAND_CONTEXT);

            final ReopenTask reopenTaskCmd = reopenTaskInstance();
            aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);

            final TaskDefinition state = aggregate.getState();

            assertEquals(TASK_ID, state.getId());
            assertEquals(OPEN, state.getTaskStatus());
        }

        @Test
        @DisplayName("cannot reopen task in draft state")
        public void cannotReopenDraft() {
            final CreateDraft createDraftCmd = createDraftInstance();
            aggregate.dispatchForTest(createDraftCmd, COMMAND_CONTEXT);

            try {
                final ReopenTask reopenTaskCmd = reopenTaskInstance();
                aggregate.dispatchForTest(reopenTaskCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotReopenTask);
            }
        }
    }
}
