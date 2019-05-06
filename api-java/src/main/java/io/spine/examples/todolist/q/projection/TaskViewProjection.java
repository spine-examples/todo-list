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
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.events.LabelAssignedToTask;
import io.spine.examples.todolist.c.events.LabelRemovedFromTask;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskCreated;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.events.TaskDraftFinalized;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;
import io.spine.server.projection.Projection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A projection which mirrors the state of a single task.
 */
@SuppressWarnings({"unused", "Duplicates", "OverlyCoupledClass"}) // OK for this projection.
public class TaskViewProjection extends Projection<TaskId, TaskView, TaskViewVBuilder> {

    public TaskViewProjection(TaskId id) {
        super(id);
    }

    @Subscribe
    void taskCreated(TaskCreated event) {
        TaskDetails taskDetails = event.getDetails();
        builder().setId(event.getTaskId())
                 .setDescription(taskDetails.getDescription())
                 .setDueDate(taskDetails.getDueDate())
                 .setStatus(TaskStatus.OPEN);
    }

    @Subscribe
    void taskCompleted(TaskCompleted event) {
        builder().setStatus(TaskStatus.COMPLETED);
    }

    @Subscribe
    void taskDraftFinalized(TaskDraftFinalized event) {
        builder().setStatus(TaskStatus.FINALIZED);
    }

    @Subscribe
    void taskReopened(TaskReopened event) {
        builder().setStatus(TaskStatus.OPEN);
    }

    @Subscribe
    void taskDeleted(TaskDeleted deleted) {
        TaskStatus currentStatus = builder().getStatus();
        if (currentStatus == TaskStatus.DRAFT) {
            eraseTask();
        } else {
            builder().setStatus(TaskStatus.DELETED);
        }
    }

    @Subscribe
    void taskDescriptionUpdated(TaskDescriptionUpdated event) {
        TaskDescription newDescription = event.getDescriptionChange()
                                              .getNewValue();
        builder().setDescription(newDescription);
    }

    @Subscribe
    void taskDueDateUpdated(TaskDueDateUpdated event) {
        Timestamp newDueDate = event.getDueDateChange()
                                    .getNewValue();
        builder().setDueDate(newDueDate);
    }

    @Subscribe
    void taskPriorityUpdated(TaskPriorityUpdated event) {
        TaskPriority newPriority = event.getPriorityChange()
                                        .getNewValue();
        builder().setPriority(newPriority);
    }

    @Subscribe
    void draftCreated(TaskDraftCreated event) {
        TaskDetails taskDetails = event.getDetails();
        builder().setId(event.getTaskId())
                 .setStatus(TaskStatus.DRAFT);
    }

    @Subscribe
    void labelAssignedToTask(LabelAssignedToTask event) {
        Collection<LabelId> list = new ArrayList<>(builder().getLabelIdsList()
                                                            .getIdsList());
        list.add(event.getLabelId());
        LabelIdsList labelIdsList = LabelIdsList
                .newBuilder()
                .addAllIds(list)
                .build();
        builder().setId(event.getTaskId())
                 .setLabelIdsList(labelIdsList);
    }

    @Subscribe
    void labelRemovedFromTask(LabelRemovedFromTask event) {
        Collection<LabelId> list = new ArrayList<>(builder().getLabelIdsList()
                                                            .getIdsList());
        list.remove(event.getLabelId());
        LabelIdsList labelIdsList = LabelIdsList
                .newBuilder()
                .addAllIds(list)
                .build();
        builder().setLabelIdsList(labelIdsList);
    }

    /**
     * Tasks that are being deleted while in {@code Draft} state are deleted beyong recovery.
     */
    private void eraseTask() {
        this.setArchived(true);
        this.setDeleted(true);
    }
}
