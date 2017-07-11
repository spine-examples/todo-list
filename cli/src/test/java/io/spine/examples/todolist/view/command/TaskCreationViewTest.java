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
import io.spine.examples.todolist.action.TransitionAction.TransitionActionProducer;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;
import io.spine.examples.todolist.view.AbstractView;
import io.spine.examples.todolist.view.View;
import io.spine.examples.todolist.view.command.TaskCreationView.EnterDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.Given.newNoOpView;
import static io.spine.examples.todolist.action.TransitionAction.newProducer;
import static io.spine.examples.todolist.view.ActionListView.getBackShortcut;
import static io.spine.examples.todolist.view.command.TaskCreationView.DESCRIPTION_LABEL;
import static io.spine.examples.todolist.view.command.TaskCreationView.EMPTY_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Dmytro Grankin
 */
@DisplayName("TaskCreationView should")
class TaskCreationViewTest extends UserIoTest {

    private static final String VALID_DESCRIPTION = "to the task";

    private final TaskCreationView view = TaskCreationView.create();

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        view.setScreen(getScreen());
    }

    @Test
    @DisplayName("handle empty description")
    void handleEmptyDescription() {
        final CreateBasicTaskVBuilder cleanBuilder = CreateBasicTaskVBuilder.newBuilder();
        final String expectedRepresentation = DESCRIPTION_LABEL + ' ' + EMPTY_VALUE;
        assertEquals(expectedRepresentation, view.representationOf(cleanBuilder));
    }

    @Test
    @DisplayName("handle non-empty description")
    void handleNonEmptyDescription() {
        final String description = "task description";
        final CreateBasicTaskVBuilder state = CreateBasicTaskVBuilder.newBuilder()
                                                                     .setDescription(description);
        final String expectedRepresentation = DESCRIPTION_LABEL + ' ' + description;
        assertEquals(expectedRepresentation, view.representationOf(state));
    }

    @Test
    @DisplayName("update the ID during rendering")
    void updateId() {
        final TaskId initialId = view.getState()
                                     .getId();

        addAnswer(getBackShortcut().getValue());
        final TransitionActionProducer<View, View> producer = newProducer("a",
                                                                          new Shortcut("a"),
                                                                          view);
        final AbstractView source = newNoOpView();
        source.setScreen(getScreen());
        producer.create(source)
                .execute();

        final TaskId idAfterRender = view.getState()
                                         .getId();
        assertNotEquals(initialId.getValue(), idAfterRender.getValue());
    }

    @Nested
    @DisplayName("EnterDescription should")
    class EnterDescriptionTest {

        private final EnterDescription enterDescription =
                new EnterDescription("d", new Shortcut("d"), view);

        @BeforeEach
        void setUp() {
            enterDescription.setScreen(getScreen());
        }

        @Test
        @DisplayName("modify description")
        void modifyDescription() {
            addAnswer(VALID_DESCRIPTION);

            final CreateBasicTaskVBuilder state = view.getState();
            enterDescription.edit();

            assertEquals(VALID_DESCRIPTION, state.getDescription());
        }
    }
}
