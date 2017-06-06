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

package io.spine.examples.todolist.testdata;

import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsChange;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;

/**
 * A factory of the label events for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestLabelEventFactory {

    private static final String LABEL_TITLE = TestLabelCommandFactory.LABEL_TITLE;
    private static final LabelColor LABEL_COLOR = LabelColor.GRAY;
    private static final LabelId LABEL_ID = TestTaskEventFactory.LABEL_ID;

    private TestLabelEventFactory() {
    }

    /**
     * Provides a pre-configured {@link LabelDetailsUpdated} event instance.
     *
     * @return the {@code LabelDetailsUpdated} instance.
     */
    public static LabelDetailsUpdated labelDetailsUpdatedInstance() {
        return labelDetailsUpdatedInstance(LABEL_ID, LABEL_COLOR, LABEL_TITLE);
    }

    /**
     * Provides the {@link LabelDetailsUpdated} event by specified label color and title.
     *
     * @param color the color of the updated label details
     * @param title the title of the updated label details
     * @return the {@code LabelDetailsUpdated} instance
     */
    public static LabelDetailsUpdated labelDetailsUpdatedInstance(LabelId labelId, LabelColor color,
            String title) {
        final LabelDetails.Builder labelDetailsBuilder =
                LabelDetails.newBuilder()
                            .setColor(color)
                            .setTitle(title);
        final LabelDetailsChange labelDetailsChange =
                LabelDetailsChange.newBuilder()
                                  .setNewDetails(labelDetailsBuilder)
                                  .build();
        final LabelDetailsUpdated result =
                LabelDetailsUpdated.newBuilder()
                                   .setLabelId(labelId)
                                   .setLabelDetailsChange(labelDetailsChange)
                                   .build();
        return result;
    }
}
