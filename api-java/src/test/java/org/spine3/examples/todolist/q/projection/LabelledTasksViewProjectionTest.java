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

package org.spine3.examples.todolist.q.projection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.spine3.base.Event;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.events.LabelledTaskRestored;
import org.spine3.examples.todolist.c.events.TaskCompleted;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.examples.todolist.c.events.TaskReopened;
import org.spine3.examples.todolist.repository.LabelledTasksViewRepository;
import org.spine3.server.BoundedContext;
import org.spine3.server.event.EventBus;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.projection.ProjectionRepository;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.StorageFactorySwitch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Events.createEvent;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestBoundedContextFactory.boundedContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventBusFactory.eventBusInstance;
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
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelledTaskRestoredInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskCompletedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDeletedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDescriptionUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDueDateUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskPriorityUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskReopenedInstance;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("OptionalGetWithoutIsPresent") // it is OK as we create all those objects.
public class LabelledTasksViewProjectionTest extends ProjectionTest {

    private static final String UPDATES_TASK_VIEW_COMPLETED_FLAG_TO_TRUE = "updates TaskView completed flag to true";
    private static final String UPDATES_TASK_VIEW_COMPLETED_FLAG_TO_FALSE = "updates TaskView completed flag to false";
    private final EventContext eventContext = eventContextInstance();
    private ProjectionRepository<LabelId, LabelledTasksViewProjection, LabelledTasksView> repository;
    private EventBus eventBus;

    @BeforeEach
    public void setUp() {
        final StorageFactorySwitch storageFactorySwitch = StorageFactorySwitch.getInstance();
        final StorageFactory storageFactory = storageFactorySwitch.get();
        final EventEnricher eventEnricher = eventEnricherInstance();
        eventBus = eventBusInstance(storageFactory, eventEnricher);
        final BoundedContext boundedContext = boundedContextInstance(eventBus, storageFactorySwitch);
        repository = new LabelledTasksViewRepository(boundedContext);
        repository.initStorage(storageFactory);
        boundedContext.register(repository);
    }

    @Nested
    @DisplayName("LabelAssignedToTask event")
    class LabelAssignedToTaskEvent {

