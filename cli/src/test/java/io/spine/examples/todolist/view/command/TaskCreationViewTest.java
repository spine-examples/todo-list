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

package io.spine.examples.todolist.view.command;

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.UserIoTest;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;
import io.spine.examples.todolist.view.command.TaskCreationView.EnterDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.validate.Validate.checkNotDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Dmytro Grankin
 */
@DisplayName("TaskCreationView should")
class TaskCreationViewTest extends UserIoTest {

    private static final String VALID_DESCRIPTION = "to the task";

    private final CommandView<CreateBasicTask, CreateBasicTaskVBuilder> view = new TaskCreationView();

    @Test
    @DisplayName("not be root view")
    void notBeRootView() {
        assertFalse(view.isRootView());
    }

    @Test
    @DisplayName("generate the ID after creation")
    void generateId() {
        final TaskId taskId = view.getState()
                                  .getId();
        checkNotDefault(taskId);
    }

    @Test
    @DisplayName("build state if valid description was set")
    void buildStateIfDescriptionWasSet() {
        final CreateBasicTaskVBuilder state = view.getState();
        state.setDescription(VALID_DESCRIPTION);
        state.build();
    }

    @Nested
    @DisplayName("EnterDescription should")
    class EnterDescriptionTest {

        private final EnterDescription enterDescription =
                new EnterDescription("d", new Shortcut("d"), view);

        @BeforeEach
        void setUp() {
            enterDescription.setUserCommunicator(getCommunicator());
        }

        @Test
        @DisplayName("modify description")
        void modifyDescription() {
            addAnswer(VALID_DESCRIPTION);

            final CreateBasicTaskVBuilder state = view.getState();
            enterDescription.updateState(state);

            assertEquals(VALID_DESCRIPTION, state.getDescription());
        }
    }
}
