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
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.view.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDeletedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDraftCreatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDraftFinalizedInstance;

/**
 * @author Illia Shepilov
 */
public class DraftTasksViewProjectionShould {

    private DraftTasksViewProjection projection;
    private TaskDraftFinalized taskDraftFinalizedEvent;
    private TaskDraftCreated taskDraftCreatedEvent;
    private TaskDeleted taskDeletedEvent;
    private final TaskListId ID = TaskListId.newBuilder()
                                            .setValue(newUuid())
                                            .build();

    @BeforeEach
    public void setUp() {
        projection = new DraftTasksViewProjection(ID);
        taskDraftFinalizedEvent = taskDraftFinalizedInstance();
        taskDeletedEvent = taskDeletedInstance();
        taskDraftCreatedEvent = taskDraftCreatedInstance();
    }

    @Test
    public void remove_task_view_from_state_when_it_is_finalized() {
        final int expectedListSize = 0;
        projection.on(taskDraftCreatedEvent);
        projection.on(taskDraftFinalizedEvent);

        final List<TaskView> views = projection.getState()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }

    @Test
    public void remove_task_view_from_state_when_it_is_deleted() {
        final int expectedListSize = 0;
        projection.on(taskDraftCreatedEvent);
        projection.on(taskDeletedEvent);

        final List<TaskView> views = projection.getState()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }

    @Test
    public void add_task_view_to_state_when_it_is_created() {
        final int expectedListSize = 1;
        projection.on(taskDraftCreatedEvent);

        final List<TaskView> views = projection.getState()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }
}
