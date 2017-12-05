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

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.change.StringChange;
import io.spine.change.TimestampChange;
import io.spine.core.CommandContext;
import io.spine.core.Subscribe;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.PriorityChange;
import io.spine.examples.todolist.TaskCreation;
import io.spine.examples.todolist.TaskCreation.Stage;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskCreationVBuilder;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CompleteTaskCreation;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.SetTaskDescription;
import io.spine.examples.todolist.c.commands.SetTaskDueDate;
import io.spine.examples.todolist.c.commands.SetTaskPriority;
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.commands.UpdateTaskDescription;
import io.spine.examples.todolist.c.commands.UpdateTaskDueDate;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.c.events.LabelsAdded;
import io.spine.examples.todolist.c.events.TaskCreationCompleted;
import io.spine.examples.todolist.c.events.TaskCreationStarted;
import io.spine.examples.todolist.c.events.TaskDescriptionSet;
import io.spine.examples.todolist.c.events.TaskDueDateSet;
import io.spine.examples.todolist.c.events.TaskPrioritySet;
import io.spine.examples.todolist.c.rejection.CannotMoveToStage;
import io.spine.server.command.Assign;
import io.spine.server.procman.CommandRouted;
import io.spine.server.procman.CommandRouter;
import io.spine.server.procman.ProcessManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.of;
import static io.spine.Identifier.newUuid;
import static io.spine.examples.todolist.TaskCreation.Stage.COMPLETED;
import static io.spine.examples.todolist.TaskCreation.Stage.DESCRIPTION_SET;
import static io.spine.examples.todolist.TaskCreation.Stage.DUE_DATE_SET;
import static io.spine.examples.todolist.TaskCreation.Stage.LABELS_ADDED;
import static io.spine.examples.todolist.TaskCreation.Stage.PRIORITY_SET;
import static io.spine.examples.todolist.TaskCreation.Stage.STARED;
import static io.spine.examples.todolist.TaskCreation.Stage.TCS_UNKNOWN;
import static io.spine.examples.todolist.TaskPriority.TP_UNDEFINED;
import static java.util.stream.Collectors.toSet;

/**
 * A process manager supervising the task creation process.
 *
 * <p>The task creation process has following stages:
 * <ol>
 *     <li><b>Started</b> - the process is started. This is the initializing stage, i.e. there were
 *         no other stages before this one. A draft for the supervised task is created once
 *         the process is started.
 *     <li><b>Description set</b> for the supervised task. The process comes to this stage after it
 *         has been started.
 *     <li><b>Priority set</b> for the supervised task.
 *     <li><b>Due date set</b> for the supervised task. This stage is optional and can be skipped.
 *     <li><b>Labels added</b> for the supervised task. At this stage the labels are added to
 *         the supervised task. If a label does not exist before facing this stage, it is created
 *         with the given parameters (title and color).
 *     <li><b>Completed</b> - the task creation process completed. This is a terminal stage, i.e. no
 *         stages may follow this stage. At this stage the supervised task is finalized and
 *         the current instance of {@code TaskCreationProcessManager} is
 *         {@linkplain io.spine.server.entity.EntityWithLifecycle#isArchived() archived}.
 * </ol>
 *
 * <p>Once started, the process cannot be canceled. In case if the process is deserted
 * (e.g. the user closes the creation wizard), the task draft persists.
 *
 * @author Dmytro Dashenkov
 */
