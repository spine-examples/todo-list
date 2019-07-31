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

import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.command.AssignLabelToTask;
import io.spine.examples.todolist.tasks.command.RemoveLabelFromTask;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.LABEL_ID;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.TASK_ID;

/**
 * A factory of the task label commands for the test needs.
 */
public final class TestTaskLabelsCommandFactory {

    private TestTaskLabelsCommandFactory() {
    }

    /**
     * Provides a pre-configured {@link AssignLabelToTask} command instance.
     *
     * @return the {@code AssignLabelToTask} instance
     */
    public static AssignLabelToTask assignLabelToTaskInstance(TaskId taskId, LabelId labelId) {
        AssignLabelToTask result = AssignLabelToTask
                .newBuilder()
                .setId(taskId)
                .setLabelId(labelId)
                .vBuild();
        return result;
    }

    /**
     * Provides a pre-configured {@link AssignLabelToTask} command instance.
     *
     * @return the {@code AssignLabelToTask} instance
     */
    public static AssignLabelToTask assignLabelToTaskInstance() {
        AssignLabelToTask result = assignLabelToTaskInstance(TASK_ID, LABEL_ID);
        return result;
    }

    /**
     * Provides a pre-configured {@link RemoveLabelFromTask} command instance.
     *
     * @return the {@code RemoveLabelFromTask} instance
     */
    public static RemoveLabelFromTask removeLabelFromTaskInstance() {
        RemoveLabelFromTask result = removeLabelFromTaskInstance(TASK_ID, LABEL_ID);
        return result;
    }

    /**
     * Provides a pre-configured {@link RemoveLabelFromTask} command instance.
     *
     * @return the {@code RemoveLabelFromTask} instance
     */
    public static RemoveLabelFromTask removeLabelFromTaskInstance(TaskId taskId, LabelId labelId) {
        RemoveLabelFromTask result = RemoveLabelFromTask
                .newBuilder()
                .setId(taskId)
                .setLabelId(labelId)
                .vBuild();
        return result;
    }
}
