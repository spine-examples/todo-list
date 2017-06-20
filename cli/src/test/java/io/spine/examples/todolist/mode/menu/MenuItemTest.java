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

package io.spine.examples.todolist.mode.menu;

import io.spine.examples.todolist.mode.Mode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.mode.menu.MenuItemTest.ExecutionCounterMode.getExecutedTimes;
import static io.spine.examples.todolist.mode.menu.MenuItemTest.ExecutionCounterMode.resetExecutionCounter;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dmytro Grankin
 */
@DisplayName("MenuItem should")
class MenuItemTest {

    @BeforeEach
    void setUp() {
        resetExecutionCounter();
    }

    @Test
    @DisplayName("start the mode and parent")
    void startModes() {
        final Mode mode = new ExecutionCounterMode();
        final MenuItem menuItem = new MenuItem("The item", mode, mode);

        menuItem.start();
        assertEquals(2, getExecutedTimes());
    }

    static class ExecutionCounterMode extends Mode {

        private static int executedTimes = 0;

        @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod") // Needed for test purposes.
        @Override
        public void start() {
            executedTimes++;
        }

        static int getExecutedTimes() {
            return executedTimes;
        }

        static void resetExecutionCounter() {
            executedTimes = 0;
        }
    }
}
