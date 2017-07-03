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

import io.spine.examples.todolist.action.AbstractTransitionActionTestSuite.DisplayCounterView;
import io.spine.examples.todolist.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author Dmytro Grankin
 */
@DisplayName("DynamicTransitionAction should")
class DynamicTransitionActionTest
        extends AbstractTransitionActionTestSuite<DynamicTransitionAction<View, DisplayCounterView>> {

    DynamicTransitionActionTest() {
        super(newAction());
    }

    @Test
    @DisplayName("update destination view")
    void updateDestinationView() {
        final View unexpectedView = getAction().getDestination();

        getAction().execute();

        assertNotSame(unexpectedView, getAction().getDestination());
    }

    private static DynamicTransitionAction<View, DisplayCounterView> newAction() {
        final ADynamicTransitionAction action = new ADynamicTransitionAction(ACTION_NAME, SHORTCUT, newSourceView());
        action.setDestination(newDisplayCounterView());
        return action;
    }

    private static class ADynamicTransitionAction extends DynamicTransitionAction<View, DisplayCounterView> {

        private static final DisplayCounterView INITIAL_DESTINATION = newDisplayCounterView(0);

        private ADynamicTransitionAction(String name, Shortcut shortcut, View source) {
            super(name, shortcut, source);
            setDestination(INITIAL_DESTINATION);
        }

        @Override
        protected DisplayCounterView createDestination() {
            final int displayedTimesYet = getDestination().getDisplayedTimes();
            return newDisplayCounterView(displayedTimesYet);
        }
    }
}
