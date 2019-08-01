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

package io.spine.examples.todolist.server.tasks.label;

import com.google.common.annotations.VisibleForTesting;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.tasks.LabelColor;
import io.spine.examples.todolist.tasks.LabelDetails;
import io.spine.examples.todolist.tasks.LabelDetailsChange;
import io.spine.examples.todolist.tasks.LabelDetailsUpdateRejected;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.RejectedLabelCommandDetails;
import io.spine.examples.todolist.tasks.TaskLabel;
import io.spine.examples.todolist.tasks.command.CreateBasicLabel;
import io.spine.examples.todolist.tasks.command.UpdateLabelDetails;
import io.spine.examples.todolist.tasks.event.LabelCreated;
import io.spine.examples.todolist.tasks.event.LabelDetailsUpdated;
import io.spine.examples.todolist.tasks.rejection.CannotUpdateLabelDetails;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * The aggregate managing the state of a {@link TaskLabel}.
 */
public class LabelAggregate extends Aggregate<LabelId, TaskLabel, TaskLabel.Builder> {

    @VisibleForTesting
    public static final LabelColor DEFAULT_LABEL_COLOR = LabelColor.GRAY;

    protected LabelAggregate(LabelId id) {
        super(id);
    }

    @Assign
    LabelCreated handle(CreateBasicLabel cmd) {
        LabelDetails labelDetails = LabelDetails
                .newBuilder()
                .setTitle(cmd.getLabelTitle())
                .buildPartial();
        LabelCreated result = LabelCreated
                .newBuilder()
                .setId(cmd.getLabelId())
                .setDetails(labelDetails)
                .vBuild();
        return result;
    }

    @Assign
    LabelDetailsUpdated handle(UpdateLabelDetails cmd)
            throws CannotUpdateLabelDetails {
        TaskLabel state = state();
        LabelDetails actualLabelDetails = LabelDetails
                .newBuilder()
                .setColor(state.getColor())
                .setTitle(state.getTitle())
                .vBuild();
        LabelDetailsChange labelDetailsChange = cmd.getLabelDetailsChange();
        LabelDetails expectedLabelDetails = labelDetailsChange.getPreviousDetails();

        boolean isEquals = actualLabelDetails.equals(expectedLabelDetails);

        if (!isEquals) {
            LabelDetails newLabelDetails = labelDetailsChange.getNewDetails();
            ValueMismatch mismatch =
                    unexpectedValue(expectedLabelDetails, actualLabelDetails, newLabelDetails);
            throwCannotUpdate(cmd, mismatch);
        }

        LabelId labelId = cmd.getId();
        LabelDetailsUpdated result = LabelDetailsUpdated
                .newBuilder()
                .setLabelId(labelId)
                .setLabelDetailsChange(labelDetailsChange)
                .vBuild();
        return result;
    }

    @Apply
    private void labelCreated(LabelCreated event) {
        builder().setId(event.getId())
                 .setTitle(event.getDetails()
                                .getTitle())
                 .setColor(DEFAULT_LABEL_COLOR);
    }

    @Apply
    private void labelDetailsUpdated(LabelDetailsUpdated event) {
        LabelDetails labelDetails = event.getLabelDetailsChange()
                                         .getNewDetails();
        builder().setId(id())
                 .setTitle(labelDetails.getTitle())
                 .setColor(labelDetails.getColor());
    }

    /**
     * Constructs and throws the {@link CannotUpdateLabelDetails} rejection according to
     * the passed parameters.
     *
     * @param cmd
     *         the rejected command
     * @param mismatch
     *         the {@link ValueMismatch}
     * @throws CannotUpdateLabelDetails
     *         the rejection to throw
     */
    private static void throwCannotUpdate(UpdateLabelDetails cmd, ValueMismatch mismatch)
            throws CannotUpdateLabelDetails {
        RejectedLabelCommandDetails commandDetails = RejectedLabelCommandDetails
                .newBuilder()
                .setLabelId(cmd.getId())
                .buildPartial();
        LabelDetailsUpdateRejected detailsUpdateRejected = LabelDetailsUpdateRejected
                .newBuilder()
                .setCommandDetails(commandDetails)
                .setLabelDetailsMismatch(mismatch)
                .buildPartial();
        CannotUpdateLabelDetails rejection = CannotUpdateLabelDetails
                .newBuilder()
                .setRejectionDetails(detailsUpdateRejected)
                .build();
        throw rejection;
    }
}
