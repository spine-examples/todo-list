/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.server.tasks;

import io.spine.examples.todolist.server.tasks.label.LabelPackage;
import io.spine.examples.todolist.server.tasks.task.TaskPackage;
import io.spine.examples.todolist.tasks.TasksContext;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;

/**
 * Utilities for creation the {@link BoundedContext} instances.
 */
public final class TasksContextFactory {

    /** Prevents instantiation of this utility class. */
    private TasksContextFactory() {
    }

    /**
     * Creates the {@link BoundedContext} instance
     * using {@code InMemoryStorageFactory} for a single tenant.
     *
     * @return the {@link BoundedContext} instance
     */
    public static BoundedContext create() {
        BoundedContextBuilder builder = builder();
        return builder.build();
    }

    /**
     * Creates and configures the builder for the Tasks context.
     *
     * <p>The returned builder has all the repositories of the context.
     */
    public static BoundedContextBuilder builder() {
        BoundedContextBuilder builder = BoundedContext.singleTenant(TasksContext.NAME);
        LabelPackage.configure(builder);
        TaskPackage.configure(builder);
        return builder;
    }
}
