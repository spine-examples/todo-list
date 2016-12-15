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
import org.spine3.examples.todolist.UpdateLabelDetails;

/**
 * Provides methods for instantiation task label commands for test needs.
 *
 * @author Illia Shepilov
 */
public class TestTaskLabelCommandFactory {

    private static final String TITLE = "label title";

    /**
     * Prevent instantiation.
     */
    private TestTaskLabelCommandFactory() {
        throw new UnsupportedOperationException("Cannot be instantiated");
    }

    /**
     * Provides default {@link CreateBasicLabel} event instance.
     *
     * @return {@link CreateBasicLabel} instance
     */
    public static CreateBasicLabel createLabelInstance() {
        return CreateBasicLabel.getDefaultInstance();
    }

    /**
     * Provides {@link UpdateLabelDetails} event by specified label's color {@code LabelColor.GRAY}
     * and title {@code TITLE}.
     *
     * @return {@link UpdateLabelDetails} instance.
     */
    public static UpdateLabelDetails updateLabelDetailsInstance() {
        return updateLabelDetailsInstance(LabelColor.GRAY, TITLE);
    }

    /**
     * Provides {@link UpdateLabelDetails} event by specified label's color and title.
     *
     * @param color {@link LabelColor} enum value.
     * @param title String value
     * @return {@link UpdateLabelDetails} instance.
     */
    public static UpdateLabelDetails updateLabelDetailsInstance(LabelColor color, String title) {
        return UpdateLabelDetails.newBuilder()
                                 .setColor(color)
                                 .setNewTitle(title)
                                 .build();
    }

    /**
     * Provides {@link LabelCreated} event by specified label's color {@code LabelColor.GRAY}
     * and label's title {@code TITLE}.
     *
     * @return {@link LabelCreated} instance
     */
    public static LabelCreated labelCreatedInstance() {
        return labelCreatedInstance(LabelColor.GRAY, TITLE);
    }

    /**
     * Provides {@link LabelCreated} event by specified label's color and title.
     *
     * @param color label's color
     * @param title label's title
     * @return {@link LabelCreated} instance
     */
    public static LabelCreated labelCreatedInstance(LabelColor color, String title) {
        return LabelCreated.newBuilder()
                           .setDetails(LabelDetails.newBuilder()
                                                   .setColor(color)
                                                   .setTitle(title))
                           .build();
    }

    /**
     * Provides {@link LabelDetailsUpdated} event by specified label's color {@code LabelColor.GRAY}
     * and label's title {@code TITLE}.
     *
     * @return {@link LabelDetailsUpdated} instance.
     */
    public static LabelDetailsUpdated labelDetailsUpdatedInstance() {
        return labelDetailsUpdatedInstance(LabelColor.GRAY, TITLE);
    }

    /**
     * Provides {@link LabelDetailsUpdated} event by specified label's color and title.
     *
     * @param color label's color
     * @param title label's title
     * @return {@link LabelDetailsUpdated} instance
     */
    public static LabelDetailsUpdated labelDetailsUpdatedInstance(LabelColor color, String title) {
        return LabelDetailsUpdated.newBuilder()
                                  .setNewDetails(LabelDetails.newBuilder()
                                                             .setColor(color)
                                                             .setTitle(title))
                                  .build();
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
