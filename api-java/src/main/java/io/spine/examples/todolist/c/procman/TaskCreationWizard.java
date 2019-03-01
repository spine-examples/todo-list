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

import io.spine.base.CommandMessage;
import io.spine.core.CommandContext;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskCreation;
import io.spine.examples.todolist.TaskCreation.Stage;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskCreationVBuilder;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.CancelTaskCreation;
import io.spine.examples.todolist.c.commands.CompleteTaskCreation;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.CreateDraftVBuilder;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraftVBuilder;
import io.spine.examples.todolist.c.commands.SetTaskDetails;
import io.spine.examples.todolist.c.commands.SkipLabels;
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.c.rejection.CannotAddLabels;
import io.spine.examples.todolist.c.rejection.CannotMoveToStage;
import io.spine.server.command.Assign;
import io.spine.server.command.Command;
import io.spine.server.model.Nothing;
import io.spine.server.procman.ProcessManager;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static io.spine.examples.todolist.TaskCreation.Stage.CANCELED;
import static io.spine.examples.todolist.TaskCreation.Stage.COMPLETED;
import static io.spine.examples.todolist.TaskCreation.Stage.CONFIRMATION;
import static io.spine.examples.todolist.TaskCreation.Stage.LABEL_ASSIGNMENT;
import static io.spine.examples.todolist.TaskCreation.Stage.TASK_DEFINITION;
import static io.spine.examples.todolist.c.aggregate.rejection.TaskLabelsPartRejections.throwCannotAddLabelsToTask;

/**
 * A process manager supervising the task creation process.
 *
 * <p>The task creation process has following stages:
 * <ol>
 *     <li><b>Task Definition</b> - the task building is started: the task is being defined by its
 *         primary fields. The process moves to this stage once the empty task draft is created.
 *     <li><b>Label Assignment</b> - the labels are being created and assigned to the supervised
 *         task. The process moves to this stage after the primary task data is set. This stage may
 *         be skipped via the {@link SkipLabels} command.
 *     <li><b>Confirmation</b> - all the data is set to the label and the user may check if the data
 *         is correct.
 *     <li><b>Completed</b> - the task creation process is completed. This is a terminal stage,
 *         i.e. no stages may follow this stage. At this stage the supervised task is finalized and
 *         the current instance of {@code TaskCreationWizard} is
 *         {@linkplain io.spine.server.entity.EntityWithLifecycle#isArchived() archived}. It is
 *         required that the process is in the <b>Confirmation</b> stage before moving to this
 *         stage.
 *     <li><b>Canceled</b> - the task creation is canceled. No entities are deleted on this stage.
 *         The user may return to the supervised task (which persists as a draft) and finalize it
 *         manually. This is a terminal stage. This instance of {@code TaskCreationWizard}
 *         is {@linkplain io.spine.server.entity.EntityWithLifecycle#isArchived() archived} on this
 *         stage.
 * </ol>
 *
 * <p>On any stage (except for the terminal ones), the process can be moved to the <b>Canceled</b>
 * stage, i.e. the task creation would be canceled. All the intermediate states of the supervised
 * entities are valid, so no additional clean up is required on cancellation.
 *
 * <p>All other stages are sequential and cannot be skipped.
 *
 * <p>The possible stage transitions may be depicted as follows:
 * <pre>
 *     {@code
 *     --> Task Definition --> Label Assignment -->  Confirmation -->  Completed.
 *                       \                    \                 \
 *                        --------------------------------------------> Canceled.
 *     }
 * </pre>
 */
