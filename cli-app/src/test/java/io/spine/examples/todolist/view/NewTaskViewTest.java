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

package io.spine.examples.todolist.view;

import io.spine.cli.Bot;
import io.spine.cli.action.Shortcut;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;
import io.spine.examples.todolist.view.NewTaskView.DescriptionEditOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.cli.NoOpAction.noOpActionProducer;
import static io.spine.examples.todolist.view.NewTaskView.DESCRIPTION_LABEL;
import static io.spine.examples.todolist.view.NewTaskView.EMPTY_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Dmytro Grankin
 */
@DisplayName("NewTaskView should")
class NewTaskViewTest extends Bot {

    private static final String ACTION_NAME = "quit";
    private static final Shortcut QUIT_SHORTCUT = new Shortcut("q");
    private static final String VALID_DESCRIPTION = "to the task";

    private final NewTaskView view = NewTaskView.create();

    @Test
    @DisplayName("handle empty description")
    void handleEmptyDescription() {
        final CreateBasicTaskVBuilder cleanBuilder = CreateBasicTaskVBuilder.newBuilder();
        final String expectedRepresentation = DESCRIPTION_LABEL + ' ' + EMPTY_VALUE;
        assertEquals(expectedRepresentation, view.renderState(cleanBuilder));
    }

    @Test
    @DisplayName("handle non-empty description")
    void handleNonEmptyDescription() {
        final String description = "task description";
        final CreateBasicTaskVBuilder state = CreateBasicTaskVBuilder.newBuilder()
                                                                     .setDescription(description);
        final String expectedRepresentation = DESCRIPTION_LABEL + ' ' + description;
        assertEquals(expectedRepresentation, view.renderState(state));
    }

    @Test
    @DisplayName("update the ID if the view was rendered")
    void updateId() {
        final TaskId initialId = view.getState()
                                     .getId();

        view.addAction(noOpActionProducer(ACTION_NAME, QUIT_SHORTCUT));
        addAnswer(QUIT_SHORTCUT.getValue());
        screen().renderView(view);

        final TaskId idAfterRender = view.getState()
                                         .getId();
        assertNotEquals(initialId.getValue(), idAfterRender.getValue());
    }

    @Nested
    @DisplayName("DescriptionEditOperation should")
    class DescriptionEditOperationTest {

        private final DescriptionEditOperation descriptionEdit = new DescriptionEditOperation();

        @Test
        @DisplayName("edit description")
        void editDescription() {
            addAnswer(VALID_DESCRIPTION);

            final CreateBasicTaskVBuilder state = view.getState();
            descriptionEdit.start(screen(), state);

            assertEquals(VALID_DESCRIPTION, state.getDescription());
        }
    }
}
