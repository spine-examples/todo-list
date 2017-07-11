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

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.spine.examples.todolist.UserIoTest;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction.TransitionActionProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.Given.newNoOpView;
import static io.spine.examples.todolist.action.TransitionAction.newProducer;
import static io.spine.examples.todolist.view.ActionListView.getBackShortcut;
import static io.spine.examples.todolist.view.ActionListView.getSelectActionMsg;
import static io.spine.protobuf.Wrapper.forString;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * @author Dmytro Grankin
 */
@DisplayName("EntityView should")
class EntityViewTest extends UserIoTest {

    private final AnEntityView view = new AnEntityView(Int32Value.getDefaultInstance());

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        view.setScreen(getScreen());
    }

    @Test
    @DisplayName("render details before actions")
    void displayDetailsBeforeActions() {
        final Shortcut back = getBackShortcut();
        addAnswer(back.getValue());

        final TransitionActionProducer<View, View> producer = newProducer("Transition",
                                                                          new Shortcut("t"),
                                                                          view);
        final AbstractView source = newNoOpView();
        source.setScreen(getScreen());
        producer.create(source)
                .execute();

        final String stateRepresentation = view.renderState(AnEntityView.RECENT_STATE);
        final String actionsRepresentation = view.getActions()
                                                 .stream()
                                                 .map(Action::toString)
                                                 .collect(joining(lineSeparator()));
        final String expectedRepresentation =
                view.formatTitle() + lineSeparator() +
                        stateRepresentation + lineSeparator() +
                        actionsRepresentation + lineSeparator() +
                        getSelectActionMsg() + lineSeparator();
        assertOutput(expectedRepresentation);
    }

    private static class AnEntityView extends EntityView<Int32Value, StringValue> {

        private static final StringValue RECENT_STATE = forString("string");

        private AnEntityView(Int32Value id) {
            super(id, "View title");
        }

        @Override
        protected StringValue load(Int32Value id) {
            return RECENT_STATE;
        }

        @Override
        protected String renderState(StringValue state) {
            return state.getValue();
        }
    }
}
