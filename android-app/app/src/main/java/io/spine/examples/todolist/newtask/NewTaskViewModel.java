/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.examples.todolist.newtask;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.CompleteTask;
import io.spine.examples.todolist.c.commands.CompleteTaskCreation;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.SetTaskDetails;
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.lifecycle.AbstractViewModel;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskListView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.Identifier.newUuid;

/**
 * The {@link android.arch.lifecycle.ViewModel ViewModel} of the {@link NewTaskActivity}.
 */
final class NewTaskViewModel extends AbstractViewModel {

    private final TaskId taskId = newId();
    private final TaskCreationId wizardId = newWizardId();

    // Required by the `ViewModelProviders` utility.
    public NewTaskViewModel() {}

    /**
     * Creates a new task with the given details fields.
     *
     * <p>Sends the {@link CreateBasicTask} command through the {@linkplain #client() gRPC client}.
     *
     * @param description the new task description
     */
    void createTask(String description, TaskPriority priority, Timestamp taskDueDate) {
        final TaskDescription taskDescription = TaskDescription.newBuilder()
                                                               .setValue(description)
                                                               .build();
        final SetTaskDetails command = SetTaskDetails.newBuilder()
                                                     .setId(wizardId)
                                                     .setDescription(taskDescription)
                                                     .setPriority(priority)
                                                     .setDueDate(taskDueDate)
                                                     .build();
        post(command);
    }

    void startCreatingTask() {
        final StartTaskCreation command = StartTaskCreation.newBuilder()
                                                           .setId(wizardId)
                                                           .setTaskId(taskId)
                                                           .build();
        post(command);
    }

    void assignLabels(Collection<LabelId> existingLabels, Collection<LabelDetails> newLabels) {
        final AddLabels command = AddLabels.newBuilder()
                                           .setId(wizardId)
                                           .addAllExistingLabels(existingLabels)
                                           .addAllNewLabels(newLabels)
                                           .build();
        post(command);
    }

    void confirmTaskCreation() {
        final CompleteTaskCreation command = CompleteTaskCreation.newBuilder()
                                                                 .setId(wizardId)
                                                                 .build();
        post(command);
    }

    Future<List<LabelledTasksView>> getLabels() {
        final SettableFuture<List<LabelledTasksView>> result = SettableFuture.create();
        execute(() -> {
            final List<LabelledTasksView> taskViews = client().getLabelledTasksView();
            result.set(taskViews);
        });
        return result;
    }

    /**
     * Generates a new random {@link TaskId}.
     *
     * @return new instance of {@link TaskId} with
     * a {@linkplain io.spine.Identifier#newUuid() UUID} value.
     */
    private static TaskId newId() {
        return TaskId.newBuilder()
                     .setValue(newUuid())
                     .build();
    }

    /**
     * Generates a new random {@link TaskCreationId}.
     *
     * @return new instance of {@link TaskCreationId} with
     * a {@linkplain io.spine.Identifier#newUuid() UUID} value.
     */
    private static TaskCreationId newWizardId() {
        return TaskCreationId.newBuilder()
                             .setValue(newUuid())
                             .build();
    }
}
