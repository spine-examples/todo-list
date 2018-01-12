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

package io.spine.examples.todolist.q.projection;

import io.spine.examples.todolist.TaskId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.spine.Identifier.newUuid;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
@DisplayName("Projections should")
public class ProjectionsTest {

    private static final TaskId TASK_ID = TaskId.newBuilder()
                                                .setValue(newUuid())
                                                .build();
    private TaskItem viewWithNonDefaultId;
    private TaskItem viewWithDefaultTaskId;
    private List<TaskItem> viewList;

    @BeforeEach
    public void setUp() {
        viewList = createViewList();
    }

    @Test
    @DisplayName("have the private constructor")
    public void havePrivateConstructor() {
        assertHasPrivateParameterlessCtor(Projections.class);
    }

    @Test
    @DisplayName("remove TaskItem from state by task ID")
    public void removeViewByTaskId() {
        final int expectedListSize = 2;
        final TaskListView view = Projections.removeViewsByTaskId(viewList, TASK_ID);

        assertEquals(expectedListSize, view.getItemsCount());
        assertFalse(viewList.contains(viewWithNonDefaultId));
    }

    @Test
    @DisplayName("not remove TaskItem from state by wrong task ID")
    public void notRemoveTaskItemByTaskId() {
        final int expectedListSize = viewList.size();
        final TaskId wrongTaskId = TaskId.newBuilder()
                                         .setValue(newUuid())
                                         .build();
        final TaskListView view = Projections.removeViewsByTaskId(viewList, wrongTaskId);

        assertEquals(expectedListSize, view.getItemsCount());
        assertTrue(viewList.contains(viewWithDefaultTaskId));
    }

    private List<TaskItem> createViewList() {
        final List<TaskItem> viewList = new ArrayList<>();
        viewWithNonDefaultId = TaskItem.newBuilder()
                                       .setId(TASK_ID)
                                       .build();
        viewWithDefaultTaskId = TaskItem.newBuilder()
                                        .setId(TaskId.getDefaultInstance())
                                        .build();
        viewList.add(viewWithNonDefaultId);
        viewList.add(viewWithNonDefaultId);
        viewList.add(viewWithDefaultTaskId);
        viewList.add(viewWithDefaultTaskId);
        return viewList;
    }
}
