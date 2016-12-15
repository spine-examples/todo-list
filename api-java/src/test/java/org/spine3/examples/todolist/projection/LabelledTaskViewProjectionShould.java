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
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.view.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestEventContextFactory.eventContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.deletedTaskRestoredInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelAssignedToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelRemovedFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDeletedInstance;

/**
 * @author Illia Shepilov
 */
class LabelledTaskViewProjectionShould {

    private LabelledTaskViewProjection projection;
    private DeletedTaskRestored deletedTaskRestoredEvent;
    private LabelRemovedFromTask labelRemovedFromTaskEvent;
    private LabelAssignedToTask labelAssignedToTaskEvent;
    private TaskDeleted taskDeletedEvent;
    private EventContext eventContext;
    private TaskLabelId ID = TaskLabelId.newBuilder()
                                        .setValue(newUuid())
                                        .build();

    @BeforeEach
    public void setUp() {
        projection = new LabelledTaskViewProjection(ID);
        deletedTaskRestoredEvent = deletedTaskRestoredInstance();
        labelAssignedToTaskEvent = labelAssignedToTaskInstance();
        labelRemovedFromTaskEvent = labelRemovedFromTaskInstance();
        taskDeletedEvent = taskDeletedInstance();
        eventContext = eventContextInstance();
    }

    @Test
    public void return_state_when_handle_label_removed_from_task_event() {
        final int expectedListSize = 0;
        projection.on(labelAssignedToTaskEvent, eventContext);
        projection.on(labelRemovedFromTaskEvent, eventContext);

        final List<TaskView> views = projection.getState()
                                               .getLabelledTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }

    @Test
    public void return_state_when_handle_deleted_task_restored_event() {
        final int expectedListSize = 1;
        projection.on(labelAssignedToTaskEvent, eventContext);
        projection.on(taskDeletedEvent, eventContext);
        projection.on(deletedTaskRestoredEvent, eventContext);

        final List<TaskView> views = projection.getState()
                                               .getLabelledTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }

    @Test
    public void return_state_when_handle_label_assigned_to_task_event() {
        final int expectedListSize = 1;
        projection.on(labelAssignedToTaskEvent, eventContext);

        final List<TaskView> views = projection.getState()
                                               .getLabelledTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }

    @Test
    public void return_state_when_handle_task_deleted_event() {
        final int expectedListSize = 0;
        projection.on(labelAssignedToTaskEvent, eventContext);
        projection.on(taskDeletedEvent, eventContext);

        final List<TaskView> views = projection.getState()
                                               .getLabelledTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }
}
