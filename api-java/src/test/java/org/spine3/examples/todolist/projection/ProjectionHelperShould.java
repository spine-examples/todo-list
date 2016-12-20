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

package org.spine3.examples.todolist.projection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;

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
    private TaskView viewWithoutLabelId;
    private TaskView viewWithoutTaskId;
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
        int expectedListSize = 1;
        TaskListView view = ProjectionHelper.removeViewByLabelId(viewList, LABEL_ID);

        assertEquals(expectedListSize, view.getItemsCount());
        assertFalse(viewList.contains(viewWithoutTaskId));
    }

    @Test
    public void remove_task_view_from_state_by_task_id() {
        int expectedListSize = 1;
        TaskListView view = ProjectionHelper.removeViewByTaskId(viewList, TASK_ID);

        assertEquals(expectedListSize, view.getItemsCount());
        assertFalse(viewList.contains(viewWithoutLabelId));
    }

    private List<TaskView> initViewList() {
        final List<TaskView> viewList = new ArrayList<>();
        viewWithoutLabelId = TaskView.newBuilder()
                                     .setId(TASK_ID)
                                     .build();
        viewWithoutTaskId = TaskView.newBuilder()
                                    .setLabelId(LABEL_ID)
                                    .build();
        viewList.add(viewWithoutLabelId);
        viewList.add(viewWithoutTaskId);
        return viewList;
    }
}
