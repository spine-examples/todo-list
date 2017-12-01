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
import static io.spine.examples.todolist.TaskPriority.TP_UNDEFINED;
import static java.util.stream.Collectors.toSet;

/**
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
    List<? extends Message> handle(StartTaskCreation command, CommandContext context) {
        checkCurrentStateAfter(STARED);
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
    List<? extends Message> handle(SetTaskDescription command, CommandContext context) {
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
    List<? extends Message> handle(SetTaskPriority command, CommandContext context) {
        checkCurrentStateAfter(DESCRIPTION_SET);
        final TaskPriority priority = command.getPriority();
        final PriorityChange priorityChange = PriorityChange.newBuilder()
                                                            .setNewValue(priority)
                                                            .setPreviousValue(TP_UNDEFINED)
                                                            .build();
        final UpdateTaskPriority updateTaskPriority =
                UpdateTaskPriority.newBuilder()
                                  .setId(getState().getTaskId())
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
    List<? extends Message> handle(SetTaskDueDate command, CommandContext context) {
        checkCurrentStateAfter(PRIORITY_SET);
        final Timestamp dueDate = command.getDueDate();
        final TimestampChange change = TimestampChange.newBuilder()
                                                      .setNewValue(dueDate)
                                                      .build();
        final UpdateTaskDueDate updateDueDate =
                UpdateTaskDueDate.newBuilder()
                                 .setId(getState().getTaskId())
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
    List<? extends Message> handle(AddLabels command, CommandContext context) {
        checkCurrentStateAfter(PRIORITY_SET);
        final Collection<? extends Message> existingLabelsCommands = addExistingLabels(command);
        final Collection<? extends Message> newLabelsCommands = addNewLabels(command);
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
    List<? extends Message> handle(CompleteTaskCreation command, CommandContext context) {
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

    private Collection<? extends Message> addExistingLabels(AddLabels command) {
        final Collection<? extends Message> commands =
                command.getExistingLabelsList()
                       .stream()
                       .map(this::assignLabel)
                       .collect(toSet());
        return commands;
    }

    private Collection<? extends Message> addNewLabels(AddLabels command) {
        final Collection<? extends Message> commands = command.getNewLabelsList()
                                                              .stream()
                                                              .flatMap(this::createAndAssignLabel)
                                                              .collect(toSet());
        return commands;
    }

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

    private AssignLabelToTask assignLabel(LabelId labelId) {
        return AssignLabelToTask.newBuilder()
                                .setId(getState().getTaskId())
                                .setLabelId(labelId)
                                .build();
    }

    private TaskId taskId() {
        return getState().getTaskId();
    }

    private void moveToStage(Stage stage) {
        getBuilder().setStage(stage);
    }

    private void checkCurrentStateAfter(Stage stage) {
        final Stage currentStage = getState().getStage();
        if (currentStage.getNumber() <= stage.getNumber()) {
            // TODO:2017-12-01:dmytro.dashenkov: Throw a rejection.
            throw new IllegalStateException();
        }
    }
}
