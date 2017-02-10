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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.TaskView;
import org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.UPDATED_LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.updateLabelDetailsInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution UpdateLabelDetails command")
public class UpdateLabelDetailsTest extends CommandLineTodoClientTest {

    @Nested
    @DisplayName("LabelledTasksView should")
    class UpdateLabelDetailsInLabelledTasksView {

        @Test
        @DisplayName("obtain task view with updated LabelDetails")
        public void obtainUpdatedView() {
            final LabelColor updatedColor = LabelColor.BLUE;
            final LabelledTasksView view = obtainViewWhenHandledCommandUpdateLabelDetails(updatedColor,
                                                                                          UPDATED_LABEL_TITLE,
                                                                                          true);
            assertEquals(UPDATED_LABEL_TITLE, view.getLabelTitle());
            final String expectedColor = "#0000ff";
            assertEquals(expectedColor, view.getLabelColor());
        }

        @Test
        @DisplayName("obtain task view with not updated LabelDetails when command has wrong task ID")
        public void obtainNotUpdatedView() {
            final LabelColor updatedColor = LabelColor.BLUE;
            final LabelledTasksView view = obtainViewWhenHandledCommandUpdateLabelDetails(updatedColor,
                                                                                          UPDATED_LABEL_TITLE,
                                                                                          false);
            assertNotEquals(UPDATED_LABEL_TITLE, view.getLabelTitle());
            final String expectedColor = "#0000ff";
            assertNotEquals(expectedColor, view.getLabelColor());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class UpdateLabelDetailsInMyListView {

        @Test
        @DisplayName("obtain task view with updated LabelDetails")
        public void obtainUpdatedView() {
            final LabelColor newColor = LabelColor.BLUE;
            final TaskView view = obtainTaskViewWhenHandledUpdateLabelDetailsCommand(newColor, true);
            assertEquals(LabelColor.BLUE, view.getLabelColor());
        }

        @Test
        @DisplayName("obtain task view with not updated LabelDetails when command has wrong task ID")
        public void obtainNotUpdatedView() {
            final LabelColor newColor = LabelColor.BLUE;
            final TaskView view = obtainTaskViewWhenHandledUpdateLabelDetailsCommand(newColor, false);
            assertNotEquals(newColor, view.getLabelColor());
        }
    }

    @Nested
    @DisplayName("DraftTasksView")
    class UpdateLabelDetailsInDraftTasksView {

        @Test
        @DisplayName("obtain task view with updated LabelDetails")
        public void obtainUpdatedView() throws Exception {
            final CreateBasicLabel createBasicLabel = createBasicLabel();
            client.create(createBasicLabel);

            final LabelColor newLabelColor = LabelColor.RED;
            final TaskView view = obtainTaskViewWhenHandledUpdateLabelDetails(newLabelColor, true);
            assertEquals(newLabelColor, view.getLabelColor());
        }

        @Test
        @DisplayName("obtain task view with not updated LabelDetails when command has wrong task ID")
        public void obtainNotUpdatedView() throws Exception {
            final CreateBasicLabel createBasicLabel = createBasicLabel();
            client.create(createBasicLabel);

            final LabelColor newLabelColor = LabelColor.RED;
            final TaskView view = obtainTaskViewWhenHandledUpdateLabelDetails(newLabelColor, false);
            assertNotEquals(newLabelColor, view.getLabelColor());
        }
    }

    private TaskView obtainTaskViewWhenHandledUpdateLabelDetailsCommand(LabelColor newColor, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId idOfCreatedTask = createTask.getId();
        final LabelId idOfCreatedLabel = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(idOfCreatedTask, idOfCreatedLabel);
        client.assignLabel(assignLabelToTask);

        final LabelId idOfUpdatedLabel = isCorrectId ? idOfCreatedLabel : getWrongTaskLabelId();

        final LabelDetails previousLabelDetails = LabelDetails.newBuilder()
                                                              .setColor(LabelColor.GRAY)
                                                              .setTitle(LABEL_TITLE)
                                                              .build();
        final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                         .setTitle(TestTaskLabelCommandFactory.UPDATED_LABEL_TITLE)
                                                         .setColor(newColor)
                                                         .build();
        final UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(idOfUpdatedLabel, previousLabelDetails, newLabelDetails);
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

    private LabelledTasksView obtainViewWhenHandledCommandUpdateLabelDetails(LabelColor updatedColor,
                                                                             String updatedTitle,
                                                                             boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final LabelId labelId = createLabel.getLabelId();

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
        final LabelId updatedLabelId = isCorrectId ? labelId : getWrongTaskLabelId();
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
}
