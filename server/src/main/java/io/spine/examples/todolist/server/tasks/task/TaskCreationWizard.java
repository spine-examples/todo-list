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

package io.spine.examples.todolist.server.tasks.task;

import io.spine.base.CommandMessage;
import io.spine.core.CommandContext;
import io.spine.examples.todolist.tasks.LabelDetails;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.TaskCreation;
import io.spine.examples.todolist.tasks.TaskCreation.Stage;
import io.spine.examples.todolist.tasks.TaskCreationId;
import io.spine.examples.todolist.tasks.TaskDetailsUpdateRejected;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.command.AddLabels;
import io.spine.examples.todolist.tasks.command.AssignLabelToTask;
import io.spine.examples.todolist.tasks.command.CancelTaskCreation;
import io.spine.examples.todolist.tasks.command.CompleteTaskCreation;
import io.spine.examples.todolist.tasks.command.CreateDraft;
import io.spine.examples.todolist.tasks.command.FinalizeDraft;
import io.spine.examples.todolist.tasks.command.SkipLabels;
import io.spine.examples.todolist.tasks.command.StartTaskCreation;
import io.spine.examples.todolist.tasks.command.UpdateTaskDetails;
import io.spine.examples.todolist.tasks.event.LabelAssignmentSkipped;
import io.spine.examples.todolist.tasks.event.TaskCreationCanceled;
import io.spine.examples.todolist.tasks.rejection.CannotAddLabels;
import io.spine.examples.todolist.tasks.rejection.CannotMoveToStage;
import io.spine.examples.todolist.tasks.rejection.CannotUpdateTaskDetails;
import io.spine.server.command.Assign;
import io.spine.server.command.Command;
import io.spine.server.procman.ProcessManager;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static io.spine.examples.todolist.server.tasks.label.TaskLabelsPartRejections.throwCannotAddLabelsToTask;
import static io.spine.examples.todolist.tasks.TaskCreation.Stage.CANCELED;
import static io.spine.examples.todolist.tasks.TaskCreation.Stage.COMPLETED;
import static io.spine.examples.todolist.tasks.TaskCreation.Stage.CONFIRMATION;
import static io.spine.examples.todolist.tasks.TaskCreation.Stage.LABEL_ASSIGNMENT;
import static io.spine.examples.todolist.tasks.TaskCreation.Stage.TASK_DEFINITION;
import static io.spine.validate.Validate.isDefault;

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
 *         {@linkplain io.spine.server.entity.AbstractEntity#isArchived() archived} It is
 *         required that the process is in the <b>Confirmation</b> stage before moving to this
 *         stage.
 *     <li><b>Canceled</b> - the task creation is canceled. No entities are deleted on this stage.
 *         The user may return to the supervised task (which persists as a draft) and finalize it
 *         manually. This is a terminal stage. This instance of {@code TaskCreationWizard}
 *         is {@linkplain io.spine.server.entity.AbstractEntity#isArchived() archived} on this
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
 *                       \                    \                \
 *                        --------------------------------------------> Canceled.
 *     }
 * </pre>
 *
 * <p>The data from the already visited stages can be re-submitted at any point of time until the
 * process is terminated. This allows moving back and forth in the wizard, performing multiple data
 * modifications.
 */
@SuppressWarnings({"unused" /* Command handler methods invoked via reflection. */,
        "OverlyCoupledClass" /* OK for process manager entity. */})
public class TaskCreationWizard
        extends ProcessManager<TaskCreationId, TaskCreation, TaskCreation.Builder> {

    protected TaskCreationWizard(TaskCreationId id) {
        super(id);
    }

    @Command
    CreateDraft handle(StartTaskCreation command, CommandContext context)
            throws CannotMoveToStage {
        return transit(TASK_DEFINITION, () -> {
            TaskId taskId = command.getTaskId();
            CreateDraft createDraft = CreateDraft
                    .newBuilder()
                    .setId(taskId)
                    .vBuild();
            initProcess(command);
            return createDraft;
        });
    }

    @Command
    Collection<? extends CommandMessage> handle(UpdateTaskDetails command, CommandContext context)
            throws CannotMoveToStage, CannotUpdateTaskDetails {
        boolean isTaskDefinition = builder().getStage() == TASK_DEFINITION;
        if (isTaskDefinition && isDefault(command.getDescriptionChange())) {
            throwCannotUpdateTaskDetails(command);
        }
        return transit(LABEL_ASSIGNMENT,
                       () -> commands().updateTaskDetails(command));
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
            Collection<AssignLabelToTask> existingLabelsCommands =
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
    LabelAssignmentSkipped handle(SkipLabels command, CommandContext context)
            throws CannotMoveToStage {
        return transit(CONFIRMATION, () -> {
            LabelAssignmentSkipped assignmentSkipped = LabelAssignmentSkipped
                    .newBuilder()
                    .setTaskId(taskId())
                    .vBuild();
            return assignmentSkipped;
        });
    }

    @Command
    FinalizeDraft handle(CompleteTaskCreation command, CommandContext context)
            throws CannotMoveToStage {
        return transit(COMPLETED, () -> {
            FinalizeDraft finalizeDraft = FinalizeDraft
                    .newBuilder()
                    .setId(taskId())
                    .vBuild();
            completeProcess();
            return finalizeDraft;
        });
    }

    @Assign
    TaskCreationCanceled handle(CancelTaskCreation command) throws CannotMoveToStage {
        return transit(CANCELED, () -> {
            completeProcess();
            TaskCreationCanceled taskCreationCanceled = TaskCreationCanceled
                    .newBuilder()
                    .setTaskId(taskId())
                    .vBuild();
            return taskCreationCanceled;
        });
    }

    /**
     * Tries to transit the process to the specified stage by handling a command with the given
     * command handler.
     *
     * <p>If the requested stage is preceding to the current state, the actual transition won't
     * happen, but the action will still be performed.
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
        moveIfSubsequent(requestedStage);
        return result;
    }

    /**
     * Obtains the ID of the supervised task.
     */
    private TaskId taskId() {
        return builder().getTaskId();
    }

    /**
     * Moves to the requested stage if it's subsequent to the current stage.
     *
     * @param stage
     *         the requested stage
     */
    private void moveIfSubsequent(Stage stage) {
        int currentStageNumber = builder().getStage()
                                          .getNumber();
        int requestedStageNumber = stage.getNumber();
        if (currentStageNumber < requestedStageNumber) {
            builder().setStage(stage);
        }
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
        Stage currentStage = builder().getStage();
        int pendingStageNumber = pendingStage.getNumber();
        int currentStageNumber = currentStage.getNumber();
        boolean movingForward = pendingStageNumber > currentStageNumber;
        if ((pendingStage == CANCELED || !movingForward) && !isTerminated()) {
            return;
        }
        int expectedStageNumber = currentStageNumber + 1;
        if (expectedStageNumber != pendingStageNumber) {
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

    private static void throwCannotUpdateTaskDetails(UpdateTaskDetails command)
            throws CannotUpdateTaskDetails {
        TaskDetailsUpdateRejected details = TaskDetailsUpdateRejected
                .newBuilder()
                .setId(command.getId())
                .setNewDescription(command.getDescriptionChange()
                                          .getNewValue())
                .setNewPriority(command.getPriorityChange()
                                       .getNewValue())
                .setNewDueDate(command.getDueDateChange()
                                      .getNewValue())
                .buildPartial();
        CannotUpdateTaskDetails rejection = CannotUpdateTaskDetails
                .newBuilder()
                .setRejectionDetails(details)
                .build();
        throw rejection;
    }
}
