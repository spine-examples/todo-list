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

package org.spine3.examples.todolist.client;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicLabel;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.UpdateLabelDetails;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;
import org.spine3.examples.todolist.view.MyListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.protobuf.Timestamps;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.removeLabelFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.UPDATED_LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.updateLabelDetailsInstance;

/**
 * @author Illia Shepilov
 */
public class MyListViewClientShould extends BasicTodoClientShould {

    @Test
    public void obtain_my_list_view_when_handled_create_task_command() {
        final CreateBasicTask createFirstTask = createBasicTaskInstance();
        client.create(createFirstTask);

        final CreateBasicTask createSecondTask = createBasicTaskInstance();
        client.create(createSecondTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedMessagesCount = 2;
        assertEquals(expectedMessagesCount, taskViews.size());

        final TaskView firstView = taskViews.get(0);
        final TaskView secondView = taskViews.get(1);
        final List<TaskId> taskIds = new ArrayList<>(2);
        taskIds.add(firstView.getId());
        taskIds.add(secondView.getId());

        assertTrue(taskIds.contains(createFirstTask.getId()));
        assertTrue(taskIds.contains(createSecondTask.getId()));
        assertEquals(DESCRIPTION, firstView.getDescription());
        assertEquals(DESCRIPTION, secondView.getDescription());
    }

    @Test
    public void obtain_updated_task_description_from_my_list_view_when_handled_command_update_task_description() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final String newDescription = UPDATED_TASK_DESCRIPTION;
        final TaskId taskId = createTask.getId();

        final UpdateTaskDescription updateTaskDescription = updateTaskDescriptionInstance(taskId, newDescription);
        client.update(updateTaskDescription);

        final MyListView view = client.getMyListView();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, view.getMyList()
                                           .getItemsCount());
        final TaskView taskView = view.getMyList()
                                      .getItems(0);
        assertEquals(taskId, taskView.getId());
        assertEquals(newDescription, taskView.getDescription());
    }

    @Test
    public void obtain_my_list_view_with_completed_task_view_when_handled_complete_task_command() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final TaskId taskId = createTask.getId();

        final CompleteTask completeTask = completeTaskInstance(taskId);
        client.complete(completeTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(taskId, view.getId());
        assertTrue(view.getCompleted());
    }

    @Test
    public void obtain_my_list_view_when_handled_assign_label_to_task_command() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabelInstance();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertEquals(taskId, taskView.getId());
        assertEquals(labelId, taskView.getLabelId());
    }

    @Test
    public void obtain_my_list_view_when_handled_remove_label_from_task_command() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabelInstance();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskId, labelId);
        client.removeLabel(removeLabelFromTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertEquals(taskId, taskView.getId());
        assertNotEquals(labelId, taskView.getLabelId());
    }

    @Test
    public void obtain_my_list_view_when_handled_command_remove_label_from_task_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabelInstance();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(getWrongTaskId(), labelId);
        client.removeLabel(removeLabelFromTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertEquals(taskId, taskView.getId());
        assertEquals(labelId, taskView.getLabelId());
    }

    @Test
    public void obtain_my_list_view_when_handled_command_assign_label_to_task_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabelInstance();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(getWrongTaskId(), labelId);
        client.assignLabel(assignLabelToTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertEquals(taskId, taskView.getId());
        assertNotEquals(labelId, taskView.getLabelId());
    }

    @Test
    public void obtain_my_list_view_with_uncompleted_task_view_when_handled_command_complete_task_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final CompleteTask completeTask = completeTaskInstance(getWrongTaskId());
        client.complete(completeTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertFalse(view.getCompleted());
    }

    @Test
    public void obtain_my_list_view_with_task_view_when_handled_command_reopen_task_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final TaskId taskId = createTask.getId();

        final CompleteTask completeTask = completeTaskInstance(taskId);
        client.complete(completeTask);

        final ReopenTask reopenTask = reopenTaskInstance(getWrongTaskId());
        client.reopen(reopenTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertEquals(taskId, taskView.getId());
        assertTrue(taskView.getCompleted());
    }

    @Test
    public void obtain_my_list_view_with_uncompleted_task_view_when_handled_command_reopen_task() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final TaskId taskId = createTask.getId();

        final CompleteTask completeTask = completeTaskInstance(taskId);
        client.complete(completeTask);

        final ReopenTask reopenTask = reopenTaskInstance(taskId);
        client.reopen(reopenTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;

        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertEquals(taskId, taskView.getId());
        assertFalse(taskView.getCompleted());
    }

    @Test
    public void obtain_my_list_view_without_updated_description_when_handled_command_update_task_description_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(getWrongTaskId(), UPDATED_TASK_DESCRIPTION);
        client.update(updateTaskDescription);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createTask.getId(), view.getId());

        final String actualDescription = view.getDescription();
        assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
        assertEquals(DESCRIPTION, actualDescription);
    }

    @Test
    public void obtain_updated_task_priority_from_my_list_view_when_handled_command_update_task_priority() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final TaskPriority newPriority = TaskPriority.HIGH;
        final TaskId taskId = createTask.getId();

        final UpdateTaskPriority updateTaskPriority = updateTaskPriorityInstance(taskId, newPriority);
        client.update(updateTaskPriority);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(taskId, view.getId());
        assertEquals(newPriority, view.getPriority());
    }

    @Test
    public void obtain_my_list_view_without_updated_priority_when_handled_command_update_task_priority_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final TaskPriority newPriority = TaskPriority.HIGH;

        final UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(getWrongTaskId(), newPriority);
        client.update(updateTaskPriority);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(createTask.getId(), view.getId());
        assertNotEquals(newPriority, view.getPriority());
    }

    @Test
    public void obtain_updated_task_due_date_from_my_list_view_when_handled_command_update_task_due_date() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final Timestamp newDueDate = Timestamps.getCurrentTime();
        final TaskId taskId = createTask.getId();

        final UpdateTaskDueDate updateTaskDueDate = updateTaskDueDateInstance(taskId, newDueDate);
        client.update(updateTaskDueDate);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(taskId, view.getId());
        assertEquals(newDueDate, view.getDueDate());
    }

    @Test
    public void obtain_empty_my_list_view_when_handled_command_update_task_due_date_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final Timestamp newDueDate = Timestamps.getCurrentTime();
        final UpdateTaskDueDate updateTaskDueDate = updateTaskDueDateInstance(getWrongTaskId(), newDueDate);

        client.update(updateTaskDueDate);
        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(createTask.getId(), view.getId());
        assertNotEquals(newDueDate, view.getDueDate());
    }

    @Test
    public void obtain_empty_my_list_view_when_handled_command_delete_task() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final TaskId taskId = createTask.getId();

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        client.delete(deleteTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_empty_my_list_view_when_handled_command_create_draft() {
        final CreateDraft createDraft = createDraftInstance();
        client.create(createDraft);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_updated_label_details_from_my_list_view_when_handled_command_update_label_details() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabelInstance();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(labelId, LabelColor.BLUE, UPDATED_LABEL_TITLE);
        client.update(updateLabelDetails);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertEquals(taskId, taskView.getId());
        assertEquals(LabelColor.BLUE, taskView.getLabelColor());
    }

    @Test
    public void obtain_not_updated_label_details_from_my_list_view_when_handled_command_update_label_details_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabelInstance();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(getWrongTaskId(), labelId);
        client.assignLabel(assignLabelToTask);

        final UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(labelId, LabelColor.BLUE, UPDATED_LABEL_TITLE);
        client.update(updateLabelDetails);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;

        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertEquals(taskId, taskView.getId());
        assertNotEquals(LabelColor.BLUE, taskView.getLabelColor());
    }
}
