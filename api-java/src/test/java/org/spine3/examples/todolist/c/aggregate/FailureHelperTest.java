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

package org.spine3.examples.todolist.c.aggregate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.UnsuccessfulTaskCommand;
import org.spine3.examples.todolist.c.failures.CannotAssignLabelToTask;
import org.spine3.examples.todolist.c.failures.CannotCreateDraft;
import org.spine3.examples.todolist.c.failures.CannotCreateTaskWithInappropriateDescription;
import org.spine3.examples.todolist.c.failures.CannotRemoveLabelFromTask;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDescription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.test.Tests.hasPrivateParameterlessCtor;

/**
 * @author Illia Shepilov
 */
@DisplayName("FailureHelper should")
class FailureHelperTest {

    private TaskId taskId;
    private LabelId labelId;

    @BeforeEach
    public void setUp() {
        taskId = createTaskId();
        labelId = createLabelId();
    }

    private static LabelId createLabelId() {
        final LabelId result = LabelId.newBuilder()
                                      .setValue(newUuid())
                                      .build();
        return result;
    }

    private static TaskId createTaskId() {
        final TaskId result = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        return result;
    }

    @Test
    @DisplayName("have the private constructor")
    public void havePrivateConstructor() {
        assertTrue(hasPrivateParameterlessCtor(FailureHelper.class));
    }

    @Test
    @DisplayName("throw CannotCreateDraft failure")
    public void throwCannotCreateDraftFailure() {
        try {
            FailureHelper.throwCannotCreateDraftFailure(taskId);
        } catch (CannotCreateDraft ex) {
            final TaskId actual = ex.getFailure()
                                    .getCreateDraftFailed()
                                    .getFailedCommand()
                                    .getTaskId();
            assertEquals(taskId, actual);
        }
    }

    @Test
    @DisplayName("throw CannotRemoveLabeFromTask failure")
    public void throwCannotRemoveLabelFromTask() {
        try {
            FailureHelper.throwCannotRemoveLabelFromTaskFailure(labelId, taskId);
        } catch (CannotRemoveLabelFromTask ex) {
            final TaskId actual = ex.getFailure()
                                    .getRemoveLabelFailed()
                                    .getFailedCommand()
                                    .getTaskId();
            assertEquals(taskId, actual);
        }
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDescription failure")
    public void throwCannotUpdateTaskDescription() {
        final String message = "Description is wrong";
        try {
            FailureHelper.throwCannotUpdateTaskDescriptionFailure(taskId, message);
        } catch (CannotUpdateTaskDescription ex) {
            final UnsuccessfulTaskCommand failedCommand = ex.getFailure()
                                                            .getUpdateFailed()
                                                            .getFailedCommand();
            final TaskId actualId = failedCommand.getTaskId();
            assertEquals(taskId, actualId);
            assertEquals(message, failedCommand.getMessage());
        }
    }

    @Test
    @DisplayName("throw CannotAssignLabelToTask failure")
    public void throwCannotAssignLabelToTask() {
        try {
            FailureHelper.throwCannotAssignLabelToTaskFailure(taskId, labelId);
        } catch (CannotAssignLabelToTask ex) {
            final UnsuccessfulTaskCommand failedCommand = ex.getFailure()
                                                            .getAssignLabelFailed()
                                                            .getFailedCommand();
            final TaskId actualId = failedCommand.getTaskId();
            assertEquals(taskId, actualId);
        }
    }

    @Test
    @DisplayName("throw CannotCreateTaskWithInappropriateDescription failure")
    public void throwCannotCreateTaskWithInappropriateDescription(){
        try {
            FailureHelper.throwCannotCreateTaskWithInappropriateDescription(taskId);
        } catch (CannotCreateTaskWithInappropriateDescription ex) {
            final TaskId actual = ex.getFailure()
                                    .getCreateTaskFailed()
                                    .getFailedCommand()
                                    .getTaskId();
            assertEquals(taskId, actual);
        }
    }
}
