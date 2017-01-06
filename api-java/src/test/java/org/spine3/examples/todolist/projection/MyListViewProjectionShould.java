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

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.protobuf.Timestamps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Identifiers.newUuid;
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
public class MyListViewProjectionShould {

    private static final TaskListId ID = TaskListId.newBuilder()
                                                   .setValue(newUuid())
                                                   .build();
    private MyListViewProjection projection;

    @BeforeEach
    void setUp() {
        projection = new MyListViewProjection(ID);
    }

    @Test
    public void add_task_view_to_state_when_task_is_created() {
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

    @Test
    public void remove_task_view_from_state_when_task_is_deleted() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        final TaskDeleted taskDeletedEvent = taskDeletedInstance();
        final int expectedListSize = 1;
        projection.on(taskCreatedEvent);
        projection.on(taskCreatedEvent);
        projection.on(taskDeletedEvent);

        List<TaskView> views = projection.getState()
                                         .getMyList()
                                         .getItemsList();
        assertEquals(expectedListSize, views.size());

        projection.on(taskDeletedEvent);
        views = projection.getState()
                          .getMyList()
                          .getItemsList();
        assertTrue(views.isEmpty());
    }

    @Test
    public void update_task_description_when_handled_event_task_description_updated() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        projection.on(taskCreatedEvent);
        final String updatedDescription = UPDATED_DESCRIPTION;
        final TaskId expectedTaskId = taskCreatedEvent.getId();
        final TaskDescriptionUpdated descriptionUpdatedEvent =
                taskDescriptionUpdatedInstance(expectedTaskId, updatedDescription);
        projection.on(descriptionUpdatedEvent);
        final int expectedViewSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();

        assertEquals(expectedViewSize, taskListView.getItemsCount());

