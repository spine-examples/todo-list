/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import io.spine.base.CommandMessage;
import io.spine.core.Command;
import io.spine.core.CommandEnvelope;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.aggregate.TaskAggregateRoot;
import io.spine.examples.todolist.c.aggregate.TaskPart;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.server.BoundedContext;
import io.spine.server.entity.Repository;
import io.spine.testing.client.TestActorRequestFactory;
import io.spine.testing.server.aggregate.AggregateCommandTest;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.TASK_ID;

/**
 * The parent class for the {@link TaskPart} test classes.
 * Provides the common methods for testing.
 */
@SuppressWarnings("PackageVisibleField") // for brevity of descendants.
abstract class TaskCommandTest<C extends CommandMessage>
        extends AggregateCommandTest<TaskId, C, Task, TaskPart> {

    private final TestActorRequestFactory requestFactory =
            TestActorRequestFactory.newInstance(getClass());

    TaskPart aggregate;

    protected TaskCommandTest(C commandMessage) {
        super(TASK_ID, commandMessage);
    }

    @Override
    public void setUp() {
        super.setUp();
        aggregate = newPart(entityId());
    }

    @Override
    protected Repository<TaskId, TaskPart> createEntityRepository() {
        return new TaskRepository();
    }

    CommandEnvelope envelopeOf(CommandMessage commandMessage) {
        Command command = createNewCommand(commandMessage);
        CommandEnvelope envelope = CommandEnvelope.of(command);
        return envelope;
    }

    Command createNewCommand(CommandMessage commandMessage) {
        Command command = requestFactory.command()
                                        .create(commandMessage);
        return command;
    }

    private static TaskAggregateRoot newRoot(TaskId id) {
        final BoundedContext boundedContext = BoundedContexts.create();
        final TaskAggregateRoot root = new TaskAggregateRoot(boundedContext, id);
        return root;
    }

    private static TaskPart newPart(TaskAggregateRoot root) {
        TaskPart taskPart = new TaskPart(root);
        return taskPart;
    }

    private static TaskPart newPart(TaskId id) {
        TaskAggregateRoot root = newRoot(id);
        TaskPart taskPart = newPart(root);
        return taskPart;
    }
}
