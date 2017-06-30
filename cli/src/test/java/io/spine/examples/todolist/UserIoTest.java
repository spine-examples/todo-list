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

package io.spine.examples.todolist;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;

import static io.spine.examples.todolist.TestUserCommunicator.getOutput;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Grankin
 */
public class UserIoTest {

    private TestUserCommunicator communicator;

    @BeforeEach
    protected void setUp() {
        TestUserCommunicator.clearOutput();
        this.communicator = new TestUserCommunicator();
    }

    protected void assertOutput(String expected) {
        assertEquals(expected, getOutput());
    }

    protected void assertOutput(Iterable<String> expectedLines) {
        final String[] actualLines = getOutput().split(lineSeparator());
        assertEquals(expectedLines, asList(actualLines));
    }

    protected void assertAllAnswersWereGiven() {
        assertTrue(communicator.getAnswers()
                               .isEmpty());
    }

    protected void addAnswer(String answer) {
        communicator.addAnswer(answer);
    }

    protected TestUserCommunicator getCommunicator() {
        return communicator;
    }
}
