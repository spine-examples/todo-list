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

package io.spine.examples.todolist.c.aggregate;

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.context.TodoListBoundedContext;
import io.spine.server.BoundedContext;
import io.spine.server.Environment;
import io.spine.server.aggregate.AggregateRoot;

/**
 * Aggregate root for the tasks.
 *
 * @author Illia Shepilov
 * @see AggregateRoot
 */
public class TaskAggregateRoot extends AggregateRoot<TaskId> {

    /**
     * It must be initialized through {@code #injectBoundedContext(BoundedContext)} method
     * during the tests, in other cases it will be initialized by default.
     */
    @SuppressWarnings("StaticVariableMayNotBeInitialized")
    private static BoundedContext boundedContext;

    static {
        initBoundedContextIfNeeded();
    }

    /**
     * Creates a new instance.
     *
     * @param boundedContext the bounded context to which the aggregate belongs
     * @param id             the ID of the aggregate
     */
    protected TaskAggregateRoot(BoundedContext boundedContext, TaskId id) {
        super(boundedContext, id);
    }

    /**
     * Obtains and sets the {@link BoundedContext} singleton instance,
     * if the {@link Environment} is non-test environment.
     *
     * @see Environment#isTests()
     * @see TodoListBoundedContext#getInstance()
     */
    private static void initBoundedContextIfNeeded() {
        final boolean testEnv = Environment.getInstance()
                                           .isTests();
        if (!testEnv) {
            boundedContext = TodoListBoundedContext.getInstance();
        }
    }

    /**
     * Creates a new {@link TaskAggregateRoot} instance.
     *
     * @param id the ID of the aggregate
     */
    protected TaskAggregateRoot(TaskId id) {
        super(boundedContext, id);
    }

    /**
     * Obtains the {@link TaskAggregateRoot} instance with the given {@code TaskId}.
     *
     * @param id a task identifier
     * @return the aggregate root for the task
     */
    public static TaskAggregateRoot get(TaskId id) {
        return new TaskAggregateRoot(id);
    }

    /**
     * Injects the {@link BoundedContext} instance.
     *
     * <p>Method uses only for test needs. As the {@code BoundedContext} must be `clear`
     * for each test, it injects through that method. Singleton instance is uses for non-test needs.
     *
     * @param boundedContext instance to inject
     */
    @VisibleForTesting
    public static void injectBoundedContext(BoundedContext boundedContext) {
        TaskAggregateRoot.boundedContext = boundedContext;
    }
}
