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

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.events.TaskCompleted;
import org.spine3.examples.todolist.c.events.TaskCreated;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.examples.todolist.c.events.TaskReopened;
import org.spine3.protobuf.Timestamps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestEventFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestEventFactory.LABEL_ID;
import static org.spine3.examples.todolist.testdata.TestEventFactory.TASK_ID;
import static org.spine3.examples.todolist.testdata.TestEventFactory.TASK_PRIORITY;
import static org.spine3.examples.todolist.testdata.TestEventFactory.UPDATED_DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelAssignedToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelDetailsUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskCompletedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskCreatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDeletedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDescriptionUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDueDateUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskPriorityUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskReopenedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.UPDATED_LABEL_TITLE;

/**
 * @author Illia Shepilov
 */
public class MyListViewProjectionTest extends ProjectionTest {

    private MyListViewProjection projection;

    @BeforeEach
    void setUp() {
        final TaskListId taskListId = createTaskListId();
        projection = new MyListViewProjection(taskListId);
    }

    @Nested
    @DisplayName("TaskCreated event")
    class TaskCreatedEvent {

        @Test
        @DisplayName(ADDS_TASK_VIEW_TO_STATE)
        public void addsView() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            final int expectedSize = 1;
            projection.on(taskCreatedEvent);

            final List<TaskView> views = projection.getState()
                                                   .getMyList()
                                                   .getItemsList();

            assertEquals(expectedSize, views.size());

            final TaskView view = views.get(0);