@SuppressWarnings("unused") // Command handler methods invoked via reflection.
public class TaskCreationWizard extends ProcessManager<TaskCreationId,
                                                       TaskCreation,
                                                       TaskCreationVBuilder> {

    protected TaskCreationWizard(TaskCreationId id) {
        super(id);
    }

    @Command
    CreateDraft handle(StartTaskCreation command, CommandContext context)
            throws CannotMoveToStage {
        return transit(TASK_DEFINITION, () -> {
            TaskId taskId = command.getTaskId();
            CreateDraft createDraft = CreateDraftVBuilder
                    .newBuilder()
                    .setId(taskId)
                    .build();
            initProcess(command);
            return createDraft;
        });
    }

    @Command
    Collection<? extends CommandMessage> handle(SetTaskDetails command, CommandContext context)
            throws CannotMoveToStage {
        return transit(LABEL_ASSIGNMENT, () -> commands().setTaskDetails(command));
    }

    @Command
    Collection<? extends CommandMessage> handle(AddLabels command, CommandContext context)
            throws CannotMoveToStage, CannotAddLabels {
        List<LabelId> existingLabels = command.getExistingLabelsList();
        List<LabelDetails> newLabels = command.getNewLabelsList();
        if (existingLabels.isEmpty() && newLabels.isEmpty()) {
            throwCannotAddLabelsToTask(command);
        }
        WizardCommands commands = commands();
        return transit(CONFIRMATION, () -> {
            Collection<? extends CommandMessage> existingLabelsCommands =
                    commands.assignExistingLabels(command);
            Collection<? extends CommandMessage> newLabelsCommands =
                    commands.assignNewLabels(command);
            Collection<CommandMessage> result = newArrayList();
            result.addAll(existingLabelsCommands);
            result.addAll(newLabelsCommands);
            return result;
        });
    }

    @Assign
    Nothing handle(SkipLabels command, CommandContext context) throws CannotMoveToStage {
        return transit(CONFIRMATION, this::nothing);
    }

    @Command
    FinalizeDraft handle(CompleteTaskCreation command, CommandContext context)
            throws CannotMoveToStage {
        return transit(COMPLETED, () -> {
            FinalizeDraft finalizeDraft = FinalizeDraftVBuilder
                    .newBuilder()
                    .setId(taskId())
                    .build();
            completeProcess();
            return finalizeDraft;
        });
    }

    @Assign
    Nothing handle(CancelTaskCreation command) throws CannotMoveToStage {
        return transit(CANCELED, () -> {
            completeProcess();
            return nothing();
        });
    }

    /**
     * Transits the process to the specified state by handling a command with the given command
     * handler.
     *
     * @param requestedStage
     *         the stage to transit to
     * @param commandHandler
     *         the routine of handling a command; returns the command handling result,
     *         an event or a list of events
     * @param <R>
     *         the type of the command handling result
     * @return the command handling result, an event or a list of events
     * @throws CannotMoveToStage
     *         if the requested transition cannot be performed
     */
    private <R> R transit(Stage requestedStage, Supplier<R> commandHandler)
            throws CannotMoveToStage {
        checkCanMoveTo(requestedStage);
        R result = commandHandler.get();
        moveToStage(requestedStage);
        return result;
    }

    /**
     * Obtains the ID of the supervised task.
     */
    private TaskId taskId() {
        return builder().getTaskId();
    }

    /**
     * Sets the current stage to the given value.
     *
     * @param stage
     *         the requested stage
     */
    private void moveToStage(Stage stage) {
        builder().setStage(stage);
    }

    /**
     * Checks if the process can move to the given stage.
     *
     * @param pendingStage
     *         the stage to move to
     * @throws CannotMoveToStage
     *         if the transition from current to specified stage is illegal
     */
    private void checkCanMoveTo(Stage pendingStage) throws CannotMoveToStage {
        if (pendingStage == CANCELED && !isTerminated()) {
            return;
        }
        Stage currentStage = builder().getStage();
        int expectedStageNumber = currentStage.getNumber() + 1;
        int actualStageNumber = pendingStage.getNumber();
        if (expectedStageNumber != actualStageNumber) {
            CannotMoveToStage rejection = CannotMoveToStage
                    .newBuilder()
                    .setProcessId(id())
                    .setRequestedStage(pendingStage)
                    .setCurrentStage(currentStage)
                    .build();
            throw rejection;
        }
    }

    /**
     * Determines if the process is in a terminal state.
     *
     * @return {@code true} if current process state is terminal, {@code false} otherwise
     */
    private boolean isTerminated() {
        Stage currentStage = builder().getStage();
        return currentStage == COMPLETED || currentStage == CANCELED;
    }

    /**
     * Initializes the wizard state with the data from the given command.
     *
     * @param cmd
     *         the command starting the process
     */
    private void initProcess(StartTaskCreation cmd) {
        TaskCreationId id = id();
        checkArgument(id.equals(cmd.getId()));
        builder().setId(id)
                 .setTaskId(cmd.getTaskId());
    }

    /**
     * Completes the process by archiving this wizard.
     */
    private void completeProcess() {
        setArchived(true);
    }

    /**
     * Obtains a wizard command factory to use for the resulting command generation.
     *
     * @return an instance of {@link WizardCommands} for this process
     */
    private WizardCommands commands() {
        TaskId taskId = taskId();
        WizardCommands result = WizardCommands.create(taskId);
        return result;
    }
}
