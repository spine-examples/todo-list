/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.q.projection;

import com.google.protobuf.Timestamp;
import io.spine.core.Subscribe;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.LabelIdsList;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskDetails;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.events.TaskCreated;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.server.projection.Projection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A projection that mirrors a state of a single task.
 */
@SuppressWarnings({"unused", "Duplicates"}) // OK for projection.
public class TaskViewProjection extends Projection<TaskId, TaskView, TaskViewVBuilder> {

    public TaskViewProjection(TaskId id) {
        super(id);
    }

    @Subscribe
    public void taskCreated(TaskCreated event) {
        TaskDetails taskDetails = event.getDetails();
        builder().setId(event.getId())
                 .setDescription(taskDetails.getDescription());
    }

    @Subscribe
    public void taskDescriptionUpdated(TaskDescriptionUpdated event) {
        String newDescriptionValue = event.getDescriptionChange()
                                          .getNewValue();
        TaskDescription newDescription = TaskDescription
                .newBuilder()
                .setValue(newDescriptionValue)
                .build();
        builder().setDescription(newDescription);
    }

    @Subscribe
    public void taskDueDateUpdated(TaskDueDateUpdated event) {
        Timestamp newDueDate = event.getDueDateChange()
                                    .getNewValue();
        builder().setDueDate(newDueDate);
    }

    @Subscribe
    public void taskPriorityUpdated(TaskPriorityUpdated event) {
        TaskPriority newPriority = event.getPriorityChange()
                                        .getNewValue();
        builder().setPriority(newPriority);
    }

    @Subscribe
    public void draftCreated(TaskDraftCreated event) {
        TaskDetails taskDetails = event.getDetails();
        builder().setId(event.getId());
    }

    @Subscribe
    public void labelAssignedToTask(LabelAssignedToTask event) {
        Collection<LabelId> list = new ArrayList<>(builder().getLabelIdsList()
                                                            .getIdsList());
        list.add(event.getLabelId());
        LabelIdsList labelIdsList = LabelIdsList
                .newBuilder()
                .addAllIds(list)
                .build();
        builder().setLabelIdsList(labelIdsList);
    }

    @Subscribe
    public void labelRemovedFromTask(LabelRemovedFromTask event) {
        Collection<LabelId> list = new ArrayList<>(builder().getLabelIdsList()
                                                            .getIdsList());
        list.remove(event.getLabelId());
        LabelIdsList labelIdsList = LabelIdsList
                .newBuilder()
                .addAllIds(list)
                .build();
        builder().setLabelIdsList(labelIdsList);
    }
}
