/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.server.tasks.task;

import com.google.protobuf.Timestamp;
import io.spine.core.Subscribe;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.LabelIdsList;
import io.spine.examples.todolist.tasks.TaskDescription;
import io.spine.examples.todolist.tasks.TaskDetails;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.TaskPriority;
import io.spine.examples.todolist.tasks.TaskStatus;
import io.spine.examples.todolist.tasks.event.DeletedTaskRestored;
import io.spine.examples.todolist.tasks.event.LabelAssignedToTask;
import io.spine.examples.todolist.tasks.event.LabelRemovedFromTask;
import io.spine.examples.todolist.tasks.event.TaskCompleted;
import io.spine.examples.todolist.tasks.event.TaskCreated;
import io.spine.examples.todolist.tasks.event.TaskDeleted;
import io.spine.examples.todolist.tasks.event.TaskDescriptionUpdated;
import io.spine.examples.todolist.tasks.event.TaskDraftCreated;
import io.spine.examples.todolist.tasks.event.TaskDraftFinalized;
import io.spine.examples.todolist.tasks.event.TaskDueDateUpdated;
import io.spine.examples.todolist.tasks.event.TaskPriorityUpdated;
import io.spine.examples.todolist.tasks.event.TaskReopened;
import io.spine.examples.todolist.tasks.view.TaskView;
import io.spine.server.projection.Projection;

import java.util.ArrayList;
import java.util.List;

import static io.spine.examples.todolist.tasks.TaskStatus.COMPLETED;
import static io.spine.examples.todolist.tasks.TaskStatus.DELETED;
import static io.spine.examples.todolist.tasks.TaskStatus.DRAFT;
import static io.spine.examples.todolist.tasks.TaskStatus.FINALIZED;
import static io.spine.examples.todolist.tasks.TaskStatus.OPEN;

/**
 * A projection which mirrors the state of a single task.
 */
@SuppressWarnings("OverlyCoupledClass")
final class TaskViewProjection extends Projection<TaskId, TaskView, TaskView.Builder> {

    @Subscribe
    void taskCreated(TaskCreated e) {
        TaskDetails taskDetails = e.getDetails();
        builder().setId(e.getTaskId())
                 .setDescription(taskDetails.getDescription())
                 .setDueDate(taskDetails.getDueDate())
                 .setStatus(OPEN);
    }

    @Subscribe
    void on(TaskCompleted e) {
        builder().setStatus(COMPLETED);
    }

    @Subscribe
    void on(TaskDraftFinalized e) {
        builder().setStatus(FINALIZED);
    }

    @Subscribe
    void on(TaskReopened e) {
        builder().setStatus(OPEN);
    }

    @Subscribe
    void on(TaskDeleted e) {
        TaskStatus currentStatus = builder().getStatus();
        if (currentStatus == DRAFT) {
            eraseTask();
        } else {
            builder().setStatus(DELETED);
        }
    }

    @Subscribe
    void on(DeletedTaskRestored e) {
        builder().setStatus(OPEN);
    }

    @Subscribe
    void on(TaskDescriptionUpdated e) {
        TaskDescription newDescription = e.getDescriptionChange()
                                          .getNewValue();
        builder().setDescription(newDescription);
    }

    @Subscribe
    void on(TaskDueDateUpdated e) {
        Timestamp newDueDate = e.getDueDateChange()
                                .getNewValue();
        builder().setDueDate(newDueDate);
    }

    @Subscribe
    void on(TaskPriorityUpdated e) {
        TaskPriority newPriority = e.getPriorityChange()
                                    .getNewValue();
        builder().setPriority(newPriority);
    }

    @Subscribe
    void on(TaskDraftCreated e) {
        builder().setId(e.getTaskId())
                 .setStatus(DRAFT);
    }

    @Subscribe
    void on(LabelAssignedToTask e) {
        LabelIdsList newLabelsList = LabelIdsList
                .newBuilder()
                .mergeFrom(builder().getLabelIdsList())
                .addIds(e.getLabelId())
                .vBuild();
        builder().setId(e.getTaskId())
                 .setLabelIdsList(newLabelsList);
    }

    @Subscribe
    void on(LabelRemovedFromTask e) {
        List<LabelId> list = new ArrayList<>(builder().getLabelIdsList()
                                                      .getIdsList());
        list.remove(e.getLabelId());
        LabelIdsList labels = LabelIdsList
                .newBuilder()
                .addAllIds(list)
                .vBuild();
        builder().setLabelIdsList(labels);
    }

    /**
     * Marks this task as both {@code archived} and {@code deleted}.
     *
     * <p>Such a task is never restored for reading. For example,
     * task {@linkplain TaskStatus#DRAFT drafts} are never restored after being deleted.
     */
    private void eraseTask() {
        this.setArchived(true);
        this.setDeleted(true);
    }
}
