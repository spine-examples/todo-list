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
import io.spine.examples.todolist.c.commands.CancelTaskCreation;
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
import io.spine.examples.todolist.c.rejection.CannotMoveToStage;
import io.spine.server.command.Assign;
import io.spine.server.procman.CommandRouted;
import io.spine.server.procman.CommandRouter;
import io.spine.server.procman.ProcessManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.of;
import static io.spine.Identifier.newUuid;
import static io.spine.examples.todolist.LabelColor.GRAY;
import static io.spine.examples.todolist.TaskCreation.Stage.BUILDING;
import static io.spine.examples.todolist.TaskCreation.Stage.CANCELED;
import static io.spine.examples.todolist.TaskCreation.Stage.COMPLETED;
import static io.spine.examples.todolist.TaskCreation.Stage.STARED;
import static io.spine.examples.todolist.TaskCreation.Stage.TCS_UNKNOWN;
import static io.spine.examples.todolist.TaskPriority.TP_UNDEFINED;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * A process manager supervising the task creation process.
 *
 * <p>The task creation process has following stages:
 * <ol>
 *     <li><b>Started</b> - the process is started. This is the initializing stage, i.e. there were
 *         no other stages before this one. A draft for the supervised task is created once
 *         the process is started.
 *     <li><b>Building</b> - the task building is started. The process moves to this stage once
 *         the first field is set to the supervised task. The order of the fields being set to
 *         the task does not matter.
 *     <li><b>Completed</b> - the task creation process is completed. This is a terminal stage,
 *         i.e. no stages may follow this stage. At this stage the supervised task is finalized and
 *         the current instance of {@code TaskCreationProcessManager} is
 *         {@linkplain io.spine.server.entity.EntityWithLifecycle#isArchived() archived}. It is
 *         required that the process is in the <b>Building</b> stage before moving to this stage.
 *     <li><b>Canceled</b> - the task creation is canceled. No entities are deleted on this stage.
 *         The user may return to the supervised task (which persists as a draft) and finalize it
 *         manually. This is a terminal stage. This instance of {@code TaskCreationProcessManager}
 *         is {@linkplain io.spine.server.entity.EntityWithLifecycle#isArchived() archived} on this
 *         stage.
 * </ol>
 *
 * <p>The possible stage transitions may be depicted as follows:
 * <pre>
 *     {@code
 *     --> Started --> Building --> Completed.
 *               \           \
 *                ---------------> Canceled.
 *     }
 * </pre>
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
    CommandRouted handle(StartTaskCreation command, CommandContext context)
            throws CannotMoveToStage {
        checkExactly(TCS_UNKNOWN);
        final TaskId taskId = command.getTaskId();
        final CreateDraft createDraft = CreateDraft.newBuilder()
                                                   .setId(taskId)
                                                   .build();
        final CommandRouted commandRouted = newRouterFor(command, context)
                .add(createDraft)
                .routeAll();
        initProcess(command);
        moveToStage(STARED);
        return commandRouted;
    }

    @Assign
    CommandRouted handle(SetTaskDescription command, CommandContext context)
            throws CannotMoveToStage {
        checkHit(STARED);
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
        moveToStage(BUILDING);
        return commandRouted;
    }

    @Assign
    CommandRouted handle(SetTaskPriority command, CommandContext context)
            throws CannotMoveToStage {
        checkHit(STARED);
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
        moveToStage(BUILDING);
        return commandRouted;
    }

    @Assign
    CommandRouted handle(SetTaskDueDate command, CommandContext context)
            throws CannotMoveToStage {
        checkHit(STARED);
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
        moveToStage(BUILDING);
        return commandRouted;
    }

    @Assign
    CommandRouted handle(AddLabels command, CommandContext context)
            throws CannotMoveToStage {
        checkHit(STARED);
        final Collection<? extends Message> existingLabelsCommands = assignExistingLabels(command);
        final Collection<? extends Message> newLabelsCommands = assignNewLabels(command);
        final CommandRouter router = newRouterFor(command, context);
        existingLabelsCommands.forEach(router::add);
        newLabelsCommands.forEach(router::add);
        final CommandRouted commandRouted = router.routeAll();
        moveToStage(BUILDING);
        return commandRouted;
    }

    @Assign
    CommandRouted handle(CompleteTaskCreation command, CommandContext context)
            throws CannotMoveToStage {
        checkExactly(BUILDING);
        final FinalizeDraft finalizeDraft = FinalizeDraft.newBuilder()
                                                         .setId(taskId())
                                                         .build();
        final CommandRouted commandRouted = newRouterFor(command, context).add(finalizeDraft)
                                                                          .routeAll();
        completeProcess();
        moveToStage(COMPLETED);
        return commandRouted;
    }

    @Assign
    List<? extends Message> handle(CancelTaskCreation command) throws CannotMoveToStage {
        checkHit(STARED);
        completeProcess();
        moveToStage(CANCELED);
        return emptyList();
    }

    /**
     * Creates commands that assign the specified in {@code AddLabels} command existing labels to
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
     * Creates the commands that:
     * <ol>
     *     <li>Create the specified in {@code AddLabels} command labels.
     *     <li>Mutate the labels as specified in the command.
     *     <li>Assign those labels to the supervised task.
     * </ol>
     *
     * @param command the command that defines the new labels to assign to the task
     * @return commands creating and assigning those labels
     */
    private Collection<? extends Message> assignNewLabels(AddLabels command) {
        final Collection<? extends Message> commands = command.getNewLabelsList()
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
    private Stream<? extends Message> createAndAssignLabel(LabelDetails label) {
        final LabelId labelId = LabelId.newBuilder()
                                       .setValue(newUuid())
                                       .build();
        final CreateBasicLabel createBasicLabel = createLabel(labelId, label);
        final UpdateLabelDetails updateLabelDetails = setColorToLabel(labelId, label);
        final AssignLabelToTask assignLabelToTask = assignLabel(labelId);
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
    private static CreateBasicLabel createLabel(LabelId labelId, LabelDetails label) {
        final CreateBasicLabel createBasicLabel = CreateBasicLabel.newBuilder()
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
    private static UpdateLabelDetails setColorToLabel(LabelId labelId, LabelDetails label) {
        final LabelDetails previousDetails = label.toBuilder()
                                                  .setColor(GRAY)
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
        return updateLabelDetails;
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
     * Checks if the process is either in the given {@code stage} or has already passed it.
     *
     * @param stage the stage to check
     * @throws CannotMoveToStage upon the check failure
     */
    private void checkHit(Stage stage) throws CannotMoveToStage {
        final Stage currentStage = getState().getStage();
        if (currentStage.getNumber() < stage.getNumber()) {
            throw new CannotMoveToStage(getId(), stage, currentStage);
        }
    }

    /**
     * Checks if the process is currently exactly in this stage.
     *
     * @param stage the stage to check
     * @throws CannotMoveToStage upon the check failure
     */
    private void checkExactly(Stage stage) throws CannotMoveToStage {
        final Stage currentStage = getState().getStage();
        if (currentStage != stage) {
            throw new CannotMoveToStage(getId(), stage, currentStage);
        }
    }

    /**
     * Initializes the process manager state with the data from the given command.
     *
     * @param cmd the command starting the process
     */
    private void initProcess(StartTaskCreation cmd) {
        final TaskCreationId id = getId();
        checkArgument(id.equals(cmd.getId()));
        getBuilder().setId(id)
                    .setTaskId(cmd.getTaskId());
    }

    /**
     * Completes the process by archiving this instance of {@code ProcessManager}.
     */
    private void completeProcess() {
        setArchived(true);
    }
}
