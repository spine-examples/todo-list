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

import io.spine.examples.todolist.c.aggregate.LabelAggregate;
import io.spine.change.ValueMismatch;
import io.spine.examples.todolist.FailedLabelCommandDetails;
import io.spine.examples.todolist.LabelDetailsUpdateFailed;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.failures.CannotUpdateLabelDetails;

/**
 * Utility class for working with {@link LabelAggregate} failures.
 *
 * @author Illia Shepilov
 */
public class LabelAggregateFailures {

    private LabelAggregateFailures() {
    }

    /**
     * Constructs and throws the {@link CannotUpdateLabelDetails} failure according to
     * the passed parameters.
     *
     * @param cmd      the failed command
     * @param mismatch the {@link ValueMismatch}
     * @throws CannotUpdateLabelDetails the failure to throw
     */
    public static void throwCannotUpdateLabelDetailsFailure(UpdateLabelDetails cmd,
                                                            ValueMismatch mismatch)
            throws CannotUpdateLabelDetails {
        final FailedLabelCommandDetails labelCommandFailed =
                FailedLabelCommandDetails.newBuilder()
                                         .setLabelId(cmd.getId())
                                         .build();
        final LabelDetailsUpdateFailed labelDetailsUpdateFailed =
                LabelDetailsUpdateFailed.newBuilder()
                                        .setFailureDetails(labelCommandFailed)
                                        .setLabelDetailsMismatch(mismatch)
                                        .build();
        throw new CannotUpdateLabelDetails(labelDetailsUpdateFailed);
    }
}
