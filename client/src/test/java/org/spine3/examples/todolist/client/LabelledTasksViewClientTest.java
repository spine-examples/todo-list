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

package org.spine3.examples.todolist.client;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.commands.ReopenTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.TaskListView;
import org.spine3.examples.todolist.q.projections.TaskView;
import org.spine3.protobuf.Timestamps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.UPDATED_LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.removeLabelFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.reopenTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.updateLabelDetailsInstance;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings({"OverlyCoupledClass", "ClassWithTooManyMethods"})
@DisplayName("LabelledTasksViewClient should")
public class LabelledTasksViewClientTest extends CommandLineTodoClientTest {

    @Test
    @DisplayName("obtain labelled view when handled RestoreDeletedTask command")
    public void obtainViewWhenTaskIsRestored() throws Exception {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        client.delete(deleteTask);

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        client.restore(restoreDeletedTask);

        final int expectedListSize = 1;
        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());

        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(taskId, view.getId());
        assertEquals(labelId, view.getLabelId());
    }

    @Test
    public void obtain_empty_labelled_tasks_view_when_handled_command_deleted_task_restored_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        client.delete(deleteTask);

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(getWrongTaskId());
        client.restore(restoreDeletedTask);

        final int expectedListSize = 1;
        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());

        final TaskListView taskListView = tasksViewList.get(0)
                                                       .getLabelledTasks();
        final List<TaskView> taskViews = taskListView.getItemsList();

        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_tasks_with_assigned_labels_from_labelled_tasks_view() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final int expectedListSize = 1;
        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());

        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(taskId, view.getId());
        assertEquals(labelId, view.getLabelId());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_assign_label_to_task_with_different_task_ids() {
        final CreateBasicTask firstTask = createBasicTask();
        client.create(firstTask);

        final CreateBasicTask secondTask = createBasicTask();
        client.create(secondTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId firstTaskId = firstTask.getId();
        final TaskId secondTaskId = secondTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(secondTaskId, labelId);
        client.assignLabel(assignLabelToTask);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, tasksViewList.size());

        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertNotEquals(firstTaskId, taskView.getId());
        assertEquals(secondTaskId, taskView.getId());
        assertEquals(labelId, taskView.getLabelId());
    }

    @Test
    public void obtain_updated_task_description_from_labelled_tasks_view_when_handled_command_update_task_description() {
        final TaskView view = obtainViewWhenHandledCommandUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, true);
        assertEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_update_task_description_with_wrong_task_id() {
        final TaskView view = obtainViewWhenHandledCommandUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, false);
        final String actualDescription = view.getDescription();
        assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
        assertEquals(DESCRIPTION, actualDescription);
    }

    @Test
    public void obtain_updated_task_priority_from_labelled_tasks_view_when_handled_command_update_task_priority() {
        final TaskPriority newPriority = TaskPriority.HIGH;
        final TaskView view = obtainViewWhenHandledCommandUpdateTaskPriority(newPriority, true);
        assertEquals(newPriority, view.getPriority());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_update_task_priority_with_wrong_task_id() {
        final TaskPriority newPriority = TaskPriority.HIGH;
        final TaskView view = obtainViewWhenHandledCommandUpdateTaskPriority(newPriority, false);
        assertNotEquals(newPriority, view.getPriority());
    }

    @Test
    public void obtain_updated_task_due_date_from_labelled_tasks_view_when_handled_command_update_task_due_date() {
        final Timestamp newDueDate = Timestamps.getCurrentTime();
        final TaskView view = obtainViewWhenHandledCommandUpdateTaskDueDate(newDueDate, true);
        assertEquals(newDueDate, view.getDueDate());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_update_task_due_date_with_wrong_task_id() {
        final Timestamp newDueDate = Timestamps.getCurrentTime();
        final TaskView view = obtainViewWhenHandledCommandUpdateTaskDueDate(newDueDate, false);
        assertNotEquals(newDueDate, view.getDueDate());
    }

    @Test
    public void obtain_empty_labelled_tasks_view_when_handled_command_remove_label_from_task() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskId, labelId);
        client.removeLabel(removeLabelFromTask);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, tasksViewList.size());

        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_remove_label_from_task_with_wrong_label_id() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskId, getWrongTaskLabelId());
        client.removeLabel(removeLabelFromTask);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        int expectedListSize = 2;
        assertEquals(expectedListSize, tasksViewList.size());

        final LabelledTasksView labelledTasksView = getLabelledTasksView(tasksViewList);
        final List<TaskView> taskViews = labelledTasksView.getLabelledTasks()
                                                          .getItemsList();

        expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());
        final TaskView view = taskViews.get(0);

        assertEquals(taskId, view.getId());
        assertFalse(taskViews.isEmpty());
    }

    @Test
    public void obtain_empty_labelled_tasks_view_when_handled_command_delete_task() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        client.delete(deleteTask);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, labelledTasksView.size());

        final List<TaskView> taskViews = labelledTasksView.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_delete_task_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final DeleteTask deleteTask = deleteTaskInstance(getWrongTaskId());
        client.delete(deleteTask);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, labelledTasksView.size());

        final List<TaskView> taskViews = labelledTasksView.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_complete_task() {
        final TaskView view = obtainViewWhenHandledCommandCompleteTask(true);
        assertTrue(view.getCompleted());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_complete_task_with_wrong_task_id() {
        final TaskView view = obtainViewWhenHandledCommandCompleteTask(false);
        assertFalse(view.getCompleted());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_reopen_task() {
        final TaskView view = obtainViewWhenHandledCommandReopenTask(true);
        assertFalse(view.getCompleted());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_reopen_task_with_wrong_task_id() {
        final TaskView view = obtainViewWhenHandledCommandReopenTask(false);
        assertTrue(view.getCompleted());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_update_label_details() {
        final LabelColor updatedColor = LabelColor.BLUE;
        final LabelledTasksView view = obtainViewWhenHandledCommandUpdateLabelDetails(updatedColor,
                                                                                      UPDATED_LABEL_TITLE,
                                                                                      true);
        assertEquals(UPDATED_LABEL_TITLE, view.getLabelTitle());
        final String expectedColor = "#0000ff";
        assertEquals(expectedColor, view.getLabelColor());
    }

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_update_label_details_with_wrong_task_id() {
        final LabelColor updatedColor = LabelColor.BLUE;
        final LabelledTasksView view = obtainViewWhenHandledCommandUpdateLabelDetails(updatedColor,
                                                                                      UPDATED_LABEL_TITLE,
                                                                                      false);
        assertNotEquals(UPDATED_LABEL_TITLE, view.getLabelTitle());
        final String expectedColor = "#0000ff";
        assertNotEquals(expectedColor, view.getLabelColor());
    }

    @Test
    public void obtain_empty_labelled_tasks_view_when_handled_command_create_draft() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        assertTrue(labelledTasksView.isEmpty());
    }

    @Test
    public void obtain_empty_labelled_tasks_view_when_handled_command_create_task() {
        final CreateBasicTask createBasicTask = createBasicTask();
        client.create(createBasicTask);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        assertTrue(labelledTasksView.isEmpty());
    }

    private static LabelledTasksView getLabelledTasksView(List<LabelledTasksView> tasksViewList) {
        LabelledTasksView result = LabelledTasksView.getDefaultInstance();

        for (LabelledTasksView labelledView : tasksViewList) {
            final boolean isEmpty = labelledView.getLabelId()
                                                .getValue()
                                                .isEmpty();
            if (!isEmpty) {
                result = labelledView;
            }
        }

        return result;
    }

    private LabelledTasksView obtainViewWhenHandledCommandUpdateLabelDetails(LabelColor updatedColor,
                                                                             String updatedTitle,
                                                                             boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final LabelDetails detailsWithCorrectId = LabelDetails.newBuilder()
                                                              .setColor(LabelColor.GRAY)
                                                              .setTitle(createLabel.getLabelTitle())
                                                              .build();
        final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                         .setColor(updatedColor)
                                                         .setTitle(updatedTitle)
                                                         .build();
        final LabelDetails previousLabelDetails =
                isCorrectId ? detailsWithCorrectId : LabelDetails.getDefaultInstance();
        final TaskLabelId updatedLabelId = isCorrectId ? labelId : getWrongTaskLabelId();
        final UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(updatedLabelId, previousLabelDetails, newLabelDetails);
        client.update(updateLabelDetails);

        final List<LabelledTasksView> labelledTasksViewList = client.getLabelledTasksView();
        final int correctIdExpectedSize = 1;
        final int incorrectIdExpectedSize = 2;
        final int expectedListSize = isCorrectId ? correctIdExpectedSize : incorrectIdExpectedSize;
        assertEquals(expectedListSize, labelledTasksViewList.size());

        final LabelledTasksView view = getLabelledTasksView(labelledTasksViewList);
        assertEquals(labelId, view.getLabelId());

        return view;
    }

    private TaskView obtainViewWhenHandledCommandReopenTask(boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final CompleteTask completeTask = completeTaskInstance(createdTaskId);
        client.complete(completeTask);

        final TaskId reopenedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final ReopenTask reopenTask = reopenTaskInstance(reopenedTaskId);
        client.reopen(reopenTask);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, labelledTasksView.size());

        final List<TaskView> taskViews = labelledTasksView.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledCommandCompleteTask(boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final TaskId completedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final CompleteTask completeTask = completeTaskInstance(completedTaskId);
        client.complete(completeTask);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, labelledTasksView.size());

        final List<TaskView> taskViews = labelledTasksView.get(0)
                                                          .getLabelledTasks()
                                                          .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledCommandUpdateTaskDescription(String newDescription, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId, createTask.getDescription(), newDescription);
        client.update(updateTaskDescription);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, tasksViewList.get(0)
                                                    .getLabelledTasks()
                                                    .getItemsCount());
        final TaskView view = tasksViewList.get(0)
                                           .getLabelledTasks()
                                           .getItems(0);
        assertEquals(labelId, view.getLabelId());
        assertEquals(taskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledCommandUpdateTaskPriority(TaskPriority priority, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final int expectedListSize = 1;
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(createdTaskId, createLabel.getLabelId());
        client.assignLabel(assignLabelToTask);

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(updatedTaskId, TaskPriority.TP_UNDEFINED, priority);
        client.update(updateTaskPriority);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());
        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();

        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(createLabel.getLabelId(), view.getLabelId());
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledCommandUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final Timestamp previousDueDate = Timestamp.getDefaultInstance();

        final UpdateTaskDueDate updateTaskDueDate = updateTaskDueDateInstance(updatedTaskId, previousDueDate, newDueDate);
        client.update(updateTaskDueDate);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, tasksViewList.size());

        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();

        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(labelId, view.getLabelId());
        assertEquals(taskId, view.getId());

        return view;
    }
}
