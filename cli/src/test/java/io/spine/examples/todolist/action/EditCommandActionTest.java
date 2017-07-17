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

package io.spine.examples.todolist.action;

import io.spine.examples.todolist.Edit;
import io.spine.examples.todolist.Screen;
import io.spine.examples.todolist.UserIoTest;
import io.spine.examples.todolist.test.action.Comment;
import io.spine.examples.todolist.test.action.CommentVBuilder;
import io.spine.examples.todolist.view.CommandView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static io.spine.examples.todolist.action.EditCommandAction.editCommandActionProducer;
import static io.spine.examples.todolist.action.NoOpAction.noOpActionProducer;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("EditCommandAction should")
class EditCommandActionTest extends UserIoTest {

    private static final String ACTION_NAME = "action";
    private static final Shortcut SHORTCUT = new Shortcut("s");

    private static final String VALID_COMMENT = "a comment";
    private static final String INVALID_COMMENT = "";

    private final ACommandView view = new ACommandView();
    private final EditCommandAction<Comment, CommentVBuilder> action =
            editCommandActionProducer(ACTION_NAME, SHORTCUT, singleton(new CommentEdit())).create(view);

    @Test
    @DisplayName("not allow empty edits")
    void notAllowEmptyEdits() {
        assertThrows(IllegalArgumentException.class,
                     () -> editCommandActionProducer(ACTION_NAME, SHORTCUT, emptyList()));
    }

    @Test
    @DisplayName("create the action with one more edit")
    void createActionWithEdits() {
        final Collection<Edit<Comment, CommentVBuilder>> edits = singleton(new CommentEdit());
        final Action action = editCommandActionProducer(ACTION_NAME, SHORTCUT, edits).create(view);
        assertEquals(ACTION_NAME, action.getName());
        assertEquals(SHORTCUT, action.getShortcut());
    }

    @Test
    @DisplayName("have same source and destination view")
    void haveSameSourceAndDestination() {
        assertSame(action.getSource(), action.getDestination());
    }

    @Test
    @DisplayName("render destination view")
    void renderDestinationView() {
        view.addAction(noOpActionProducer(ACTION_NAME, SHORTCUT));
        addAnswer(VALID_COMMENT);
        addAnswer(SHORTCUT.getValue());
        action.execute();
        assertTrue(view.wasRendered);
    }

    @Test
    @DisplayName("update state of a view")
    void updateViewState() {
        view.addAction(noOpActionProducer(ACTION_NAME, SHORTCUT));
        addAnswer(VALID_COMMENT);
        addAnswer(SHORTCUT.getValue());

        action.execute();
        assertEquals(VALID_COMMENT, view.getState()
                                        .build()
                                        .getValue());
    }

    @Test
    @DisplayName("repeat an edit if ValidationException is occurred")
    void repeatEditAfterValidationException() {
        addAnswer(INVALID_COMMENT);
        addAnswer(VALID_COMMENT);
        action.start(new CommentEdit(), getScreen());
        assertAllAnswersWereGiven();
    }

    private static class ACommandView extends CommandView<Comment, CommentVBuilder> {

        private boolean wasRendered;

        private ACommandView() {
            super("View title");
        }

        @Override
        public void render(Screen screen) {
            wasRendered = true;
            super.render(screen);
        }

        @Override
        protected String renderState(CommentVBuilder state) {
            return "";
        }
    }

    private static class CommentEdit implements Edit<Comment, CommentVBuilder> {

        @Override
        public void start(Screen screen, CommentVBuilder state) {
            final String commentValue = screen.promptUser("Enter a comment");
            state.setValue(commentValue);
        }
    }
}