@SuppressWarnings({
        "unused", // Reflective access.
        "OverlyCoupledClass" // OK for a Process Manager.
})
public class TaskCreationProcessManager extends ProcessManager<TaskCreationId,
                                                               TaskCreation,
                                                               TaskCreationVBuilder> {

    protected TaskCreationProcessManager(TaskCreationId id) {
        super(id);
    }

    @Assign
    List<? extends Message> handle(StartTaskCreation command, CommandContext context)
            throws CannotMoveToStage {
        checkCurrentStateAfter(TCS_UNKNOWN);
        final TaskId taskId = command.getTaskId();
        final TaskCreationStarted event = TaskCreationStarted.newBuilder()
                                                             .setId(command.getId())
                                                             .setTaskId(taskId)
                                                             .build();
        final CreateDraft createDraft = CreateDraft.newBuilder()
                                                   .setId(taskId)
                                                   .build();
        final CommandRouted commandRouted = newRouterFor(command, context)
                .add(createDraft)
                .routeAll();
        return of(event, commandRouted);
    }

    @Assign
    List<? extends Message> handle(SetTaskDescription command, CommandContext context)
            throws CannotMoveToStage {
        checkCurrentStateAfter(STARED);
        final TaskDescription description = command.getDescription();
        final StringChange change = StringChange.newBuilder()
                                                .setNewValue(description.getValue())
                                                .build();
        final UpdateTaskDescription updateCommand =
                UpdateTaskDescription.newBuilder()
                                     .setId(taskId())
                                     .setDescriptionChange(change)
                                     .build();
        final CommandRouted commandRouted = newRouterFor(command, context)
                .add(updateCommand)
                .routeAll();
        final TaskDescriptionSet event = TaskDescriptionSet.newBuilder()
                                                           .setId(getId())
                                                           .setDescription(description)
                                                           .build();
        return of(commandRouted, event);
    }

    @Assign
    List<? extends Message> handle(SetTaskPriority command, CommandContext context)
            throws CannotMoveToStage {
        checkCurrentStateAfter(DESCRIPTION_SET);
        final TaskPriority priority = command.getPriority();
        final PriorityChange priorityChange = PriorityChange.newBuilder()
                                                            .setNewValue(priority)
                                                            .setPreviousValue(TP_UNDEFINED)
                                                            .build();
        final UpdateTaskPriority updateTaskPriority =
                UpdateTaskPriority.newBuilder()
                                  .setId(taskId())
                                  .setPriorityChange(priorityChange)
                                  .build();
        final CommandRouted commandRouted = newRouterFor(command, context)
                .add(updateTaskPriority)
                .routeAll();
        final TaskPrioritySet event = TaskPrioritySet.newBuilder()
                                                     .setId(getId())
                                                     .setPriority(priority)
                                                     .build();
        return of(commandRouted, event);
    }

    @Assign
    List<? extends Message> handle(SetTaskDueDate command, CommandContext context)
            throws CannotMoveToStage {
        checkCurrentStateAfter(PRIORITY_SET);
        final Timestamp dueDate = command.getDueDate();
        final TimestampChange change = TimestampChange.newBuilder()
                                                      .setNewValue(dueDate)
                                                      .build();
        final UpdateTaskDueDate updateDueDate =
                UpdateTaskDueDate.newBuilder()
                                 .setId(taskId())
                                 .setDueDateChange(change)
                                 .build();
        final CommandRouted commandRouted = newRouterFor(command, context)
                .add(updateDueDate)
                .routeAll();
        final TaskDueDateSet event = TaskDueDateSet.newBuilder()
                                                   .setId(getId())
                                                   .setDueDate(dueDate)
                                                   .build();
        return of(commandRouted, event);
    }

    @Assign
    List<? extends Message> handle(AddLabels command, CommandContext context)
            throws CannotMoveToStage {
        checkCurrentStateAfter(PRIORITY_SET);
        final Collection<? extends Message> existingLabelsCommands = assignExistingLabels(command);
        final Collection<? extends Message> newLabelsCommands = assignNewLabels(command);
        final CommandRouter router = newRouterFor(command, context);
        existingLabelsCommands.forEach(router::add);
        newLabelsCommands.forEach(router::add);
        final Message event = LabelsAdded.newBuilder()
                                         .setId(getId())
                                         .addAllExistingLabels(command.getExistingLabelsList())
                                         .addAllNewLabels(command.getNewLabelsList())
                                         .build();
        return of(router.routeAll(), event);
    }

    @Assign
    List<? extends Message> handle(CompleteTaskCreation command, CommandContext context)
            throws CannotMoveToStage {
        checkCurrentStateAfter(LABELS_ADDED);
        final TaskCreationCompleted event = TaskCreationCompleted.newBuilder()
                                                                 .setId(getId())
                                                                 .build();
        final FinalizeDraft finalizeDraft = FinalizeDraft.newBuilder()
                                                         .setId(taskId())
                                                         .build();
        final CommandRouted commandRouted = newRouterFor(command, context).add(finalizeDraft)
                                                                          .routeAll();
        return of(commandRouted, event);
    }

    @Subscribe
    public void on(TaskCreationStarted event) {
        moveToStage(STARED);
        getBuilder().setId(event.getId())
                    .setTaskId(event.getTaskId());
    }

    @Subscribe
    public void on(TaskDescriptionSet event) {
        moveToStage(DESCRIPTION_SET);
    }

    @Subscribe
    public void on(TaskPrioritySet event) {
        moveToStage(PRIORITY_SET);
    }

    @Subscribe
    public void on(TaskDueDateSet event) {
        moveToStage(DUE_DATE_SET);
    }

    @Subscribe
    public void on(LabelsAdded event) {
        moveToStage(LABELS_ADDED);
    }

    @Subscribe
    public void on(TaskCreationCompleted event) {
        moveToStage(COMPLETED);
        setArchived(true);
    }

    /**
     * Creates commands that assign the specified with the {@link AddLabels} command labels to
     * the supervised task.
     *
     * @param command the command that defines the labels to assign
     * @return the commands assigning those tasks
     */
    private Collection<? extends Message> assignExistingLabels(AddLabels command) {
        final Collection<? extends Message> commands =
                command.getExistingLabelsList()
                       .stream()
                       .map(this::assignLabel)
                       .collect(toSet());
        return commands;
    }

    /**
     * Creates commands that create and assign to the supervised task the yet non-existing labels
     * defined with the given {@link AddLabels} command.
     *
     * @param command the command that defines the new labels to assign to the task
     * @return commands creating and assigning those labels
     */
    private Collection<? extends Message> assignNewLabels(AddLabels command) {
        final Collection<? extends Message> commands = command.getNewLabelsList()
                                                              .stream()
                                                              .flatMap(this::createAndAssignLabel)
                                                              .collect(toSet());
        return commands;
    }

    /**
     * Generates commands that create a label from the given {@link LabelDetails} and assign that
     * label to the supervised task.
     *
     * @param label the label details describing the task to create
     * @return the command messages creating an assigning a label
     */
    private Stream<? extends Message> createAndAssignLabel(LabelDetails label) {
        final LabelId labelId = LabelId.newBuilder()
                                       .setValue(newUuid())
                                       .build();
        final CreateBasicLabel createBasicLabel = CreateBasicLabel.newBuilder()
                                                                  .setLabelId(labelId)
                                                                  .setLabelTitle(label.getTitle())
                                                                  .build();
        final LabelDetails previousDetails = label.toBuilder()
                                                  .clearColor()
                                                  .build();
        final LabelDetailsChange change = LabelDetailsChange.newBuilder()
                                                            .setPreviousDetails(previousDetails)
                                                            .setNewDetails(label)
                                                            .build();
        final UpdateLabelDetails updateLabelDetails =
                UpdateLabelDetails.newBuilder()
                                  .setId(labelId)
                                  .setLabelDetailsChange(change)
                                  .build();
        final AssignLabelToTask assignLabelToTask = assignLabel(labelId);
        return of(createBasicLabel, updateLabelDetails, assignLabelToTask).stream();
    }

    /**
     * Creates an {@link AssignLabelToTask} command which assigns the label with the given ID to
     * the supervised task.
     *
     * @param labelId the ID of the label to assign
     * @return new instance of a command message
     */
    private AssignLabelToTask assignLabel(LabelId labelId) {
        return AssignLabelToTask.newBuilder()
                                .setId(taskId())
                                .setLabelId(labelId)
                                .build();
    }

    private TaskId taskId() {
        return getState().getTaskId();
    }

    /**
     * Sets the current stage to the given value.
     *
     * @param stage the requested stage
     */
    private void moveToStage(Stage stage) {
        getBuilder().setStage(stage);
    }

    /**
     * Checks if the given {@code stage} may go after the current one and throws
     * {@link CannotMoveToStage} rejection if it may not.
     *
     * @param stage the stage to check
     */
    private void checkCurrentStateAfter(Stage stage) throws CannotMoveToStage {
        final Stage currentStage = getState().getStage();
        if (currentStage.getNumber() <= stage.getNumber()) {
            throw new CannotMoveToStage(getId(), stage, currentStage);
        }
    }
}
