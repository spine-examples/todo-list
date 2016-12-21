/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.client.builder;

import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.TaskId;

import static org.spine3.base.Identifiers.newUuid;

/**
 * @author Illia Shepilov
 */
public final class TaskBuilder {

    private TaskBuilder() {
    }

    /* package */
    static TaskBuilder getInstance() {
        return new TaskBuilder();
    }

    public CreateBasicTaskBuilder createTask() {
        return new CreateBasicTaskBuilder();
    }

    public CreateTaskDraftBuilder createDraft() {
        return new CreateTaskDraftBuilder();
    }

    public static final class CreateBasicTaskBuilder {
        private final CreateBasicTask.Builder builder = CreateBasicTask.newBuilder();

        public CreateBasicTaskBuilder setDescription(String description) {
            builder.setDescription(description);
            return this;
        }

        public CreateBasicTask build() {
            final TaskId id = generateId();
            builder.setId(id);
            return builder.build();
        }
    }

    public static final class CreateTaskDraftBuilder {
        private final CreateDraft.Builder builder = CreateDraft.newBuilder();

        public CreateDraft build() {
            final TaskId id = generateId();
            builder.setId(id);
            return builder.build();
        }
    }

    private static TaskId generateId() {
        final TaskId id = TaskId.newBuilder()
                                .setValue(newUuid())
                                .build();
        return id;
    }
}
