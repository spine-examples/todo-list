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

package io.spine.examples.todolist.q.projection;

import io.spine.examples.todolist.repository.LabelledTasksViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import io.spine.base.Event;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.events.LabelledTaskRestored;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;
import io.spine.server.BoundedContext;
import io.spine.server.event.EventBus;
import io.spine.server.event.enrich.EventEnricher;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.StorageFactorySwitch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.spine.base.Identifiers.newUuid;
import static io.spine.examples.todolist.testdata.TestBoundedContextFactory.boundedContextInstance;
import static io.spine.examples.todolist.testdata.TestEventBusFactory.eventBusInstance;
import static io.spine.examples.todolist.testdata.TestEventEnricherFactory.LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestEventEnricherFactory.eventEnricherInstance;
import static io.spine.examples.todolist.testdata.TestLabelEventFactory.labelDetailsUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.labelledTaskRestoredInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskCompletedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskDeletedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskReopenedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.LABEL_ID;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.TASK_ID;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UPDATED_DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UPDATED_TASK_DUE_DATE;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UPDATED_TASK_PRIORITY;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskDescriptionUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskDueDateUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskPriorityUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsEventFactory.labelAssignedToTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsEventFactory.labelRemovedFromTaskInstance;

/**
 * @author Illia Shepilov
 */
class LabelledTasksViewProjectionTest extends ProjectionTest {

