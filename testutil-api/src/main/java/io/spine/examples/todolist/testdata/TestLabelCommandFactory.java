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

package io.spine.examples.todolist.testdata;

import io.spine.examples.todolist.tasks.LabelColor;
import io.spine.examples.todolist.tasks.LabelDetails;
import io.spine.examples.todolist.tasks.LabelDetailsChange;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.command.CreateBasicLabel;
import io.spine.examples.todolist.tasks.command.UpdateLabelDetails;

/**
 * A factory of the label commands for the test needs.
 */
public final class TestLabelCommandFactory {

    public static final String LABEL_TITLE = "label title";
    private static final String UPDATED_LABEL_TITLE = "updated label title";
    private static final LabelId LABEL_ID = LabelId.generate();

    private TestLabelCommandFactory() {
    }

    /**
     * Provides the {@link CreateBasicLabel} event instance by specified label ID.
     *
     * @return the {@code CreateBasicLabel} instance
     */
    public static CreateBasicLabel createLabelInstance() {
        CreateBasicLabel result = createLabelInstance(LABEL_ID);
        return result;
    }

    /**
     * Provides a pre-configured {@link CreateBasicLabel} event instance.
     *
     * @return the {@code CreateBasicLabel} instance
     */
    public static CreateBasicLabel createLabelInstance(LabelId labelId) {
        CreateBasicLabel result = CreateBasicLabel
                .newBuilder()
                .setLabelId(labelId)
                .setLabelTitle(LABEL_TITLE)
                .vBuild();
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateLabelDetails} command instance.
     *
     * @return the {@code UpdateLabelDetails} instance.
     */
    public static UpdateLabelDetails updateLabelDetailsInstance(LabelId labelId) {
        LabelDetails previousLabelDetails = LabelDetails
                .newBuilder()
                .setTitle(LABEL_TITLE)
                .setColor(LabelColor.GRAY)
                .vBuild();
        LabelDetails newLabelDetails = LabelDetails
                .newBuilder()
                .setTitle(UPDATED_LABEL_TITLE)
                .setColor(LabelColor.GREEN)
                .vBuild();
        return updateLabelDetailsInstance(labelId, previousLabelDetails, newLabelDetails);
    }

    /**
     * Provides the {@link UpdateLabelDetails} event by specified label color and title.
     *
     * @param previousLabelDetails
     *         the previous label details
     * @param newLabelDetails
     *         the new label details
     * @return the {@code UpdateLabelDetails} instance.
     */
    public static UpdateLabelDetails updateLabelDetailsInstance(LabelId id,
                                                                LabelDetails previousLabelDetails,
                                                                LabelDetails newLabelDetails) {
        LabelDetailsChange labelDetailsChange = LabelDetailsChange
                .newBuilder()
                .setPreviousDetails(previousLabelDetails)
                .setNewDetails(newLabelDetails)
                .buildPartial();
        UpdateLabelDetails result = UpdateLabelDetails
                .newBuilder()
                .setId(id)
                .setLabelDetailsChange(labelDetailsChange)
                .vBuild();
        return result;
    }
}
