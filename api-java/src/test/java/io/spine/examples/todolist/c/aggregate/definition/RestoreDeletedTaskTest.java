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
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.c.events.LabelledTaskRestored;
import io.spine.examples.todolist.c.rejection.CannotRestoreDeletedTask;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.grpc.MemoizingObserver;
import io.spine.grpc.StreamObservers;
import io.spine.logging.Logging;
import io.spine.protobuf.AnyPacker;
import io.spine.server.BoundedContext;
import io.spine.server.commandbus.CommandBus;
import io.spine.server.event.EventStreamQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("RestoreDeletedTask command should be interpreted by TaskPart and")
class RestoreDeletedTaskTest extends TaskCommandTest<RestoreDeletedTask> {

    private MemoizingObserver<Ack> responseObserver;
    private BoundedContext boundedContext;
    private CommandBus commandBus;

    RestoreDeletedTaskTest() {
        super(restoreDeletedTaskInstance());
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        responseObserver = StreamObservers.memoizingObserver();
        boundedContext = BoundedContexts.create();
        commandBus = boundedContext.commandBus();
        TaskAggregateRoot root = new TaskAggregateRoot(boundedContext, entityId());
        aggregate = new TaskPart(root);
    }

    @Test
    @DisplayName("produce LabelledTaskRestored event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(entityId(), DESCRIPTION);
        Command createTaskCmd = createNewCommand(createTask);
        commandBus.post(createTaskCmd, responseObserver);

        UpdateTaskPriority updateTaskPriority = updateTaskPriorityInstance(entityId());
        Command updateTaskPriorityCmd = createNewCommand(updateTaskPriority);
        commandBus.post(updateTaskPriorityCmd, responseObserver);

        CreateBasicLabel createLabel = createLabelInstance(LABEL_ID);
        Command createLabelCmd = createNewCommand(createLabel);
        commandBus.post(createLabelCmd, responseObserver);

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(entityId(), LABEL_ID);
        Command assignLabelToTaskCmd = createNewCommand(assignLabelToTask);
        commandBus.post(assignLabelToTaskCmd, responseObserver);

        DeleteTask deleteTask = deleteTaskInstance(entityId());
        Command deleteTaskCmd = createNewCommand(deleteTask);
        commandBus.post(deleteTaskCmd, responseObserver);

        RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(entityId());
        Command restoreDeletedTaskCmd = createNewCommand(restoreDeletedTask);
        commandBus.post(restoreDeletedTaskCmd, responseObserver);

        EventStreamQuery query = EventStreamQuery.newBuilder()
                                                 .build();
        EventStreamObserver eventStreamObserver = new EventStreamObserver();

        boundedContext.eventBus()
                      .eventStore()
                      .read(query, eventStreamObserver);
        List<Event> events = eventStreamObserver.events;
        LabelledTaskRestored labelledTaskRestored =
                events.stream()
                      .filter(event -> AnyPacker.unpack(event.getMessage())
                                                .getClass()
                                                .isAssignableFrom(LabelledTaskRestored.class))
                      .findFirst()
                      .map(event -> AnyPacker.unpack(event.getMessage()))
                      .map(LabelledTaskRestored.class::cast)
                      .orElseThrow(() -> new IllegalStateException("Event was not produced."));
        assertEquals(entityId(), labelledTaskRestored.getTaskId());
        assertEquals(LABEL_ID, labelledTaskRestored.getLabelId());
    }

    @Test
    @DisplayName("restore the deleted task")
    void restoreTask() {
        createBasicTask();

        DeleteTask deleteTask = deleteTaskInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(deleteTask));

        restoreDeletedTask();

        Task state = aggregate.state();
        assertEquals(entityId(), state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    @DisplayName("restore the deleted task draft")
    void restoreDraft() {
        createDraft();

        DeleteTask deleteTask = deleteTaskInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(deleteTask));

        Task state = aggregate.state();
        assertEquals(entityId(), state.getId());
        assertEquals(DELETED, state.getTaskStatus());

        restoreDeletedTask();

        state = aggregate.state();
        assertEquals(entityId(), state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask rejection upon an attempt to " +
            "restore the completed task")
    void cannotRestoreCompletedTask() {
        createBasicTask();

        CompleteTask completeTask = completeTaskInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(completeTask));

        Throwable t = assertThrows(Throwable.class, this::restoreDeletedTask);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotRestoreDeletedTask.class));
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask upon an attempt to restore the finalized task")
    void cannotRestoreFinalizedTask() {
        createBasicTask();

        Throwable t = assertThrows(Throwable.class, this::restoreDeletedTask);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotRestoreDeletedTask.class));
    }

    @Test
    @DisplayName("throw CannotRestoreDeletedTask upon an attempt to restore the draft")
    void cannotRestoreDraft() {
        createDraft();

        Throwable t = assertThrows(Throwable.class, this::restoreDeletedTask);
        assertThat(Throwables.getRootCause(t), instanceOf(CannotRestoreDeletedTask.class));
    }

    private void createBasicTask() {
        CreateBasicTask createTask = createTaskInstance(entityId(), DESCRIPTION);
        dispatchCommand(aggregate, envelopeOf(createTask));
    }

    private void createDraft() {
        CreateDraft createDraft = createDraftInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(createDraft));
    }

    private void restoreDeletedTask() {
        RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(restoreDeletedTask));
    }

    private static class EventStreamObserver implements StreamObserver<Event>, Logging {

        private final List<Event> events = newArrayList();

        @Override
        public void onNext(Event value) {
            events.add(value);
        }

        @Override
        public void onError(Throwable t) {
            _error("Occurred exception", t);
        }

        @Override
        public void onCompleted() {
            _info("completed");
        }
    }
}
