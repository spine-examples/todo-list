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

package io.spine.examples.todolist.q.projection;

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.events.DeletedTaskRestored;
import io.spine.examples.todolist.c.events.TaskDeleted;
import io.spine.examples.todolist.repository.DeletedTaskProjectionRepository;
import io.spine.testing.server.blackbox.BlackBoxBoundedContext;
import io.spine.testing.server.blackbox.SingleTenantBlackBoxContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.testdata.TestEventEnricherFactory.eventEnricherInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Deleted task projection should")
public class DeletedTaskProjectionTest extends ProjectionTest {

    private SingleTenantBlackBoxContext boundedContext;
    private DeletedTaskProjectionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new DeletedTaskProjectionRepository();
        boundedContext = BlackBoxBoundedContext.singleTenant(eventEnricherInstance())
                                               .with(repository);
    }

    private void taskGotDeleted(TaskId taskId) {
        TaskDeleted taskDeleted = TaskDeleted
                .newBuilder()
                .setTaskId(taskId)
                .build();
        boundedContext.receivesEvent(taskDeleted);
    }

    private void taskGotRestored(TaskId taskId) {
        DeletedTaskRestored restored = DeletedTaskRestored
                .newBuilder()
                .setTaskId(taskId)
                .build();
        boundedContext.receivesEvent(restored);
    }

    @Test
    @DisplayName("receive `TaskDeleted` event and set a respective ID")
    void receiveAndGetId() {
        TaskId taskToDelete = newTaskId();
        taskGotDeleted(taskToDelete);
        boolean projectionCreated = repository.find(taskToDelete)
                                              .isPresent();
        assertTrue(projectionCreated);
    }

    @Test
    @DisplayName("receive a `DeletedTaskRestored` event and get deleted")
    void receiveRestored() {
        TaskId taskToDeleteAndRestore = newTaskId();
        taskGotDeleted(taskToDeleteAndRestore);
        taskGotRestored(taskToDeleteAndRestore);
        DeletedTaskProjection projection =
                repository.find(taskToDeleteAndRestore)
                          .orElseThrow(() -> new IllegalStateException(
                                  "Projection could not be found in its repository"));
        assertTrue(projection.isDeleted());
    }
}
