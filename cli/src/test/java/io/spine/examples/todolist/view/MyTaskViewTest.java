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
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.TaskPriority.NORMAL;
import static io.spine.examples.todolist.view.DateFormatter.format;
import static io.spine.examples.todolist.view.MyTaskView.DESCRIPTION_VALUE;
import static io.spine.examples.todolist.view.MyTaskView.DUE_DATE_VALUE;
import static io.spine.examples.todolist.view.MyTaskView.PRIORITY_VALUE;
import static io.spine.time.Time.getCurrentTime;
import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("MyTaskView should")
class MyTaskViewTest {

    private final TaskView taskView = TaskView.newBuilder()
                                              .setDescription("my task description")
                                              .setPriority(NORMAL)
                                              .setDueDate(getCurrentTime())
                                              .build();
    private final MyTaskView myTaskView = new MyTaskView(TaskId.getDefaultInstance());

    @Test
    @DisplayName("throw the exception if nonexistent task ID is specified")
    void notAllowNonexistentTaskId() {
        final TaskId id = TaskId.newBuilder()
                                .setValue("invalid ID")
                                .build();
        assertThrows(IllegalStateException.class, () -> myTaskView.load(id));
    }

    @Test
    @DisplayName("renderBody TaskView")
    void renderTaskView() {
        final String expectedResult =
                DESCRIPTION_VALUE + taskView.getDescription() + lineSeparator() +
                        PRIORITY_VALUE + taskView.getPriority() + lineSeparator() +
                        DUE_DATE_VALUE + format(taskView.getDueDate());
        assertEquals(expectedResult, myTaskView.renderState(taskView));
    }

}