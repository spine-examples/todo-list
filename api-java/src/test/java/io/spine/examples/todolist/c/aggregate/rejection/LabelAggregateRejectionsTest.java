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
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.rejection.CannotUpdateLabelDetails;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.c.aggregate.rejection.LabelAggregateRejections.throwCannotUpdateLabelDetails;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Illia Shepilov
 */
@DisplayName("LabelAggregateRejections should")
class LabelAggregateRejectionsTest extends UtilityClassTest<LabelAggregateRejections> {

    LabelAggregateRejectionsTest() {
        super(LabelAggregateRejections.class);
    }

    @Test
    @DisplayName("have the private constructor")
    void havePrivateConstructor() {
        assertHasPrivateParameterlessCtor(LabelAggregateRejections.class);
    }

    @Test
    @DisplayName("throw CannotUpdateLabelDetails rejection")
    void throwCannotUpdateLabelDetailsRejection() {
        final UpdateLabelDetails cmd = UpdateLabelDetails.getDefaultInstance();
        final ValueMismatch valueMismatch = ValueMismatch.getDefaultInstance();
        assertThrows(CannotUpdateLabelDetails.class,
                     () -> throwCannotUpdateLabelDetails(cmd, valueMismatch));
    }
}
