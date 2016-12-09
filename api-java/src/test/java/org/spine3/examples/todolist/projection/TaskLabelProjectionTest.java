//
// Copyright 2016, TeamDev Ltd. All rights reserved.
//
// Redistribution and use in source and/or binary forms, with or without
// modification, must retain the above copyright notice and the following
// disclaimer.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
package org.spine3.examples.todolist.projection;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelCreated;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.server.projection.Projection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestTaskLabelEventFactory.labelCreatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelEventFactory.labelDetailsUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelEventFactory.labelRemovedFromTaskInstance;

/**
 * @author Illia Shepilov
 */
public class TaskLabelProjectionTest {

    private static final String DEFAULT_LABEL_TITLE = "label title.";
    private TaskLabelProjection projection;
    private LabelCreated labelCreatedEvent;
    private LabelRemovedFromTask labelRemovedFromTaskEvent;
    private LabelDetailsUpdated labelDetailsUpdatedEvent;
    private LabelAssignedToTask labelAssignedToTaskEvent;
    private LabelDetails labelDetailsEvent;
    private static final TaskLabelId ID = TaskLabelId.newBuilder()
                                                     .setValue(newUuid())
                                                     .build();

    @BeforeEach
    void setUp() {
        projection = new TaskLabelProjection(ID);
        labelCreatedEvent = labelCreatedInstance();
        labelDetailsUpdatedEvent = labelDetailsUpdatedInstance();
        labelRemovedFromTaskEvent = labelRemovedFromTaskInstance();
    }

    @Test
    public void return_event_classes_which_it_handles() {
        final int expectedSize = 5;

        final ImmutableSet<Class<? extends Message>> classes = Projection.getEventClasses(TaskLabelProjection.class);

        assertEquals(expectedSize, classes.size());
        assertTrue(classes.contains(LabelCreated.class));
        assertTrue(classes.contains(LabelAssignedToTask.class));
        assertTrue(classes.contains(LabelRemovedFromTask.class));
        assertTrue(classes.contains(LabelDetailsUpdated.class));
        assertTrue(classes.contains(LabelDetails.class));
    }

    @Test
    public void return_state_when_handle_label_created_event() {
        projection.on(labelCreatedEvent);
        TaskLabel state = projection.getState();

        assertEquals(LabelColor.GRAY, state.getColor());
        assertEquals(DEFAULT_LABEL_TITLE, state.getTitle());

        final String labelTitle = "label title v2.";
        labelCreatedEvent = labelCreatedInstance(LabelColor.BLUE, labelTitle);
        projection.on(labelCreatedEvent);
        state = projection.getState();

        assertEquals(LabelColor.BLUE, state.getColor());
        assertEquals(labelTitle, state.getTitle());
    }

    @Test
    public void return_state_when_handle_label_details_updated_event() {
        projection.on(labelDetailsUpdatedEvent);
        TaskLabel state = projection.getState();

        assertEquals(LabelColor.GRAY, state.getColor());
        assertEquals(DEFAULT_LABEL_TITLE, state.getTitle());

        final String updatedTitle = "Updated label title.";
        labelDetailsUpdatedEvent = labelDetailsUpdatedInstance(LabelColor.RED, updatedTitle);
        projection.on(labelDetailsUpdatedEvent);
        state = projection.getState();

        assertEquals(LabelColor.RED, state.getColor());
        assertEquals(updatedTitle, state.getTitle());
    }

}