        final TaskView view = taskListView.getItemsList()
                                          .get(0);
        assertEquals(expectedTaskId, view.getId());
        assertEquals(updatedDescription, view.getDescription());
    }

    @Test
    public void not_update_task_description_when_handled_event_task_description_updated_with_wrong_task_id() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        projection.on(taskCreatedEvent);
        final String updatedDescription = UPDATED_DESCRIPTION;
        final TaskDescriptionUpdated descriptionUpdatedEvent =
                taskDescriptionUpdatedInstance(TaskId.getDefaultInstance(), updatedDescription);
        projection.on(descriptionUpdatedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView view = taskListView.getItemsList()
                                          .get(0);
        assertEquals(TASK_ID, view.getId());
        assertNotEquals(updatedDescription, view.getDescription());
    }

    @Test
    public void update_task_due_date_when_handled_event_task_due_date_updated() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        projection.on(taskCreatedEvent);
        final Timestamp updatedDueDate = Timestamps.getCurrentTime();
        final TaskId expectedTaskId = taskCreatedEvent.getId();
        final TaskDueDateUpdated taskDueDateUpdatedEvent = taskDueDateUpdatedInstance(expectedTaskId, updatedDueDate);
        projection.on(taskDueDateUpdatedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(expectedTaskId, taskView.getId());
        assertEquals(updatedDueDate, taskView.getDueDate());
    }

    @Test
    public void not_update_task_due_date_when_handled_event_task_due_date_updated_with_wrong_task_id() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        projection.on(taskCreatedEvent);
        final Timestamp updatedDueDate = Timestamps.getCurrentTime();
        final TaskDueDateUpdated taskDueDateUpdatedEvent =
                taskDueDateUpdatedInstance(TaskId.getDefaultInstance(), updatedDueDate);
        projection.on(taskDueDateUpdatedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertNotEquals(updatedDueDate, taskView.getDueDate());
    }

    @Test
    public void update_task_priority_when_handled_event_task_priority_updated() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        projection.on(taskCreatedEvent);
        final TaskPriority updatedTaskPriority = TaskPriority.LOW;
        final TaskId expectedTaskId = taskCreatedEvent.getId();
        final TaskPriorityUpdated taskPriorityUpdatedEvent =
                taskPriorityUpdatedInstance(expectedTaskId, updatedTaskPriority);
        projection.on(taskPriorityUpdatedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(expectedTaskId, taskView.getId());
        assertEquals(updatedTaskPriority, taskView.getPriority());
    }

    @Test
    public void not_update_task_priority_when_handled_event_task_priority_updated_with_wrong_task_id() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        projection.on(taskCreatedEvent);
        final TaskPriority updatedTaskPriority = TaskPriority.LOW;
        final TaskPriorityUpdated taskPriorityUpdatedEvent =
                taskPriorityUpdatedInstance(TaskId.getDefaultInstance(), updatedTaskPriority);
        projection.on(taskPriorityUpdatedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(TASK_ID, taskView.getId());
        assertNotEquals(updatedTaskPriority, taskView.getPriority());
    }

    @Test
    public void update_label_details_when_handled_event_label_details_updated() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        projection.on(taskCreatedEvent);
        final TaskId expectedTaskId = taskCreatedEvent.getId();
        final LabelAssignedToTask labelAssignedToTaskEvent = labelAssignedToTaskInstance(expectedTaskId, LABEL_ID);
        projection.on(labelAssignedToTaskEvent);
        final LabelColor updatedColor = LabelColor.BLUE;
        final LabelDetailsUpdated labelDetailsUpdatedEvent =
                labelDetailsUpdatedInstance(updatedColor, UPDATED_LABEL_TITLE);
        projection.on(labelDetailsUpdatedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(LABEL_ID, taskView.getLabelId());
        assertEquals(updatedColor, taskView.getLabelColor());
    }

    @Test
    public void not_update_label_details_when_handled_event_label_details_updated_with_wrong_label_id() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        projection.on(taskCreatedEvent);
        final TaskId expectedTaskId = taskCreatedEvent.getId();
        final LabelAssignedToTask labelAssignedToTaskEvent =
                labelAssignedToTaskInstance(expectedTaskId, TaskLabelId.getDefaultInstance());
        projection.on(labelAssignedToTaskEvent);
        final LabelColor updatedColor = LabelColor.BLUE;
        final LabelDetailsUpdated labelDetailsUpdatedEvent =
                labelDetailsUpdatedInstance(updatedColor, UPDATED_LABEL_TITLE);
        projection.on(labelDetailsUpdatedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(expectedTaskId, taskView.getId());
        assertNotEquals(updatedColor, taskView.getLabelColor());
    }

    @Test
    public void update_completed_when_handled_event_task_completed() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        final TaskCompleted taskCompletedEvent = taskCompletedInstance();
        projection.on(taskCreatedEvent);
        projection.on(taskCompletedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(TASK_ID, taskView.getId());
        assertTrue(taskView.getCompleted());
    }

    @Test
    public void not_update_completed_when_handled_event_task_completed_with_wrong_task_id() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        projection.on(taskCreatedEvent);
        final TaskCompleted taskCompletedEvent = taskCompletedInstance(TaskId.getDefaultInstance());
        projection.on(taskCompletedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(TASK_ID, taskView.getId());
        assertFalse(taskView.getCompleted());
    }

    @Test
    public void update_completed_when_handled_event_task_reopened() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        final TaskCompleted taskCompletedEvent = taskCompletedInstance();
        final TaskReopened taskReopenedEvent = taskReopenedInstance();
        projection.on(taskCreatedEvent);
        projection.on(taskCompletedEvent);
        projection.on(taskReopenedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(TASK_ID, taskView.getId());
        assertFalse(taskView.getCompleted());
    }

    @Test
    public void not_update_completed_when_handled_event_task_reopened_with_wrong_task_id() {
        final TaskCreated taskCreatedEvent = taskCreatedInstance();
        final TaskCompleted taskCompletedEvent = taskCompletedInstance();
        projection.on(taskCreatedEvent);
        projection.on(taskCompletedEvent);
        final TaskReopened taskReopenedEvent = taskReopenedInstance(TaskId.getDefaultInstance());
        projection.on(taskReopenedEvent);
        final int expectedViewListSize = 1;
        final TaskListView taskListView = projection.getState()
                                                    .getMyList();
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(TASK_ID, taskView.getId());
        assertTrue(taskView.getCompleted());
    }
}
