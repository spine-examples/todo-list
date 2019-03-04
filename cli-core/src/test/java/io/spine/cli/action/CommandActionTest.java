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
import io.spine.cli.CreateProject;
import io.spine.cli.CreateProjectVBuilder;
import io.spine.cli.ProjectId;
import io.spine.cli.ProjectIdVBuilder;
import io.spine.cli.Screen;
import io.spine.cli.view.CommandView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.cli.NoOpAction.noOpActionProducer;
import static io.spine.validate.Validate.isDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CommandAction should")
class CommandActionTest {

    private static final String ACTION_NAME = "quit";
    private static final Shortcut QUIT = new Shortcut("q");

    private final CreateProjectView view = new CreateProjectView();
    private final CreateProjectAction action = new CreateProjectAction(view);

    @BeforeEach
    void setUp() {
        view.addAction(noOpActionProducer(ACTION_NAME, QUIT));
        new Bot().addAnswer(QUIT.getValue());
    }

    @Test
    @DisplayName("have same source and destination view")
    void haveSameSourceAndDestination() {
        assertSame(action.getSource(), action.getDestination());
    }

    @Test
    @DisplayName("execute command and render a source view")
    void executeCommandAndRenderSource() {
        ProjectId expectedId = ProjectIdVBuilder
                .newBuilder()
                .setValue("Some ID")
                .build();
        CreateProjectVBuilder viewState = view.getState();
        viewState.setProjectId(expectedId);
        action.execute();

        assertEquals(expectedId, action.getCommandMessageBeforeExecution()
                                       .getProjectId());
        assertTrue(view.wasRendered());
    }

    @Test
    @DisplayName("clear state of source view after successful execution")
    void clearSourceState() {
        ProjectId nonDefaultId = ProjectIdVBuilder
                .newBuilder()
                .setValue("Non-default ID")
                .build();
        CreateProjectVBuilder viewState = view.getState();
        viewState.setProjectId(nonDefaultId);
        action.execute();
        assertTrue(isDefault(viewState.internalBuild()));
    }

    private static class CreateProjectAction
            extends CommandAction<CreateProject, CreateProjectVBuilder> {

        private CreateProject commandMessageBeforeExecution;

        private CreateProjectAction(CommandView<CreateProject, CreateProjectVBuilder> source) {
            super(source);
        }

        @Override
        protected void post(CreateProject commandMessage) {
            commandMessageBeforeExecution = commandMessage;
        }

        private CreateProject getCommandMessageBeforeExecution() {
            return commandMessageBeforeExecution;
        }
    }

    private static class CreateProjectView
            extends CommandView<CreateProject, CreateProjectVBuilder> {

        private boolean rendered;

        private CreateProjectView() {
            super("View title");
        }

        @Override
        protected void renderBody(Screen screen) {
            rendered = true;
        }

        @Override
        protected String renderState(CreateProjectVBuilder state) {
            return String.valueOf(rendered);
        }

        private boolean wasRendered() {
            return rendered;
        }
    }
}
