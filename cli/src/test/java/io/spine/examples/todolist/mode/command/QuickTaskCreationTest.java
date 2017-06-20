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

package io.spine.examples.todolist.mode.command;

import io.spine.examples.todolist.mode.UserIOTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Dmytro Grankin
 */
@DisplayName("QuickTaskCreation should")
class QuickTaskCreationTest extends UserIOTest {

    private static final String QUESTION = "?";
    private static final String INVALID_DESCRIPTION = "";
    private static final String VALID_DESCRIPTION = "123";

    private final QuickTaskCreation quickTaskCreation = new QuickTaskCreation();

    @BeforeEach
    void setUp() {
        quickTaskCreation.setUserCommunicator(getCommunicator());
    }

    @Test
    @DisplayName("ask description while it is not valid")
    void askDescription() {
        addAnswer(INVALID_DESCRIPTION);
        addAnswer(VALID_DESCRIPTION);
        quickTaskCreation.setDescription(QUESTION);
        assertAllAnswersWereGiven();
    }
}
