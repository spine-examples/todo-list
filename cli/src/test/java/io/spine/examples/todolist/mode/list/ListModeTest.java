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

package io.spine.examples.todolist.mode.list;

import com.google.protobuf.StringValue;
import io.spine.examples.todolist.TestUserCommunicator;
import io.spine.examples.todolist.mode.UserIOTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.protobuf.StringValue.getDefaultInstance;
import static io.spine.examples.todolist.mode.DisplayHelper.getLineSeparator;
import static io.spine.examples.todolist.mode.list.ListModeTest.StringListMode.EMPTY_VIEW;
import static io.spine.examples.todolist.mode.list.ListModeTest.StringListMode.RECENT_STATE;
import static io.spine.protobuf.Wrapper.forString;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Dmytro Grankin
 */
@DisplayName("ListMode should")
class ListModeTest extends UserIOTest {

    private final ListMode<StringValue> stringListMode = new StringListMode();

    @Test
    @DisplayName("display recent state")
    void displayState() {
        assertNotEquals(RECENT_STATE, stringListMode.getState());
        stringListMode.start();
        assertEquals(RECENT_STATE, stringListMode.getState());
        assertOutput(stringListMode.getView() + getLineSeparator());
    }

    @Test
    @DisplayName("return empty view if state is empty")
    void returnEmptyView() {
        final List<StringValue> emptyState = emptyList();
        stringListMode.setState(emptyState);
        assertEquals(EMPTY_VIEW, stringListMode.getView());
    }

    @Test
    @DisplayName("return non-empty view if state is not empty")
    void returnNonEmptyView() {
        final StringValue item = getDefaultInstance();
        final List<StringValue> nonEmptyState = singletonList(item);
        stringListMode.setState(nonEmptyState);
        assertNotEquals(EMPTY_VIEW, stringListMode.getView());
    }

    static class StringListMode extends ListMode<StringValue> {

        private static final StringValue VALUE = forString("list item");
        static final List<StringValue> RECENT_STATE = singletonList(VALUE);
        static final String EMPTY_VIEW = "Empty";

        private StringListMode() {
            setUserCommunicator(new TestUserCommunicator());
        }

        @Override
        protected List<StringValue> receiveRecentState() {
            return unmodifiableList(RECENT_STATE);
        }

        @Override
        protected String getEmptyView() {
            return EMPTY_VIEW;
        }

        @Override
        protected String getItemView(StringValue item) {
            return item.getValue();
        }
    }
}
