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
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.q.projections.DraftTasksView;
import org.spine3.examples.todolist.q.projections.TaskView;
import org.spine3.protobuf.Timestamps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.UPDATED_LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.finalizeDraftInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.removeLabelFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.updateLabelDetailsInstance;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings({"OverlyCoupledClass", "ClassWithTooManyMethods"})
@DisplayName("Client should")
public class DraftTasksViewClientTest extends CommandLineTodoClientTest {

    @Test
    @DisplayName("obtain empty view list when handled DeleteTask command")
    public void emptyViewsWhenDraftIsDeleted() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final DeleteTask deleteTask = deleteTaskInstance(createDraft.getId());
        client.delete(deleteTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    @DisplayName("obtain created task draft when handled DeleteTask command with wrong ID")
    public void obtainViewWhenDeletedWrongDraft() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final TaskId taskId = createDraft.getId();
        final DeleteTask deleteTask = deleteTaskInstance(getWrongTaskId());
        client.delete(deleteTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());
    }

    @Test
    @DisplayName("obtain task view according to the created draft when handled CreateDraft command")
    public void obtainView() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final DraftTasksView draftTasksView = client.getDraftTasksView();
        final List<TaskView> taskViewList = draftTasksView.getDraftTasks()
                                                          .getItemsList();
        final int expectedListSize = 1;

