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

package io.spine.examples.todolist.server;

import io.spine.examples.todolist.server.label.LabelAggregateRepository;
import io.spine.examples.todolist.server.label.LabelViewRepository;
import io.spine.examples.todolist.server.task.TaskCreationWizardRepository;
import io.spine.examples.todolist.server.task.TaskLabelsRepository;
import io.spine.examples.todolist.server.task.TaskRepository;
import io.spine.examples.todolist.server.task.TaskViewRepository;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;

/**
 * Utilities for creation the {@link BoundedContext} instances.
 */
public final class TodoListContext {

    /**
     * The name of the context.
     *
     * <p>See {@code package-info.java} for using the value in the annotation for the package.
     */
    static final String NAME = "TodoList";

    /** Prevents instantiation of this utility class. */
    private TodoListContext() {
    }

    /**
     * Creates the {@link BoundedContext} instance
     * using {@code InMemoryStorageFactory} for a single tenant.
     *
     * @return the {@link BoundedContext} instance
     */
    public static BoundedContext create() {
        BoundedContextBuilder builder = BoundedContext
                .singleTenant(NAME)
                .add(new TaskRepository())
                .add(new TaskLabelsRepository())
                .add(new LabelAggregateRepository())
                .add(new TaskViewRepository())
                .add(new LabelViewRepository())
                .add(new TaskCreationWizardRepository());
        return builder.build();
    }
}
