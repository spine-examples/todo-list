/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Timestamp;
import io.spine.base.CommandMessage;
import io.spine.change.StringChange;
import io.spine.change.StringChangeVBuilder;
import io.spine.change.TimestampChange;
import io.spine.change.TimestampChangeVBuilder;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsChange;
import io.spine.examples.todolist.LabelDetailsChangeVBuilder;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.LabelIdVBuilder;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.PriorityChangeVBuilder;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.AssignLabelToTaskVBuilder;
import io.spine.examples.todolist.c.commands.CreateBasicLabelVBuilder;
import io.spine.examples.todolist.c.commands.SetTaskDetails;
import io.spine.examples.todolist.c.commands.UpdateLabelDetailsVBuilder;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDescriptionVBuilder;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDateVBuilder;
import io.spine.examples.todolist.c.commands.UpdateTaskPriorityVBuilder;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.LabelColor.GRAY;
import static io.spine.examples.todolist.TaskPriority.TP_UNDEFINED;
import static io.spine.validate.Validate.isNotDefault;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * A factory of commands emitted by the {@link TaskCreationWizard}.
 */
final class WizardCommands {

    private final TaskId taskId;

    private WizardCommands(TaskId taskId) {
        this.taskId = taskId;
    }

    /**
     * Creates an instance of {@code WizardCommands} which creates commands with the given
     * {@code TaskId}.
     *
     * <p>The task ID may or may not identify the intended command handler.
     *
     * @param taskId
     *         the ID of the task which the commands are associated with
     * @return new instance of {@code WizardCommands}
     */
    static WizardCommands create(TaskId taskId) {
        return new WizardCommands(taskId);
    }

    /**
     * Creates commands setting the task details to the target task
     *
     * <p>This method is guaranteed to generate at least one command. Effectively, each non-default
     * field of the {@code SetTaskDetails} command causes a command to be generated.
     *
     * @param src
     *         the source command
     * @return new commands generated from the given {@code src} command
     */
    Collection<? extends CommandMessage> setTaskDetails(SetTaskDetails src) {
        ImmutableSet.Builder<CommandMessage> commands = ImmutableSet.builder();
        TaskDescription description = src.getDescription();
        CommandMessage updateDescription = updateTaskDescription(description);
        commands.add(updateDescription);
        TaskPriority priority = src.getPriority();
        if (enumIsNotDefault(priority)) {
            CommandMessage updatePriority = updateTaskPriority(priority);
            commands.add(updatePriority);
        }
        Timestamp dueDate = src.getDueDate();
        if (isNotDefault(dueDate)) {
            CommandMessage updateDueDate = updateTaskDueDate(dueDate);
            commands.add(updateDueDate);
        }
        return commands.build();
    }

    /**
     * Creates a command setting the description of the target task to the given value.
     *
     * <p>This method creates a command which initializes the task description, i.e. the task
     * doesn't have a description yet.
     *
     * @param description
     *         the description to set to the target task
     * @return the command to the target task
     */
    private CommandMessage updateTaskDescription(TaskDescription description) {
        checkArgument(isNotDefault(description));
        StringChange change = StringChangeVBuilder
                .newBuilder()
                .setNewValue(description.getValue())
                .build();
        UpdateTaskDescription updateCommand = UpdateTaskDescriptionVBuilder
                .newBuilder()
                .setId(taskId)
                .setDescriptionChange(change)
                .build();
        return updateCommand;
    }

    /**
     * Creates a command setting the priority of the target task to the given value.
     *
     * <p>This method creates a command which initializes the task priority, i.e. the task priority
     * is not set yet.
     *
     * @param priority
     *         the priority to set to the target task
     * @return the command to the target task
     */
    private CommandMessage updateTaskPriority(TaskPriority priority) {
        checkArgument(enumIsNotDefault(priority));
        PriorityChange change = PriorityChangeVBuilder
                .newBuilder()
                .setNewValue(priority)
                .setPreviousValue(TP_UNDEFINED)
                .build();
        CommandMessage updateCommand = UpdateTaskPriorityVBuilder
                .newBuilder()
                .setId(taskId)
                .setPriorityChange(change)
                .build();
        return updateCommand;
    }

