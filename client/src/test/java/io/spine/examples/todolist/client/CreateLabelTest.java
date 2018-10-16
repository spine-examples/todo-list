/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.client;

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("After CreateBasicLabel")
class CreateLabelTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("label list should contain the label")
    void testContains() {
        final CreateBasicLabel createBasicLabel = createLabel();
        client.postCommand(createBasicLabel);

        final List<TaskLabel> labels = client.getLabels();
        assertEquals(1, labels.size());
        final TaskLabel label = labels.get(0);
        assertEquals(createBasicLabel.getLabelId(), label.getId());
        assertEquals(createBasicLabel.getLabelTitle(), label.getTitle());
    }

    @Test
    @DisplayName("the label should be reachable by ID")
    void testFetchable() {
        final CreateBasicLabel createBasicLabel = createLabel();
        client.postCommand(createBasicLabel);

        final LabelId labelId = createBasicLabel.getLabelId();
        final TaskLabel label = client.getLabelOr(labelId, null);
        assertNotNull(label);
        assertEquals(labelId, label.getId());
        assertEquals(createBasicLabel.getLabelTitle(), label.getTitle());
    }
}
