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

import com.google.protobuf.Timestamp;
import io.spine.base.EventContext;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskListId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskCreated;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.UPDATED_LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestLabelEventFactory.labelDetailsUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskCompletedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskCreatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskDeletedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskReopenedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.LABEL_ID;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.TASK_ID;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.TASK_PRIORITY;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UPDATED_DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskDescriptionUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskDueDateUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskPriorityUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsEventFactory.labelAssignedToTaskInstance;
import static io.spine.server.projection.ProjectionEventDispatcher.dispatch;
import static io.spine.time.Time.getCurrentTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
class MyListViewProjectionTest extends ProjectionTest {

    private MyListViewProjection projection;

    @BeforeEach
    void setUp() {
        final TaskListId taskListId = createTaskListId();
        projection = new MyListViewProjection(taskListId);
    }

    @Nested
    @DisplayName("TaskCreated event should be interpreted by MyListViewProjection and")
    class TaskCreatedEvent {

        @Test
        @DisplayName("add TaskView to MyListView")
        void addView() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final List<TaskView> views = projection.getState()
                                                   .getMyList()
                                                   .getItemsList();
            assertEquals(1, views.size());

            final TaskView view = views.get(0);
            assertEquals(TASK_PRIORITY, view.getPriority());
            assertEquals(DESCRIPTION, view.getDescription());
        }
    }

    @Nested
    @DisplayName("TaskDeleted event should be interpreted by MyListViewProjection and")
    class TaskDeletedEvent {

        @Test
        @DisplayName("remove TaskView from MyListView")
        void removeView() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final TaskDeleted taskDeletedEvent = taskDeletedInstance();
            dispatch(projection, taskDeletedEvent, EventContext.getDefaultInstance());

            List<TaskView> views = projection.getState()
                                             .getMyList()
                                             .getItemsList();
            assertTrue(views.isEmpty());
        }
    }

    @Nested
    @DisplayName("TaskDescriptionUpdated event should be interpreted by MyListViewProjection and")
    class TaskDescriptionUpdatedEvent {

        @Test
        @DisplayName("update the task description in MyListView")
        void updateDescription() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final TaskId expectedTaskId = taskCreatedEvent.getId();
            final TaskDescriptionUpdated descriptionUpdatedEvent =
                    taskDescriptionUpdatedInstance(expectedTaskId, UPDATED_DESCRIPTION);
            dispatch(projection, descriptionUpdatedEvent, EventContext.getDefaultInstance());

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView view = taskListView.getItemsList()
                                              .get(0);
            assertEquals(expectedTaskId, view.getId());
            assertEquals(UPDATED_DESCRIPTION, view.getDescription());
        }

        @Test
        @DisplayName("not update the task description in MyListView by wrong task ID")
        void notUpdateDescription() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final String updatedDescription = UPDATED_DESCRIPTION;

            final TaskDescriptionUpdated descriptionUpdatedEvent =
                    taskDescriptionUpdatedInstance(TaskId.getDefaultInstance(), updatedDescription);
            dispatch(projection, descriptionUpdatedEvent, EventContext.getDefaultInstance());

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView view = taskListView.getItemsList()
                                              .get(0);
            assertEquals(TASK_ID, view.getId());
            assertNotEquals(updatedDescription, view.getDescription());
        }
    }

    @Nested
    @DisplayName("TaskDueDateUpdated event should be interpreted by MyListViewProjection and")
    class TaskDueDateUpdatedEvent {

        @Test
        @DisplayName("update the task due date on MyListView")
        void updateDueDate() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final Timestamp updatedDueDate = getCurrentTime();
            final TaskId expectedTaskId = taskCreatedEvent.getId();

            final TaskDueDateUpdated taskDueDateUpdatedEvent =
                    taskDueDateUpdatedInstance(expectedTaskId, updatedDueDate);
            dispatch(projection, taskDueDateUpdatedEvent, EventContext.getDefaultInstance());

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(expectedTaskId, taskView.getId());
            assertEquals(updatedDueDate, taskView.getDueDate());
        }

        @Test
        @DisplayName("not update the task due date in MyListView by wrong task ID")
        void doeNotUpdateDueDate() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final Timestamp updatedDueDate = getCurrentTime();

            final TaskDueDateUpdated taskDueDateUpdatedEvent =
                    taskDueDateUpdatedInstance(TaskId.getDefaultInstance(), updatedDueDate);
            dispatch(projection, taskDueDateUpdatedEvent, EventContext.getDefaultInstance());

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertNotEquals(updatedDueDate, taskView.getDueDate());
        }
    }

    @Nested
    @DisplayName("TaskPriorityUpdated event should be interpreted by MyListViewProjection and")
    class TaskPriorityUpdatedEvent {

        @Test
        @DisplayName("update the task priority in MyListView")
        void updatePriority() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final TaskPriority updatedTaskPriority = TaskPriority.LOW;
            final TaskId expectedTaskId = taskCreatedEvent.getId();

            final TaskPriorityUpdated taskPriorityUpdatedEvent =
                    taskPriorityUpdatedInstance(expectedTaskId, updatedTaskPriority);
            dispatch(projection, taskPriorityUpdatedEvent, EventContext.getDefaultInstance());

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(expectedTaskId, taskView.getId());
            assertEquals(updatedTaskPriority, taskView.getPriority());
        }

        @Test
        @DisplayName("not update the task priority in MyListView by wrong task ID")
        void notUpdatePriority() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final TaskPriority updatedTaskPriority = TaskPriority.LOW;

            final TaskPriorityUpdated taskPriorityUpdatedEvent =
                    taskPriorityUpdatedInstance(TaskId.getDefaultInstance(), updatedTaskPriority);
            dispatch(projection, taskPriorityUpdatedEvent, EventContext.getDefaultInstance());

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(TASK_ID, taskView.getId());
            assertNotEquals(updatedTaskPriority, taskView.getPriority());
        }
    }

    @Nested
    @DisplayName("LabelDetailsUpdated event should be interpreted by MyListViewProjection and")
    class LabelDetailsUpdatedEvent {

        @Test
        @DisplayName("update the label details in MyListView")
        void updateLabelDetails() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final TaskId expectedTaskId = taskCreatedEvent.getId();

            final LabelAssignedToTask labelAssignedToTaskEvent =
                    labelAssignedToTaskInstance(expectedTaskId, LABEL_ID);
            dispatch(projection, labelAssignedToTaskEvent, EventContext.getDefaultInstance());

            final LabelColor updatedColor = LabelColor.BLUE;

            final LabelDetailsUpdated labelDetailsUpdatedEvent =
                    labelDetailsUpdatedInstance(LABEL_ID, updatedColor, UPDATED_LABEL_TITLE);
            dispatch(projection, labelDetailsUpdatedEvent, EventContext.getDefaultInstance());

            final TaskListView taskListView = projection.getState()
                                                        .getMyList();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(LABEL_ID, taskView.getLabelId());
            assertEquals(updatedColor, taskView.getLabelColor());
        }

        @Test
        @DisplayName("not update the label details in MyListView by wrong label ID")
        void doesNotUpdateLabelDetails() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final TaskId expectedTaskId = taskCreatedEvent.getId();

            final LabelAssignedToTask labelAssignedToTaskEvent =
                    labelAssignedToTaskInstance(expectedTaskId, LabelId.getDefaultInstance());
            dispatch(projection, labelAssignedToTaskEvent, EventContext.getDefaultInstance());

            final LabelColor updatedColor = LabelColor.BLUE;

            final LabelDetailsUpdated labelDetailsUpdatedEvent =
                    labelDetailsUpdatedInstance(LABEL_ID, updatedColor, UPDATED_LABEL_TITLE);
            dispatch(projection,labelDetailsUpdatedEvent , EventContext.getDefaultInstance());

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
    @DisplayName("TaskCompleted event should be interpreted by MyListViewProjection and")
    class TaskCompletedEvent {

        @Test
        @DisplayName("set `completed` to `true` in MyListView")
        void completeTask() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final TaskCompleted taskCompletedEvent = taskCompletedInstance();
            dispatch(projection, taskCompletedEvent, EventContext.getDefaultInstance());

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
        @DisplayName("not set `completed` to `true` in MyListView by wrong task ID")
        void doesNotComplete() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final TaskCompleted taskCompletedEvent =
                    taskCompletedInstance(TaskId.getDefaultInstance());
            dispatch(projection, taskCompletedEvent, EventContext.getDefaultInstance());

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
    @DisplayName("TaskReopened event should be interpreted by MyListViewProjection and")
    class TaskReopenedEvent {

        @Test
        @DisplayName("set `completed` to `false` in MyListView")
        void reopenTask() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final TaskCompleted taskCompletedEvent = taskCompletedInstance();
            dispatch(projection, taskCompletedEvent, EventContext.getDefaultInstance());

            final TaskReopened taskReopenedEvent = taskReopenedInstance();
            dispatch(projection, taskReopenedEvent, EventContext.getDefaultInstance());

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
        @DisplayName("not set `completed` to `true` by wrong task ID")
        void notReopenTask() {
            final TaskCreated taskCreatedEvent = taskCreatedInstance();
            dispatch(projection, taskCreatedEvent, EventContext.getDefaultInstance());

            final TaskCompleted taskCompletedEvent = taskCompletedInstance();
            dispatch(projection, taskCompletedEvent, EventContext.getDefaultInstance());

            final TaskReopened taskReopenedEvent =
                    taskReopenedInstance(TaskId.getDefaultInstance());
            dispatch(projection, taskReopenedEvent, EventContext.getDefaultInstance());

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
