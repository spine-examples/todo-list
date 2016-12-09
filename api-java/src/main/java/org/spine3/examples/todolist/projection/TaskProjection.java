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

import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.Task;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

/**
 * Holds a structural representation of data extracted from a stream of events related to task.
 * Contains the data about the task.
 *
 * @author Illia Shepilov
 * @see Projection
 */
public class TaskProjection extends Projection<TaskId, Task> {

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public TaskProjection(TaskId id) {
        super(id);
    }

    @Subscribe
    public void on(TaskCreated event) {
        final Task state = getState().newBuilderForType()
                                     .setDescription(event.getDetails()
                                                          .getDescription())
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDraftCreated event) {
        final TaskDetails taskDetails = event.getDetails();
        final Task state = getState().newBuilderForType()
                                     .setDescription(taskDetails.getDescription())
                                     .setCompleted(taskDetails.getCompleted())
                                     .setPriority(taskDetails.getPriority())
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDescriptionUpdated event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setDescription(event.getNewDescription())
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskPriorityUpdated event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setPriority(event.getNewPriority())
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDueDateUpdated event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setDueDate(event.getNewDueDate())
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDraftFinalized event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskCompleted event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setCompleted(true)
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskReopened event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setCompleted(false)
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setDeleted(true)
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(DeletedTaskRestored event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setDeleted(false)
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDetails event) {
        final Task state = getState().newBuilderForType()
                                     .setCompleted(event.getCompleted())
                                     .setDescription(event.getDescription())
                                     .setPriority(event.getPriority())
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelAssignedToTask event) {
        final Task currentState = getState();
        final int nextLabelIndex = currentState.getLabelIdsList()
                                               .size();

        final Task updatedState = getState().newBuilderForType()
                                            .setId(event.getId())
                                            .setLabelIds(nextLabelIndex, event.getLabelId())
                                            .build();
        incrementState(updatedState);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final Task currentState = getState();
        final int nextLabelIndex = currentState.getLabelIdsList()
                                               .size();

        final Task updatedState = getState().newBuilderForType()
                                            .setId(event.getId())
                                            .setLabelIds(nextLabelIndex, event.getLabelId())
                                            .build();
        incrementState(updatedState);
    }

}
