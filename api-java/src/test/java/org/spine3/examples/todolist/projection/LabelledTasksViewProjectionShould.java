/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.projection;

import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.base.Event;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.testdata.TestEventFactory;
import org.spine3.examples.todolist.view.LabelledTasksView;
import org.spine3.server.BoundedContext;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.projection.ProjectionRepository;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Events.createEvent;
import static org.spine3.examples.todolist.testdata.TestBoundedContextFactory.boundedContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventContextFactory.eventContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventEnricherFactory.eventEnricherInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.deletedTaskRestoredInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelAssignedToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelRemovedFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDeletedInstance;

/**
 * @author Illia Shepilov
 */
public class LabelledTasksViewProjectionShould {

    private Event labelAssignedToTaskEvent;
    private Event labelRemovedFromTaskEvent;
    private Event deletedTaskRestoredEvent;
    private Event deletedTaskEvent;
    private ProjectionRepository<TaskLabelId, LabelledTasksViewProjection, LabelledTasksView> repository;
    private static final TaskLabelId ID = TestEventFactory.LABEL_ID;

    @BeforeEach
    public void setUp() {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        final EventEnricher eventEnricher = eventEnricherInstance();
        final BoundedContext boundedContext = boundedContextInstance(eventEnricher, storageFactory);
        repository = new LabelledTasksViewProjectionRepository(boundedContext);
        repository.initStorage(storageFactory);
        repository.setOnline();
        boundedContext.register(repository);
        final EventContext eventContext = eventContextInstance();
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final LabelRemovedFromTask labelRemovedFromTask = labelRemovedFromTaskInstance();
        final DeletedTaskRestored deletedTaskRestored = deletedTaskRestoredInstance();
        final TaskDeleted taskDeleted = taskDeletedInstance();
        labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        labelRemovedFromTaskEvent = createEvent(labelRemovedFromTask, eventContext);
        deletedTaskRestoredEvent = createEvent(deletedTaskRestored, eventContext);
        deletedTaskEvent = createEvent(taskDeleted, eventContext);
    }

    @Test
    public void return_current_state_when_label_assigned_to_task_event_handled() {
        int expectedListSize = 1;
        repository.dispatch(labelAssignedToTaskEvent);
        int actualListSize = repository.load(ID)
                                       .getState()
                                       .getLabelledTasks()
                                       .getItemsCount();
        assertEquals(expectedListSize, actualListSize);
        repository.dispatch(labelAssignedToTaskEvent);
        expectedListSize = 2;
        actualListSize = repository.load(ID)
                                   .getState()
                                   .getLabelledTasks()
                                   .getItemsCount();
        assertEquals(expectedListSize, actualListSize);
    }

    @Test
    public void return_current_state_when_label_removed_from_task_event_handled() {
        int expectedListSize = 1;
        repository.dispatch(labelAssignedToTaskEvent);
        repository.dispatch(labelAssignedToTaskEvent);
        repository.dispatch(labelRemovedFromTaskEvent);
        int actualListSize = repository.load(ID)
                                       .getState()
                                       .getLabelledTasks()
                                       .getItemsCount();
        assertEquals(expectedListSize, actualListSize);
        repository.dispatch(labelRemovedFromTaskEvent);
        expectedListSize = 0;
        actualListSize = repository.load(ID)
                                   .getState()
                                   .getLabelledTasks()
                                   .getItemsCount();
        assertEquals(expectedListSize, actualListSize);
    }

    @Test
    public void return_current_state_when_deleted_task_restored_event_handled() {
        int expectedListSize = 1;
        repository.dispatch(labelAssignedToTaskEvent);
        repository.dispatch(labelAssignedToTaskEvent);
        repository.dispatch(labelRemovedFromTaskEvent);
        repository.dispatch(labelRemovedFromTaskEvent);
        repository.dispatch(deletedTaskRestoredEvent);
        int actualListSize = repository.load(ID)
                                       .getState()
                                       .getLabelledTasks()
                                       .getItemsCount();
        assertEquals(expectedListSize, actualListSize);
        repository.dispatch(deletedTaskRestoredEvent);
        expectedListSize = 2;
        actualListSize = repository.load(ID)
                                   .getState()
                                   .getLabelledTasks()
                                   .getItemsCount();
        assertEquals(expectedListSize, actualListSize);
    }

    @Test
    public void return_current_state_when_task_deleted_event_handled() {
        int expectedListSize = 1;
        repository.dispatch(labelAssignedToTaskEvent);
        repository.dispatch(labelAssignedToTaskEvent);
        repository.dispatch(deletedTaskEvent);
        int actualListSize = repository.load(ID)
                                       .getState()
                                       .getLabelledTasks()
                                       .getItemsCount();
        assertEquals(expectedListSize, actualListSize);
        repository.dispatch(deletedTaskEvent);
        expectedListSize = 0;
        actualListSize = repository.load(ID)
                                   .getState()
                                   .getLabelledTasks()
                                   .getItemsCount();
        assertEquals(expectedListSize, actualListSize);
    }

    private static class LabelledTasksViewProjectionRepository
            extends ProjectionRepository<TaskLabelId, LabelledTasksViewProjection, LabelledTasksView> {

        @Override
        protected TaskLabelId getEntityId(Message event, EventContext context) {
            return ID;
        }

        protected LabelledTasksViewProjectionRepository(BoundedContext boundedContext) {
            super(boundedContext);
        }
    }
}
