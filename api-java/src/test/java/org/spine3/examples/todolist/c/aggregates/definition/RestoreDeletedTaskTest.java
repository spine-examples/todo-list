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

package org.spine3.examples.todolist.c.aggregates.definition;

import com.google.common.base.Throwables;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.base.Event;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.aggregates.TaskAggregateRoot;
import org.spine3.examples.todolist.c.aggregates.TaskDefinitionPart;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.c.events.LabelledTaskRestored;
import org.spine3.examples.todolist.c.failures.CannotRestoreDeletedTask;
import org.spine3.examples.todolist.context.TodoListBoundedContext;
import org.spine3.examples.todolist.testdata.TestResponseObserver;
import org.spine3.protobuf.AnyPacker;
import org.spine3.protobuf.TypeName;
import org.spine3.server.BoundedContext;
import org.spine3.server.command.CommandBus;
import org.spine3.server.event.EventFilter;
import org.spine3.server.event.EventStreamQuery;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Commands.create;
import static org.spine3.examples.todolist.TaskStatus.DELETED;
import static org.spine3.examples.todolist.TaskStatus.OPEN;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.LABEL_ID;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("RestoreDeletedTask command")
public class RestoreDeletedTaskTest extends TaskDefinitionCommandTest<RestoreDeletedTask> {

    private final CommandContext commandContext = getCommandContext();
    private TestResponseObserver responseObserver;
    private TaskDefinitionPart taskDefinitionPart;
    private BoundedContext boundedContext;
    private CommandBus commandBus;
    private TaskId taskId;

    @Override
    @BeforeEach
    public void setUp() {
        boundedContext = TodoListBoundedContext.getTestInstance();
        TaskAggregateRoot.injectBoundedContext(boundedContext);
        commandBus = boundedContext.getCommandBus();
        responseObserver = new TestResponseObserver();
        taskId = getTaskId();
        taskDefinitionPart = new TaskDefinitionPart(taskId);
    }

    @Test
    @DisplayName("produces LabelledTaskRestored event")
    public void producesEvent() {
        final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
        final Command createTaskCmd = create(createTask, commandContext);
        commandBus.post(createTaskCmd, responseObserver);

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, LABEL_ID);
        final Command assignLabelToTaskCmd = create(assignLabelToTask, commandContext);
        commandBus.post(assignLabelToTaskCmd, responseObserver);

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        final Command deleteTaskCmd = create(deleteTask, commandContext);
        commandBus.post(deleteTaskCmd, responseObserver);

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        final Command restoreDeletedTaskCmd = create(restoreDeletedTask, commandContext);
        commandBus.post(restoreDeletedTaskCmd, responseObserver);

        final int expectedSetSize = 1;

        final String typeName = TypeName.of(LabelledTaskRestored.class);
        final EventFilter eventFilter = EventFilter.newBuilder()
                                                   .setEventType(typeName)
                                                   .build();
        final EventStreamQuery query = EventStreamQuery.newBuilder()
                                                       .addFilter(eventFilter)
                                                       .build();
        final EventStreamObserver eventStreamObserver = new EventStreamObserver();

        boundedContext.getEventBus()
                      .getEventStore()
                      .read(query, eventStreamObserver);

        final Set<Event> events = eventStreamObserver.eventSet;

        assertEquals(expectedSetSize, events.size());
        final LabelledTaskRestored labelledTaskRestored = AnyPacker.unpack(events.iterator()
                                                                                 .next()
                                                                                 .getMessage());
        assertEquals(taskId, labelledTaskRestored.getTaskId());
        assertEquals(LABEL_ID, labelledTaskRestored.getLabelId());
    }

    @Test
    @DisplayName("restores deleted task")
    public void restoresTask() {
        createBasicTask();

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        taskDefinitionPart.dispatchForTest(deleteTask, commandContext);

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        taskDefinitionPart.dispatchForTest(restoreDeletedTask, commandContext);

        final TaskDefinition state = taskDefinitionPart.getState();
        assertEquals(taskId, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    @DisplayName("restores deleted task draft")
    public void restoresDraft() {
        final CreateDraft createDraft = createDraftInstance(taskId);
        taskDefinitionPart.dispatchForTest(createDraft, commandContext);

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        taskDefinitionPart.dispatchForTest(deleteTask, commandContext);

        TaskDefinition state = taskDefinitionPart.getState();
        assertEquals(taskId, state.getId());
        assertEquals(DELETED, state.getTaskStatus());

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        taskDefinitionPart.dispatchForTest(restoreDeletedTask, commandContext);

        state = taskDefinitionPart.getState();
        assertEquals(taskId, state.getId());
        assertEquals(OPEN, state.getTaskStatus());
    }

    @Test
    @DisplayName("cannot restore task when task is completed")
    public void cannotRestoreCompletedTask() {
        createBasicTask();

        final CompleteTask completeTask = completeTaskInstance(taskId);
        taskDefinitionPart.dispatchForTest(completeTask, commandContext);

        try {
            final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
            taskDefinitionPart.dispatchForTest(restoreDeletedTask, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotRestoreDeletedTask);
        }
    }

    @Test
    @DisplayName("cannot restore task when task is finalized")
    public void cannotRestoreFinalizedTask() {
        final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
        final Command createTaskCmd = create(createTask, commandContext);
        commandBus.post(createTaskCmd, responseObserver);

        try {
            final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
            final Command restoreDeletedTaskCmd = create(restoreDeletedTask, commandContext);
            commandBus.post(restoreDeletedTaskCmd, responseObserver);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotRestoreDeletedTask);
        }
    }

    @Test
    @DisplayName("cannot restore task when task in draft state")
    public void cannotRestoreDraft() {
        final CreateDraft createDraft = createDraftInstance(taskId);
        taskDefinitionPart.dispatchForTest(createDraft, commandContext);

        try {
            final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
            taskDefinitionPart.dispatchForTest(restoreDeletedTask, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // We need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotRestoreDeletedTask);
        }
    }

    private void createBasicTask() {
        final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
        taskDefinitionPart.dispatchForTest(createTask, commandContext);
    }

    private static class EventStreamObserver implements StreamObserver<Event> {

        private final Set<Event> eventSet = newHashSet();

        @Override
        public void onNext(Event value) {
            eventSet.add(value);
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
