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
        final CreateBasicTask createFirstTask = createBasicTask();
        client.create(createFirstTask);

        final CreateBasicTask createSecondTask = createBasicTask();
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
        final TaskView view = obtainTaskViewWhenHandledUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, true);
        final String actualDescription = view.getDescription();
        assertEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
    }

    @Test
    public void obtain_my_list_view_without_updated_description_when_handled_command_update_task_description_with_wrong_task_id() {
        final TaskView view = obtainTaskViewWhenHandledUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, false);
        final String actualDescription = view.getDescription();
        assertEquals(DESCRIPTION, actualDescription);
        assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
    }

    @Test
    public void obtain_my_list_view_with_completed_task_view_when_handled_complete_task_command() {
        final TaskView view = obtainTaskViewWhenHandledCompleteTask(true);
        assertTrue(view.getCompleted());
    }

    @Test
    public void obtain_my_list_view_with_uncompleted_task_view_when_handled_command_complete_task_with_wrong_task_id() {
        final TaskView view = obtainTaskViewWhenHandledCompleteTask(false);
        assertFalse(view.getCompleted());
    }

    @Test
    public void obtain_my_list_view_when_handled_remove_label_from_task_command() {
        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);
        final TaskLabelId labelId = createLabel.getLabelId();

        final TaskView view = obtainTaskViewWhenHandledRemoveLabelFromTask(labelId, true);
        assertNotEquals(labelId, view.getLabelId());
    }

    @Test
    public void obtain_my_list_view_when_handled_command_remove_label_from_task_with_wrong_task_id() {
        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);
        final TaskLabelId labelId = createLabel.getLabelId();

        final TaskView view = obtainTaskViewWhenHandledRemoveLabelFromTask(labelId, false);
        assertEquals(labelId, view.getLabelId());
    }

    @Test
    public void obtain_my_list_view_when_handled_command_assign_label_to_task() {
        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskView view = obtainTaskViewWhenHandledAssignLabelToTask(labelId, true);

        assertEquals(labelId, view.getLabelId());
    }

    @Test
    public void obtain_my_list_view_when_handled_command_assign_label_to_task_with_wrong_task_id() {
        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskView view = obtainTaskViewWhenHandledAssignLabelToTask(labelId, false);

        assertNotEquals(labelId, view.getLabelId());
    }

    @Test
    public void obtain_my_list_view_with_uncompleted_task_view_when_handled_command_reopen_task() {
        final TaskView view = obtainTaskViewWhenHandledReopenTask(true);
        assertFalse(view.getCompleted());
    }

    @Test
    public void obtain_my_list_view_with_completed_task_view_when_handled_command_reopen_task_with_wrong_task_id() {
        final TaskView view = obtainTaskViewWhenHandledReopenTask(false);
        assertTrue(view.getCompleted());
    }

    @Test
    public void obtain_updated_task_priority_from_my_list_view_when_handled_command_update_task_priority() {
        final TaskPriority newPriority = TaskPriority.HIGH;
        final TaskView view = obtainTaskViewWhenHandledUpdateTaskPriority(newPriority, true);
        assertEquals(newPriority, view.getPriority());
    }

    @Test
    public void obtain_my_list_view_without_updated_priority_when_handled_command_update_task_priority_with_wrong_task_id() {
        final TaskPriority newPriority = TaskPriority.HIGH;
        final TaskView view = obtainTaskViewWhenHandledUpdateTaskPriority(newPriority, false);
        assertNotEquals(newPriority, view.getPriority());
    }

    @Test
    public void obtain_updated_task_due_date_from_my_list_view_when_handled_command_update_task_due_date() {
        final Timestamp newDueDate = Timestamps.getCurrentTime();
        final TaskView view = obtainTaskViewWhenHandledUpdateTaskDueDate(newDueDate, true);
        assertEquals(newDueDate, view.getDueDate());
    }

    @Test
    public void obtain_empty_my_list_view_when_handled_command_update_task_due_date_with_wrong_task_id() {
        final Timestamp newDueDate = Timestamps.getCurrentTime();
        final TaskView view = obtainTaskViewWhenHandledUpdateTaskDueDate(newDueDate, false);
        assertNotEquals(newDueDate, view.getDueDate());
    }

    @Test
    public void obtain_updated_label_details_from_my_list_view_when_handled_command_update_label_details() {
        final LabelColor newColor = LabelColor.BLUE;
        final TaskView view = obtainTaskViewWhenHandledUpdateLabelDetails(newColor, true);
        assertEquals(LabelColor.BLUE, view.getLabelColor());
    }

    @Test
    public void obtain_not_updated_label_details_from_my_list_view_when_handled_command_update_label_details_with_wrong_task_id() {
        final LabelColor newColor = LabelColor.BLUE;
        final TaskView view = obtainTaskViewWhenHandledUpdateLabelDetails(newColor, false);
        assertNotEquals(newColor, view.getLabelColor());
    }

    @Test
    public void obtain_empty_my_list_view_when_handled_command_delete_task() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        final List<TaskView> taskViews = obtainTaskViewListWhenHandledDeleteTask(idOfCreatedTask, true);

        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_not_empty_my_list_view_when_handled_command_delete_task_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        final List<TaskView> taskViews = obtainTaskViewListWhenHandledDeleteTask(idOfCreatedTask, false);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        final TaskId taskId = createTask.getId();
        assertEquals(taskId, view.getId());
    }

    @Test
    public void obtain_empty_my_list_view_when_handled_command_create_draft() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    private List<TaskView> obtainTaskViewListWhenHandledDeleteTask(TaskId idOfCreatedTask, boolean isCorrectId) {
        final TaskId idOfDeletedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();

        final DeleteTask deleteTask = deleteTaskInstance(idOfDeletedTask);
        client.delete(deleteTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        return taskViews;
    }

    private TaskView obtainTaskViewWhenHandledUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();

        final UpdateTaskDueDate updateTaskDueDate = updateTaskDueDateInstance(idOfUpdatedTask, newDueDate);

        client.update(updateTaskDueDate);
        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private TaskView obtainTaskViewWhenHandledUpdateLabelDetails(LabelColor newColor, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId idOfCreatedTask = createTask.getId();
        final TaskLabelId idOfCreatedLabel = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(idOfCreatedTask, idOfCreatedLabel);
        client.assignLabel(assignLabelToTask);

        final TaskLabelId idOfUpdatedLabel = isCorrectId ? idOfCreatedLabel : getWrongTaskLabelId();

        final UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(idOfUpdatedLabel, newColor, UPDATED_LABEL_TITLE);
        client.update(updateLabelDetails);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private TaskView obtainTaskViewWhenHandledUpdateTaskPriority(TaskPriority newPriority, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();

        final UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(idOfUpdatedTask, newPriority);
        client.update(updateTaskPriority);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();

        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private TaskView obtainTaskViewWhenHandledReopenTask(boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId idOfReopenedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();

        final CompleteTask completeTask = completeTaskInstance(idOfCreatedTask);
        client.complete(completeTask);

        final ReopenTask reopenTask = reopenTaskInstance(idOfReopenedTask);
        client.reopen(reopenTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private TaskView obtainTaskViewWhenHandledAssignLabelToTask(TaskLabelId labelId, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(idOfUpdatedTask, labelId);
        client.assignLabel(assignLabelToTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private TaskView obtainTaskViewWhenHandledRemoveLabelFromTask(TaskLabelId labelId, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(idOfCreatedTask, labelId);
        client.assignLabel(assignLabelToTask);

        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();
        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(idOfUpdatedTask, labelId);
        client.removeLabel(removeLabelFromTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private TaskView obtainTaskViewWhenHandledCompleteTask(boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId idOfCreatedTask = createTask.getId();
        client.create(createTask);

        final TaskId idOfCompletedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();
        final CompleteTask completeTask = completeTaskInstance(idOfCompletedTask);
        client.complete(completeTask);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private TaskView obtainTaskViewWhenHandledUpdateTaskDescription(String newDescription, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);
        final TaskId idOfCreatedTask = createTask.getId();

        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : getWrongTaskId();
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(idOfUpdatedTask, newDescription);
        client.update(updateTaskDescription);

        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createTask.getId(), view.getId());

        return view;
    }
}
