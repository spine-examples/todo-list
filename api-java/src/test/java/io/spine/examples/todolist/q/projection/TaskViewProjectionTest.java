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

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.events.TaskCreated;
import io.spine.examples.todolist.q.projection.given.TaskViewProjectionTestEnv;
import io.spine.examples.todolist.repository.TaskViewRepository;
import io.spine.server.entity.AbstractEntity;
import io.spine.testing.server.blackbox.BlackBoxBoundedContext;
import io.spine.testing.server.blackbox.SingleTenantBlackBoxContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static io.spine.examples.todolist.q.projection.given.TaskViewProjectionTestEnv.randomTaskId;
import static io.spine.examples.todolist.q.projection.given.TaskViewProjectionTestEnv.theDayAfterTomorrow;
import static io.spine.util.Exceptions.newIllegalStateException;
import static org.junit.Assert.assertTrue;

@DisplayName("Task view projection should")
class TaskViewProjectionTest {

    private SingleTenantBlackBoxContext boundedContext;
    private TaskId taskId;
    private TaskViewRepository repository;
    private TaskViewProjectionTestEnv testEnv;

    @BeforeEach
    void setUp() {
        taskId = randomTaskId();
        repository = new TaskViewRepository();
        boundedContext = BlackBoxBoundedContext.singleTenant()
                                               .with(repository);
        testEnv = new TaskViewProjectionTestEnv(taskId);
    }

    @DisplayName("get created with an `Open` status upon receiving a `TaskCreated` event")
    @Test
    void getCreated() {
        TaskCreated taskCreated = testEnv.taskCreated();
        boundedContext.receivesEvent(taskCreated);
        assertProjectionIs(view -> view.getStatus() == TaskStatus.OPEN);
    }

    @DisplayName("change its status to `Completed` upon receiving a `TaskCompleted` event ")
    @Test
    void getCompleted() {
        boundedContext.receivesEvent(testEnv.taskCreated())
                      .receivesEvent(testEnv.taskCompleted());
        assertProjectionIs(view -> view.getStatus() == TaskStatus.COMPLETED);
    }

    @DisplayName("set its task status to `Deleted` upon receiving a `TaskDeleted` event")
    @Test
    void testDeletedDeletes() {
        boundedContext.receivesEvent(testEnv.taskCreated())
                      .receivesEvent(testEnv.taskDeleted());
        assertProjectionIs(view -> view.getStatus() == TaskStatus.DELETED);
    }

    @DisplayName("set its entity status to `deleted` if it was deleted in the `Draft` state")
    @Test
    void testDeletedDraftErased() {
        boundedContext.receivesEvent(testEnv.draftCreated())
                      .receivesEvent(testEnv.taskDeleted());
        TaskViewProjection projection = repository
                .find(taskId)
                .orElseThrow(() -> newIllegalStateException("Projection not found"));
        boolean projectionGotErased = projection.isDeleted();
        assertTrue(projectionGotErased);
    }

    @DisplayName("update the description upon receiving `TaskDescriptionUpdated` event")
    @Test
    void testDescriptionUpdated() {
        String newDescription = "Walk my dog";
        boundedContext.receivesEvent(testEnv.taskCreated())
                      .receivesEvent(testEnv.descriptionUpdated(newDescription));
        assertProjectionIs(view -> view.getDescription()
                                       .getValue()
                                       .equals(newDescription));
    }

    @DisplayName("update its due date upon receiving `TaskDueDateUpdated` event")
    @Test
    void testDueDateUpdated() {
        Timestamp newDueDate = theDayAfterTomorrow();
        boundedContext.receivesEvent(testEnv.taskCreated())
                      .receivesEvent(testEnv.dueDateUpdated(newDueDate));
        assertProjectionIs(view -> view.getDueDate()
                                       .equals(newDueDate));
    }

    @DisplayName("update its priority upon receiving a `TaskPriorityUpdated` event")
    @Test
    void testUpdatePriority() {
        TaskPriority newPriority = TaskPriority.HIGH;
        boundedContext.receivesEvent(testEnv.taskCreated())
                      .receivesEvent(testEnv.priorityUpdated(newPriority));
        assertProjectionIs(view -> view.getPriority() == newPriority);
    }

    @DisplayName("update its task status to `Open` after receiving `TaskReopened` event")
    @Test
    void testTaskReopened() {
        boundedContext.receivesEvent(testEnv.taskCreated())
                      .receivesEvent(testEnv.taskCompleted())
                      .receivesEvent(testEnv.taskReopened());
        assertProjectionIs(view -> view.getStatus() == TaskStatus.OPEN);
    }

    @DisplayName("get created with `Draft` status upon receiving a `TaskDraftCreated` event")
    @Test
    void testTaskDraftCreation() {
        boundedContext.receivesEvent(testEnv.draftCreated());
        assertProjectionIs(view -> view.getStatus() == TaskStatus.DRAFT);
    }

    private void assertProjectionIs(Predicate<TaskView> predicate) {
        boolean matches = predicate.test(projectionState());
        assertTrue(matches);
    }

    private TaskView projectionState() {
        return repository.find(taskId)
                         .map(AbstractEntity::state)
                         .orElseThrow(() -> newIllegalStateException("Projection not found"));
    }
}