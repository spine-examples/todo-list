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

package io.spine.server.catchup;

import io.spine.Identifier;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.client.CommandLineTodoClient;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * @author Dmytro Dashenkov
 */
@DisplayName("Kafka-based catch up should")
class KafkaCatchUpTest {

    private static final String[] EMPTY_STRING_ARRAY = {};

    private final TodoClient client =
            new CommandLineTodoClient("localhost", DEFAULT_CLIENT_SERVICE_PORT);

    @Test
    @DisplayName("handle concurrent commands")
    void testCommandInMultipleThreads() throws InterruptedException {
        final int taskCount = 10;
        final ExecutorService executor = Executors.newFixedThreadPool(taskCount);
        final Set<String> taskTexts = newHashSetWithExpectedSize(taskCount);
        for (int i = 0; i < taskCount; i++) {
            final String text = "Task #" + i;
            executor.execute(() -> client.create(createTask(text)));
            taskTexts.add(text);
        }
        executor.awaitTermination(5, SECONDS);

        final Collection<TaskItem> tasks = client.getMyListView()
                                                 .getMyList()
                                                 .getItemsList();
        final Collection<String> taskContents = tasks.stream()
                                                     .map(task -> task.getDescription().getValue())
                                                     .collect(toList());
        assertThat(taskTexts, containsInAnyOrder(taskContents.toArray(EMPTY_STRING_ARRAY)));
    }

    private static CreateBasicTask createTask(String text) {
        final TaskDescription description = TaskDescription.newBuilder()
                                                           .setValue(text)
                                                           .build();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(Identifier.newUuid())
                                    .build();
        return CreateBasicTask.newBuilder()
                              .setId(taskId)
                              .setDescription(description)
                              .build();
    }
}
