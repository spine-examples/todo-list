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

package org.spine3.examples.todolist.q.projections;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDraftCreated;
import org.spine3.examples.todolist.c.events.TaskDraftFinalized;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.protobuf.Timestamps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelRemovedFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDeletedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDescriptionUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDraftCreatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDraftFinalizedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDueDateUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskPriorityUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.UPDATED_LABEL_TITLE;

/**
 * @author Illia Shepilov
 */
public class DraftTasksViewProjectionShould {

    private static final TaskListId TASK_LIST_ID = TaskListId.newBuilder()
                                                             .setValue(newUuid())
                                                             .build();
    private DraftTasksViewProjection projection;

    @BeforeEach
    public void setUp() {
        projection = new DraftTasksViewProjection(TASK_LIST_ID);
    }

    @Test
    public void add_task_view_to_state_when_task_draft_is_created() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final TaskListView listView = projection.getState()
                                                .getDraftTasks();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, listView.getItemsCount());

        final TaskView taskView = listView.getItems(0);

        assertEquals(TASK_ID, taskView.getId());
        assertEquals(DESCRIPTION, taskView.getDescription());
        assertEquals(TASK_PRIORITY, taskView.getPriority());
    }

    @Test
    public void remove_task_view_from_state_when_it_is_finalized() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final TaskDraftFinalized taskDraftFinalizedEvent = taskDraftFinalizedInstance();
        projection.on(taskDraftFinalizedEvent);

        final List<TaskView> views = projection.getState()
                                               .getDraftTasks()
                                               .getItemsList();
        assertTrue(views.isEmpty());
    }

    @Test
    public void remove_task_view_from_state_when_it_is_deleted() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final TaskDeleted taskDeletedEvent = taskDeletedInstance();
        projection.on(taskDeletedEvent);

        final List<TaskView> views = projection.getState()
                                               .getDraftTasks()
                                               .getItemsList();
        assertTrue(views.isEmpty());
    }

    @Test
    public void update_task_description_when_handled_event_task_description_updated() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final String updatedDescription = UPDATED_DESCRIPTION;
        final TaskId expectedTaskId = taskDraftCreatedEvent.getId();

        final TaskDescriptionUpdated descriptionUpdatedEvent =
                taskDescriptionUpdatedInstance(expectedTaskId, updatedDescription);
        projection.on(descriptionUpdatedEvent);

        final TaskListView taskListView = projection.getState()
                                                    .getDraftTasks();
        final int expectedViewSize = 1;
        assertEquals(expectedViewSize, taskListView.getItemsCount());

        final TaskView view = taskListView.getItemsList()
                                          .get(0);
        assertEquals(expectedTaskId, view.getId());
        assertEquals(updatedDescription, view.getDescription());
    }

    @Test
    public void not_update_task_description_when_handled_event_task_description_updated_with_wrong_task_id() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final String updatedDescription = UPDATED_DESCRIPTION;

        final TaskDescriptionUpdated descriptionUpdatedEvent =
                taskDescriptionUpdatedInstance(TaskId.getDefaultInstance(), updatedDescription);
        projection.on(descriptionUpdatedEvent);

        final TaskListView taskListView = projection.getState()
                                                    .getDraftTasks();
        final int expectedViewListSize = 1;
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView view = taskListView.getItemsList()
                                          .get(0);
        assertEquals(TASK_ID, view.getId());
        assertNotEquals(updatedDescription, view.getDescription());
    }

    @Test
    public void update_task_due_date_when_handled_event_task_due_date_updated() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final Timestamp updatedDueDate = Timestamps.getCurrentTime();
        final TaskId expectedTaskId = taskDraftCreatedEvent.getId();

        final TaskDueDateUpdated taskDueDateUpdatedEvent = taskDueDateUpdatedInstance(expectedTaskId, updatedDueDate);
        projection.on(taskDueDateUpdatedEvent);

        final TaskListView taskListView = projection.getState()
                                                    .getDraftTasks();
        final int expectedViewListSize = 1;
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(expectedTaskId, taskView.getId());
        assertEquals(updatedDueDate, taskView.getDueDate());
    }

    @Test
    public void not_update_task_due_date_when_handled_event_task_due_date_updated_with_wrong_task_id() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final Timestamp updatedDueDate = Timestamps.getCurrentTime();

        final TaskDueDateUpdated taskDueDateUpdatedEvent =
                taskDueDateUpdatedInstance(TaskId.getDefaultInstance(), updatedDueDate);
        projection.on(taskDueDateUpdatedEvent);

        final TaskListView taskListView = projection.getState()
                                                    .getDraftTasks();
        final int expectedViewListSize = 1;
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(TASK_ID, taskView.getId());
        assertNotEquals(updatedDueDate, taskView.getDueDate());
    }

    @Test
    public void update_task_priority_when_handled_event_task_priority_updated() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final TaskPriority updatedTaskPriority = TaskPriority.LOW;
        final TaskId expectedTaskId = taskDraftCreatedEvent.getId();

        final TaskPriorityUpdated taskPriorityUpdatedEvent =
                taskPriorityUpdatedInstance(expectedTaskId, updatedTaskPriority);
        projection.on(taskPriorityUpdatedEvent);

        final TaskListView taskListView = projection.getState()
                                                    .getDraftTasks();
        final int expectedViewListSize = 1;
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(expectedTaskId, taskView.getId());
        assertEquals(updatedTaskPriority, taskView.getPriority());
    }

    @Test
    public void not_update_task_priority_when_handled_event_task_priority_updated_with_wrong_task_id() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final TaskPriority updatedTaskPriority = TaskPriority.LOW;

        final TaskPriorityUpdated taskPriorityUpdatedEvent =
                taskPriorityUpdatedInstance(TaskId.getDefaultInstance(), updatedTaskPriority);
        projection.on(taskPriorityUpdatedEvent);

        final TaskListView taskListView = projection.getState()
                                                    .getDraftTasks();
        final int expectedViewListSize = 1;
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(TASK_ID, taskView.getId());
        assertNotEquals(updatedTaskPriority, taskView.getPriority());
    }

    @Test
    public void update_label_details_when_handled_event_label_details_updated() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final TaskId expectedTaskId = taskDraftCreatedEvent.getId();

        final LabelAssignedToTask labelAssignedToTaskEvent = labelAssignedToTaskInstance(expectedTaskId, LABEL_ID);
        projection.on(labelAssignedToTaskEvent);

        final LabelColor updatedColor = LabelColor.BLUE;

        final LabelDetailsUpdated labelDetailsUpdatedEvent =
                labelDetailsUpdatedInstance(LABEL_ID, updatedColor, UPDATED_LABEL_TITLE);
        projection.on(labelDetailsUpdatedEvent);

        final TaskListView taskListView = projection.getState()
                                                    .getDraftTasks();
        final int expectedViewListSize = 1;
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(LABEL_ID, taskView.getLabelId());
        assertEquals(updatedColor, taskView.getLabelColor());
    }

    @Test
    public void not_update_label_details_when_handled_event_label_details_updated_with_wrong_label_id() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final TaskId expectedTaskId = taskDraftCreatedEvent.getId();

        final LabelAssignedToTask labelAssignedToTaskEvent =
                labelAssignedToTaskInstance(expectedTaskId, TaskLabelId.getDefaultInstance());
        projection.on(labelAssignedToTaskEvent);

        final LabelColor updatedColor = LabelColor.BLUE;

        final LabelDetailsUpdated labelDetailsUpdatedEvent =
                labelDetailsUpdatedInstance(LABEL_ID, updatedColor, UPDATED_LABEL_TITLE);
        projection.on(labelDetailsUpdatedEvent);

        final TaskListView taskListView = projection.getState()
                                                    .getDraftTasks();
        final int expectedViewListSize = 1;
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(TASK_ID, taskView.getId());
        assertNotEquals(updatedColor, taskView.getLabelColor());
    }

    @Test
    public void remove_label_from_task_view_when_handled_event_label_removed_from_task() {
        final TaskDraftCreated taskDraftCreatedEvent = taskDraftCreatedInstance();
        projection.on(taskDraftCreatedEvent);

        final LabelAssignedToTask labelAssignedToTaskEvent = labelAssignedToTaskInstance();
        projection.on(labelAssignedToTaskEvent);

        final LabelRemovedFromTask labelRemovedFromTaskEvent = labelRemovedFromTaskInstance();
        projection.on(labelRemovedFromTaskEvent);

        final TaskListView taskListView = projection.getState()
                                                    .getDraftTasks();
        final int expectedViewListSize = 1;
        assertEquals(expectedViewListSize, taskListView.getItemsCount());

        final TaskView taskView = taskListView.getItemsList()
                                              .get(0);
        assertEquals(TASK_ID, taskView.getId());
        assertNotEquals(LABEL_ID, taskView.getLabelId());
    }
}
