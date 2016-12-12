//
// Copyright 2016, TeamDev Ltd. All rights reserved.
//
// Redistribution and use in source and/or binary forms, with or without
// modification, must retain the above copyright notice and the following
// disclaimer.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
package org.spine3.examples.todolist.projection;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.Task;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.protobuf.Timestamps;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.LABEL_ID;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.TASK_DUE_DATE;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.TASK_PRIORITY;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.deletedTaskRestoredInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.labelAssignedToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.taskCompletedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.taskCreatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.taskDeletedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.taskDescriptionUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.taskDraftCreatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.taskDraftFinalizedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.taskDueDateUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.taskPriorityUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.taskReopenedInstance;

/**
 * @author Illia Shepilov
 */
class TaskProjectionTest {

    private TaskProjection projection;
    private TaskCreated taskCreatedEvent;
    private TaskDraftCreated taskDraftCreatedEvent;
    private TaskDescriptionUpdated taskDescriptionUpdatedEvent;
    private TaskPriorityUpdated taskPriorityUpdatedEvent;
    private TaskDueDateUpdated taskDueDateUpdatedEvent;
    private TaskDraftFinalized taskDraftFinalizedEvent;
    private TaskCompleted taskCompletedEvent;
    private TaskReopened taskReopenedEvent;
    private TaskDeleted taskDeletedEvent;
    private DeletedTaskRestored deletedTaskRestoredEvent;
    private LabelAssignedToTask labelAssignedToTaskEvent;
    private static final TaskId ID = TaskId.newBuilder()
                                           .setValue(newUuid())
                                           .build();

    @BeforeEach
    void setUp() {
        projection = new TaskProjection(ID);
        taskCreatedEvent = taskCreatedInstance();
        taskDraftCreatedEvent = taskDraftCreatedInstance();
        taskDescriptionUpdatedEvent = taskDescriptionUpdatedInstance();
        taskPriorityUpdatedEvent = taskPriorityUpdatedInstance();
        taskDueDateUpdatedEvent = taskDueDateUpdatedInstance();
        taskDraftFinalizedEvent = taskDraftFinalizedInstance();
        taskCompletedEvent = taskCompletedInstance();
        taskReopenedEvent = taskReopenedInstance();
        taskDeletedEvent = taskDeletedInstance();
        deletedTaskRestoredEvent = deletedTaskRestoredInstance();
        labelAssignedToTaskEvent = labelAssignedToTaskInstance();
    }

    @Test
    public void return_state_when_handle_task_created_event() {
        projection.on(taskCreatedEvent);
        final Task state = projection.getState();

        assertEquals(DESCRIPTION, state.getDescription());
        assertEquals(TASK_PRIORITY, state.getPriority());
    }

    @Test
    public void return_state_when_handle_task_draft_created_event() {
        projection.on(taskDraftCreatedEvent);
        final Task state = projection.getState();

        assertEquals(DESCRIPTION, state.getDescription());
        assertEquals(TASK_PRIORITY, state.getPriority());
        assertTrue(state.getDraft());
    }

    @Test
    public void return_state_when_handle_task_description_updated_event() {
        projection.on(taskDescriptionUpdatedEvent);
        Task state = projection.getState();

        assertEquals(DESCRIPTION, state.getDescription());

        final String updatedDescription = "Updated task description.";
        taskDescriptionUpdatedEvent = taskDescriptionUpdatedInstance(updatedDescription);
        projection.on(taskDescriptionUpdatedEvent);
        state = projection.getState();

        assertEquals(updatedDescription, state.getDescription());
    }

    @Test
    public void return_state_when_handle_task_priority_updated_event() {
        projection.on(taskPriorityUpdatedEvent);
        Task state = projection.getState();

        assertEquals(TASK_PRIORITY, state.getPriority());

        final TaskPriority updatedPriority = TaskPriority.HIGH;
        taskPriorityUpdatedEvent = taskPriorityUpdatedInstance(updatedPriority);
        projection.on(taskPriorityUpdatedEvent);
        state = projection.getState();

        assertEquals(updatedPriority, state.getPriority());
    }

    @Test
    public void return_state_when_handle_task_due_date_updated_event() {
        projection.on(taskDueDateUpdatedEvent);
        Task state = projection.getState();

        assertEquals(TASK_DUE_DATE, state.getDueDate());

        final Timestamp newDueDate = Timestamps.getCurrentTime();
        taskDueDateUpdatedEvent = taskDueDateUpdatedInstance(newDueDate);
        projection.on(taskDueDateUpdatedEvent);
        state = projection.getState();

        assertEquals(newDueDate, state.getDueDate());
    }

    @Test
    public void return_state_when_handle_task_draft_finalized_event() {
        projection.on(taskDraftFinalizedEvent);
        final Task state = projection.getState();

        assertFalse(state.getDraft());
    }

    @Test
    public void return_state_when_handle_task_completed_event() {
        projection.on(taskCompletedEvent);
        final Task state = projection.getState();

        assertTrue(state.getCompleted());
    }

    @Test
    public void return_state_when_handle_task_reopened_event() {
        projection.on(taskReopenedEvent);
        final Task state = projection.getState();

        assertFalse(state.getCompleted());
    }

    @Test
    public void return_state_when_handle_task_deleted_event() {
        projection.on(taskDeletedEvent);
        final Task state = projection.getState();

        assertTrue(state.getDeleted());
    }

    @Test
    public void return_state_when_handle_deleted_task_restored_event() {
        projection.on(deletedTaskRestoredEvent);
        final Task state = projection.getState();

        assertFalse(state.getDeleted());
    }

    @Test
    public void return_state_when_label_assigned_to_task_event() {
        int expectedListSize = 1;
        projection.on(labelAssignedToTaskEvent);
        Task state = projection.getState();
        List<TaskLabelId> labelsList = state.getLabelIdsList();

        assertEquals(expectedListSize, labelsList.size());
        assertEquals(LABEL_ID, labelsList.get(0));
    }

}