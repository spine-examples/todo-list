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

package io.spine.examples.todolist.c.procman;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Timestamp;
import io.spine.change.StringChange;
import io.spine.change.TimestampChange;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.SetTaskDetails;
import io.spine.examples.todolist.c.commands.TodoCommand;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.of;
import static io.spine.Identifier.newUuid;
import static io.spine.examples.todolist.LabelColor.GRAY;
import static io.spine.examples.todolist.TaskPriority.TP_UNDEFINED;
import static io.spine.validate.Validate.isNotDefault;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Dmytro Dashenkov
 */
final class WizardCommands {

    private final TaskId taskId;

    private WizardCommands(TaskId taskId) {
        this.taskId = taskId;
    }

    static WizardCommands create(TaskId taskId) {
        return new WizardCommands(taskId);
    }

    Collection<? extends TodoCommand> setTaskDetailt(SetTaskDetails src) {
        final ImmutableSet.Builder<TodoCommand> commands = ImmutableSet.builder();
        final TaskDescription description = src.getDescription();
        final TodoCommand updateDescription = updateTaskDescription(description);
        commands.add(updateDescription);
        final TaskPriority priority = src.getPriority();
        if (enumIsNotDefault(priority)) {
            final TodoCommand updatePriority = updateTaskPriority(priority);
            commands.add(updatePriority);
        }
        final Timestamp dueDate = src.getDueDate();
        if (isNotDefault(dueDate)) {
            final TodoCommand updateDueDate = updateTaskDueDate(dueDate);
            commands.add(updateDueDate);
        }
        return commands.build();
    }

    private TodoCommand updateTaskDescription(TaskDescription description) {
        checkArgument(isNotDefault(description));
        final StringChange change = StringChange.newBuilder()
                                                .setNewValue(description.getValue())
                                                .build();
        final UpdateTaskDescription updateCommand =
                UpdateTaskDescription.newBuilder()
                                     .setId(taskId)
                                     .setDescriptionChange(change)
                                     .build();
        return updateCommand;
    }

    private TodoCommand updateTaskPriority(TaskPriority priority) {
        checkArgument(enumIsNotDefault(priority));
        final PriorityChange change = PriorityChange.newBuilder()
                                                    .setNewValue(priority)
                                                    .setPreviousValue(TP_UNDEFINED)
                                                    .build();
        final TodoCommand updateCommand = UpdateTaskPriority.newBuilder()
                                                            .setId(taskId)
                                                            .setPriorityChange(change)
                                                            .build();
        return updateCommand;
    }

    private TodoCommand updateTaskDueDate(Timestamp dueDate) {
        checkArgument(isNotDefault(dueDate));
        final TimestampChange change = TimestampChange.newBuilder()
                                                      .setNewValue(dueDate)
                                                      .build();
        final TodoCommand updateCommand = UpdateTaskDueDate.newBuilder()
                                                           .setId(taskId)
                                                           .setDueDateChange(change)
                                                           .build();
        return updateCommand;
    }

    /**
     * Creates commands that assign the specified in {@code AddLabels} command existing labels to
     * the supervised task.
     *
     * @param command the command that defines the labels to assign
     * @return the commands assigning those tasks
     */
    Collection<? extends TodoCommand> assignExistingLabels(AddLabels command) {
        final Collection<? extends TodoCommand> commands =
                command.getExistingLabelsList()
                       .stream()
                       .map(this::assignLabel)
                       .collect(toSet());
        return commands;
    }

    /**
     * Creates the commands that:
     * <ol>
     * <li>Create the specified in {@code AddLabels} command labels.
     * <li>Mutate the labels as specified in the command.
     * <li>Assign those labels to the supervised task.
     * </ol>
     *
     * @param command the command that defines the new labels to assign to the task
     * @return commands creating and assigning those labels
     */
    Collection<? extends TodoCommand> assignNewLabels(AddLabels command) {
        final Collection<? extends TodoCommand> commands =
                command.getNewLabelsList()
                       .stream()
                       .flatMap(this::createAndAssignLabel)
                       .collect(toList());
        return commands;
    }

    /**
     * Generates commands that create a label from the given {@link LabelDetails} and assign that
     * label to the supervised task.
     *
     * @param label the label details describing the task to create
     * @return the command messages creating an assigning a label
     */
    private Stream<? extends TodoCommand> createAndAssignLabel(LabelDetails label) {
        final LabelId labelId = LabelId.newBuilder()
                                       .setValue(newUuid())
                                       .build();
        final TodoCommand createBasicLabel = createLabel(labelId, label);
        final TodoCommand updateLabelDetails = setColorToLabel(labelId, label);
        final TodoCommand assignLabelToTask = assignLabel(labelId);
        return of(createBasicLabel, updateLabelDetails, assignLabelToTask).stream();
    }

    /**
     * Creates a label with the given ID from the given {@code LabelDetails}.
     *
     * @param labelId the ID to assign to the new label
     * @param label   the label data
     * @return a command creating a basic label with the given ID and title; note that the label
     *         color is ignored for now
     */
    private static TodoCommand createLabel(LabelId labelId, LabelDetails label) {
        final TodoCommand createBasicLabel = CreateBasicLabel.newBuilder()
                                                             .setLabelId(labelId)
                                                             .setLabelTitle(label.getTitle())
                                                             .build();
        return createBasicLabel;
    }

    /**
     * Updates the details of the label with the given ID.
     *
     * <p>It is expected that the label color is the default {@code GRAY} and the label title has
     * not changed.
     *
     * @param labelId the ID of the label to update
     * @param label   the new label details
     * @return a command updating the label details
     */
    private static TodoCommand setColorToLabel(LabelId labelId, LabelDetails label) {
        final LabelDetails previousDetails = label.toBuilder()
                                                  .setColor(GRAY)
                                                  .build();
        final LabelDetailsChange change = LabelDetailsChange.newBuilder()
                                                            .setPreviousDetails(previousDetails)
                                                            .setNewDetails(label)
                                                            .build();
        final TodoCommand updateLabelDetails =
                UpdateLabelDetails.newBuilder()
                                  .setId(labelId)
                                  .setLabelDetailsChange(change)
                                  .build();
        return updateLabelDetails;
    }

    /**
     * Creates an {@link AssignLabelToTask} command which assigns the label with the given ID to
     * the supervised task.
     *
     * @param labelId the ID of the label to assign
     * @return new instance of a command message
     */
    private TodoCommand assignLabel(LabelId labelId) {
        return AssignLabelToTask.newBuilder()
                                .setId(taskId)
                                .setLabelId(labelId)
                                .build();
    }

    private static boolean enumIsNotDefault(TaskPriority priority) {
        return priority.getNumber() > 0;
    }
}
