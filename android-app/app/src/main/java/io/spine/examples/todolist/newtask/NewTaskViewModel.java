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

package io.spine.examples.todolist.newtask;

import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.lifecycle.BaseViewModel;

import static io.spine.Identifier.newUuid;

/**
 * The {@link android.arch.lifecycle.ViewModel ViewModel} of the {@link NewTaskActivity}.
 */
final class NewTaskViewModel extends BaseViewModel {

    // Required by the `ViewModelProviders` utility.
    public NewTaskViewModel() {
    }

    /**
     * Creates a new task with the given {@linkplain TaskDescription description}.
     *
     * <p>Sends the {@link CreateBasicTask} command through the {@linkplain #client() gRPC client}.
     *
     * @param description the new task description
     */
    void createTask(TaskDescription description) {
        final CreateBasicTask command = CreateBasicTask.newBuilder()
                                                       .setId(newId())
                                                       .setDescription(description)
                                                       .build();
        execute(() -> client().create(command));
    }

    /**
     * Generates a new random {@link TaskId}.
     *
     * @return new instance of {@link TaskId} with
     * a {@linkplain io.spine.Identifier#newUuid() UUID} value.
     */
    private static TaskId newId() {
        return TaskId.newBuilder()
                     .setValue(newUuid())
                     .build();
    }
}
