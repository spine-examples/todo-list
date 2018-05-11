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

import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.LabelDetailsUpdateRejected;
import io.spine.examples.todolist.RejectedLabelCommandDetails;
import io.spine.examples.todolist.c.aggregate.LabelAggregate;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.rejection.CannotUpdateLabelDetails;

/**
 * Utility class for working with {@link LabelAggregate} rejection.
 *
 * @author Illia Shepilov
 */
public class LabelAggregateRejections {

    private LabelAggregateRejections() {
    }

    /**
     * Constructs and throws the {@link CannotUpdateLabelDetails} rejection according to
     * the passed parameters.
     *
     * @param cmd      the rejected command
     * @param mismatch the {@link ValueMismatch}
     * @throws CannotUpdateLabelDetails the rejection to throw
     */
    public static void throwCannotUpdateLabelDetails(UpdateLabelDetails cmd,
                                                     ValueMismatch mismatch)
            throws CannotUpdateLabelDetails {
        final RejectedLabelCommandDetails commandDetails =
                RejectedLabelCommandDetails.newBuilder()
                                           .setLabelId(cmd.getId())
                                           .build();
        final LabelDetailsUpdateRejected detailsUpdateRejected =
                LabelDetailsUpdateRejected.newBuilder()
                                          .setCommandDetails(commandDetails)
                                          .setLabelDetailsMismatch(mismatch)
                                          .build();
        throw new CannotUpdateLabelDetails(detailsUpdateRejected);
    }
}
