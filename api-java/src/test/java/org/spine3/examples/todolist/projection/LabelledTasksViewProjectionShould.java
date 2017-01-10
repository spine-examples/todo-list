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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.base.Event;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.LabelledTaskCompleted;
import org.spine3.examples.todolist.LabelledTaskDeleted;
import org.spine3.examples.todolist.LabelledTaskDescriptionUpdated;
import org.spine3.examples.todolist.LabelledTaskDueDateUpdated;
import org.spine3.examples.todolist.LabelledTaskPriorityUpdated;
import org.spine3.examples.todolist.LabelledTaskReopened;
import org.spine3.examples.todolist.LabelledTaskRestored;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.repository.LabelledTasksViewRepository;
import org.spine3.examples.todolist.view.LabelledTasksView;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.server.BoundedContext;
import org.spine3.server.event.EventBus;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.projection.ProjectionRepository;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Events.createEvent;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestBoundedContextFactory.boundedContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventContextFactory.eventContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventEnricherFactory.LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestEventEnricherFactory.eventEnricherInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.LABEL_ID;
import static org.spine3.examples.todolist.testdata.TestEventFactory.TASK_ID;
import static org.spine3.examples.todolist.testdata.TestEventFactory.UPDATED_DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestEventFactory.UPDATED_TASK_DUE_DATE;
import static org.spine3.examples.todolist.testdata.TestEventFactory.UPDATED_TASK_PRIORITY;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelAssignedToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelDetailsUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelRemovedFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelledTaskCompletedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelledTaskDeletedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelledTaskDescriptionUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelledTaskDueDateUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelledTaskPriorityUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelledTaskReopenedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelledTaskRestoredInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelledTskPriorityUpdatedInstance;

/**
 * @author Illia Shepilov
 */
public class LabelledTasksViewProjectionShould {

    private EventBus eventBus;
    private EventContext eventContext = eventContextInstance();
    private ProjectionRepository<TaskLabelId, LabelledTasksViewProjection, LabelledTasksView> repository;

    @BeforeEach
    public void setUp() {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        final EventEnricher eventEnricher = eventEnricherInstance();
        final BoundedContext boundedContext = boundedContextInstance(eventEnricher, storageFactory);
        repository = new LabelledTasksViewRepository(boundedContext);
        repository.initStorage(storageFactory);
        boundedContext.register(repository);
        eventBus = boundedContext.getEventBus();
        eventContext = eventContextInstance();
    }

    @Test
    public void add_task_view_to_state_when_label_assigned_to_task() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        LabelledTasksView labelledTaskView = repository.load(LABEL_ID)
                                                       .getState();
        TaskListView listView = labelledTaskView.getLabelledTasks();

        matchesExpectedValues(labelledTaskView);

        int actualListSize = listView.getItemsCount();
        int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        TaskView view = listView.getItems(0);

        matchesExpectedValues(view);

        eventBus.post(labelAssignedToTaskEvent);

        labelledTaskView = repository.load(LABEL_ID)
                                     .getState();
        listView = labelledTaskView.getLabelledTasks();
        actualListSize = listView.getItemsCount();

        expectedListSize = 2;
        assertEquals(expectedListSize, actualListSize);

        view = listView.getItems(0);

        matchesExpectedValues(view);
        matchesExpectedValues(labelledTaskView);

        view = listView.getItems(1);

