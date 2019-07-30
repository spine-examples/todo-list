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

package io.spine.examples.todolist.server.view;

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.view.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Time.currentTime;
import static io.spine.examples.todolist.TaskPriority.NORMAL;
import static io.spine.examples.todolist.server.view.DateFormatter.format;
import static io.spine.examples.todolist.server.view.ViewOfTask.DESCRIPTION_VALUE;
import static io.spine.examples.todolist.server.view.ViewOfTask.DUE_DATE_VALUE;
import static io.spine.examples.todolist.server.view.ViewOfTask.PRIORITY_VALUE;
import static io.spine.examples.todolist.testdata.Given.newDescription;
import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TaskView should")
class TaskViewTest extends ViewTest {

    private TaskView task;
    private ViewOfTask taskView;

    @BeforeEach
    void createTaskAndView() {
        task = TaskView
                .newBuilder()
                .setId(TaskId.generate())
                .setDescription(newDescription("my task description"))
                .setPriority(NORMAL)
                .setDueDate(currentTime())
                .vBuild();
        taskView = new ViewOfTask(TaskId.getDefaultInstance());
    }

    @Test
    @DisplayName("throw the exception if non-existing task ID is specified")
    void notAllowNonexistentTaskId() {
        TaskId id = TaskId
                .newBuilder()
                .setUuid("invalid ID")
                .vBuild();
        assertThrows(IllegalStateException.class, () -> taskView.load(id));
    }

    @Test
    @DisplayName("render TaskItem state")
    void renderTaskItem() {
        String expectedResult =
                DESCRIPTION_VALUE + task.getDescription() + lineSeparator() +
                        PRIORITY_VALUE + task.getPriority() + lineSeparator() +
                        DUE_DATE_VALUE + format(task.getDueDate());

        assertThat(taskView.renderState(task))
             .isEqualTo(expectedResult);
    }
}
