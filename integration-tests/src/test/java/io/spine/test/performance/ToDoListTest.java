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

package io.spine.test.performance;

import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.q.projection.TaskItem;
import io.spine.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dmitry Ganzha
 */
@DisplayName("TodoList Performance Test")
public class ToDoListTest extends AbstractIntegrationTest {

    @Test
    @Disabled
    @DisplayName("Create tasks concurrently and retrieve projection")
    void firstFlow() throws InterruptedException {
        final TodoClient[] clients = getClients();
        final int numberOfRequests = 100;
        asyncPerformanceTest(iterationNumber -> {
            final CreateBasicTask basicTask = createBasicTask();
            clients[iterationNumber % clients.length].postCommand(basicTask);
        }, numberOfRequests);

        final List<TaskItem> taskItems = getClient().getMyListView()
                                                    .getMyList()
                                                    .getItemsList();

        final int expected = numberOfRequests;
        assertEquals(expected, taskItems.size());
    }

    @Test
    @DisplayName("Create tasks concurrently and retrieve aggregate state")
    void secondFlow() throws InterruptedException {
        final TodoClient[] clients = getClients();
        final int numberOfRequests = 100;
        asyncPerformanceTest(iterationNumber -> {
            final CreateBasicTask basicTask = createBasicTask();
            clients[iterationNumber % clients.length].postCommand(basicTask);
        }, numberOfRequests);

        final List<Task> tasks = getClient().getTasks();

        final int expected = numberOfRequests;
        assertEquals(expected, tasks.size());
    }
}
