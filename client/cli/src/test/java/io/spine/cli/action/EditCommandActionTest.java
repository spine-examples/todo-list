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

package io.spine.cli.action;

import io.spine.cli.Bot;
import io.spine.cli.EditOperation;
import io.spine.cli.Screen;
import io.spine.cli.view.CommandView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.spine.cli.NoOpAction.noOpActionProducer;
import static io.spine.cli.action.EditCommandAction.editCommandActionProducer;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EditCommandAction should")
class EditCommandActionTest {

    private static final String ACTION_NAME = "action";
    private static final Shortcut SHORTCUT = new Shortcut("s");
    private static final String VALID_COMMENT = "a comment";
    private static final String INVALID_COMMENT = "";

    private Bot bot;
    private final ACommandView view = new ACommandView();
    private final Set<EditOperation<Comment, Comment.Builder>> edits =
            singleton(new CommentEditOperation());
    private final EditCommandAction<Comment, Comment.Builder> action =
            editCommandActionProducer(ACTION_NAME, SHORTCUT, edits).create(view);

    @BeforeEach
    void setUp() {
        bot = new Bot();
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // Method called to throw exception.
    @Test
    @DisplayName("not allow empty edits")
    void notAllowEmptyEdits() {
        assertThrows(IllegalArgumentException.class,
                     () -> editCommandActionProducer(ACTION_NAME, SHORTCUT, emptyList()));
    }

    @Test
    @DisplayName("create the action with one more edit")
    void createActionWithEdits() {
        Action action = editCommandActionProducer(ACTION_NAME, SHORTCUT, edits).create(view);
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
        bot.addAnswer(VALID_COMMENT);
        bot.addAnswer(SHORTCUT.getValue());
        action.execute();
        assertTrue(view.wasRendered);
    }

    @Test
    @DisplayName("update state of a view")
    void updateViewState() {
        view.addAction(noOpActionProducer(ACTION_NAME, SHORTCUT));
        bot.addAnswer(VALID_COMMENT);
        bot.addAnswer(SHORTCUT.getValue());

        action.execute();
        Assertions.assertEquals(VALID_COMMENT, view.getState()
                                                   .build()
                                                   .getValue());
    }

    @Test
    @DisplayName("repeat an edit if ValidationException is occurred")
    void repeatEditAfterValidationException() {
        bot.addAnswer(INVALID_COMMENT);
        bot.addAnswer(VALID_COMMENT);
        action.start(new CommentEditOperation(), bot.screen());
        bot.assertAllAnswersWereGiven();
    }

    private static class ACommandView extends CommandView<Comment, Comment.Builder> {

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
        protected String renderState(Comment.Builder state) {
            return "";
        }
    }

    private static class CommentEditOperation implements EditOperation<Comment, Comment.Builder> {

        @Override
        @SuppressWarnings("CheckReturnValue") // Builder.vBuild() called for validation only.
        public void start(Screen screen, Comment.Builder builder) {
            String commentValue = screen.promptUser("Enter a comment");
            builder.setValue(commentValue);
            builder.vBuild();
        }
    }
}
