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

package io.spine.examples.todolist;

import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.AppConfig.getClient;
import static io.spine.examples.todolist.DataSource.getTaskViewById;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("DataSource should")
class DataSourceTest {

    private final DataSource dataSource = new DataSource(getClient());

    @Test
    @DisplayName("throw the exception if nonexistent task ID is specified")
    void notAllowNonexistentTaskId() {
        final TaskId id = newTaskId("invalid ID");
        assertThrows(IllegalStateException.class, () -> dataSource.getMyTaskView(id));
    }

    @Test
    @DisplayName("return task by ID")
    void returnTaskById() {
        final List<TaskView> views = asList(newTaskWithRandomId(), newTaskWithRandomId());
        final TaskView expectedView = views.get(0);
        final TaskId expectedViewId = expectedView.getId();
        assertSame(expectedView, getTaskViewById(views, expectedViewId));
    }

    private static TaskView newTaskWithRandomId() {
        return TaskView.newBuilder()
                       .setId(newTaskId(newUuid()))
                       .build();
    }

    private static TaskId newTaskId(String value) {
        return TaskId.newBuilder()
                     .setValue(value)
                     .build();
    }
}
