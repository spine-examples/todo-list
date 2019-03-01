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

package io.spine.examples.todolist.q.projection;

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.spine.base.Identifier.newUuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProjectionHelper should")
class ProjectionHelperTest extends UtilityClassTest<ProjectionHelper> {

    private static final TaskId TASK_ID = TaskId.newBuilder()
                                                .setValue(newUuid())
                                                .build();
    private static final LabelId LABEL_ID = LabelId.newBuilder()
                                                   .setValue(newUuid())
                                                   .build();
    private TaskItem viewWithDefaultLabelId;
    private TaskItem viewWithDefaultTaskId;
    private List<TaskItem> viewList;

    ProjectionHelperTest() {
        super(ProjectionHelper.class);
    }

    @BeforeEach
    void setUp() {
        viewList = createViewList();
    }

    @Test
    @DisplayName("remove TaskItem from state by label ID")
    void removeView() {
        int expectedListSize = 2;
        TaskListView view = ProjectionHelper.removeViewsByLabelId(viewList, LABEL_ID);

        assertEquals(expectedListSize, view.getItemsCount());
        assertFalse(viewList.contains(viewWithDefaultTaskId));
    }

    @Test
    @DisplayName("not remove TaskItem from state by wrong label ID")
    void notRemoveViewByLabelId() {
        int expectedListSize = viewList.size();
        LabelId wrongLabelId = LabelId
                .newBuilder()
                .setValue(newUuid())
                .build();
        TaskListView view = ProjectionHelper.removeViewsByLabelId(viewList, wrongLabelId);

        assertEquals(expectedListSize, view.getItemsCount());
        assertTrue(viewList.contains(viewWithDefaultTaskId));
    }

    @Test
    @DisplayName("remove TaskItem from state by task ID")
    void removeViewByTaskId() {
        int expectedListSize = 2;
        TaskListView view = ProjectionHelper.removeViewsByTaskId(viewList, TASK_ID);

        assertEquals(expectedListSize, view.getItemsCount());
        assertFalse(viewList.contains(viewWithDefaultLabelId));
    }

    @Test
    @DisplayName("not remove TaskItem from state by wrong task ID")
    void notRemoveTaskItemByTaskId() {
        int expectedListSize = viewList.size();
        TaskId wrongTaskId = TaskId
                .newBuilder()
                .setValue(newUuid())
                .build();
        TaskListView view = ProjectionHelper.removeViewsByTaskId(viewList, wrongTaskId);

        assertEquals(expectedListSize, view.getItemsCount());
        assertTrue(viewList.contains(viewWithDefaultTaskId));
    }

    private List<TaskItem> createViewList() {
        List<TaskItem> viewList = new ArrayList<>();
        viewWithDefaultLabelId = TaskItem
                .newBuilder()
                .setId(TASK_ID)
                .setLabelId(LabelId.getDefaultInstance())
                .build();
        viewWithDefaultTaskId = TaskItem
                .newBuilder()
                .setLabelId(LABEL_ID)
                .setId(TaskId.getDefaultInstance())
                .build();
        viewList.add(viewWithDefaultLabelId);
        viewList.add(viewWithDefaultLabelId);
        viewList.add(viewWithDefaultTaskId);
        viewList.add(viewWithDefaultTaskId);
        return viewList;
    }
}
