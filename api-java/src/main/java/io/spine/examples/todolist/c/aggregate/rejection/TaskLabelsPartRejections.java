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

package io.spine.examples.todolist.c.aggregate.rejection;

import io.spine.examples.todolist.AddLabelsRejected;
import io.spine.examples.todolist.AddLabelsRejectedVBuilder;
import io.spine.examples.todolist.AssignLabelToTaskRejected;
import io.spine.examples.todolist.AssignLabelToTaskRejectedVBuilder;
import io.spine.examples.todolist.RejectedTaskCommandDetails;
import io.spine.examples.todolist.RejectedTaskCommandDetailsVBuilder;
import io.spine.examples.todolist.RemoveLabelFromTaskRejected;
import io.spine.examples.todolist.RemoveLabelFromTaskRejectedVBuilder;
import io.spine.examples.todolist.c.aggregate.TaskLabelsPart;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.rejection.CannotAddLabels;
import io.spine.examples.todolist.c.rejection.CannotAssignLabelToTask;
import io.spine.examples.todolist.c.rejection.CannotRemoveLabelFromTask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for working with {@link TaskLabelsPart} rejection.
 */
public final class TaskLabelsPartRejections {

    private TaskLabelsPartRejections() {
    }

    /**
     * Constructs and throws the {@link CannotAssignLabelToTask} rejection according to
     * the passed parameters.
     *
     * @param cmd the {@code AssignLabelToTask} command which thrown the rejection
     * @throws CannotAssignLabelToTask the rejection to throw
     */
    public static void throwCannotAssignLabelToTask(AssignLabelToTask cmd)
            throws CannotAssignLabelToTask {
        checkNotNull(cmd);
        RejectedTaskCommandDetails commandDetails = RejectedTaskCommandDetailsVBuilder
                .newBuilder()
                .setTaskId(cmd.getId())
                .build();
        AssignLabelToTaskRejected assignLabelToTaskRejected = AssignLabelToTaskRejectedVBuilder
                .newBuilder()
                .setCommandDetails(commandDetails)
                .setLabelId(cmd.getLabelId())
                .build();
        CannotAssignLabelToTask rejection = CannotAssignLabelToTask
                .newBuilder()
                .setRejectionDetails(assignLabelToTaskRejected)
                .build();
        throw rejection;
    }

    /**
     * Constructs and throws the {@link CannotRemoveLabelFromTask} rejection according to
     * the passed parameters.
     *
     * @param cmd the {@code AssignLabelToTask} command which thrown the rejection
     * @throws CannotRemoveLabelFromTask the rejection to throw
     */
    public static void throwCannotRemoveLabelFromTask(RemoveLabelFromTask cmd)
            throws CannotRemoveLabelFromTask {
        checkNotNull(cmd);
        RejectedTaskCommandDetails commandDetails = RejectedTaskCommandDetailsVBuilder
                .newBuilder()
                .setTaskId(cmd.getId())
                .build();
        RemoveLabelFromTaskRejected removeLabelRejected = RemoveLabelFromTaskRejectedVBuilder
                .newBuilder()
                .setLabelId(cmd.getLabelId())
                .setCommandDetails(commandDetails)
                .build();
        CannotRemoveLabelFromTask rejection = CannotRemoveLabelFromTask
                .newBuilder()
                .setRejectionDetails(removeLabelRejected)
                .build();
        throw rejection;
    }

    /**
     * Constructs and throws the {@link CannotAddLabels} rejection according to
     * the passed parameters.
     *
     * @param cmd the {@code AddLabels} command which thrown the rejection
     * @throws CannotAddLabels the rejection to throw
     */
    public static void throwCannotAddLabelsToTask(AddLabels cmd) throws CannotAddLabels {
        checkNotNull(cmd);
        AddLabelsRejected addLabelsRejected = AddLabelsRejectedVBuilder
                .newBuilder()
                .setId(cmd.getId())
                .addAllExistingLabels(cmd.getExistingLabelsList())
                .addAllNewLabels(cmd.getNewLabelsList())
                .build();
        CannotAddLabels rejection = CannotAddLabels
                .newBuilder()
                .setRejectionDetails(addLabelsRejected)
                .build();
        throw rejection;
    }
}
