/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.client;

import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.q.projection.LabelColorView;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import io.spine.examples.todolist.testdata.TestLabelCommandFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.updateLabelDetailsInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.UPDATED_LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution of UpdateLabelDetails command")
class UpdateLabelDetailsTest extends TodoClientTest {

    private static final String EXPECTED_COLOR = LabelColorView.BLUE_COLOR.getHexColor();

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class UpdateLabelDetailsInLabelledTasksView {

        @Test
        @DisplayName("contain the task view with updated label details")
        void containUpdatedView() {
            final LabelColor updatedColor = LabelColor.BLUE;
            final LabelledTasksView view = obtainViewWhenHandledCommandUpdateLabelDetails(
                    updatedColor,
                    UPDATED_LABEL_TITLE,
                    true);
            assertEquals(UPDATED_LABEL_TITLE, view.getLabelTitle());
            assertEquals(EXPECTED_COLOR, view.getLabelColor());
        }

        @Test
        @DisplayName("contain the task view with non-updated label details " +
                "when command has wrong task ID")
        void containNonUpdatedView() {
            final LabelColor updatedColor = LabelColor.BLUE;
            final LabelledTasksView view = obtainViewWhenHandledCommandUpdateLabelDetails(
                    updatedColor,
                    UPDATED_LABEL_TITLE,
                    false);
            assertNotEquals(UPDATED_LABEL_TITLE, view.getLabelTitle());
            assertNotEquals(EXPECTED_COLOR, view.getLabelColor());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class UpdateLabelDetailsInMyListView {

        @Test
        @DisplayName("contain the task view with updated label details")
        void containUpdatedView() {
            final LabelColor newColor = LabelColor.BLUE;
            final TaskItem view = obtainTaskItemWhenHandledUpdateLabelDetailsCommand(newColor,
                                                                                     true);
            assertEquals(LabelColor.BLUE, view.getLabelColor());
        }

        @Test
        @DisplayName("contain the task view with non-updated label details " +
                "when command has wrong task ID")
        void containNonUpdatedView() {
            final LabelColor newColor = LabelColor.BLUE;
            final TaskItem view =
                    obtainTaskItemWhenHandledUpdateLabelDetailsCommand(newColor, false);
            assertNotEquals(newColor, view.getLabelColor());
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class UpdateLabelDetailsInDraftTasksView {

        @Test
        @DisplayName("contain the task view with updated label details")
        void containUpdatedView() throws Exception {
            final CreateBasicLabel createBasicLabel = createBasicLabel();
            client.postCommand(createBasicLabel);

            final LabelColor newLabelColor = LabelColor.RED;
            final TaskItem view =
                    obtainTaskItemWhenHandledUpdateLabelDetails(newLabelColor, true);
            assertEquals(newLabelColor, view.getLabelColor());
        }

        @Test
        @DisplayName("contain the task view with non-updated label details " +
                "when command has wrong task ID")
        void containNonUpdatedView() {
            final CreateBasicLabel createBasicLabel = createBasicLabel();
            client.postCommand(createBasicLabel);

            final LabelColor newLabelColor = LabelColor.RED;
            final TaskItem view =
                    obtainTaskItemWhenHandledUpdateLabelDetails(newLabelColor, false);
            assertNotEquals(newLabelColor, view.getLabelColor());
        }
    }

    private TaskItem obtainTaskItemWhenHandledUpdateLabelDetailsCommand(LabelColor newColor,
            boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);

        final TaskId idOfCreatedTask = createTask.getId();
        final LabelId idOfCreatedLabel = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(idOfCreatedTask,
                                                                              idOfCreatedLabel);
        client.postCommand(assignLabelToTask);

        final LabelId idOfUpdatedLabel = isCorrectId ? idOfCreatedLabel : createWrongTaskLabelId();

        final LabelDetails previousLabelDetails = LabelDetails.newBuilder()
                                                              .setColor(LabelColor.GRAY)
                                                              .setTitle(LABEL_TITLE)
                                                              .build();
        final LabelDetails newLabelDetails =
                LabelDetails.newBuilder()
                            .setTitle(TestLabelCommandFactory.UPDATED_LABEL_TITLE)
                            .setColor(newColor)
                            .build();
        final UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(idOfUpdatedLabel, previousLabelDetails, newLabelDetails);
        client.postCommand(updateLabelDetails);

        final List<TaskItem> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskItem view = taskViews.get(0);
        assertEquals(idOfCreatedTask, view.getId());

        return view;
    }

    private LabelledTasksView obtainViewWhenHandledCommandUpdateLabelDetails(
            LabelColor updatedColor,
            String updatedTitle,
            boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);

        final TaskId taskId = createTask.getId();
        final LabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

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
        final LabelId updatedLabelId = isCorrectId ? labelId : createWrongTaskLabelId();
        final UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(updatedLabelId, previousLabelDetails, newLabelDetails);
        client.postCommand(updateLabelDetails);

        final List<LabelledTasksView> labelledTasksViewList = client.getLabelledTasksView();
        final int expectedListSize = isCorrectId ? 1 : 2;
        assertEquals(expectedListSize, labelledTasksViewList.size());

        final LabelledTasksView view = getLabelledTasksView(labelledTasksViewList);
        assertEquals(labelId, view.getId());

        return view;
    }

    private TaskItem obtainTaskItemWhenHandledUpdateLabelDetails(LabelColor newLabelColor,
            boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);

        final CreateBasicLabel createBasicLabel = createBasicLabel();
        client.postCommand(createBasicLabel);

        final TaskId taskId = createDraft.getId();
        final LabelId labelId = createBasicLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        final LabelId updatedLabelId = isCorrectId ? labelId : createWrongTaskLabelId();

        final LabelDetails previousLabelDetails =
                LabelDetails.newBuilder()
                            .setTitle(createBasicLabel.getLabelTitle())
                            .setColor(LabelColor.GRAY)
                            .build();
        final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                         .setTitle(UPDATED_LABEL_TITLE)
                                                         .setColor(newLabelColor)
                                                         .build();
        final UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(updatedLabelId, previousLabelDetails, newLabelDetails);
        client.postCommand(updateLabelDetails);

        final List<TaskItem> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskItem view = taskViews.get(0);
        assertEquals(taskId, view.getId());
        assertEquals(labelId, view.getLabelId());

        return view;
    }
}
