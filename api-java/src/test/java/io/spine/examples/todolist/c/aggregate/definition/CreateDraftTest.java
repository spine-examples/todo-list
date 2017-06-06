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

package io.spine.examples.todolist.c.aggregate.definition;

import com.google.common.base.Throwables;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.spine.examples.todolist.TaskDefinition;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.failures.CannotCreateDraft;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static io.spine.examples.todolist.TaskStatus.DRAFT;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("CreateDraft command should be interpreted by TaskDefinitionPart and")
public class CreateDraftTest extends TaskDefinitionCommandTest<CreateDraft> {

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("produce TaskDraftCreated event")
    void produceEvent() {
        final CreateDraft createDraftCmd = createDraftInstance(taskId);

        final List<? extends Message> messageList =
                aggregate.dispatchForTest(envelopeOf(createDraftCmd));
        assertEquals(1, messageList.size());
        assertEquals(TaskDraftCreated.class, messageList.get(0)
                                                        .getClass());

        final TaskDraftCreated taskDraftCreated = (TaskDraftCreated) messageList.get(0);
        assertEquals(taskId, taskDraftCreated.getId());
    }

    @Test
    @DisplayName("create the draft")
    void createDraft() {
        final CreateDraft createDraftCmd = createDraftInstance(taskId);
        aggregate.dispatchForTest(envelopeOf(createDraftCmd));
        final TaskDefinition state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(DRAFT, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotCreateDraft failure upon " +
            "an attempt to create draft with deleted task ID")
    void notCreateDraft() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(envelopeOf(createTaskCmd));

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(envelopeOf(deleteTaskCmd));

        final CreateDraft createDraftCmd = createDraftInstance(taskId);
        final Throwable t = assertThrows(Throwable.class,
                                         () -> aggregate.dispatchForTest(
                                                 envelopeOf(createDraftCmd)));
        final Throwable cause = Throwables.getRootCause(t);
        final CannotCreateDraft failure = (CannotCreateDraft) cause;
        final TaskId actualId = failure.getFailureMessage()
                                       .getCreateDraftFailed()
                                       .getFailureDetails()
                                       .getTaskId();
        assertEquals(taskId, actualId);
    }
}
