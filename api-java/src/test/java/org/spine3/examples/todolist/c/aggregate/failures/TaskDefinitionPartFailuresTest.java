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

package org.spine3.examples.todolist.c.aggregate.failures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.FailedTaskCommandDetails;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.failures.CannotAssignLabelToTask;
import org.spine3.examples.todolist.c.failures.CannotCreateDraft;
import org.spine3.examples.todolist.c.failures.CannotCreateTaskWithInappropriateDescription;
import org.spine3.examples.todolist.c.failures.CannotRemoveLabelFromTask;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDescription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.c.aggregate.failures.TaskDefinitionPartFailures.TaskCreationFailures.throwCannotCreateDraftFailure;
import static org.spine3.examples.todolist.c.aggregate.failures.TaskDefinitionPartFailures.TaskCreationFailures.throwCannotCreateTaskWithInappropriateDescriptionFailure;
import static org.spine3.examples.todolist.c.aggregate.failures.TaskDefinitionPartFailures.UpdateFailures.throwCannotUpdateTaskDescriptionFailure;
import static org.spine3.examples.todolist.c.aggregate.failures.TaskLabelsPartFailures.throwCannotAssignLabelToTaskFailure;
import static org.spine3.examples.todolist.c.aggregate.failures.TaskLabelsPartFailures.throwCannotRemoveLabelFromTaskFailure;
import static org.spine3.test.Tests.hasPrivateParameterlessCtor;

/**
 * @author Illia Shepilov
 */
@DisplayName("TaskDefinitionPartFailures should")
class TaskDefinitionPartFailuresTest {

    private final TaskId taskId = TaskId.getDefaultInstance();
    private final LabelId labelId = LabelId.getDefaultInstance();

    @Test
    @DisplayName("have the private constructor")
    public void havePrivateConstructor() {
        assertTrue(hasPrivateParameterlessCtor(TaskDefinitionPartFailures.class));
    }

    @Test
    @DisplayName("throw CannotCreateDraft failure")
    public void throwCannotCreateDraft() {
        try {
            throwCannotCreateDraftFailure(taskId);
        } catch (CannotCreateDraft ex) {
            final TaskId actual = ex.getFailure()
                                    .getCreateDraftFailed()
                                    .getFailureDetails()
                                    .getTaskId();
            assertEquals(taskId, actual);
        }
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription failure")
    public void throwCannotUpdateTaskDescription() {
        try {
            throwCannotUpdateTaskDescriptionFailure(taskId);
        } catch (CannotUpdateTaskDescription ex) {
            final FailedTaskCommandDetails failedCommand = ex.getFailure()
                                                             .getUpdateFailed()
                                                             .getFailureDetails();
            final TaskId actualId = failedCommand.getTaskId();
            assertEquals(taskId, actualId);
        }
    }

    @Test
    @DisplayName("throw CannotCreateTaskWithInappropriateDescription failure")
    public void throwCannotCreateTaskWithInappropriateDescription() {
        try {
            throwCannotCreateTaskWithInappropriateDescriptionFailure(taskId);
        } catch (CannotCreateTaskWithInappropriateDescription ex) {
            final TaskId actual = ex.getFailure()
                                    .getCreateTaskFailed()
                                    .getFailureDetails()
                                    .getTaskId();
            assertEquals(taskId, actual);
        }
    }
}
