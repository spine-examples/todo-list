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

package org.spine3.examples.todolist.c.aggregates;

import com.google.common.annotations.VisibleForTesting;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelIds;
import org.spine3.examples.todolist.context.TodoListBoundedContext;
import org.spine3.server.BoundedContext;
import org.spine3.server.aggregate.AggregateRoot;

/**
 * @author Illia Shepilov
 */
public class TaskAggregateRoot extends AggregateRoot<TaskId> {

    private static BoundedContext boundedContext = TodoListBoundedContext.getInstance();

    /**
     * Creates a new instance.
     *
     * @param id the ID of the aggregate
     */
    protected TaskAggregateRoot(TaskId id) {
        super(boundedContext, id);
    }

    public static TaskAggregateRoot get(TaskId id) {
        return new TaskAggregateRoot(id);
    }

    public TaskDefinition getTaskDefinitionState() {
        final TaskDefinition result = getPartState(TaskDefinition.class);
        return result;
    }

    public TaskLabelIds getTaskLabelIdsState() {
        final TaskLabelIds result = getPartState(TaskLabelIds.class);
        return result;
    }

    @VisibleForTesting
    public static void injectBoundedContext(BoundedContext boundedContext) {
        TaskAggregateRoot.boundedContext = boundedContext;
    }
}
