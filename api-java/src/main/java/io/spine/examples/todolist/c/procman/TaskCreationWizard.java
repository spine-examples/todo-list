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
import io.spine.core.CommandContext;
import io.spine.examples.todolist.TaskCreation;
import io.spine.examples.todolist.TaskCreation.Stage;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskCreationVBuilder;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.CancelTaskCreation;
import io.spine.examples.todolist.c.commands.CompleteTaskCreation;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.c.commands.SetTaskDetails;
import io.spine.examples.todolist.c.commands.StartTaskCreation;
import io.spine.examples.todolist.c.commands.TodoCommand;
import io.spine.examples.todolist.c.rejection.CannotMoveToStage;
import io.spine.server.command.Assign;
import io.spine.server.procman.CommandRouted;
import io.spine.server.procman.CommandRouter;
import io.spine.server.procman.ProcessManager;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.of;
import static io.spine.examples.todolist.TaskCreation.Stage.CANCELED;
import static io.spine.examples.todolist.TaskCreation.Stage.COMPLETED;
import static io.spine.examples.todolist.TaskCreation.Stage.CONFIRMATION;
import static io.spine.examples.todolist.TaskCreation.Stage.LABEL_ASSIGNMENT;
import static io.spine.examples.todolist.TaskCreation.Stage.TASK_BUILDING;
import static java.util.Collections.emptyList;

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
 *         the current instance of {@code TaskCreationWizard} is
 *         {@linkplain io.spine.server.entity.EntityWithLifecycle#isArchived() archived}. It is
 *         required that the process is in the <b>Building</b> stage before moving to this stage.
 *     <li><b>Canceled</b> - the task creation is canceled. No entities are deleted on this stage.
 *         The user may return to the supervised task (which persists as a draft) and finalize it
 *         manually. This is a terminal stage. This instance of {@code TaskCreationWizard}
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
@SuppressWarnings("unused") // Reflective access.
public class TaskCreationWizard extends ProcessManager<TaskCreationId,
                                                       TaskCreation,
                                                       TaskCreationVBuilder> {

    protected TaskCreationWizard(TaskCreationId id) {
        super(id);
    }

    @Assign
    CommandRouted handle(StartTaskCreation command, CommandContext context)
            throws CannotMoveToStage {
        return handle(TASK_BUILDING, () -> {
            final TaskId taskId = command.getTaskId();
            final TodoCommand createDraft = CreateDraft.newBuilder()
                                                       .setId(taskId)
                                                       .build();
            final CommandRouted commandRouted = newRouterFor(command, context).add(createDraft)
                                                                              .routeAll();
            initProcess(command);
            return commandRouted;
        });
    }

    @Assign
    CommandRouted handle(SetTaskDetails command, CommandContext context) throws CannotMoveToStage {
        final WizardCommands commands = commands();
        return handle(LABEL_ASSIGNMENT, () -> {
            final Collection<? extends TodoCommand> resultCommands =
                    commands.setTaskDetailt(command);
            final CommandRouter router = newRouterFor(command, context);
            resultCommands.forEach(router::add);
            final CommandRouted commandRouted = router.routeAll();
            return commandRouted;
        });
    }

    @Assign
    List<? extends Message> handle(AddLabels command, CommandContext context)
            throws CannotMoveToStage {
        final WizardCommands commands = commands();
        return handleMulticast(CONFIRMATION, () -> {
            final Collection<? extends TodoCommand> existingLabelsCommands =
                    commands.assignExistingLabels(command);
            final Collection<? extends TodoCommand> newLabelsCommands =
                    commands.assignNewLabels(command);
            final List<? extends Message> result;
            if (existingLabelsCommands.isEmpty() && newLabelsCommands.isEmpty()) {
                result = emptyList();
            } else {
                final CommandRouter router = newRouterFor(command, context);
                existingLabelsCommands.forEach(router::add);
                newLabelsCommands.forEach(router::add);
                final CommandRouted commandRouted = router.routeAll();
                result = of(commandRouted);
            }
            return result;
        });
    }

    @Assign
    CommandRouted handle(CompleteTaskCreation command, CommandContext context)
            throws CannotMoveToStage {
        return handle(COMPLETED, () -> {
            final TodoCommand finalizeDraft = FinalizeDraft.newBuilder()
                                                           .setId(taskId())
                                                           .build();
            final CommandRouted commandRouted = newRouterFor(command, context).add(finalizeDraft)
                                                                              .routeAll();
            completeProcess();
            return commandRouted;
        });
    }

    @Assign
    List<? extends Message> handle(CancelTaskCreation command) throws CannotMoveToStage {
        return handleMulticast(CANCELED, () -> {
            completeProcess();
            return emptyList();
        });
    }

    private <E extends Message> E handle(Stage pendingStage, Supplier<E> commandHandler)
            throws CannotMoveToStage {
        checkCanMoveTo(pendingStage);
        final E event = commandHandler.get();
        moveToStage(pendingStage);
        return event;
    }

    private List<? extends Message>
    handleMulticast(Stage pendingStage, Supplier<List<? extends Message>> commandHandler)
            throws CannotMoveToStage {
        checkCanMoveTo(pendingStage);
        final List<? extends Message> event = commandHandler.get();
        moveToStage(pendingStage);
        return event;
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

    private void checkCanMoveTo(Stage pendingStage) throws CannotMoveToStage {
        if (pendingStage == CANCELED && !isTerminated()) {
            return;
        }
        final Stage currentStage = getState().getStage();
        final int expectedStageNumber = currentStage.getNumber() + 1;
        final int actualStageNumber = pendingStage.getNumber();
        if (expectedStageNumber != actualStageNumber) {
            throw new CannotMoveToStage(getId(), pendingStage, currentStage);
        }
    }

    private boolean isTerminated() {
        final Stage currentStage = getState().getStage();
        return currentStage == COMPLETED || currentStage == CANCELED;
    }

    /**
     * Initializes the wizard state with the data from the given command.
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
     * Completes the process by archiving this wizard.
     */
    private void completeProcess() {
        setArchived(true);
    }

    private WizardCommands commands() {
        final TaskId taskId = taskId();
        final WizardCommands result = WizardCommands.create(taskId);
        return result;
    }
}
