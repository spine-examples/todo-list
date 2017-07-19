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
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskListId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.events.TaskDraftFinalized;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.UPDATED_LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestLabelEventFactory.labelDetailsUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskDeletedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskDraftCreatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskDraftFinalizedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.LABEL_ID;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.TASK_ID;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.TASK_PRIORITY;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UPDATED_DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskDescriptionUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskDueDateUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskPriorityUpdatedInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsEventFactory.labelAssignedToTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsEventFactory.labelRemovedFromTaskInstance;
import static io.spine.server.projection.ProjectionEventDispatcher.dispatch;
import static io.spine.time.Time.getCurrentTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
class DraftTasksViewProjectionTest extends ProjectionTest {

    private DraftTasksViewProjection projection;

    @BeforeEach
    void setUp() {
        final TaskListId taskListId = createTaskListId();
        projection = new DraftTasksViewProjection(taskListId);
    }

    private void taskDraftCreated() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        dispatch(projection, createEvent(taskDraftCreatedEvent));
    }

    @Nested
    @DisplayName("TaskDraftCreated event should be interpreted by DraftTasksViewProjection and")
    class TaskDraftCreatedEvent {

        @Test
        @DisplayName("add TaskView to DraftTasksView")
        void addView() {
            taskDraftCreated();

            final TaskListView listView = projection.getState()
                                                    .getDraftTasks();
            assertEquals(1, listView.getItemsCount());

            final TaskView taskView = listView.getItems(0);
            assertEquals(TASK_ID, taskView.getId());
            assertEquals(DESCRIPTION, taskView.getDescription());
            assertEquals(TASK_PRIORITY, taskView.getPriority());
        }
    }

    @Nested
    @DisplayName("TaskDraftFinalized event should be interpreted by DraftTasksViewProjection and")
    class TaskDraftFinalizedEvent {

        @Test
        @DisplayName("remove TaskView from DraftTasksView")
        void removeView() {
            taskDraftCreated();

            final TaskDraftFinalized taskDraftFinalizedEvent = taskDraftFinalizedInstance();
            dispatch(projection, createEvent(taskDraftFinalizedEvent));

            final List<TaskView> views = projection.getState()
                                                   .getDraftTasks()
                                                   .getItemsList();
            assertTrue(views.isEmpty());
        }
    }

    @Nested
    @DisplayName("TaskDeleted event should")
    class TaskDeletedEvent {

        @Test
        @DisplayName("remove TaskView from DraftTasksView")
        void removeView() {
            taskDraftCreated();

            final TaskDeleted taskDeletedEvent = taskDeletedInstance();
            dispatch(projection, createEvent(taskDeletedEvent));

            final List<TaskView> views = projection.getState()
                                                   .getDraftTasks()
                                                   .getItemsList();
            assertTrue(views.isEmpty());
        }
    }

    @Nested
    @DisplayName("TaskDescriptionUpdated event should")
    class TaskDescriptionUpdatedEvent {

        @Test
        @DisplayName("update the task description in DraftTaskView")
        void updateDescription() {
            final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
            dispatch(projection, createEvent(taskDraftCreatedEvent));

            final TaskId expectedTaskId = taskDraftCreatedEvent.getId();
            final TaskDescriptionUpdated descriptionUpdatedEvent =
                    taskDescriptionUpdatedInstance(expectedTaskId, UPDATED_DESCRIPTION);
            dispatch(projection, createEvent(descriptionUpdatedEvent));

            final TaskListView taskListView = projection.getState()
                                                        .getDraftTasks();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView view = taskListView.getItemsList()
                                              .get(0);
            assertEquals(expectedTaskId, view.getId());
            assertEquals(UPDATED_DESCRIPTION, view.getDescription());
        }

        @Test
        @DisplayName("not update the task description by wrong task ID")
        void notUpdateDescription() {
            taskDraftCreated();

            final TaskDescriptionUpdated descriptionUpdatedEvent =
                    taskDescriptionUpdatedInstance(TaskId.getDefaultInstance(), UPDATED_DESCRIPTION);
            dispatch(projection, createEvent(descriptionUpdatedEvent));

            final TaskListView taskListView = projection.getState()
                                                        .getDraftTasks();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView view = taskListView.getItemsList()
                                              .get(0);
            assertEquals(TASK_ID, view.getId());
            assertNotEquals(UPDATED_DESCRIPTION, view.getDescription());
        }
    }

    @Nested
    @DisplayName("TaskDueDateUpdated event should be interpreted by DraftTasksViewProjection and")
    class DueDateUpdatedEvent {

        @Test
        @DisplayName("update the task due date in DraftTaskView")
        void updateDueDate() {
            final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
            dispatch(projection, createEvent(taskDraftCreatedEvent));

            final Timestamp updatedDueDate = getCurrentTime();
            final TaskId expectedTaskId = taskDraftCreatedEvent.getId();

            final TaskDueDateUpdated taskDueDateUpdatedEvent =
                    taskDueDateUpdatedInstance(expectedTaskId, updatedDueDate);
            dispatch(projection, createEvent(taskDueDateUpdatedEvent));

            final TaskListView taskListView = projection.getState()
                                                        .getDraftTasks();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(expectedTaskId, taskView.getId());
            assertEquals(updatedDueDate, taskView.getDueDate());
        }

        @Test
        @DisplayName("not update the task due date by wrong task ID")
        void notUpdate() {
            taskDraftCreated();

            final Timestamp updatedDueDate = getCurrentTime();

            final TaskDueDateUpdated taskDueDateUpdatedEvent =
                    taskDueDateUpdatedInstance(TaskId.getDefaultInstance(), updatedDueDate);
            dispatch(projection, createEvent(taskDueDateUpdatedEvent));

            final TaskListView taskListView = projection.getState()
                                                        .getDraftTasks();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(TASK_ID, taskView.getId());
            assertNotEquals(updatedDueDate, taskView.getDueDate());
        }
    }

    @Nested
    @DisplayName("TaskPriorityUpdated event should be interpreted by DraftTasksViewProjection and")
    class TaskPriorityUpdatedEvent {

        @Test
        @DisplayName("update the task priority in DraftTasksView")
        void updatesPriority() {
            final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
            dispatch(projection, createEvent(taskDraftCreatedEvent));

            final TaskPriority updatedTaskPriority = TaskPriority.LOW;
            final TaskId expectedTaskId = taskDraftCreatedEvent.getId();

            final TaskPriorityUpdated taskPriorityUpdatedEvent =
                    taskPriorityUpdatedInstance(expectedTaskId, updatedTaskPriority);
            dispatch(projection, createEvent(taskPriorityUpdatedEvent));

            final TaskListView taskListView = projection.getState()
                                                        .getDraftTasks();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(expectedTaskId, taskView.getId());
            assertEquals(updatedTaskPriority, taskView.getPriority());
        }

        @Test
        @DisplayName("not update the task priority by wrong task ID")
        void notUpdatePriority() {
            taskDraftCreated();

            final TaskPriority updatedTaskPriority = TaskPriority.LOW;

            final TaskPriorityUpdated taskPriorityUpdatedEvent =
                    taskPriorityUpdatedInstance(TaskId.getDefaultInstance(), updatedTaskPriority);
            dispatch(projection, createEvent(taskPriorityUpdatedEvent));

            final TaskListView taskListView = projection.getState()
                                                        .getDraftTasks();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(TASK_ID, taskView.getId());
            assertNotEquals(updatedTaskPriority, taskView.getPriority());
        }
    }

    @Nested
    @DisplayName("LabelDetailsUpdated event should be interpreted by DraftTasksViewProjection and")
    class LabelDetailsUpdatedEvent {

        @Test
        @DisplayName("update the label details in DraftTasksView")
        void updateLabelDetails() {
            final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
            dispatch(projection, createEvent(taskDraftCreatedEvent));

            final TaskId expectedTaskId = taskDraftCreatedEvent.getId();

            final LabelAssignedToTask labelAssignedToTaskEvent =
                    labelAssignedToTaskInstance(expectedTaskId, LABEL_ID);
            dispatch(projection, createEvent(labelAssignedToTaskEvent));

            final LabelColor updatedColor = LabelColor.BLUE;

            final LabelDetailsUpdated labelDetailsUpdatedEvent =
                    labelDetailsUpdatedInstance(LABEL_ID, updatedColor, UPDATED_LABEL_TITLE);
            dispatch(projection, createEvent(labelDetailsUpdatedEvent));

            final TaskListView taskListView = projection.getState()
                                                        .getDraftTasks();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(LABEL_ID, taskView.getLabelId());
            assertEquals(updatedColor, taskView.getLabelColor());
        }

        @Test
        @DisplayName("not update label details in DraftTasksView by wrong label id")
        void doesNotUpdateLabelDetails() {
            final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
            dispatch(projection, createEvent(taskDraftCreatedEvent));

            final TaskId expectedTaskId = taskDraftCreatedEvent.getId();
            final LabelId wrongLabelId = LabelId.newBuilder()
                                                .setValue(newUuid())
                                                .build();
            final LabelAssignedToTask labelAssignedToTaskEvent =
                    labelAssignedToTaskInstance(expectedTaskId, wrongLabelId);
            dispatch(projection, createEvent(labelAssignedToTaskEvent));

            final LabelColor updatedColor = LabelColor.BLUE;

            final LabelDetailsUpdated labelDetailsUpdatedEvent =
                    labelDetailsUpdatedInstance(LABEL_ID, updatedColor, UPDATED_LABEL_TITLE);
            dispatch(projection, createEvent(labelDetailsUpdatedEvent));

            final TaskListView taskListView = projection.getState()
                                                        .getDraftTasks();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(TASK_ID, taskView.getId());
            assertNotEquals(updatedColor, taskView.getLabelColor());
        }
    }

    @Nested
    @DisplayName("LabelRemovedFromTask event should be interpreted by DraftTasksViewProjection and")
    class LabelRemovedFromTaskEvent {

        @Test
        @DisplayName("remove TaskView from DraftTasksView")
        void removeView() {
            taskDraftCreated();

            final LabelAssignedToTask labelAssignedToTaskEvent = labelAssignedToTaskInstance();
            dispatch(projection, createEvent(labelAssignedToTaskEvent));

            final LabelRemovedFromTask labelRemovedFromTaskEvent = labelRemovedFromTaskInstance();
            dispatch(projection, createEvent(labelRemovedFromTaskEvent));

            final TaskListView taskListView = projection.getState()
                                                        .getDraftTasks();
            assertEquals(1, taskListView.getItemsCount());

            final TaskView taskView = taskListView.getItemsList()
                                                  .get(0);
            assertEquals(TASK_ID, taskView.getId());
            assertNotEquals(LABEL_ID, taskView.getLabelId());
        }
    }
}
