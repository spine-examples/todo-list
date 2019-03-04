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

package io.spine.examples.todolist.c.aggregate;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.TaskLabelVBuilder;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.events.LabelCreated;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.examples.todolist.c.rejection.CannotUpdateLabelDetails;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import java.util.Collections;
import java.util.List;

import static io.spine.examples.todolist.c.aggregate.rejection.LabelAggregateRejections.throwCannotUpdateLabelDetails;

/**
 * The aggregate managing the state of a {@link TaskLabel}.
 */
@SuppressWarnings("unused") // A lot of reflectively used handler methods.
public class LabelAggregate extends Aggregate<LabelId, TaskLabel, TaskLabelVBuilder> {

    @VisibleForTesting
    static final LabelColor DEFAULT_LABEL_COLOR = LabelColor.GRAY;

    protected LabelAggregate(LabelId id) {
        super(id);
    }

    @Assign
    List<? extends Message> handle(CreateBasicLabel cmd) {
        LabelDetails.Builder labelDetails = LabelDetails
                .newBuilder()
                .setTitle(cmd.getLabelTitle());
        LabelCreated result = LabelCreated
                .newBuilder()
                .setId(cmd.getLabelId())
                .setDetails(labelDetails)
                .build();
        return Collections.singletonList(result);
    }

    @Assign
    List<? extends Message> handle(UpdateLabelDetails cmd)
            throws CannotUpdateLabelDetails {
        TaskLabel state = state();
        LabelDetails actualLabelDetails = LabelDetails
                .newBuilder()
                .setColor(state.getColor())
                .setTitle(state.getTitle())
                .build();
        LabelDetailsChange labelDetailsChange = cmd.getLabelDetailsChange();
        LabelDetails expectedLabelDetails = labelDetailsChange.getPreviousDetails();

        boolean isEquals = actualLabelDetails.equals(expectedLabelDetails);

        if (!isEquals) {
            LabelDetails newLabelDetails = labelDetailsChange.getNewDetails();
            ValueMismatch mismatch =
                    unexpectedValue(expectedLabelDetails, actualLabelDetails, newLabelDetails);
            throwCannotUpdateLabelDetails(cmd, mismatch);
        }

        LabelId labelId = cmd.getId();
        LabelDetailsUpdated labelDetailsUpdated = LabelDetailsUpdated
                .newBuilder()
                .setLabelId(labelId)
                .setLabelDetailsChange(labelDetailsChange)
                .build();
        List<? extends Message> result = Collections.singletonList(labelDetailsUpdated);
        return result;
    }

    @Apply
    void labelCreated(LabelCreated event) {
        builder().setId(event.getId())
                 .setTitle(event.getDetails()
                                .getTitle())
                 .setColor(DEFAULT_LABEL_COLOR);
    }

    @Apply
    void labelDetailsUpdated(LabelDetailsUpdated event) {
        LabelDetails labelDetails = event.getLabelDetailsChange()
                                         .getNewDetails();
        builder().setId(id())
                 .setTitle(labelDetails.getTitle())
                 .setColor(labelDetails.getColor());
    }
}
