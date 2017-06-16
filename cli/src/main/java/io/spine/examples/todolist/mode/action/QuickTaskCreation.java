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

package io.spine.examples.todolist.mode.action;

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;
import io.spine.examples.todolist.mode.Mode;
import io.spine.validate.ValidationException;

import java.util.Optional;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.UserIO.askUser;
import static io.spine.examples.todolist.mode.action.ValidationExceptionFormatter.format;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * @author Dmytro Grankin
 */
public class QuickTaskCreation extends Mode {

    private static final String SET_DESCRIPTION_MESSAGE = "Please enter the task description";

    private final CreateBasicTaskVBuilder builder = CreateBasicTaskVBuilder.newBuilder();

    @Override
    public void start() {
        final TaskId taskId = newTaskId();
        builder.setId(taskId);

        inputDescription(SET_DESCRIPTION_MESSAGE);

        buildAndPost();
    }

    private void inputDescription(String message) {
        final String description = askUser(message);
        final Optional<String> errMsg = trySet(() -> builder.setDescription(description));
        errMsg.ifPresent(this::inputDescription);
    }

    private void buildAndPost() {
        try {
            final CreateBasicTask createTask = builder.build();
            getClient().create(createTask);
        } catch (ValidationException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static Optional<String> trySet(Runnable r) {
        try {
            r.run();
            return Optional.empty();
        } catch (ValidationException e) {
            final String errMsg = format(e);
            return Optional.of(errMsg);
        }
    }

    private static TaskId newTaskId() {
        final TaskId result = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        return result;
    }
}
