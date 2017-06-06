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

package io.spine.examples.todolist.c.aggregate.failures;

import io.spine.base.CommandContext;
import io.spine.examples.todolist.AssignLabelToTaskFailed;
import io.spine.examples.todolist.FailedTaskCommandDetails;
import io.spine.examples.todolist.RemoveLabelFromTaskFailed;
import io.spine.examples.todolist.c.aggregate.TaskLabelsPart;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.failures.CannotAssignLabelToTask;
import io.spine.examples.todolist.c.failures.CannotRemoveLabelFromTask;

/**
 * Utility class for working with {@link TaskLabelsPart} failures.
 *
 * @author Illia Shepilov
 */
public class TaskLabelsPartFailures {

    private TaskLabelsPartFailures() {
    }

    /**
     * Constructs and throws the {@link CannotAssignLabelToTask} failure according to
     * the passed parameters.
     *
     * @param cmd the {@code AssignLabelToTask} command which thrown the failure
     * @param ctx the {@code CommandContext}
     * @throws CannotAssignLabelToTask the failure to throw
     */
    public static void throwCannotAssignLabelToTaskFailure(AssignLabelToTask cmd,
            CommandContext ctx)
            throws CannotAssignLabelToTask {
        final FailedTaskCommandDetails commandFailed =
                FailedTaskCommandDetails.newBuilder()
                                        .setTaskId(cmd.getId())
                                        .build();
        final AssignLabelToTaskFailed assignLabelToTaskFailed =
                AssignLabelToTaskFailed.newBuilder()
                                       .setFailureDetails(commandFailed)
                                       .setLabelId(cmd.getLabelId())
                                       .build();
        throw new CannotAssignLabelToTask(cmd, ctx, assignLabelToTaskFailed);
    }

    /**
     * Constructs and throws the {@link CannotRemoveLabelFromTask} failure according to
     * the passed parameters.
     *
     * @param cmd the {@code AssignLabelToTask} command which thrown the failure
     * @param ctx the {@code CommandContext}
     * @throws CannotRemoveLabelFromTask the failure to throw
     */
    public static void throwCannotRemoveLabelFromTaskFailure(RemoveLabelFromTask cmd,
            CommandContext ctx)
            throws CannotRemoveLabelFromTask {
        final FailedTaskCommandDetails commandFailed =
                FailedTaskCommandDetails.newBuilder()
                                        .setTaskId(cmd.getId())
                                        .build();
        final RemoveLabelFromTaskFailed removeLabelFromTaskFailed =
                RemoveLabelFromTaskFailed.newBuilder()
                                         .setLabelId(cmd.getLabelId())
                                         .setFailureDetails(commandFailed)
                                         .build();
        throw new CannotRemoveLabelFromTask(cmd, ctx, removeLabelFromTaskFailed);
    }
}
