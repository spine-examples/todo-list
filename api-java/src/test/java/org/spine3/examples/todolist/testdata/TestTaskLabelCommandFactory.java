//
// Copyright 2016, TeamDev Ltd. All rights reserved.
//
// Redistribution and use in source and/or binary forms, with or without
// modification, must retain the above copyright notice and the following
// disclaimer.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
package org.spine3.examples.todolist.testdata;

import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CreateBasicLabel;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.UpdateLabelDetails;

/**
 * Provides methods for instantiation task label commands for test needs.
 *
 * @author Illia Shepilov
 */
public class TestTaskLabelCommandFactory {

    private static final String TITLE = "label title";

    private TestTaskLabelCommandFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Provides {@link CreateBasicLabel} instance.
     *
     * @return {@link CreateBasicLabel} instance.
     */
    public static CreateBasicLabel createLabelInstance() {
        return CreateBasicLabel.getDefaultInstance();
    }

    /**
     * Provides {@link UpdateLabelDetails} instance.
     *
     * @return {@link UpdateLabelDetails} instance.
     */
    public static UpdateLabelDetails updateLabelDetailsInstance() {
        return updateLabelDetailsInstance(LabelColor.GRAY, TITLE);
    }

    /**
     * Provides {@link UpdateLabelDetails} instance.
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
     * Provides {@link AssignLabelToTask} instance.
     *
     * @return {@link AssignLabelToTask} instance.
     */
    public static AssignLabelToTask assignLabelToTaskInstance() {
        return AssignLabelToTask.getDefaultInstance();
    }

    /**
     * Provides {@link RemoveLabelFromTask} instance.
     *
     * @return {@link RemoveLabelFromTask} instance.
     */
    public static RemoveLabelFromTask removeLabelFromTaskInstance() {
        return RemoveLabelFromTask.getDefaultInstance();
    }

}
