/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.q.projections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.test.Tests.hasPrivateUtilityConstructor;

/**
 * @author Illia Shepilov
 */
public class ProjectionHelperShould {

    private static final TaskId TASK_ID = TaskId.newBuilder()
                                                .setValue(newUuid())
                                                .build();
    private static final TaskLabelId LABEL_ID = TaskLabelId.newBuilder()
                                                           .setValue(newUuid())
                                                           .build();
    private TaskView viewWithDefaultLabelId;
    private TaskView viewWithDefaultTaskId;
    private List<TaskView> viewList;

    @BeforeEach
    public void setUp() {
        viewList = initViewList();
    }

    @Test
    public void have_private_constructor() {
        assertTrue(hasPrivateUtilityConstructor(ProjectionHelper.class));
    }

    @Test
    public void remove_task_view_from_state_by_label_id() {
        final int expectedListSize = 1;
        final TaskListView view = ProjectionHelper.removeViewByLabelId(viewList, LABEL_ID);

        assertEquals(expectedListSize, view.getItemsCount());
        assertFalse(viewList.contains(viewWithDefaultTaskId));
    }

    @Test
    public void not_remove_task_view_from_state_by_wrong_label_id() {
        final int expectedListSize = 2;
        final TaskLabelId wrongLabelId = TaskLabelId.newBuilder()
                                                    .setValue(newUuid())
                                                    .build();
        final TaskListView view = ProjectionHelper.removeViewByLabelId(viewList, wrongLabelId);

        assertEquals(expectedListSize, view.getItemsCount());
        assertTrue(viewList.contains(viewWithDefaultTaskId));
    }

    @Test
    public void remove_task_view_from_state_by_task_id() {
        final int expectedListSize = 1;
        final TaskListView view = ProjectionHelper.removeViewByTaskId(viewList, TASK_ID);

        assertEquals(expectedListSize, view.getItemsCount());
        assertFalse(viewList.contains(viewWithDefaultLabelId));
    }

    @Test
    public void not_remove_task_view_from_state_by_wrong_task_id() {
        final int expectedListSize = 2;
        final TaskId wrongTaskId = TaskId.newBuilder()
                                         .setValue(newUuid())
                                         .build();
        final TaskListView view = ProjectionHelper.removeViewByTaskId(viewList, wrongTaskId);

        assertEquals(expectedListSize, view.getItemsCount());
        assertTrue(viewList.contains(viewWithDefaultTaskId));
    }

    private List<TaskView> initViewList() {
        final List<TaskView> viewList = new ArrayList<>();
        viewWithDefaultLabelId = TaskView.newBuilder()
                                         .setId(TASK_ID)
                                         .setLabelId(TaskLabelId.getDefaultInstance())
                                         .build();
        viewWithDefaultTaskId = TaskView.newBuilder()
                                        .setLabelId(LABEL_ID)
                                        .setId(TaskId.getDefaultInstance())
                                        .build();
        viewList.add(viewWithDefaultLabelId);
        viewList.add(viewWithDefaultTaskId);
        return viewList;
    }
}
