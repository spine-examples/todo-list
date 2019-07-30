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
import io.spine.examples.todolist.event.DeletedTaskRestored;
import io.spine.examples.todolist.event.LabelAssignedToTask;
import io.spine.examples.todolist.event.LabelRemovedFromTask;
import io.spine.examples.todolist.event.TaskCompleted;
import io.spine.examples.todolist.event.TaskCreated;
import io.spine.examples.todolist.event.TaskDeleted;
import io.spine.examples.todolist.event.TaskDescriptionUpdated;
import io.spine.examples.todolist.event.TaskDraftCreated;
import io.spine.examples.todolist.event.TaskDraftFinalized;
import io.spine.examples.todolist.event.TaskDueDateUpdated;
import io.spine.examples.todolist.event.TaskPriorityUpdated;
import io.spine.examples.todolist.event.TaskReopened;
import io.spine.server.projection.Projection;

import java.util.ArrayList;
import java.util.List;

/**
 * A projection which mirrors the state of a single task.
 */
@SuppressWarnings({"Duplicates", "OverlyCoupledClass"}) // OK for this projection.
public class TaskViewProjection extends Projection<TaskId, TaskView, TaskView.Builder> {

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
    void taskRestored(DeletedTaskRestored restored) {
        builder().setStatus(TaskStatus.OPEN);
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
        builder().setId(event.getTaskId())
                 .setStatus(TaskStatus.DRAFT);
    }

    @Subscribe
    void labelAssignedToTask(LabelAssignedToTask event) {
        LabelIdsList newLabelsList = LabelIdsList
                .newBuilder()
                .mergeFrom(builder().getLabelIdsList())
                .addIds(event.getLabelId())
                .vBuild();
        builder().setId(event.getTaskId())
                 .setLabelIdsList(newLabelsList);
    }

    @Subscribe
    void labelRemovedFromTask(LabelRemovedFromTask event) {
        List<LabelId> list = new ArrayList<>(builder().getLabelIdsList()
                                                      .getIdsList());
        list.remove(event.getLabelId());
        LabelIdsList labelIdsList = LabelIdsList
                .newBuilder()
                .addAllIds(list)
                .build();
        builder().setLabelIdsList(labelIdsList);
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