            assertEquals(TASK_PRIORITY, view.getPriority());
            assertEquals(DESCRIPTION, view.getDescription());
        }
    }

    @Nested
    @DisplayName(TASK_DELETED_EVENT)
    class TaskDeletedEvent {

        @Test
        @DisplayName(REMOVES_TASK_VIEW_FORM_STATE)
        public void removesView() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);
            projection.on(taskCreatedEvent);

            final TaskDeleted taskDeletedEvent = taskDeletedInstance();
            projection.on(taskDeletedEvent);

            List<TaskView> views = projection.getState()
                                             .getMyList()
                                             .getItemsList();
            final int expectedListSize = 1;
            assertEquals(expectedListSize, views.size());

            projection.on(taskDeletedEvent);
            views = projection.getState()
                              .getMyList()
                              .getItemsList();
            assertTrue(views.isEmpty());
        }
    }

    @Nested
    @DisplayName(TASK_DESCRIPTION_UPDATED_EVENT)
    class TaskDescriptionUpdatedEvent {

        @Test
        @DisplayName(UPDATES_TASK_DESCRIPTION)
        public void updatesDescription() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final TaskId expectedTaskId = taskCreatedEvent.getId();
            final TaskDescriptionUpdated descriptionUpdatedEvent =
                    taskDescriptionUpdatedInstance(expectedTaskId, UPDATED_DESCRIPTION);
            projection.on(descriptionUpdatedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();

            final int expectedViewSize = 1;
            assertEquals(expectedViewSize, taskListView.getItemsCount());

            final TaskView view = taskListView.getItemsList()
                                              .get(0);
            assertEquals(expectedTaskId, view.getId());
            assertEquals(UPDATED_DESCRIPTION, view.getDescription());
        }

        @Test
        @DisplayName(DOES_NOT_UPDATE_TASK_DESCRIPTION_BY_WRONG_TASK_ID)
        public void doesNotUpdateDescription() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final String updatedDescription = UPDATED_DESCRIPTION;

            final TaskDescriptionUpdated descriptionUpdatedEvent =
                    taskDescriptionUpdatedInstance(TaskId.getDefaultInstance(), updatedDescription);
            projection.on(descriptionUpdatedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView view = taskListView.getItemsList()
                                              .get(0);
            assertEquals(TASK_ID, view.getId());
            assertNotEquals(updatedDescription, view.getDescription());
        }
    }

    @Nested
    @DisplayName(TASK_DUE_DATE_UPDATED_EVENT)
    class TaskDueDateUpdatedEvent {

        @Test
        @DisplayName(UPDATES_TASK_DUE_DATE)
        public void updatesDueDate() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final Timestamp updatedDueDate = Timestamps.getCurrentTime();
            final TaskId expectedTaskId = taskCreatedEvent.getId();

            final TaskDueDateUpdated taskDueDateUpdatedEvent = taskDueDateUpdatedInstance(expectedTaskId, updatedDueDate);
            projection.on(taskDueDateUpdatedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(expectedTaskId, taskView.getId());
            assertEquals(updatedDueDate, taskView.getDueDate());
        }

        @Test
        @DisplayName(DOES_NOT_UPDATE_TASK_DUE_DATE)
        public void doeNotUpdateDueDate() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final Timestamp updatedDueDate = Timestamps.getCurrentTime();

            final TaskDueDateUpdated taskDueDateUpdatedEvent =
                    taskDueDateUpdatedInstance(TaskId.getDefaultInstance(), updatedDueDate);
            projection.on(taskDueDateUpdatedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertNotEquals(updatedDueDate, taskView.getDueDate());
        }
    }

    @Nested
    @DisplayName(TASK_PRIORITY_UPDATED_EVENT)
    class TaskPriorityUpdatedEvent {

        @Test
        @DisplayName(UPDATES_TASK_PRIORITY)
        public void updatesPriority() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final TaskPriority updatedTaskPriority = TaskPriority.LOW;
            final TaskId expectedTaskId = taskCreatedEvent.getId();

            final TaskPriorityUpdated taskPriorityUpdatedEvent =
                    taskPriorityUpdatedInstance(expectedTaskId, updatedTaskPriority);
            projection.on(taskPriorityUpdatedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(expectedTaskId, taskView.getId());
            assertEquals(updatedTaskPriority, taskView.getPriority());
        }

        @Test
        @DisplayName(DOES_NOT_UPDATE_TASK_PRIORITY_BY_WRONG_TASK_ID)
        public void doesNotUpdatePriority() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final TaskPriority updatedTaskPriority = TaskPriority.LOW;

            final TaskPriorityUpdated taskPriorityUpdatedEvent =
                    taskPriorityUpdatedInstance(TaskId.getDefaultInstance(), updatedTaskPriority);
            projection.on(taskPriorityUpdatedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(TASK_ID, taskView.getId());
            assertNotEquals(updatedTaskPriority, taskView.getPriority());
        }
    }

    @Nested
    @DisplayName(LABEL_DETAILS_UPDATED_EVENT)
    class LabelDetailsUpdatedEvent {

        @Test
        @DisplayName(UPDATES_LABEL_DETAILS)
        public void updatesLabelDetails() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final TaskId expectedTaskId = taskCreatedEvent.getId();

            final LabelAssignedToTask labelAssignedToTaskEvent = labelAssignedToTaskInstance(expectedTaskId, LABEL_ID);
            projection.on(labelAssignedToTaskEvent);

            final LabelColor updatedColor = LabelColor.BLUE;

            final LabelDetailsUpdated labelDetailsUpdatedEvent =
                    labelDetailsUpdatedInstance(LABEL_ID, updatedColor, UPDATED_LABEL_TITLE);
            projection.on(labelDetailsUpdatedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(LABEL_ID, taskView.getLabelId());
            assertEquals(updatedColor, taskView.getLabelColor());
        }

        @Test
        @DisplayName(DOES_NOT_UPDATE_LABEL_DETAILS)
        public void doesNotUpdateLabelDetails() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final TaskId expectedTaskId = taskCreatedEvent.getId();

            final LabelAssignedToTask labelAssignedToTaskEvent =
                    labelAssignedToTaskInstance(expectedTaskId, LabelId.getDefaultInstance());
            projection.on(labelAssignedToTaskEvent);

            final LabelColor updatedColor = LabelColor.BLUE;

            final LabelDetailsUpdated labelDetailsUpdatedEvent =
                    labelDetailsUpdatedInstance(LABEL_ID, updatedColor, UPDATED_LABEL_TITLE);
            projection.on(labelDetailsUpdatedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(expectedTaskId, taskView.getId());
            assertNotEquals(updatedColor, taskView.getLabelColor());
        }
    }

    @Nested
    @DisplayName(TASK_COMPLETED_EVENT)
    class TaskCompletedEvent {

        @Test
        @DisplayName("completes task")
        public void completesTask() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final TaskCompleted taskCompletedEvent = taskCompletedInstance();
            projection.on(taskCompletedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(TASK_ID, taskView.getId());
            assertTrue(taskView.getCompleted());
        }

        @Test
        @DisplayName("does not complete task by wrong task ID")
        public void doesNotComplete() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final TaskCompleted taskCompletedEvent = taskCompletedInstance(TaskId.getDefaultInstance());
            projection.on(taskCompletedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(TASK_ID, taskView.getId());
            assertFalse(taskView.getCompleted());
        }
    }

    @Nested
    @DisplayName(TASK_REOPENED_EVENT)
    class TaskReopenedEvent {

        @Test
        @DisplayName("reopens task")
        public void reopensTask() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final TaskCompleted taskCompletedEvent = taskCompletedInstance();
            projection.on(taskCompletedEvent);

            final TaskReopened taskReopenedEvent = taskReopenedInstance();
            projection.on(taskReopenedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(TASK_ID, taskView.getId());
            assertFalse(taskView.getCompleted());
        }

        @Test
        @DisplayName("does not reopen task by wrong task ID")
        public void doesNotReopenTask() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            projection.on(taskCreatedEvent);

            final TaskCompleted taskCompletedEvent = taskCompletedInstance();
            projection.on(taskCompletedEvent);

            final TaskReopened taskReopenedEvent = taskReopenedInstance(TaskId.getDefaultInstance());
            projection.on(taskReopenedEvent);

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            final int expectedViewListSize = 1;
            assertEquals(expectedViewListSize, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(TASK_ID, taskView.getId());
            assertTrue(taskView.getCompleted());
        }
    }
}
