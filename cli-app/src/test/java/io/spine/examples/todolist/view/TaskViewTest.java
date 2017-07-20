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

package io.spine.examples.todolist.view;

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.TaskPriority.NORMAL;
import static io.spine.examples.todolist.testdata.MessageFactory.newDescription;
import static io.spine.examples.todolist.view.DateFormatter.format;
import static io.spine.examples.todolist.view.TaskView.DESCRIPTION_VALUE;
import static io.spine.examples.todolist.view.TaskView.DUE_DATE_VALUE;
import static io.spine.examples.todolist.view.TaskView.PRIORITY_VALUE;
import static io.spine.time.Time.getCurrentTime;
import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("TaskView should")
class TaskViewTest {

    private final TaskItem task = TaskItem.newBuilder()
                                          .setDescription(newDescription("my task description"))
                                          .setPriority(NORMAL)
                                          .setDueDate(getCurrentTime())
                                          .build();
    private final TaskView taskView = new TaskView(TaskId.getDefaultInstance());

    @Test
    @DisplayName("throw the exception if nonexistent task ID is specified")
    void notAllowNonexistentTaskId() {
        final TaskId id = TaskId.newBuilder()
                                .setValue("invalid ID")
                                .build();
        assertThrows(IllegalStateException.class, () -> taskView.load(id));
    }

    @Test
    @DisplayName("render TaskItem state")
    void renderTaskItem() {
        final String expectedResult =
                DESCRIPTION_VALUE + task.getDescription() + lineSeparator() +
                        PRIORITY_VALUE + task.getPriority() + lineSeparator() +
                        DUE_DATE_VALUE + format(task.getDueDate());
        assertEquals(expectedResult, taskView.renderState(task));
    }

}
