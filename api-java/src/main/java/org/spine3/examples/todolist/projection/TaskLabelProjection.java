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

import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelCreated;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

/**
 * TaskLabel projection.
 */
public class TaskLabelProjection extends Projection<TaskLabelId, TaskLabel> {

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public TaskLabelProjection(TaskLabelId id) {
        super(id);
    }

    @Subscribe
    public void on(LabelAssignedToTask event) {
        TaskLabel state = getState().newBuilderForType()
                                    .setId(event.getLabelId())
                                    .build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        TaskLabel state = getState().newBuilderForType()
                                    .setId(event.getLabelId())
                                    .build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelCreated event) {
        LabelDetails labelDetails = event.getDetails();
        TaskLabel state = getState().newBuilderForType()
                                    .setId(event.getId())
                                    .setTitle(labelDetails.getTitle())
                                    .setColor(labelDetails.getColor())
                                    .build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelDetailsUpdated event) {
        LabelDetails labelDetails = event.getNewDetails();
        TaskLabel state = getState().newBuilderForType()
                                    .setId(event.getId())
                                    .setTitle(labelDetails.getTitle())
                                    .setColor(labelDetails.getColor())
                                    .build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelDetails event) {
        TaskLabel state = getState().newBuilderForType()
                                    .setColor(event.getColor())
                                    .setTitle(event.getTitle())
                                    .build();
        incrementState(state);
    }

}
