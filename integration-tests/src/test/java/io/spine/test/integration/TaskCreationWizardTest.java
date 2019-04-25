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

package io.spine.test.integration;

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.q.projection.LabelView;
import io.spine.examples.todolist.q.projection.TaskView;
import io.spine.test.AbstractIntegrationTest;
import io.spine.test.integration.given.TaskCreationWizardTestEnv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Timestamps.add;
import static io.spine.base.Time.getCurrentTime;
import static io.spine.examples.todolist.LabelColor.BLUE;
import static io.spine.examples.todolist.LabelColor.GRAY;
import static io.spine.examples.todolist.LabelColor.GREEN;
import static io.spine.examples.todolist.LabelColor.RED;
import static io.spine.examples.todolist.TaskPriority.LOW;
import static io.spine.examples.todolist.TaskStatus.DRAFT;
import static io.spine.examples.todolist.TaskStatus.FINALIZED;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.newPid;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.newTaskId;
import static io.spine.util.Exceptions.newIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TaskCreationWizard should")
class TaskCreationWizardTest extends AbstractIntegrationTest {

    private TodoClient client;
    private TaskCreationWizardTestEnv testEnv;

    @BeforeEach
    void before() {
        client = getClient();
        testEnv = TaskCreationWizardTestEnv.with(client);
    }

    @Test
    @DisplayName("supervise task creation")
    void firstCase() {
        TaskCreationId pid = newPid();
        TaskId taskId = newTaskId();
        testEnv.createDraft(pid, taskId);
        String description = "firstCase";
        testEnv.setDetails(pid, description);
        testEnv.skipLabels(pid);
        testEnv.complete(pid);

        Task actualTask = testEnv.taskById(taskId);
        assertEquals(FINALIZED, actualTask.getTaskStatus());
        assertEquals(description, actualTask.getDescription()
                                            .getValue());
    }

    @Test
    @DisplayName("create and assign new labels")
    void secondCase() {
        TaskCreationId pid = newPid();
        TaskId taskId = newTaskId();
        testEnv.createDraft(pid, taskId);
        testEnv.setDetails(pid, "secondCase");
        LabelDetails redLabel = LabelDetails
                .newBuilder()
                .setTitle("red label")
                .setColor(RED)
                .build();
        LabelDetails greenLabel = LabelDetails
                .newBuilder()
                .setTitle("green label")
                .setColor(GREEN)
                .build();
        LabelDetails blueLabel = LabelDetails
                .newBuilder()
                .setTitle("blue label")
                .setColor(BLUE)
                .build();
        AddLabels addLabels = AddLabels
                .newBuilder()
                .setId(pid)
                .addNewLabels(redLabel)
                .addNewLabels(greenLabel)
                .addNewLabels(blueLabel)
                .build();
        client.postCommand(addLabels);
        assertAssignedLabel(taskId, redLabel.getTitle(), RED);
        assertAssignedLabel(taskId, greenLabel.getTitle(), GREEN);
        assertAssignedLabel(taskId, blueLabel.getTitle(), BLUE);
    }

    @Test
    @DisplayName("set all the optional fields")
    void thirdCase() {
        TaskCreationId pid = newPid();
        TaskId taskId = newTaskId();
        testEnv.createDraft(pid, taskId);

        String description = "thirdCase";
        TaskPriority priority = LOW;
        Timestamp dueDate = add(getCurrentTime(), fromSeconds(100));
        testEnv.setDetails(pid, description, priority, dueDate);
        String labelTitle = "thirdCase-label";
        LabelId labelId = testEnv.createNewLabel(labelTitle);
        testEnv.addLabel(pid, labelId);
        testEnv.complete(pid);

        Task task = testEnv.taskById(taskId);
        assertEquals(description, task.getDescription()
                                      .getValue());
        assertEquals(priority, task.getPriority());
        assertAssignedLabel(taskId, labelTitle, GRAY);
    }

    @Test
    @DisplayName("cancel the process")
    void forthCase() {
        TaskCreationId pid = newPid();
        TaskId taskId = newTaskId();
        testEnv.createDraft(pid, taskId);
        String description = "forthCase";
        testEnv.setDetails(pid, description);
        testEnv.cancel(pid);

        Task actualTask = testEnv.taskById(taskId);
        assertEquals(DRAFT, actualTask.getTaskStatus());
    }

    private void assertAssignedLabel(TaskId taskId, String labelTitle, LabelColor labelColor) {
        TaskView task = client.getTaskViews()
                              .stream()
                              .filter(view -> view.getId()
                                                  .equals(taskId))
                              .findAny()
                              .orElseThrow(() -> newIllegalStateException("Task not found."));
        LabelId labelId = task.getLabelIdsList()
                              .getIdsList()
                              .get(0);
        LabelView label = client.getLabelView(labelId)
                                .orElseThrow(() -> newIllegalStateException("Label not found."));
        assertEquals(labelColor, label.getColor());
        assertEquals(labelTitle, label.getTitle());
    }
}
