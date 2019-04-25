/*
 * Copyright 2019, TeamDev. All rights reserved.
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
import io.spine.examples.todolist.LabelDetailsVBuilder;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.q.projection.LabelView;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.updateLabelDetailsInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.UPDATED_LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("After execution of UpdateLabelDetails command the list of task views should")
class UpdateLabelDetailsTest extends TodoClientTest {

    private static final LabelColor EXPECTED_COLOR = LabelColor.BLUE;

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("contain an updated labelled task")
    void containUpdatedLabelledTask() {
        LabelColor updatedColor = LabelColor.BLUE;
        TaskView view = obtainViewWhenHandledCommandUpdateLabelDetails(
                updatedColor,
                UPDATED_LABEL_TITLE,
                true);
        LabelId assignedLabelId = view.getLabelIdsList()
                                      .getIds(0);
        LabelView label = findLabel(assignedLabelId);
        assertEquals(UPDATED_LABEL_TITLE, label.getTitle());
        assertEquals(EXPECTED_COLOR, label.getColor());
    }

    @Test
    @DisplayName("contain an unchanged labelled task if the command had an incorrect ID")
    void containNonUpdatedView() {
        LabelColor updatedColor = LabelColor.BLUE;
        TaskView view = obtainViewWhenHandledCommandUpdateLabelDetails(
                updatedColor,
                UPDATED_LABEL_TITLE,
                false);
        LabelId labelId = view.getLabelIdsList()
                              .getIds(0);
        LabelView label = findLabel(labelId);
        assertNotEquals(UPDATED_LABEL_TITLE, label.getTitle());
        assertNotEquals(EXPECTED_COLOR, label.getColor());
    }

    private TaskView
    obtainViewWhenHandledCommandUpdateLabelDetails(LabelColor updatedColor,
                                                   String updatedTitle,
                                                   boolean correctId) {
        CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);

        UpdateTaskPriority updateTaskPriority = setInitialTaskPriority(createTask.getId());
        client.postCommand(updateTaskPriority);

        CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);

        TaskId taskId = createTask.getId();
        LabelId labelId = createLabel.getLabelId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        LabelDetails detailsWithCorrectId = LabelDetailsVBuilder
                .newBuilder()
                .setColor(LabelColor.GRAY)
                .setTitle(createLabel.getLabelTitle())
                .build();
        LabelDetails newLabelDetails = LabelDetailsVBuilder
                .newBuilder()
                .setColor(updatedColor)
                .setTitle(updatedTitle)
                .build();
        LabelDetails previousLabelDetails =
                correctId ? detailsWithCorrectId : LabelDetails.getDefaultInstance();
        LabelId updatedLabelId = correctId ? labelId : createWrongTaskLabelId();
        UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(updatedLabelId,
                                           previousLabelDetails,
                                           newLabelDetails);
        client.postCommand(updateLabelDetails);
        TaskView view = getLabelledTasksView(labelId, client.getTaskViews());
        return view;
    }

    @Test
    @DisplayName("contain the task view with updated label details")
    void containUpdatedView() {
        CreateBasicLabel createBasicLabel = createBasicLabel();
        client.postCommand(createBasicLabel);

        LabelColor newLabelColor = LabelColor.RED;
        TaskView view = obtainTaskItemWhenHandledUpdateLabelDetails(newLabelColor, true);
        LabelId labelId = view.getLabelIdsList()
                              .getIds(0);
        LabelView label = findLabel(labelId);
        assertEquals(newLabelColor, label.getColor());
    }

    @Test
    @DisplayName("contain the task view with non-updated label details " +
            "when command has wrong task ID")
    void containNonUpdatedLabel() {
        CreateBasicLabel createBasicLabel = createBasicLabel();
        client.postCommand(createBasicLabel);

        LabelColor newLabelColor = LabelColor.RED;
        TaskView view = obtainTaskItemWhenHandledUpdateLabelDetails(newLabelColor, false);
        LabelId labelId = view.getLabelIdsList()
                              .getIds(0);
        LabelView label = findLabel(labelId);
        assertNotEquals(newLabelColor, label.getColor());
    }

    private TaskView
    obtainTaskItemWhenHandledUpdateLabelDetails(LabelColor newLabelColor, boolean correctId) {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);

        UpdateTaskPriority updateTaskPriority = setInitialTaskPriority(createDraft.getId());
        client.postCommand(updateTaskPriority);

        CreateBasicLabel createBasicLabel = createBasicLabel();
        client.postCommand(createBasicLabel);

        TaskId taskId = createDraft.getId();
        LabelId labelId = createBasicLabel.getLabelId();

        AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.postCommand(assignLabelToTask);

        LabelId updatedLabelId = correctId ? labelId : createWrongTaskLabelId();

        LabelDetails previousLabelDetails = LabelDetailsVBuilder
                .newBuilder()
                .setTitle(createBasicLabel.getLabelTitle())
                .setColor(LabelColor.GRAY)
                .build();
        LabelDetails newLabelDetails = LabelDetailsVBuilder
                .newBuilder()
                .setTitle(UPDATED_LABEL_TITLE)
                .setColor(newLabelColor)
                .build();
        UpdateLabelDetails updateLabelDetails =
                updateLabelDetailsInstance(updatedLabelId,
                                           previousLabelDetails,
                                           newLabelDetails);
        client.postCommand(updateLabelDetails);

        List<TaskView> taskViews = client
                .getTaskViews()
                .stream()
                .filter(view -> view.getStatus() == TaskStatus.DRAFT)
                .collect(toList());
        assertEquals(1, taskViews.size());

        TaskView view = taskViews.get(0);
        assertEquals(taskId, view.getId());
        assertEquals(labelId, view.getLabelIdsList()
                                  .getIds(0));

        return view;
    }

    private LabelView findLabel(LabelId labelId) {
        return client.getLabelView(labelId)
                     .orElseThrow(() -> newIllegalStateException("Label not found"));
    }
}