        @Test
        @DisplayName(ADDS_TASK_VIEW_TO_STATE)
        public void addView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);
            LabelledTasksView labelledTaskView = repository.load(LABEL_ID)
                                                           .get()
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
                                         .get()
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
    }

    @Nested
    @DisplayName(LABEL_REMOVED_FROM_TASK_EVENT)
    class LabelRemovedFromTaskEvent {

        @Test
        @DisplayName(REMOVES_TASK_VIEW_FORM_STATE)
        public void removesView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);
            eventBus.post(labelAssignedToTaskEvent);

            LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                            .get()
                                                            .getState();
            assertEquals(LABEL_ID, labelledTasksView.getLabelId());

            final LabelRemovedFromTask labelRemovedFromTask = labelRemovedFromTaskInstance();
            final Event labelRemovedFromTaskEvent = createEvent(labelRemovedFromTask, eventContext);
            eventBus.post(labelRemovedFromTaskEvent);

            labelledTasksView = repository.load(LABEL_ID)
                                          .get()
                                          .getState();
            doesNotMatchValues(labelledTasksView);

            final TaskListView labelledTasks = labelledTasksView.getLabelledTasks();

            assertTrue(labelledTasks.getItemsList()
                                    .isEmpty());
        }
    }

    @Nested
    @DisplayName("LabelledTaskRestored event")
    class LabelledTaskRestoredEvent {

        @Test
        @DisplayName(ADDS_TASK_VIEW_TO_STATE)
        public void addsView() {
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
                                                            .get()
                                                            .getState();
            matchesExpectedValues(labelledTasksView);

            TaskListView listView = labelledTasksView.getLabelledTasks();
            int actualListSize = listView.getItemsCount();

            int expectedListSize = 1;
            assertEquals(expectedListSize, actualListSize);
            final TaskView taskView = listView.getItems(0);
            assertEquals(TASK_ID, taskView.getId());

            eventBus.post(deletedTaskRestoredEvent);
            labelledTasksView = repository.load(LABEL_ID)
                                          .get()
                                          .getState();
            matchesExpectedValues(labelledTasksView);
            listView = repository.load(LABEL_ID)
                                 .get()
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
    }

    @Nested
    @DisplayName(TASK_DELETED_EVENT)
    class TaskDeletedEvent {

        @Test
        @DisplayName(REMOVES_TASK_VIEW_FORM_STATE)
        public void removesView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDeleted taskDeleted = taskDeletedInstance();
            final Event deletedTaskEvent = createEvent(taskDeleted, eventContext);
            eventBus.post(deletedTaskEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
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
                                 .get()
                                 .getState()
                                 .getLabelledTasks();
            actualListSize = listView.getItemsCount();

            eventBus.post(deletedTaskEvent);
            assertEquals(expectedListSize, actualListSize);
            assertTrue(listView.getItemsList()
                               .isEmpty());
        }
    }

    @Nested
    @DisplayName(TASK_DESCRIPTION_UPDATED_EVENT)
    class UpdateTaskDescriptionEvent {

        @Test
        @DisplayName(UPDATES_TASK_DESCRIPTION)
        public void updatesDescription() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDescriptionUpdated taskDescriptionUpdated = taskDescriptionUpdatedInstance();
            final Event descriptionUpdatedEvent = createEvent(taskDescriptionUpdated, eventContext);
            eventBus.post(descriptionUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
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
        @DisplayName(DOES_NOT_UPDATE_TASK_DESCRIPTION_BY_WRONG_TASK_ID)
        public void doesNotUpdateDescription() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDescriptionUpdated taskDescriptionUpdated =
                    taskDescriptionUpdatedInstance(TaskId.getDefaultInstance(), UPDATED_DESCRIPTION);
            final Event descriptionUpdatedEvent = createEvent(taskDescriptionUpdated, eventContext);
            eventBus.post(descriptionUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            int actualListSize = listView.getItemsCount();

            final int expectedListSize = 1;
            assertEquals(expectedListSize, actualListSize);

            final TaskView taskView = listView.getItems(0);

            assertNotEquals(UPDATED_DESCRIPTION, taskView.getDescription());
        }

    }

    @Nested
    @DisplayName(TASK_PRIORITY_UPDATED_EVENT)
    class TaskPriorityUpdatedEvent {

        @Test
        @DisplayName(UPDATES_TASK_PRIORITY)
        public void updatesPriority() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskPriorityUpdated taskPriorityUpdated = taskPriorityUpdatedInstance();
            final Event taskPriorityUpdatedEvent = createEvent(taskPriorityUpdated, eventContext);
            eventBus.post(taskPriorityUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
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
        @DisplayName(DOES_NOT_UPDATE_TASK_PRIORITY_BY_WRONG_TASK_ID)
        public void doesNotUpdatePriority() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskPriorityUpdated taskPriorityUpdated =
                    taskPriorityUpdatedInstance(TaskId.getDefaultInstance(), UPDATED_TASK_PRIORITY);
            final Event taskPriorityUpdatedEvent = createEvent(taskPriorityUpdated, eventContext);
            eventBus.post(taskPriorityUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();

            final int expectedListSize = 1;
            assertEquals(expectedListSize, actualListSize);

            final TaskView taskView = listView.getItems(0);

            assertNotEquals(UPDATED_TASK_PRIORITY, taskView.getPriority());
        }
    }

    @Nested
    @DisplayName(TASK_DUE_DATE_UPDATED_EVENT)
    class TaskDueDateUpdatedEvent {

        @Test
        @DisplayName(UPDATES_TASK_DUE_DATE)
        public void updatesDueDate() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDueDateUpdated taskDueDateUpdated = taskDueDateUpdatedInstance();
            final Event taskDueDateUpdatedEvent = createEvent(taskDueDateUpdated, eventContext);
            eventBus.post(taskDueDateUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
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
        @DisplayName(DOES_NOT_UPDATE_TASK_DUE_DATE)
        public void doesNotUpdateDueDate() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDueDateUpdated taskDueDateUpdated =
                    taskDueDateUpdatedInstance(TaskId.getDefaultInstance(), UPDATED_TASK_DUE_DATE);
            final Event taskDueDateUpdatedEvent = createEvent(taskDueDateUpdated, eventContext);
            eventBus.post(taskDueDateUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();

            final int expectedListSize = 1;
            assertEquals(expectedListSize, actualListSize);

            final TaskView taskView = listView.getItems(0);

            assertNotEquals(UPDATED_TASK_DUE_DATE, taskView.getDueDate());
        }
    }

    @Nested
    @DisplayName(TASK_COMPLETED_EVENT)
    class TaskCompletedEvent {

        @Test
        @DisplayName(UPDATES_TASK_VIEW_COMPLETED_FLAG_TO_TRUE)
        public void updatesCompletedFlagToTrue() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
            eventBus.post(taskCompletedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
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
        @DisplayName(UPDATES_TASK_VIEW_COMPLETED_FLAG_TO_FALSE)
        public void updatesCompletedFlagToFalse() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
            eventBus.post(taskCompletedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();

            final int expectedListSize = 1;
            assertEquals(expectedListSize, actualListSize);

            final TaskView taskView = listView.getItems(0);

            assertTrue(taskView.getCompleted());
        }
    }

    @Nested
    @DisplayName(TASK_REOPENED_EVENT)
    class TaskReopenedEvent {

        @Test
        @DisplayName(UPDATES_TASK_VIEW_COMPLETED_FLAG_TO_FALSE)
        public void updatesCompletedFlagToFalse() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
            eventBus.post(taskCompletedEvent);

            final TaskReopened taskReopened = taskReopenedInstance();
            final Event taskReopenedEvent = createEvent(taskReopened, eventContext);
            eventBus.post(taskReopenedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
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
        @DisplayName(UPDATES_TASK_VIEW_COMPLETED_FLAG_TO_TRUE)
        public void updatesCompletedFlagToTrue() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
            eventBus.post(taskCompletedEvent);

            final TaskReopened taskReopened = taskReopenedInstance(TaskId.getDefaultInstance());
            final Event taskReopenedEvent = createEvent(taskReopened, eventContext);
            eventBus.post(taskReopenedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();

            final int expectedListSize = 1;
            assertEquals(expectedListSize, actualListSize);

            final TaskView taskView = listView.getItems(0);

            assertTrue(taskView.getCompleted());
        }
    }

    @Nested
    @DisplayName(LABEL_DETAILS_UPDATED_EVENT)
    class LabelDetailsUpdatedEvent {

        private static final String UPDATED_LABEL_TITLE = "Updated label title.";

        @Test
        @DisplayName("updates LabelDetails")
        public void updatesLabelDetails() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final LabelDetailsUpdated labelDetailsUpdated =
                    labelDetailsUpdatedInstance(LABEL_ID, LabelColor.RED, UPDATED_LABEL_TITLE);
            final Event labelDetailsUpdatedEvent = createEvent(labelDetailsUpdated, eventContext);
            eventBus.post(labelDetailsUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            assertEquals(LABEL_ID, labelledTasksView.getLabelId());
            assertEquals(UPDATED_LABEL_TITLE, labelledTasksView.getLabelTitle());
            assertEquals(LabelColorView.RED_COLOR.getHexColor(), labelledTasksView.getLabelColor());
        }

        @Test
        @DisplayName("does not update LabelDetails by wrong task ID")
        public void doesNotUpdateLabelDetails() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final LabelId wrongLabelId = LabelId.newBuilder()
                                                .setValue(newUuid())
                                                .build();

            final LabelDetailsUpdated labelDetailsUpdated =
                    labelDetailsUpdatedInstance(wrongLabelId, LabelColor.RED, UPDATED_LABEL_TITLE);
            final Event labelDetailsUpdatedEvent = createEvent(labelDetailsUpdated, eventContext);
            eventBus.post(labelDetailsUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            assertEquals(LABEL_ID, labelledTasksView.getLabelId());
            assertNotEquals(UPDATED_LABEL_TITLE, labelledTasksView.getLabelTitle());
            assertNotEquals(LabelColorView.RED_COLOR.getHexColor(), labelledTasksView.getLabelColor());
        }
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
