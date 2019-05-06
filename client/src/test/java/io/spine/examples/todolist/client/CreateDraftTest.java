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

package io.spine.examples.todolist.client;

import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.q.projection.TaskView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("After execution of CreateDraft command")
class CreateDraftTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("1 draft should be present")
    void obtainDraftView() {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);

        List<TaskView> allTasks = client.taskViews();
        assertEquals(1, allTasks.size());

        List<TaskView> drafts = allTasks.stream()
                                        .filter(view -> view.getStatus() == TaskStatus.DRAFT)
                                        .collect(toList());
        assertEquals(1, drafts.size());
        assertEquals(createDraft.getId(), drafts.get(0)
                                                .getId());
    }

    @Test
    @DisplayName("no labelled tasks should be present")
    void obtainLabelledView() {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);

        boolean noDraftsPresent = client
                .taskViews()
                .stream()
                .map(TaskView::getLabelIdsList)
                .allMatch(list -> list.getIdsList()
                                      .isEmpty());
        assertTrue(noDraftsPresent);
    }
}
