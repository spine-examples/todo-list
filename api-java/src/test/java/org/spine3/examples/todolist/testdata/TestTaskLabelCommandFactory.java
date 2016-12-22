/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.testdata;

import org.spine3.examples.todolist.CreateBasicLabel;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelCreated;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.UpdateLabelDetails;

import static org.spine3.base.Identifiers.newUuid;

/**
 * A factory of the task label commands for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestTaskLabelCommandFactory {

    public static final String LABEL_TITLE = "label title";
    public static final String UPDATED_LABEL_TITLE = "updated label title";
    public static final TaskLabelId LABEL_ID = TaskLabelId.newBuilder()
                                                          .setValue(newUuid())
                                                          .build();

    /**
     * Prevent instantiation.
     */
    private TestTaskLabelCommandFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Provides default {@link CreateBasicLabel} event instance.
     *
     * @return {@link CreateBasicLabel} instance
     */
    public static CreateBasicLabel createLabelInstance() {
        final CreateBasicLabel result = CreateBasicLabel.newBuilder()
                                                        .setLabelId(LABEL_ID)
                                                        .setLabelTitle(LABEL_TITLE)
                                                        .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link UpdateLabelDetails} command instance.
     *
     * @return {@link UpdateLabelDetails} instance.
     */
    public static UpdateLabelDetails updateLabelDetailsInstance() {
        return updateLabelDetailsInstance(LabelColor.GREEN, UPDATED_LABEL_TITLE);
    }

    /**
     * Provides {@link UpdateLabelDetails} event by specified label's color and title.
     *
     * @param color {@link LabelColor} enum value.
     * @param title String value
     * @return {@link UpdateLabelDetails} instance.
     */
    public static UpdateLabelDetails updateLabelDetailsInstance(LabelColor color, String title) {
        final UpdateLabelDetails result = UpdateLabelDetails.newBuilder()
                                                            .setId(LABEL_ID)
                                                            .setColor(color)
                                                            .setNewTitle(title)
                                                            .build();
        return result;
    }

    /**
     * Provides default {@link LabelRemovedFromTask} event instance.
     *
     * @return {@link LabelRemovedFromTask} instance
     */
    public static LabelRemovedFromTask labelRemovedFromTaskInstance() {
        return LabelRemovedFromTask.getDefaultInstance();
    }
}
