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

import com.google.common.collect.ImmutableList;
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

@DisplayName("TodoList Performance Test")
class ToDoListTest extends AbstractIntegrationTest {

    @Test
    @Disabled
    @DisplayName("Create tasks concurrently and retrieve projection")
    void firstFlow() {
        ImmutableList<TodoClient> clients = getClients();
        int numberOfRequests = 100;
        asyncPerformanceTest(iterationNumber -> {
            CreateBasicTask basicTask = createBasicTask();
            clients.get(iterationNumber % clients.size())
                   .postCommand(basicTask);
        }, numberOfRequests);

        List<TaskItem> taskItems = getClient().getMyListView()
                                              .getMyList()
                                              .getItemsList();

        assertEquals(numberOfRequests, taskItems.size());
    }

    @Test
    @DisplayName("Create tasks concurrently and retrieve aggregate state")
    void secondFlow() {
        ImmutableList<TodoClient> clients = getClients();
        int numberOfRequests = 100;
        asyncPerformanceTest(iterationNumber -> {
            CreateBasicTask basicTask = createBasicTask();
            clients.get(iterationNumber % clients.size())
                   .postCommand(basicTask);
        }, numberOfRequests);

        List<Task> tasks = getClient().getTasks();

        assertEquals(numberOfRequests, tasks.size());
    }
}
