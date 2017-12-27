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

package io.spine.examples.todolist.ui.newtask;

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.CompleteTaskCreation;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.SetTaskDetails;
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.ui.AbstractViewModel;
import io.spine.examples.todolist.Callback;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static io.spine.Identifier.newUuid;
import static io.spine.examples.todolist.TaskPriority.NORMAL;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;

/**
 * The {@link android.arch.lifecycle.ViewModel ViewModel} of the {@link NewTaskActivity}.\
 *
 * @author Dmytro Dashenkov
 */
final class NewTaskViewModel extends AbstractViewModel {

    private final TaskId taskId = newId();
    private final TaskCreationId wizardId = newWizardId();

    private TaskDescription taskDescription = TaskDescription.getDefaultInstance();
    private TaskPriority taskPriority = NORMAL;
    private Timestamp taskDueDate = Timestamp.getDefaultInstance();
    private Collection<LabelDetails> taskLabels = emptySet();

    void startCreatingTask() {
        final StartTaskCreation command = StartTaskCreation.newBuilder()
                                                           .setId(wizardId)
                                                           .setTaskId(taskId)
                                                           .build();
        post(command);
    }

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
        this.taskDescription = taskDescription;
        this.taskPriority = priority;
        this.taskDueDate = taskDueDate;
    }

    void assignLabels(Collection<TaskLabel> existingLabels, Collection<LabelDetails> newLabels) {
        final Collection<LabelId> existingLabelIds = transform(existingLabels, TaskLabel::getId);
        final AddLabels command = AddLabels.newBuilder()
                                           .setId(wizardId)
                                           .addAllExistingLabels(existingLabelIds)
                                           .addAllNewLabels(newLabels)
                                           .build();
        post(command);
        final Collection<LabelDetails> existingLabelDetails = transform(existingLabels, label -> {
            checkNotNull(label);
            return LabelDetails.newBuilder()
                               .setTitle(label.getTitle())
                               .setColor(label.getColor())
                               .build();
        });
        final int labelsCount = existingLabelDetails.size() + newLabels.size();
        final Collection<LabelDetails> allLabels = newArrayListWithCapacity(labelsCount);
        allLabels.addAll(existingLabelDetails);
        allLabels.addAll(newLabels);
        this.taskLabels = allLabels;
    }

    void confirmTaskCreation() {
        final CompleteTaskCreation command = CompleteTaskCreation.newBuilder()
                                                                 .setId(wizardId)
                                                                 .build();
        post(command);
    }

    void fetchLabels(Callback<List<TaskLabel>> callback) {
        execute(() -> {
            final List<TaskLabel> labels = client().getLabels();
            inMainThread(() -> callback.accept(labels));
        });
    }

    TaskDescription getTaskDescription() {
        return taskDescription;
    }

    TaskPriority getTaskPriority() {
        return taskPriority;
    }

    Timestamp getTaskDueDate() {
        return taskDueDate;
    }

    Collection<LabelDetails> getTaskLabels() {
        return unmodifiableCollection(taskLabels);
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
