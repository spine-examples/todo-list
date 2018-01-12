/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution of UpdateTaskDescription command")
class UpdateTaskDescriptionTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("MyListView should")
    class UpdateTaskDescriptionInMyListView {

        @Test
        @DisplayName("contain the task view with updated task description")
        void containUpdatedView() {
            final TaskItem view = obtainTaskItemWhenHandledUpdateTaskDescriptionCommand(
                    UPDATED_TASK_DESCRIPTION, true);
            final TaskDescription actualDescription = view.getDescription();
            assertEquals(UPDATED_TASK_DESCRIPTION, actualDescription.getValue());
        }

        @Test
        @DisplayName("contain the task view with non-updated task description " +
                "when command ID does not match the aggregate")
        void containNonUpdatedView() {
            final TaskItem view = obtainTaskItemWhenHandledUpdateTaskDescriptionCommand(
                    UPDATED_TASK_DESCRIPTION, false);
            final TaskDescription actualDescription = view.getDescription();
            assertEquals(DESCRIPTION, actualDescription.getValue());
            assertNotEquals(UPDATED_TASK_DESCRIPTION, actualDescription);
        }
    }

    private TaskItem obtainTaskItemWhenHandledUpdateTaskDescriptionCommand(String newDescription,
                                                                           boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);

        updateDescription(newDescription, isCorrectId, createTask);

        final List<TaskItem> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskItem view = taskViews.get(0);
        assertEquals(createTask.getId(), view.getId());

        return view;
    }

    private void updateDescription(String newDescription, boolean isCorrectId,
                                   CreateBasicTask createTask) {
        final TaskId idOfCreatedTask = createTask.getId();
        final TaskId updatedTaskId = isCorrectId ? idOfCreatedTask : createWrongTaskId();
        final UpdateTaskDescription updateTaskDescription =
                updateTaskDescriptionInstance(updatedTaskId, createTask.getDescription()
                                                                       .getValue(),
                                              newDescription);
        client.postCommand(updateTaskDescription);
    }
}