        matchesExpectedValues(view);
        matchesExpectedValues(labelledTaskView);
    }

    @Test
    public void remove_task_view_from_state_when_label_removed_from_task() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);
        eventBus.post(labelAssignedToTaskEvent);

        LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                        .getState();
        assertEquals(LABEL_ID, labelledTasksView.getLabelId());

        final LabelRemovedFromTask labelRemovedFromTask = labelRemovedFromTaskInstance();
        final Event labelRemovedFromTaskEvent = createEvent(labelRemovedFromTask, eventContext);
        eventBus.post(labelRemovedFromTaskEvent);

        labelledTasksView = repository.load(LABEL_ID)
                                      .getState();
        doesNotMatchValues(labelledTasksView);

        final TaskListView labelledTasks = labelledTasksView.getLabelledTasks();

        assertTrue(labelledTasks.getItemsList()
                                .isEmpty());
    }

    @Test
    public void remove_task_view_from_state_when_deleted_task_is_restored() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelRemovedFromTask labelRemovedFromTask = labelRemovedFromTaskInstance();
        final Event labelRemovedFromTaskEvent = createEvent(labelRemovedFromTask, eventContext);
        eventBus.post(labelRemovedFromTaskEvent);

        final LabelledTaskRestored deletedTaskRestored = labelledTaskRestoredInstance();
        final Event deletedTaskRestoredEvent = createEvent(deletedTaskRestored, eventContext);
        eventBus.post(deletedTaskRestoredEvent);

        LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                        .getState();
        matchesExpectedValues(labelledTasksView);

        TaskListView listView = labelledTasksView.getLabelledTasks();
        int actualListSize = listView.getItemsCount();

        int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        eventBus.post(deletedTaskRestoredEvent);

        final TaskView taskView = listView.getItems(0);

        assertEquals(TASK_ID, taskView.getId());

        labelledTasksView = repository.load(LABEL_ID)
                                      .getState();

        matchesExpectedValues(labelledTasksView);

        listView = repository.load(LABEL_ID)
                             .getState()
                             .getLabelledTasks();
        actualListSize = listView.getItemsCount();

        expectedListSize = 2;
        assertEquals(expectedListSize, actualListSize);
        assertEquals(TASK_ID, listView.getItems(0)
                                      .getId());
        assertEquals(TASK_ID, listView.getItems(1)
                                      .getId());

    }

    @Test
    public void remove_task_view_from_state_when_task_is_deleted() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskDeleted taskDeleted = labelledTaskDeletedInstance();
        final Event deletedTaskEvent = createEvent(taskDeleted, eventContext);
        eventBus.post(deletedTaskEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);

        TaskListView listView = labelledTasksView.getLabelledTasks();
        int actualListSize = listView.getItemsCount();

        int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        matchesExpectedValues(taskView);

        eventBus.post(deletedTaskEvent);

        expectedListSize = 0;
        listView = repository.load(LABEL_ID)
                             .getState()
                             .getLabelledTasks();
        actualListSize = listView.getItemsCount();

        eventBus.post(deletedTaskEvent);
        assertEquals(expectedListSize, actualListSize);
        assertTrue(listView.getItemsList()
                           .isEmpty());
    }

    @Test
    public void update_task_description_when_handled_event_task_description_updated() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskDescriptionUpdated taskDescriptionUpdated = labelledTaskDescriptionUpdatedInstance();
        final Event descriptionUpdatedEvent = createEvent(taskDescriptionUpdated, eventContext);
        eventBus.post(descriptionUpdatedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);

        final TaskListView listView = labelledTasksView.getLabelledTasks();
        final int actualListSize = listView.getItemsCount();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        assertEquals(UPDATED_DESCRIPTION, taskView.getDescription());
    }

    @Test
    public void not_update_task_description_when_handled_event_task_description_updated_with_wrong_task_id() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskDescriptionUpdated taskDescriptionUpdated =
                labelledTaskDescriptionUpdatedInstance(TaskId.getDefaultInstance(), LABEL_ID, UPDATED_DESCRIPTION);
        final Event descriptionUpdatedEvent = createEvent(taskDescriptionUpdated, eventContext);
        eventBus.post(descriptionUpdatedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);
        final TaskListView listView = labelledTasksView.getLabelledTasks();
        int actualListSize = listView.getItemsCount();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        assertNotEquals(UPDATED_DESCRIPTION, taskView.getDescription());
    }

    @Test
    public void update_task_priority_when_handled_event_task_priority_updated() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskPriorityUpdated taskPriorityUpdated = labelledTskPriorityUpdatedInstance();
        final Event taskPriorityUpdatedEvent = createEvent(taskPriorityUpdated, eventContext);
        eventBus.post(taskPriorityUpdatedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);
        final TaskListView listView = labelledTasksView.getLabelledTasks();
        final int actualListSize = listView.getItemsCount();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        assertEquals(UPDATED_TASK_PRIORITY, taskView.getPriority());
    }

    @Test
    public void not_update_task_priority_when_handled_event_task_priority_updated_with_wrong_task_id() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskPriorityUpdated taskPriorityUpdated =
                labelledTaskPriorityUpdatedInstance(TaskId.getDefaultInstance(), LABEL_ID, UPDATED_TASK_PRIORITY);
        final Event taskPriorityUpdatedEvent = createEvent(taskPriorityUpdated, eventContext);
        eventBus.post(taskPriorityUpdatedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);
        final TaskListView listView = labelledTasksView.getLabelledTasks();
        final int actualListSize = listView.getItemsCount();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        assertNotEquals(UPDATED_TASK_PRIORITY, taskView.getPriority());
    }

    @Test
    public void update_task_due_date_when_handled_event_task_due_date_updated() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskDueDateUpdated taskDueDateUpdated = labelledTaskDueDateUpdatedInstance();
        final Event taskDueDateUpdatedEvent = createEvent(taskDueDateUpdated, eventContext);
        eventBus.post(taskDueDateUpdatedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);
        final TaskListView listView = labelledTasksView.getLabelledTasks();
        final int actualListSize = listView.getItemsCount();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        assertEquals(UPDATED_TASK_DUE_DATE, taskView.getDueDate());
    }

    @Test
    public void update_task_due_date_when_handled_event_task_due_date_updated_with_wrong_task_id() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskDueDateUpdated taskDueDateUpdated =
                labelledTaskDueDateUpdatedInstance(TaskId.getDefaultInstance(), LABEL_ID, UPDATED_TASK_DUE_DATE);
        final Event taskDueDateUpdatedEvent = createEvent(taskDueDateUpdated, eventContext);
        eventBus.post(taskDueDateUpdatedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);
        final TaskListView listView = labelledTasksView.getLabelledTasks();
        final int actualListSize = listView.getItemsCount();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        assertNotEquals(UPDATED_TASK_DUE_DATE, taskView.getDueDate());
    }

    @Test
    public void update_task_completed_state_when_handled_event_task_completed() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskCompleted taskCompleted = labelledTaskCompletedInstance();
        final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
        eventBus.post(taskCompletedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);
        final TaskListView listView = labelledTasksView.getLabelledTasks();
        final int actualListSize = listView.getItemsCount();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        assertTrue(taskView.getCompleted());
    }

    @Test
    public void update_task_completed_state_when_handled_event_task_completed_with_wrong_task_id() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskCompleted taskCompleted = labelledTaskCompletedInstance();
        final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
        eventBus.post(taskCompletedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);
        final TaskListView listView = labelledTasksView.getLabelledTasks();
        final int actualListSize = listView.getItemsCount();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        assertTrue(taskView.getCompleted());
    }

    @Test
    public void update_task_completed_state_when_handled_event_task_reopened() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskCompleted taskCompleted = labelledTaskCompletedInstance();
        final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
        eventBus.post(taskCompletedEvent);

        final LabelledTaskReopened taskReopened = labelledTaskReopenedInstance();
        final Event taskReopenedEvent = createEvent(taskReopened, eventContext);
        eventBus.post(taskReopenedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);
        final TaskListView listView = labelledTasksView.getLabelledTasks();
        final int actualListSize = listView.getItemsCount();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        assertFalse(taskView.getCompleted());
    }

    @Test
    public void update_task_completed_state_when_handled_event_task_reopened_with_wrong_task_id() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final LabelledTaskCompleted taskCompleted = labelledTaskCompletedInstance();
        final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
        eventBus.post(taskCompletedEvent);

        final LabelledTaskReopened taskReopened = labelledTaskReopenedInstance(TaskId.getDefaultInstance(), LABEL_ID);
        final Event taskReopenedEvent = createEvent(taskReopened, eventContext);
        eventBus.post(taskReopenedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        matchesExpectedValues(labelledTasksView);
        final TaskListView listView = labelledTasksView.getLabelledTasks();
        final int actualListSize = listView.getItemsCount();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, actualListSize);

        final TaskView taskView = listView.getItems(0);

        assertTrue(taskView.getCompleted());
    }

    @Test
    public void update_label_details_when_handled_event_label_details_updated() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final String newTitle = "Updated label title.";

        final LabelDetailsUpdated labelDetailsUpdated = labelDetailsUpdatedInstance(LABEL_ID, LabelColor.RED, newTitle);
        final Event labelDetailsUpdatedEvent = createEvent(labelDetailsUpdated, eventContext);
        eventBus.post(labelDetailsUpdatedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        assertEquals(LABEL_ID, labelledTasksView.getLabelId());
        assertEquals(newTitle, labelledTasksView.getLabelTitle());
        assertEquals("#ff0000", labelledTasksView.getLabelColor());
    }

    @Test
    public void not_update_label_details_when_handled_event_label_details_updated_with_wrong_label_id() {
        final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
        final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
        eventBus.post(labelAssignedToTaskEvent);

        final String newTitle = "Updated label title.";
        final TaskLabelId wrongLabelId = TaskLabelId.newBuilder()
                                                    .setValue(newUuid())
                                                    .build();

        final LabelDetailsUpdated labelDetailsUpdated =
                labelDetailsUpdatedInstance(wrongLabelId, LabelColor.RED, newTitle);
        final Event labelDetailsUpdatedEvent = createEvent(labelDetailsUpdated, eventContext);
        eventBus.post(labelDetailsUpdatedEvent);

        final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                              .getState();
        assertEquals(LABEL_ID, labelledTasksView.getLabelId());
        assertNotEquals(newTitle, labelledTasksView.getLabelTitle());
        assertNotEquals("#ff0000", labelledTasksView.getLabelColor());
    }

    private static void matchesExpectedValues(TaskView taskView) {
        assertEquals(TASK_ID, taskView.getId());
        assertEquals(LABEL_ID, taskView.getLabelId());
    }

    private static void matchesExpectedValues(LabelledTasksView labelledTaskView) {
        assertEquals(LabelColorView.valueOf(LabelColor.BLUE), labelledTaskView.getLabelColor());
        assertEquals(LABEL_TITLE, labelledTaskView.getLabelTitle());
    }

    private static void doesNotMatchValues(LabelledTasksView labelledTaskView) {
        assertNotEquals(LabelColorView.valueOf(LabelColor.BLUE), labelledTaskView.getLabelColor());
        assertNotEquals(LABEL_TITLE, labelledTaskView.getLabelTitle());
    }
}
