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

package org.spine3.examples.todolist.c.aggregate.definition;

import com.google.common.base.Throwables;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.base.Command;
import org.spine3.base.Commands;
import org.spine3.base.Event;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.c.aggregate.TaskAggregateRoot;
import org.spine3.examples.todolist.c.aggregate.TaskDefinitionPart;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.c.events.LabelledTaskRestored;
import org.spine3.examples.todolist.c.failures.CannotRestoreDeletedTask;
import org.spine3.examples.todolist.context.TodoListBoundedContext;
import org.spine3.examples.todolist.testdata.TestResponseObserver;
import org.spine3.protobuf.AnyPacker;
import org.spine3.server.BoundedContext;
import org.spine3.server.commandbus.CommandBus;
import org.spine3.server.event.EventStreamQuery;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.spine3.examples.todolist.TaskStatus.DELETED;
import static org.spine3.examples.todolist.TaskStatus.OPEN;
import static org.spine3.examples.todolist.testdata.TestLabelCommandFactory.createLabelInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.LABEL_ID;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("RestoreDeletedTask command should be interpreted by TaskDefinitionPart and")
public class RestoreDeletedTaskTest extends TaskDefinitionCommandTest<RestoreDeletedTask> {

    private TestResponseObserver responseObserver;
    private BoundedContext boundedContext;
    private CommandBus commandBus;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        responseObserver = new TestResponseObserver();
        boundedContext = TodoListBoundedContext.createTestInstance();
        TaskAggregateRoot.injectBoundedContext(boundedContext);

        commandBus = boundedContext.getCommandBus();
        aggregate = new TaskDefinitionPart(TaskAggregateRoot.get(taskId));
    }

    @Test
    @DisplayName("produce LabelledTaskRestored event")
    public void produceEvent() {
        final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
        final Command createTaskCmd = Commands.createCommand(createTask, commandContext);
        commandBus.post(createTaskCmd, responseObserver);

        final CreateBasicLabel createLabel = createLabelInstance(LABEL_ID);
        final Command createLabelCmd = Commands.createCommand(createLabel, commandContext);
        commandBus.post(createLabelCmd, responseObserver);

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, LABEL_ID);
        final Command assignLabelToTaskCmd = Commands.createCommand(assignLabelToTask,
                                                                    commandContext);
        commandBus.post(assignLabelToTaskCmd, responseObserver);

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        final Command deleteTaskCmd = Commands.createCommand(deleteTask, commandContext);
        commandBus.post(deleteTaskCmd, responseObserver);

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        final Command restoreDeletedTaskCmd = Commands.createCommand(restoreDeletedTask,
                                                                     commandContext);
        commandBus.post(restoreDeletedTaskCmd, responseObserver);

        final EventStreamQuery query = EventStreamQuery.newBuilder()
                                                       .build();
        final EventStreamObserver eventStreamObserver = new EventStreamObserver();

        boundedContext.getEventBus()
                      .getEventStore()
                      .read(query, eventStreamObserver);
        final List<Event> events = eventStreamObserver.events;
        final LabelledTaskRestored labelledTaskRestored =
                events.stream()
                      .filter(event -> AnyPacker.unpack(event.getMessage())
                                                .getClass()
                                                .isAssignableFrom(LabelledTaskRestored.class))
                      .findFirst()
                      .map(event -> AnyPacker.unpack(event.getMessage()))
                      .map(LabelledTaskRestored.class::cast)
                      .orElse(LabelledTaskRestored.getDefaultInstance());
        assertEquals(taskId, labelledTaskRestored.getTaskId());
        assertEquals(LABEL_ID, labelledTaskRestored.getLabelId());
    }

    @Test
    @DisplayName("restore the deleted task")
    public void restoreTask() {
        createBasicTask();

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(deleteTask, commandContext);

        restoreDeletedTask();

        final TaskDefinition state = aggregate.getState();
        assertEquals(taskId, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    @DisplayName("restore the deleted task draft")
    public void restoreDraft() {
        createDraft();

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(deleteTask, commandContext);

        TaskDefinition state = aggregate.getState();
        assertEquals(taskId, state.getId());
        assertEquals(DELETED, state.getTaskStatus());

        restoreDeletedTask();

        state = aggregate.getState();
        assertEquals(taskId, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask failure upon an attempt to " +
            "restore the completed task")
    public void cannotRestoreCompletedTask() {
        createBasicTask();

        final CompleteTask completeTask = completeTaskInstance(taskId);
        aggregate.dispatchForTest(completeTask, commandContext);

        final Throwable t = assertThrows(Throwable.class, this::restoreDeletedTask);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotRestoreDeletedTask.class));
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask upon an attempt to restore the finalized task")
    public void cannotRestoreFinalizedTask() {
        createBasicTask();

        final Throwable t = assertThrows(Throwable.class, this::restoreDeletedTask);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotRestoreDeletedTask.class));
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask upon an attempt to restore the draft")
    public void cannotRestoreDraft() {
        createDraft();

        final Throwable t = assertThrows(Throwable.class, this::restoreDeletedTask);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotRestoreDeletedTask.class));
    }

    private void createBasicTask() {
        final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createTask, commandContext);
    }

    private void createDraft() {
        final CreateDraft createDraft = createDraftInstance(taskId);
        aggregate.dispatchForTest(createDraft, commandContext);
    }

    private void restoreDeletedTask() {
        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        aggregate.dispatchForTest(restoreDeletedTask, commandContext);
    }

    private static class EventStreamObserver implements StreamObserver<Event> {

        private final List<Event> events = newArrayList();

        @Override
        public void onNext(Event value) {
            events.add(value);
        }

        @Override
        public void onError(Throwable t) {
            log().error("Occurred exception", t);
        }

        @Override
        public void onCompleted() {
            log().info("completed");
        }

        private enum LogSingleton {
            INSTANCE;

            @SuppressWarnings("NonSerializableFieldInSerializableClass")
            private final Logger value = LoggerFactory.getLogger(EventStreamObserver.class);
        }

        private static Logger log() {
            return LogSingleton.INSTANCE.value;
        }
    }
}