    private ProjectionRepository<LabelId, LabelledTasksViewProjection,
            LabelledTasksView> repository;
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        final StorageFactorySwitch storageFactorySwitch = StorageFactorySwitch.getInstance(false);
        final StorageFactory storageFactory = storageFactorySwitch.get();
        final EventEnricher eventEnricher = eventEnricherInstance();
        eventBus = eventBusInstance(storageFactory, eventEnricher);
        final BoundedContext boundedContext = boundedContextInstance(eventBus,
                                                                     storageFactorySwitch);
        repository = new LabelledTasksViewRepository(boundedContext);
        repository.initStorage(storageFactory);
        boundedContext.register(repository);
    }

    @Nested
    @DisplayName("LabelAssignedToTask event should be interpreted " +
            "by LabelledTasksViewProjection and")
    class LabelAssignedToTaskEvent {

        @Test
        @DisplayName("add TaskView to LabelledTasksView")
        void addView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);
            LabelledTasksView labelledTaskView = repository.find(LABEL_ID)
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

            labelledTaskView = repository.find(LABEL_ID)
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
    @DisplayName("LabelRemovedFromTask event should be interpreted by " +
            "LabelledTasksViewProjection and")
    class LabelRemovedFromTaskEvent {

        @Test
        @DisplayName("remove TaskView from LabelledTasksView")
        void removeView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);
            eventBus.post(labelAssignedToTaskEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            assertEquals(LABEL_ID, labelledTasksView.getLabelId());
            assertEquals(2, labelledTasksView.getLabelledTasks()
                                             .getItemsList()
                                             .size());

            final LabelRemovedFromTask labelRemovedFromTask = labelRemovedFromTaskInstance();
            final Event labelRemovedFromTaskEvent = createEvent(labelRemovedFromTask);
            eventBus.post(labelRemovedFromTaskEvent);

            final LabelledTasksView updatedLabelledTaskView = repository.find(LABEL_ID)
                                                                        .get()
                                                                        .getState();
            final TaskListView labelledTasks = updatedLabelledTaskView.getLabelledTasks();

            assertTrue(labelledTasks.getItemsList()
                                    .isEmpty());
        }
    }

    @Nested
    @DisplayName("LabelledTaskRestored event should be interpreted by " +
            "LabelledTasksViewProjection and")
    class LabelledTaskRestoredEvent {

        @Test
        @DisplayName("add TaskView to LabelledTasksView")
        void addView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);
            eventBus.post(labelAssignedToTaskEvent);

            final LabelRemovedFromTask labelRemovedFromTask = labelRemovedFromTaskInstance();
            final Event labelRemovedFromTaskEvent = createEvent(labelRemovedFromTask);
            eventBus.post(labelRemovedFromTaskEvent);

            final LabelledTaskRestored deletedTaskRestored = labelledTaskRestoredInstance();
            final Event deletedTaskRestoredEvent = createEvent(deletedTaskRestored);
            eventBus.post(deletedTaskRestoredEvent);

            LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
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
            labelledTasksView = repository.find(LABEL_ID)
                                          .get()
                                          .getState();
            matchesExpectedValues(labelledTasksView);
            listView = repository.find(LABEL_ID)
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
    @DisplayName("TaskDeleted event should be interpreted by LabelledTasksViewProjection and")
    class TaskDeletedEvent {

        @Test
        @DisplayName("remove TaskView from LabelledTasksView")
        void removesView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDeleted taskDeleted = taskDeletedInstance();
            final Event deletedTaskEvent = createEvent(taskDeleted);
            eventBus.post(deletedTaskEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);

            TaskListView listView = labelledTasksView.getLabelledTasks();
            assertTrue(listView.getItemsList()
                               .isEmpty());
        }
    }

    @Nested
    @DisplayName("TaskDescriptionUpdated event should be interpreted by " +
            "LabelledTasksViewProjection and")
    class UpdateTaskDescriptionEvent {

        @Test
        @DisplayName("update the task description in LabelledTasksView")
        void updateDescription() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDescriptionUpdated taskDescriptionUpdated = taskDescriptionUpdatedInstance();
            final Event descriptionUpdatedEvent = createEvent(taskDescriptionUpdated);
            eventBus.post(descriptionUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);

            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertEquals(UPDATED_DESCRIPTION, taskView.getDescription());
        }

        @Test
        @DisplayName("not update the task description in LabelledTasksView by wrong task ID")
        void notUpdateDescription() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDescriptionUpdated taskDescriptionUpdated =
                    taskDescriptionUpdatedInstance(TaskId.getDefaultInstance(),
                                                   UPDATED_DESCRIPTION);
            final Event descriptionUpdatedEvent = createEvent(taskDescriptionUpdated);
            eventBus.post(descriptionUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertNotEquals(UPDATED_DESCRIPTION, taskView.getDescription());
        }

    }

    @Nested
    @DisplayName("TaskPriorityUpdated event should be interpreted by " +
            "LabelledTasksViewProjection and")
    class TaskPriorityUpdatedEvent {

        @Test
        @DisplayName("update the task priority in LabelledTasksView")
        void updatePriority() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskPriorityUpdated taskPriorityUpdated = taskPriorityUpdatedInstance();
            final Event taskPriorityUpdatedEvent = createEvent(taskPriorityUpdated);
            eventBus.post(taskPriorityUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertEquals(UPDATED_TASK_PRIORITY, taskView.getPriority());
        }

        @Test
        @DisplayName("not update the task priority in LabelledTasksView by wrong task ID")
        void notUpdatePriority() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskPriorityUpdated taskPriorityUpdated =
                    taskPriorityUpdatedInstance(TaskId.getDefaultInstance(), UPDATED_TASK_PRIORITY);
            final Event taskPriorityUpdatedEvent = createEvent(taskPriorityUpdated);
            eventBus.post(taskPriorityUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertNotEquals(UPDATED_TASK_PRIORITY, taskView.getPriority());
        }
    }

    @Nested
    @DisplayName("TaskDueDateUpdated event should be interpreted by " +
            "LabelledTasksViewProjection and")
    class TaskDueDateUpdatedEvent {

        @Test
        @DisplayName("update the task due date in LabelledTasksView")
        void updateDueDate() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDueDateUpdated taskDueDateUpdated = taskDueDateUpdatedInstance();
            final Event taskDueDateUpdatedEvent = createEvent(taskDueDateUpdated);
            eventBus.post(taskDueDateUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertEquals(UPDATED_TASK_DUE_DATE, taskView.getDueDate());
        }

        @Test
        @DisplayName("not update the task due date in LabelledTasksView by wrong task ID")
        void doesNotUpdateDueDate() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDueDateUpdated taskDueDateUpdated =
                    taskDueDateUpdatedInstance(TaskId.getDefaultInstance(), UPDATED_TASK_DUE_DATE);
            final Event taskDueDateUpdatedEvent = createEvent(taskDueDateUpdated);
            eventBus.post(taskDueDateUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertNotEquals(UPDATED_TASK_DUE_DATE, taskView.getDueDate());
        }
    }

    @Nested
    @DisplayName("TaskCompleted event should be interpreted by LabelledTasksViewProjection and")
    class TaskCompletedEvent {

        @Test
        @DisplayName("set `completed` to true in LabelledTasksView")
        void setCompletedFlagToTrue() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted);
            eventBus.post(taskCompletedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertTrue(taskView.getCompleted());
        }

        @Test
        @DisplayName("set `completed` to false in LabelledTasksView")
        void setCompletedFlagToFalse() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted);
            eventBus.post(taskCompletedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertTrue(taskView.getCompleted());
        }
    }

    @Nested
    @DisplayName("TaskReopened event should be interpreted by LabelledTasksViewProjection and")
    class TaskReopenedEvent {

        @Test
        @DisplayName("set `completed` to `false` in LabelledTasksView")
        void setCompletedFlagToFalse() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted);
            eventBus.post(taskCompletedEvent);

            final TaskReopened taskReopened = taskReopenedInstance();
            final Event taskReopenedEvent = createEvent(taskReopened);
            eventBus.post(taskReopenedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertFalse(taskView.getCompleted());
        }

        @Test
        @DisplayName("set `completed` to `true` in LabelledTasksView")
        void setCompletedFlagToTrue() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted);
            eventBus.post(taskCompletedEvent);

            final TaskReopened taskReopened = taskReopenedInstance(TaskId.getDefaultInstance());
            final Event taskReopenedEvent = createEvent(taskReopened);
            eventBus.post(taskReopenedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertTrue(taskView.getCompleted());
        }
    }

    @Nested
    @DisplayName("LabelDetailsUpdated event should be interpreted by " +
            "LabelledTasksViewProjection and")
    class LabelDetailsUpdatedEvent {

        private static final String UPDATED_LABEL_TITLE = "Updated label title.";

        @Test
        @DisplayName("update the label details in LabelledTasksView")
        void updateLabelDetails() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final LabelDetailsUpdated labelDetailsUpdated =
                    labelDetailsUpdatedInstance(LABEL_ID, LabelColor.RED, UPDATED_LABEL_TITLE);
            final Event labelDetailsUpdatedEvent = createEvent(labelDetailsUpdated);
            eventBus.post(labelDetailsUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            assertEquals(LABEL_ID, labelledTasksView.getLabelId());
            assertEquals(UPDATED_LABEL_TITLE, labelledTasksView.getLabelTitle());
            assertEquals(LabelColorView.RED_COLOR.getHexColor(), labelledTasksView.getLabelColor());
        }

        @Test
        @DisplayName("not update the label details in LabelledTasksView by wrong task ID")
        void notUpdateLabelDetails() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask);
            eventBus.post(labelAssignedToTaskEvent);

            final LabelId wrongLabelId = LabelId.newBuilder()
                                                .setValue(newUuid())
                                                .build();

            final LabelDetailsUpdated labelDetailsUpdated =
                    labelDetailsUpdatedInstance(wrongLabelId, LabelColor.RED, UPDATED_LABEL_TITLE);
            final Event labelDetailsUpdatedEvent = createEvent(labelDetailsUpdated);
            eventBus.post(labelDetailsUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.find(LABEL_ID)
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
