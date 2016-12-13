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
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.examples.todolist.TaskStatus;
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

import java.util.List;
import java.util.stream.Collectors;

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
        final TaskDetails taskDetails = event.getDetails();
        final Task state = getState().newBuilderForType()
                                     .setDescription(taskDetails.getDescription())
                                     .setPriority(taskDetails.getPriority())
                                     .setTaskStatus(TaskStatus.FINALIZED)
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDraftCreated event) {
        final TaskDetails taskDetails = event.getDetails();
        final Task state = getState().newBuilderForType()
                                     .setDescription(taskDetails.getDescription())
                                     .setPriority(taskDetails.getPriority())
                                     .setTaskStatus(TaskStatus.DRAFT)
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
                                     .setTaskStatus(TaskStatus.FINALIZED)
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskCompleted event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setTaskStatus(TaskStatus.COMPLETED)
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskReopened event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setTaskStatus(TaskStatus.OPEN)
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setTaskStatus(TaskStatus.DELETED)
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(DeletedTaskRestored event) {
        final Task state = getState().newBuilderForType()
                                     .setId(event.getId())
                                     .setTaskStatus(TaskStatus.OPEN)
                                     .build();
        incrementState(state);
    }

    @Subscribe
    public void on(LabelAssignedToTask event) {
        final List<TaskLabelId> list = getState().getLabelIdsList()
                                                 .stream()
                                                 .collect(Collectors.toList());
        list.add(event.getLabelId());
        final Task updatedState = getState().newBuilderForType()
                                            .setId(event.getId())
                                            .clearLabelIds()
                                            .addAllLabelIds(list)
                                            .build();
        incrementState(updatedState);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final List<TaskLabelId> list = getState().getLabelIdsList()
                                                 .stream()
                                                 .collect(Collectors.toList());
        list.remove(event.getLabelId());
        final Task state = getState().newBuilderForType()
                                     .clearLabelIds()
                                     .addAllLabelIds(list)
                                     .setId(event.getId())
                                     .build();

        incrementState(state);
    }

    @Subscribe
    public void on(TaskDetails event) {
        final Task state = getState().newBuilderForType()
                                     .setDescription(event.getDescription())
                                     .setPriority(event.getPriority())
                                     .setTaskStatus(event.getTaskStatus())
                                     .build();
        incrementState(state);
    }

}
