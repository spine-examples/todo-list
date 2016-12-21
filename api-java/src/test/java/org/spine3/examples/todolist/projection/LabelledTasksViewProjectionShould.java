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
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.server.BoundedContext;
import org.spine3.server.event.EventBus;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.projection.ProjectionRepository;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Events.createEvent;
import static org.spine3.examples.todolist.testdata.TestBoundedContextFactory.boundedContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventContextFactory.eventContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventEnricherFactory.eventEnricherInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.LABEL_ID;
import static org.spine3.examples.todolist.testdata.TestEventFactory.TASK_ID;
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
    private EventBus eventBus;
    private ProjectionRepository<TaskLabelId, LabelledTasksViewProjection, LabelledTasksView> repository;
    private static final TaskLabelId ID = TestEventFactory.LABEL_ID;
    private static final String EXPECTED_COLOR = "#0000ff";
    private static final String EXPECTED_TITLE = "title";

    @BeforeEach
    public void setUp() {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        final EventEnricher eventEnricher = eventEnricherInstance();
        final BoundedContext boundedContext = boundedContextInstance(eventEnricher, storageFactory);
        repository = new LabelledTasksViewProjectionRepository(boundedContext);
        repository.initStorage(storageFactory);
        repository.setOnline();
        boundedContext.register(repository);
        eventBus = boundedContext.getEventBus();
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
    public void add_task_view_to_state_when_label_assigned_to_task() {
        int expectedListSize = 1;
        eventBus.post(labelAssignedToTaskEvent);
        LabelledTasksView labelledTaskView = repository.load(ID)
                                                       .getState();
        TaskListView listView = labelledTaskView.getLabelledTasks();

        checkOkLabelledTaskView(labelledTaskView);

        int actualListSize = listView.getItemsCount();
        assertEquals(expectedListSize, actualListSize);

        TaskView view = listView.getItems(0);

        checkOkTaskView(view);

        eventBus.post(labelAssignedToTaskEvent);
        expectedListSize = 2;
        labelledTaskView = repository.load(ID)
                                     .getState();
        listView = labelledTaskView.getLabelledTasks();
        actualListSize = listView.getItemsCount();

        assertEquals(expectedListSize, actualListSize);

        view = listView.getItems(0);

        checkOkTaskView(view);
        checkOkLabelledTaskView(labelledTaskView);

        view = listView.getItems(1);

        checkOkTaskView(view);
        checkOkLabelledTaskView(labelledTaskView);
    }

    @Test
    public void remove_task_view_from_state_when_label_removed_from_task() {
        int expectedListSize = 1;
        eventBus.post(labelAssignedToTaskEvent);
        eventBus.post(labelAssignedToTaskEvent);
        eventBus.post(labelRemovedFromTaskEvent);
        final LabelledTasksView labelledTasksView = repository.load(ID)
                                                              .getState();
        checkOkLabelledTaskView(labelledTasksView);

        TaskListView labelledTasks = labelledTasksView.getLabelledTasks();
        int actualListSize = labelledTasks.getItemsCount();

        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = labelledTasks.getItems(0);

        assertEquals(LABEL_ID, taskView.getLabelId());
        assertEquals(TASK_ID, taskView.getId());

        eventBus.post(labelRemovedFromTaskEvent);
        expectedListSize = 0;
        labelledTasks = repository.load(ID)
                                  .getState()
                                  .getLabelledTasks();
        actualListSize = labelledTasks.getItemsCount();

        assertEquals(expectedListSize, actualListSize);
        assertTrue(labelledTasks.getItemsList()
                                .isEmpty());
    }

    @Test
    public void remove_task_view_from_state_when_deleted_task_is_restored() {
        int expectedListSize = 1;
        eventBus.post(labelAssignedToTaskEvent);
        eventBus.post(labelAssignedToTaskEvent);
        eventBus.post(labelRemovedFromTaskEvent);
        eventBus.post(labelRemovedFromTaskEvent);
        eventBus.post(deletedTaskRestoredEvent);
        LabelledTasksView labelledTasksView = repository.load(ID)
                                                        .getState();
        checkOkLabelledTaskView(labelledTasksView);

        TaskListView listView = labelledTasksView.getLabelledTasks();
        int actualListSize = listView.getItemsCount();

        assertEquals(expectedListSize, actualListSize);

        eventBus.post(deletedTaskRestoredEvent);
        final TaskView taskView = listView.getItems(0);

        assertEquals(TASK_ID, taskView.getId());

        expectedListSize = 2;
        labelledTasksView = repository.load(ID)
                                      .getState();

        checkOkLabelledTaskView(labelledTasksView);

        listView = repository.load(ID)
                             .getState()
                             .getLabelledTasks();
        actualListSize = listView.getItemsCount();

        assertEquals(expectedListSize, actualListSize);
        assertEquals(TASK_ID, listView.getItems(0)
                                      .getId());
        assertEquals(TASK_ID, listView.getItems(1)
                                      .getId());

    }

    @Test
    public void remove_task_view_from_state_when_task_is_deleted() {
        int expectedListSize = 1;
        eventBus.post(labelAssignedToTaskEvent);
        eventBus.post(labelAssignedToTaskEvent);
        eventBus.post(deletedTaskEvent);
        final LabelledTasksView labelledTasksView = repository.load(ID)
                                                              .getState();
        checkOkLabelledTaskView(labelledTasksView);

        TaskListView listView = labelledTasksView.getLabelledTasks();
        final TaskView taskView = listView.getItems(0);
        int actualListSize = listView.getItemsCount();

        assertEquals(expectedListSize, actualListSize);
        checkOkTaskView(taskView);

        eventBus.post(deletedTaskEvent);
        expectedListSize = 0;
        listView = repository.load(ID)
                             .getState()
                             .getLabelledTasks();
        actualListSize = listView.getItemsCount();

        assertEquals(expectedListSize, actualListSize);
        assertTrue(listView.getItemsList()
                           .isEmpty());
    }

    private static void checkOkTaskView(TaskView taskView) {
        assertEquals(TASK_ID, taskView.getId());
        assertEquals(LABEL_ID, taskView.getLabelId());
    }

    private static void checkOkLabelledTaskView(LabelledTasksView labelledTaskView) {
        assertEquals(EXPECTED_COLOR, labelledTaskView.getLabelColor());
        assertEquals(EXPECTED_TITLE, labelledTaskView.getLabelTitle());
    }

    /*
     * Stub projection repository
     *************************************************/

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
