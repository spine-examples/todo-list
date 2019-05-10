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

package io.spine.examples.todolist.c.aggregate.definition;

import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.rejection.Rejections;
import io.spine.examples.todolist.q.projection.TaskView;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.examples.todolist.repository.TaskViewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;

@DisplayName("UpdateTaskDescription command should be interpreted by TaskPart and")
class UpdateTaskDescriptionTest extends TodoListCommandTestBase {

    private static final String NEW_DESCRIPTION = "Wash my dog.";

    UpdateTaskDescriptionTest() {
        super(new TaskRepository(), new TaskViewRepository());
    }

    @Test
    @DisplayName("produce TaskDescriptionUpdated event")
    void produceEvent() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        String previousDescription = createTask.getDescription()
                                               .getValue();
        UpdateTaskDescription updateDescription =
                updateTaskDescriptionInstance(taskId(), previousDescription, NEW_DESCRIPTION);
        boundedContext().receivesCommand(createTask)
                        .receivesCommand(updateDescription)
                        .assertEmitted(TaskDescriptionUpdated.class);
    }

    @Test
    @DisplayName("update the task description")
    void updateDescription() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        String previousDescription = createTask.getDescription()
                                               .getValue();
        UpdateTaskDescription updateDescription =
                updateTaskDescriptionInstance(taskId(), previousDescription, NEW_DESCRIPTION);
        TaskView expected = TaskView
                .vBuilder()
                .setId(taskId())
                .setDescription(updateDescription.getDescriptionChange()
                                                 .getNewValue())
                .build();
        isEqualToAfterReceiving(expected, createTask, updateDescription);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription rejection " +
            "upon an attempt to update the description of the deleted task")
    void cannotUpdateDeletedTaskDescription() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        String previousDescription = createTask.getDescription()
                                               .getValue();

        DeleteTask deleteTask = deleteTaskInstance(taskId());
        UpdateTaskDescription updateDescription =
                updateTaskDescriptionInstance(taskId(), previousDescription, NEW_DESCRIPTION);

        boundedContext().receivesCommand(createTask)
                        .receivesCommand(deleteTask)
                        .receivesCommand(updateDescription)
                        .assertRejectedWith(Rejections.CannotUpdateTaskDescription.class);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription rejection " +
            "upon an attempt to update the description of the completed task")
    void cannotUpdateCompletedTaskDescription() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        String previousDescription = createTask.getDescription()
                                               .getValue();

        CompleteTask completeTask = completeTaskInstance(taskId());
        UpdateTaskDescription updateDescription =
                updateTaskDescriptionInstance(taskId(), previousDescription, NEW_DESCRIPTION);

        boundedContext().receivesCommand(createTask)
                        .receivesCommand(completeTask)
                        .receivesCommand(updateDescription)
                        .assertRejectedWith(Rejections.CannotUpdateTaskDescription.class);
    }

    @Test
    @DisplayName("produce CannotUpdateTaskDescription rejection")
    void produceRejection() {
        CreateBasicTask createTask = createTaskInstance(taskId());
        String incorrectDescription = createTask.getDescription()
                                                .getValue() + "random suffix";

        CompleteTask completeTask = completeTaskInstance(taskId());
        UpdateTaskDescription updateDescription =
                updateTaskDescriptionInstance(taskId(), incorrectDescription, NEW_DESCRIPTION);

        boundedContext().receivesCommand(createTask)
                        .receivesCommand(completeTask)
                        .receivesCommand(updateDescription)
                        .assertRejectedWith(Rejections.CannotUpdateTaskDescription.class);
    }
}