        assertEquals(expectedListSize, taskViewList.size());
        assertEquals(createDraft.getId(), taskViewList.get(0)
                                                      .getId());
    }

    @Test
    @DisplayName("obtain empty view list when handled FinalizeDraft command")
    public void obtainEmptyViewsWhenDraftIsFinalized() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        DraftTasksView draftTasksView = client.getDraftTasksView();

        List<TaskView> taskViewList = draftTasksView.getDraftTasks()
                                                    .getItemsList();
        final int expectedListSize = 1;

        assertEquals(expectedListSize, taskViewList.size());
        assertEquals(createDraft.getId(), taskViewList.get(0)
                                                      .getId());
        final FinalizeDraft finalizeDraft = finalizeDraftInstance(createDraft.getId());
        client.finalize(finalizeDraft);

        draftTasksView = client.getDraftTasksView();
        taskViewList = draftTasksView.getDraftTasks()
                                     .getItemsList();
        assertTrue(taskViewList.isEmpty());
    }

    @Test
    @DisplayName("obtain view when handled FinalizeDraft command with wrong task ID")
    public void obtainViewWhenFinalizedWrongDraft() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);
        final TaskId taskId = createDraft.getId();

        final FinalizeDraft finalizeDraft = finalizeDraftInstance(getWrongTaskId());
        client.finalize(finalizeDraft);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());
    }

    @Test
    @DisplayName("obtain task view with not updated description when handled UpdateTaskDescription command with wrong task ID")
    public void obtainNotUpdatedDescription() {
        final TaskView view = obtainViewWhenHandledUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, false);
        assertNotEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
    }

    @Test
    @DisplayName("obtain task view with updated description when handled UpdateTaskDescription")
    public void obtainUpdatedDescription() {
        final TaskView view = obtainViewWhenHandledUpdateTaskDescription(UPDATED_TASK_DESCRIPTION, true);
        assertEquals(UPDATED_TASK_DESCRIPTION, view.getDescription());
    }

    @Test
    @DisplayName("obtain task view with updated task priority when handled UpdateTaskPriority command")
    public void obtainUpdatedPriority() {
        final TaskPriority newPriority = TaskPriority.HIGH;
        final TaskView view = obtainViewWhenHandledUpdateTaskPriority(newPriority, true);

        assertEquals(newPriority, view.getPriority());
    }

    @Test
    @DisplayName("obtain task view with not updated priority when handled UpdateTaskPriority command with wrong task ID")
    public void obtainNotUpdatedPriority() {
        final TaskPriority newPriority = TaskPriority.HIGH;
        final TaskView view = obtainViewWhenHandledUpdateTaskPriority(newPriority, false);

        assertNotEquals(newPriority, view.getPriority());
    }

    @Test
    @DisplayName("obtain task view with updated due date when handled UpdateTaskDueDate command")
    public void obtainUpdatedDueDate() {
        final Timestamp newDueDate = Timestamps.getCurrentTime();
        final TaskView view = obtainViewWhenHandledUpdateTaskDueDate(newDueDate, true);

        assertEquals(newDueDate, view.getDueDate());
    }

    @Test
    @DisplayName("obtain task view with not updated due date when handled UpdateTaskDueDate command with wrong task ID")
    public void obtainNotUpdatedDueDate() {
        final Timestamp newDueDate = Timestamps.getCurrentTime();
        final TaskView view = obtainViewWhenHandledUpdateTaskDueDate(newDueDate, false);

        assertNotEquals(newDueDate, view.getDueDate());
    }

    @Test
    @DisplayName("obtain task view with labels when handled AssignLabelToTask command")
    public void obtainLabelledView() {
        final CreateBasicLabel createBasicLabel = createBasicLabel();
        client.create(createBasicLabel);

        final LabelId labelId = createBasicLabel.getLabelId();
        final TaskView view = obtainViewWhenHandledAssignLabelToTask(labelId, true);

        assertEquals(labelId, view.getLabelId());
    }

    @Test
    @DisplayName("obtain task view without labels when handled AssignLabelToTask command with wrong task ID")
    public void obtainViewWithoutLabelsWhenTaskIdIsWrong() {
        final CreateBasicLabel createBasicLabel = createBasicLabel();
        client.create(createBasicLabel);
        final LabelId labelId = createBasicLabel.getLabelId();

        final TaskView view = obtainViewWhenHandledAssignLabelToTask(labelId, false);
        assertNotEquals(labelId, view.getLabelId());
    }

    @Test
    @DisplayName("obtain task view without labels when RemoveLabelFromTask command handled")
    public void obtainViewWithoutLabels() {
        final CreateBasicLabel createBasicLabel = createBasicLabel();
        client.create(createBasicLabel);
        final LabelId labelId = createBasicLabel.getLabelId();

        final TaskView view = obtainTaskViewWhenHandledRemoveLabeledFromTask(labelId, true);
        assertNotEquals(labelId, view.getLabelId());
    }

    @Test
    @DisplayName("obtain task view with labels when handled RemoveLabelFromTask command handled by wrong task ID")
    public void obtainLabelledViewWhenTaskIdIsWrong() {
        final CreateBasicLabel createBasicLabel = createBasicLabel();
        client.create(createBasicLabel);
        final LabelId labelId = createBasicLabel.getLabelId();

        final TaskView view = obtainTaskViewWhenHandledRemoveLabeledFromTask(labelId, false);
        assertEquals(labelId, view.getLabelId());
    }

    @Test
    @DisplayName("obtain task view with updated color when handled UpdateLabelDetails command")
    public void obtainUpdatedColor() throws Exception {
        final CreateBasicLabel createBasicLabel = createBasicLabel();
        client.create(createBasicLabel);

        final LabelColor newLabelColor = LabelColor.RED;
        final TaskView view = obtainTaskViewWhenHandledUpdateLabelDetails(newLabelColor, true);
        assertEquals(newLabelColor, view.getLabelColor());
    }

    @Test
    @DisplayName("obtain task view without updated color when handled UpdateLabelDetails command with wrong task ID")
    public void obtainNotUpdatedColor() throws Exception {
        final CreateBasicLabel createBasicLabel = createBasicLabel();
        client.create(createBasicLabel);

        final LabelColor newLabelColor = LabelColor.RED;
        final TaskView view = obtainTaskViewWhenHandledUpdateLabelDetails(newLabelColor, false);
        assertNotEquals(newLabelColor, view.getLabelColor());
    }

    @Test
    @DisplayName("obtain empty view list when handled CreateBasicTask command")
    public void obtainEmptyViewList() {
        final CreateBasicTask createBasicTask = createBasicTask();
        client.create(createBasicTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    private TaskView obtainTaskViewWhenHandledUpdateLabelDetails(LabelColor newLabelColor, boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final CreateBasicLabel createBasicLabel = createBasicLabel();
        client.create(createBasicLabel);

        final TaskId taskId = createDraft.getId();
        final LabelId labelId = createBasicLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final LabelId updatedLabelId = isCorrectId ? labelId : getWrongTaskLabelId();

        final LabelDetails previousLabelDetails = LabelDetails.newBuilder()
                                                              .setTitle(createBasicLabel.getLabelTitle())
                                                              .setColor(LabelColor.GRAY)
                                                              .build();
        final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                         .setTitle(UPDATED_LABEL_TITLE)
                                                         .setColor(newLabelColor)
                                                         .build();
        final UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(updatedLabelId, previousLabelDetails, newLabelDetails);
        client.update(updateLabelDetails);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());
        assertEquals(labelId, view.getLabelId());

        return view;
    }

    private TaskView obtainTaskViewWhenHandledRemoveLabeledFromTask(LabelId labelId, boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final TaskId taskId = createDraft.getId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final TaskId taskIdToRemove = isCorrectId ? taskId : getWrongTaskId();
        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskIdToRemove, labelId);
        client.removeLabel(removeLabelFromTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledAssignLabelToTask(LabelId labelId, boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final TaskId createTaskId = createDraft.getId();
        final TaskId taskIdToAssign = isCorrectId ? createTaskId : getWrongTaskId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskIdToAssign, labelId);
        client.assignLabel(assignLabelToTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createTaskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final TaskId createdTaskId = createDraft.getId();
        final TaskId updatedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final Timestamp previousDueDate = Timestamp.getDefaultInstance();

        final UpdateTaskDueDate updateTaskDueDate = updateTaskDueDateInstance(updatedTaskId, previousDueDate, newDueDate);
        client.update(updateTaskDueDate);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledUpdateTaskPriority(TaskPriority newPriority,
                                                             boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);
        final TaskId createdTaskId = createDraft.getId();

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(updatedTaskId, TaskPriority.TP_UNDEFINED, newPriority);
        client.update(updateTaskPriority);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledUpdateTaskDescription(String newDescription,
                                                                boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);
        final TaskId createdTaskId = createDraft.getId();

        final TaskId updatedTaskId = isCorrectId ? createdTaskId : getWrongTaskId();
        final String previousDescription = "";
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId, previousDescription, newDescription);
        client.update(updateTaskDescription);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createdTaskId, view.getId());

        return view;
    }
}
