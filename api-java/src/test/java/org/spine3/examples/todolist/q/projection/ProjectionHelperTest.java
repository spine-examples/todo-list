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

package org.spine3.examples.todolist.q.projection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.test.Tests.assertHasPrivateParameterlessCtor;

/**
 * @author Illia Shepilov
 */
@DisplayName("ProjectionHelper should")
public class ProjectionHelperTest {

    private static final TaskId TASK_ID = TaskId.newBuilder()
                                                .setValue(newUuid())
                                                .build();
    private static final LabelId LABEL_ID = LabelId.newBuilder()
                                                   .setValue(newUuid())
                                                   .build();
    private TaskView viewWithDefaultLabelId;
    private TaskView viewWithDefaultTaskId;
    private List<TaskView> viewList;

    @BeforeEach
    public void setUp() {
        viewList = createViewList();
    }

    @Test
    @DisplayName("have the private constructor")
    public void havePrivateConstructor() {
        assertHasPrivateParameterlessCtor(ProjectionHelper.class);
    }

    @Test
    @DisplayName("remove TaskView from state by label ID")
    public void removeView() {
        final int expectedListSize = 2;
        final TaskListView view = ProjectionHelper.removeViewsByLabelId(viewList, LABEL_ID);

        assertEquals(expectedListSize, view.getItemsCount());
        assertFalse(viewList.contains(viewWithDefaultTaskId));
    }

    @Test
    @DisplayName("not remove TaskView from state by wrong label ID")
    public void notRemoveViewByLabelId() {
        final int expectedListSize = viewList.size();
        final LabelId wrongLabelId = LabelId.newBuilder()
                                            .setValue(newUuid())
                                            .build();
        final TaskListView view = ProjectionHelper.removeViewsByLabelId(viewList, wrongLabelId);

        assertEquals(expectedListSize, view.getItemsCount());
        assertTrue(viewList.contains(viewWithDefaultTaskId));
    }

    @Test
    @DisplayName("remove TaskView from state by task ID")
    public void removeViewByTaskId() {
        final int expectedListSize = 2;
        final TaskListView view = ProjectionHelper.removeViewsByTaskId(viewList, TASK_ID);

        assertEquals(expectedListSize, view.getItemsCount());
        assertFalse(viewList.contains(viewWithDefaultLabelId));
    }

    @Test
    @DisplayName("not remove TaskView from state by wrong task ID")
    public void notRemoveTaskViewByTaskId() {
        final int expectedListSize = viewList.size();
        final TaskId wrongTaskId = TaskId.newBuilder()
                                         .setValue(newUuid())
                                         .build();
        final TaskListView view = ProjectionHelper.removeViewsByTaskId(viewList, wrongTaskId);

        assertEquals(expectedListSize, view.getItemsCount());
        assertTrue(viewList.contains(viewWithDefaultTaskId));
    }

    private List<TaskView> createViewList() {
        final List<TaskView> viewList = new ArrayList<>();
        viewWithDefaultLabelId = TaskView.newBuilder()
                                         .setId(TASK_ID)
                                         .setLabelId(LabelId.getDefaultInstance())
                                         .build();
        viewWithDefaultTaskId = TaskView.newBuilder()
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
