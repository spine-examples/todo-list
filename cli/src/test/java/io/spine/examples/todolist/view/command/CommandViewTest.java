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

import io.spine.examples.todolist.UserIoTest;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.test.CreateComment;
import io.spine.examples.todolist.test.CreateCommentVBuilder;
import io.spine.validate.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.view.ActionListView.getBackShortcut;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
@DisplayName("CommandView should")
class CommandViewTest extends UserIoTest {

    private static final Shortcut BACK_SHORTCUT = getBackShortcut();

    private final CreateCommentView view = new CreateCommentView();
    private final Action buildState = new BuildState(view);

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        view.setUserCommunicator(getCommunicator());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("wrap ValidationException and display the source view")
    void displayViewOnValidationException() {
        assertThrows(ValidationException.class, buildState::execute);
        assertFalse(view.wasDisplayed);

        addAnswer(BACK_SHORTCUT.getValue());
        view.executeAction(buildState);

        assertTrue(view.wasDisplayed);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    @Nested
    @DisplayName("TypeInfo should")
    class TypeInfoTest {

        @Test
        @DisplayName("have the private constructor")
        void havePrivateCtor() {
            assertHasPrivateParameterlessCtor(CommandView.TypeInfo.class);
        }
    }

    private static class CreateCommentView extends CommandView<CreateComment, CreateCommentVBuilder> {

        private boolean wasDisplayed = false;

        private CreateCommentView() {
            super(true, emptySet());
        }

        @Override
        protected void display() {
            wasDisplayed = true;
            super.display();
        }
    }

    private static class BuildState extends Action {

        private final CreateCommentView view;

        private BuildState(CreateCommentView view) {
            super("b", new Shortcut("f"));
            this.view = view;
        }

        @Override
        public void execute() {
            view.getState()
                .build();
        }
    }
}
