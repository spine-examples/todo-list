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

import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;

/**
 * A factory of the task label commands for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestTaskLabelsCommandFactory {

    private static final TaskId TASK_ID = TestTaskCommandFactory.TASK_ID;
    private static final LabelId LABEL_ID = TestTaskCommandFactory.LABEL_ID;

    private TestTaskLabelsCommandFactory() {
    }

    /**
     * Provides a pre-configured {@link AssignLabelToTask} command instance.
     *
     * @return the {@code AssignLabelToTask} instance
     */
    public static AssignLabelToTask assignLabelToTaskInstance(TaskId taskId, LabelId labelId) {
        final AssignLabelToTask result = AssignLabelToTask.newBuilder()
                                                          .setId(taskId)
                                                          .setLabelId(labelId)
                                                          .build();
        return result;
    }

    /**
     * Provides a pre-configured {@link AssignLabelToTask} command instance.
     *
     * @return the {@code AssignLabelToTask} instance
     */
    public static AssignLabelToTask assignLabelToTaskInstance() {
        final AssignLabelToTask result = assignLabelToTaskInstance(TASK_ID, LABEL_ID);
        return result;
    }

    /**
     * Provides a pre-configured {@link RemoveLabelFromTask} command instance.
     *
     * @return the {@code RemoveLabelFromTask} instance
     */
    public static RemoveLabelFromTask removeLabelFromTaskInstance() {
        final RemoveLabelFromTask result = removeLabelFromTaskInstance(TASK_ID, LABEL_ID);
        return result;
    }

    /**
     * Provides a pre-configured {@link RemoveLabelFromTask} command instance.
     *
     * @return the {@code RemoveLabelFromTask} instance
     */
    public static RemoveLabelFromTask removeLabelFromTaskInstance(TaskId taskId, LabelId labelId) {
        final RemoveLabelFromTask result = RemoveLabelFromTask.newBuilder()
                                                              .setId(taskId)
                                                              .setLabelId(labelId)
                                                              .build();
        return result;
    }
}
