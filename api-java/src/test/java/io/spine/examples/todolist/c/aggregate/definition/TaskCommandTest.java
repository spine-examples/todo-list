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

import com.google.protobuf.Message;
import io.spine.envelope.CommandEnvelope;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.aggregate.TaskAggregateRoot;
import io.spine.examples.todolist.c.aggregate.TaskPart;
import io.spine.examples.todolist.context.TodoListBoundedContext;
import io.spine.test.AggregatePartCommandTest;
import io.spine.test.TestActorRequestFactory;

import static io.spine.base.Identifier.newUuid;

/**
 * The parent class for the {@link TaskPart} test classes.
 * Provides the common methods for testing.
 *
 * @author Illia Shepilov
 */
abstract class TaskCommandTest<C extends Message> extends AggregatePartCommandTest<C, TaskPart> {

    private final TestActorRequestFactory requestFactory =
            TestActorRequestFactory.newInstance(getClass());
    TaskPart aggregate;
    TaskId taskId;

    @Override
    protected void setUp() {
        super.setUp();
        aggregate = aggregatePart().get();
    }

    @Override
    protected TaskPart createAggregatePart() {
        TaskAggregateRoot.injectBoundedContext(TodoListBoundedContext.createTestInstance());

        taskId = createTaskId();
        final TaskAggregateRoot root = TaskAggregateRoot.get(taskId);
        return new TaskPart(root);
    }

    CommandEnvelope envelopeOf(Message commandMessage) {
        return CommandEnvelope.of(requestFactory.command()
                                                .create(commandMessage));
    }

    private static TaskId createTaskId() {
        return TaskId.newBuilder()
                     .setValue(newUuid())
                     .build();
    }
}
