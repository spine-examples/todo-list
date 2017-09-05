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

package io.spine.examples.todolist.c.aggregate.definition;

import com.google.common.base.Throwables;
import io.grpc.stub.StreamObserver;
import io.spine.core.Ack;
import io.spine.core.Command;
import io.spine.core.Event;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.c.aggregate.TaskAggregateRoot;
import io.spine.examples.todolist.c.aggregate.TaskPart;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.RestoreDeletedTask;
import io.spine.examples.todolist.c.events.LabelledTaskRestored;
import io.spine.examples.todolist.c.failures.CannotRestoreDeletedTask;
import io.spine.examples.todolist.context.TodoListBoundedContext;
import io.spine.grpc.MemoizingObserver;
import io.spine.grpc.StreamObservers;
import io.spine.protobuf.AnyPacker;
import io.spine.server.BoundedContext;
import io.spine.server.commandbus.CommandBus;
import io.spine.server.event.EventStreamQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.examples.todolist.TaskStatus.DELETED;
import static io.spine.examples.todolist.TaskStatus.OPEN;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.createLabelInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.LABEL_ID;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static io.spine.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Illia Shepilov
 */
@DisplayName("RestoreDeletedTask command should be interpreted by TaskPart and")
public class RestoreDeletedTaskTest extends TaskCommandTest<RestoreDeletedTask> {

    private MemoizingObserver<Ack> responseObserver;
    private BoundedContext boundedContext;
    private CommandBus commandBus;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        responseObserver = StreamObservers.memoizingObserver();
        boundedContext = TodoListBoundedContext.createTestInstance();
        commandBus = boundedContext.getCommandBus();
        final TaskAggregateRoot root = new TaskAggregateRoot(boundedContext, taskId);
        aggregate = new TaskPart(root);
    }

    @Test
    @DisplayName("produce LabelledTaskRestored event")
    void produceEvent() {
        final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
        final Command createTaskCmd = createDifferentCommand(createTask);
        commandBus.post(createTaskCmd, responseObserver);

        final CreateBasicLabel createLabel = createLabelInstance(LABEL_ID);
        final Command createLabelCmd = createDifferentCommand(createLabel);
        commandBus.post(createLabelCmd, responseObserver);

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, LABEL_ID);
        final Command assignLabelToTaskCmd = createDifferentCommand(assignLabelToTask);
        commandBus.post(assignLabelToTaskCmd, responseObserver);

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        final Command deleteTaskCmd = createDifferentCommand(deleteTask);
        commandBus.post(deleteTaskCmd, responseObserver);

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        final Command restoreDeletedTaskCmd = createDifferentCommand(restoreDeletedTask);
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
                      .orElseThrow(() -> new IllegalStateException("Event was not produced."));
        assertEquals(taskId, labelledTaskRestored.getTaskId());
        assertEquals(LABEL_ID, labelledTaskRestored.getLabelId());
    }

    @Test
    @DisplayName("restore the deleted task")
    void restoreTask() {
        createBasicTask();

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        dispatchCommand(aggregate, envelopeOf(deleteTask));

        restoreDeletedTask();

        final Task state = aggregate.getState();
        assertEquals(taskId, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    @DisplayName("restore the deleted task draft")
    void restoreDraft() {
        createDraft();

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        dispatchCommand(aggregate, envelopeOf(deleteTask));

        Task state = aggregate.getState();
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
    void cannotRestoreCompletedTask() {
        createBasicTask();

        final CompleteTask completeTask = completeTaskInstance(taskId);
        dispatchCommand(aggregate, envelopeOf(completeTask));

        final Throwable t = assertThrows(Throwable.class, this::restoreDeletedTask);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotRestoreDeletedTask.class));
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask upon an attempt to restore the finalized task")
    void cannotRestoreFinalizedTask() {
        createBasicTask();

        final Throwable t = assertThrows(Throwable.class, this::restoreDeletedTask);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotRestoreDeletedTask.class));
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask upon an attempt to restore the draft")
    void cannotRestoreDraft() {
        createDraft();

        final Throwable t = assertThrows(Throwable.class, this::restoreDeletedTask);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotRestoreDeletedTask.class));
    }

    private void createBasicTask() {
        final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
        dispatchCommand(aggregate, envelopeOf(createTask));
    }

    private void createDraft() {
        final CreateDraft createDraft = createDraftInstance(taskId);
        dispatchCommand(aggregate, envelopeOf(createDraft));
    }

    private void restoreDeletedTask() {
        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        dispatchCommand(aggregate, envelopeOf(restoreDeletedTask));
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
