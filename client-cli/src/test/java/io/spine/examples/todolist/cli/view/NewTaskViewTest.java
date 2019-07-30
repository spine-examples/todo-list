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

package io.spine.examples.todolist.cli.view;

import io.spine.cli.Bot;
import io.spine.cli.action.Shortcut;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.cli.view.NewTaskView.DescriptionEditOperation;
import io.spine.examples.todolist.command.CreateBasicTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.cli.NoOpAction.noOpActionProducer;
import static io.spine.examples.todolist.cli.view.NewTaskView.DESCRIPTION_LABEL;
import static io.spine.examples.todolist.cli.view.NewTaskView.EMPTY_VALUE;
import static io.spine.examples.todolist.testdata.Given.newDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("NewTaskView should")
class NewTaskViewTest extends ViewTest {

    private static final String ACTION_NAME = "quit";
    private static final Shortcut QUIT_SHORTCUT = new Shortcut("q");
    private static final TaskDescription VALID_DESCRIPTION = newDescription("task description");

    private final Bot bot = new Bot();
    private final NewTaskView view = NewTaskView.create();

    @Test
    @DisplayName("handle empty description")
    void handleEmptyDescription() {
        CreateBasicTask.Builder cleanBuilder = CreateBasicTask.newBuilder();
        String expectedRepresentation = DESCRIPTION_LABEL + ' ' + EMPTY_VALUE;
        assertEquals(expectedRepresentation, view.renderState(cleanBuilder));
    }

    @Test
    @DisplayName("handle non-empty description")
    void handleNonEmptyDescription() {
        CreateBasicTask.Builder state = CreateBasicTask
                .newBuilder()
                .setDescription(VALID_DESCRIPTION);
        String expectedRepresentation =
                DESCRIPTION_LABEL + ' ' + VALID_DESCRIPTION.getValue();
        assertEquals(expectedRepresentation, view.renderState(state));
    }

    @Test
    @DisplayName("update the ID if the view was rendered")
    void updateId() {
        TaskId initialId = view.getState()
                               .getId();

        view.addAction(noOpActionProducer(ACTION_NAME, QUIT_SHORTCUT));
        bot.addAnswer(QUIT_SHORTCUT.getValue());
        bot.screen()
           .renderView(view);
        TaskId idAfterRender = view.getState()
                                   .getId();
        assertNotEquals(initialId, idAfterRender);
    }

    @Nested
    @DisplayName("DescriptionEditOperation should")
    class DescriptionEditOperationTest {

        private final DescriptionEditOperation descriptionEdit = new DescriptionEditOperation();

        @Test
        @DisplayName("edit description")
        void editDescription() {
            bot.addAnswer(VALID_DESCRIPTION.getValue());

            CreateBasicTask.Builder state = view.getState();
            descriptionEdit.start(bot.screen(), state);

            assertEquals(VALID_DESCRIPTION, state.getDescription());
        }
    }
}
