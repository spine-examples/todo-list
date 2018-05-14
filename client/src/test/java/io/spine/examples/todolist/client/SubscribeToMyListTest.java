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

import io.spine.client.Subscription;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskItem;
import io.spine.grpc.MemoizingObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.client.TodoClient.HOST;
import static io.spine.grpc.StreamObservers.memoizingObserver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dmytro Dashenkov
 */
@DisplayName("SubscribingTodoClient should")
class SubscribeToMyListTest extends TodoClientTest {

    private static SubscribingTodoClient client = null;

    @BeforeAll
    static void beforeAll() {
        client = SubscribingTodoClient.instance(HOST, PORT);
    }

    @Test
    @DisplayName("subscribe to MyList updates")
    void testSubscribe() throws InterruptedException {
        final MemoizingObserver<MyListView> observer = memoizingObserver();
        final Subscription subscription = client.subscribeToTasks(observer);
        assertNotNull(subscription);
        final CreateBasicTask command = createTask();
        Thread.sleep(2000L);
        final MyListView view = observer.firstResponse();
        final TaskItem taskItem = view.getMyList()
                                      .getItemsList()
                                      .stream()
                                      .findAny()
                                      .orElseThrow(AssertionError::new);
        assertEquals(command.getId(), taskItem.getId());
        assertEquals(command.getDescription(), taskItem.getDescription());
        client.unSubscribe(subscription);
    }
}