    /**
     * Creates a command setting the due date of the target task to the given value.
     *
     * <p>This method creates a command which initializes the task due date, i.e. the task due date
     * is not set yet.
     *
     * @param dueDate
     *         the priority to set to the target task
     * @return the command to the target task
     */
    private CommandMessage updateTaskDueDate(Timestamp dueDate) {
        checkArgument(isNotDefault(dueDate));
        TimestampChange change = TimestampChangeVBuilder
                .newBuilder()
                .setNewValue(dueDate)
                .build();
        CommandMessage updateCommand = UpdateTaskDueDateVBuilder
                .newBuilder()
                .setId(taskId)
                .setDueDateChange(change)
                .build();
        return updateCommand;
    }

    /**
     * Creates commands that assign the specified in {@code AddLabels} command existing labels to
     * the target task.
     *
     * @param src
     *         the command that defines the labels to assign
     * @return the commands assigning those tasks
     */
    Collection<? extends CommandMessage> assignExistingLabels(AddLabels src) {
        Collection<? extends CommandMessage> commands =
                src.getExistingLabelsList()
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
     * <li>Assign those labels to the target task.
     * </ol>
     *
     * @param src
     *         the command that defines the new labels to assign to the task
     * @return commands creating and assigning those labels
     */
    Collection<? extends CommandMessage> assignNewLabels(AddLabels src) {
        Collection<? extends CommandMessage> commands =
                src.getNewLabelsList()
                   .stream()
                   .flatMap(this::createAndAssignLabel)
                   .collect(toList());
        return commands;
    }

    /**
     * Generates commands that create a label from the given {@link LabelDetails} and assign that
     * label to the target task.
     *
     * @param label
     *         the label details describing the task to create
     * @return the command messages creating and assigning a label
     */
    private Stream<? extends CommandMessage> createAndAssignLabel(LabelDetails label) {
        LabelId labelId = LabelIdVBuilder
                .newBuilder()
                .setValue(newUuid())
                .build();
        CommandMessage createBasicLabel = createLabel(labelId, label);
        CommandMessage updateLabelDetails = setColorToLabel(labelId, label);
        CommandMessage assignLabelToTask = assignLabel(labelId);
        return ImmutableList.of(createBasicLabel, updateLabelDetails, assignLabelToTask)
                            .stream();
    }

    /**
     * Creates a label with the given ID from the given {@code LabelDetails}.
     *
     * @param labelId
     *         the ID to assign to the new label
     * @param label
     *         the label data
     * @return a command creating a basic label with the given ID and title; note that the label
     *         color is ignored for now
     */
    private static CommandMessage createLabel(LabelId labelId, LabelDetails label) {
        CommandMessage createBasicLabel = CreateBasicLabelVBuilder
                .newBuilder()
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
     * @param labelId
     *         the ID of the label to update
     * @param label
     *         the new label details
     * @return a command updating the label details
     */
    private static CommandMessage setColorToLabel(LabelId labelId, LabelDetails label) {
        LabelDetails previousDetails =
                label.toBuilder()
                     .setColor(GRAY)
                     .build();
        LabelDetailsChange change = LabelDetailsChangeVBuilder
                .newBuilder()
                .setPreviousDetails(previousDetails)
                .setNewDetails(label)
                .build();
        CommandMessage updateLabelDetails = UpdateLabelDetailsVBuilder
                .newBuilder()
                .setId(labelId)
                .setLabelDetailsChange(change)
                .build();
        return updateLabelDetails;
    }

    /**
     * Creates an {@link AssignLabelToTask} command which assigns the label with the given ID to
     * the target task.
     *
     * @param labelId
     *         the ID of the label to assign
     * @return new instance of a command message
     */
    private CommandMessage assignLabel(LabelId labelId) {
        return AssignLabelToTaskVBuilder.newBuilder()
                                        .setId(taskId)
                                        .setLabelId(labelId)
                                        .build();
    }

    /**
     * Checks if the given value is not default.
     *
     * <p>A Protobuf enum value is considered non-default if its number is greater than zero.
     *
     * @param priority
     *         the value to check
     * @return {@code true} if the value is not default, {@code false} otherwise
     */
    private static boolean enumIsNotDefault(TaskPriority priority) {
        return priority.getNumber() > 0;
    }
}
