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
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.view.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskCreatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDeletedInstance;

/**
 * @author Illia Shepilov
 */
class MyListViewProjectionShould {

    private MyListViewProjection projection;
    private TaskCreated taskCreatedEvent;
    private TaskDeleted taskDeletedEvent;
    private TaskListId ID = TaskListId.newBuilder()
                                      .setValue(newUuid())
                                      .build();

    @BeforeEach
    void setUp() {
        projection = new MyListViewProjection(ID);
        taskCreatedEvent = taskCreatedInstance();
        taskDeletedEvent = taskDeletedInstance();
    }

    @Test
    public void return_state_when_handle_task_created_event() {
        final int expectedSize = 1;
        projection.on(taskCreatedEvent);

        final List<TaskView> views = projection.getState()
                                               .getMyList()
                                               .getItemsList();
        assertEquals(expectedSize, views.size());
    }

    @Test
    public void return_state_when_handle_task_created_and_deleted_event() {
        int expectedListSize = 1;
        projection.on(taskCreatedEvent);
        projection.on(taskCreatedEvent);
        projection.on(taskDeletedEvent);

        List<TaskView> views = projection.getState()
                                         .getMyList()
                                         .getItemsList();
        assertEquals(expectedListSize, views.size());

        projection.on(taskDeletedEvent);
        expectedListSize = 0;
        views = projection.getState()
                          .getMyList()
                          .getItemsList();

        assertEquals(expectedListSize, views.size());
    }
}
