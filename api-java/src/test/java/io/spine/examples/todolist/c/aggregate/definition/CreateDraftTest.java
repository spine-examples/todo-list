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

package io.spine.examples.todolist.c.aggregate.definition;

import com.google.common.base.Throwables;
import com.google.protobuf.Message;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.DeleteTask;
import io.spine.examples.todolist.c.events.TaskDraftCreated;
import io.spine.examples.todolist.c.rejection.CannotCreateDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.TaskStatus.DRAFT;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createDraftInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CreateDraft command should be interpreted by TaskPart and")
class CreateDraftTest extends TaskCommandTest<CreateDraft> {

    CreateDraftTest() {
        super(createDraftInstance());
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("produce TaskDraftCreated event")
    void produceEvent() {
        CreateDraft createDraftCmd = createDraftInstance(entityId());
        List<? extends Message> messageList = dispatchCommand(aggregate,
                                                              envelopeOf(createDraftCmd));
        assertEquals(1, messageList.size());
        assertEquals(TaskDraftCreated.class, messageList.get(0)
                                                        .getClass());
        TaskDraftCreated taskDraftCreated = (TaskDraftCreated) messageList.get(0);
        assertEquals(entityId(), taskDraftCreated.getId());
    }

    @Test
    @DisplayName("create the draft")
    void createDraft() {
        CreateDraft createDraftCmd = createDraftInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(createDraftCmd));
        Task state = aggregate.state();

        assertEquals(entityId(), state.getId());
        assertEquals(DRAFT, state.getTaskStatus());
    }

    @Test
    @DisplayName("throw CannotCreateDraft rejection upon " +
            "an attempt to create draft with deleted task ID")
    void notCreateDraft() {
        CreateBasicTask createTaskCmd = createTaskInstance(entityId(), DESCRIPTION);
        dispatchCommand(aggregate, envelopeOf(createTaskCmd));

        DeleteTask deleteTaskCmd = deleteTaskInstance(entityId());
        dispatchCommand(aggregate, envelopeOf(deleteTaskCmd));

        CreateDraft createDraftCmd = createDraftInstance(entityId());
        Throwable t = assertThrows(Throwable.class,
                                   () -> dispatchCommand(aggregate,
                                                         envelopeOf(createDraftCmd)));
        Throwable cause = Throwables.getRootCause(t);
        CannotCreateDraft rejection = (CannotCreateDraft) cause;
        TaskId actualId = rejection.messageThrown()
                                   .getRejectionDetails()
                                   .getCommandDetails()
                                   .getTaskId();
        assertEquals(entityId(), actualId);
    }
}
