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

package io.spine.server.aggregate;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import io.spine.client.TestActorRequestFactory;
import io.spine.core.Command;
import io.spine.core.CommandEnvelope;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.TaskStatus;
import io.spine.examples.todolist.c.aggregate.LabelAggregate;
import io.spine.examples.todolist.c.aggregate.TaskPart;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.server.KafkaBoundedContextFactory;
import io.spine.examples.todolist.server.repository.LabelKAggregateRepository;
import io.spine.examples.todolist.server.repository.TaskKRepository;
import io.spine.server.BoundedContext;
import io.spine.server.entity.Repository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.Identifier.newUuid;
import static io.spine.examples.todolist.LabelColor.GRAY;
import static io.spine.examples.todolist.LabelColor.RED;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Dashenkov
 */
@DisplayName("KAggregateRepository should")
class KAggregateRepositoryTest {

    private static LabelKAggregateRepository labelRepository = null;
    private static TaskKRepository taskRepository = null;

    @BeforeAll
    static void setUp() {
        final BoundedContext boundedContext = KafkaBoundedContextFactory.instance()
                                                                        .create();
        labelRepository = repository(boundedContext, TaskLabel.class);
        taskRepository = repository(boundedContext, Task.class);
    }

    @DisplayName("apply events onto an Aggregate")
    @Test
    void testEventsApplying() {
        final LabelId id = newLabelId();

        // Dispatch 2 events
        final LabelDetails initialDtls = LabelDetails.newBuilder()
                                                     .setTitle("initial label name")
                                                     .setColor(GRAY)
                                                     .build();
        final CreateBasicLabel createCmd = CreateBasicLabel.newBuilder()
                                                           .setLabelId(id)
                                                           .setLabelTitle(initialDtls.getTitle())
                                                           .build();
        dispatchLabelCommand(createCmd);
        final LabelDetails newLabelDtls = LabelDetails.newBuilder()
                                                      .setColor(LabelColor.GREEN)
                                                      .setTitle("Label ABC")
                                                      .build();
        final LabelDetailsChange change = LabelDetailsChange.newBuilder()
                                                            .setNewDetails(newLabelDtls)
                                                            .setPreviousDetails(initialDtls)
                                                            .build();
        final UpdateLabelDetails updateCmd = UpdateLabelDetails.newBuilder()
                                                               .setId(id)
                                                               .setLabelDetailsChange(change)
                                                               .build();
        dispatchLabelCommand(updateCmd);

        // Wait
        waitTime(3000L);

        // Check aggregate is updated
        final LabelAggregate aggregate = fetch(id);
        final TaskLabel label = aggregate.getState();
        assertEquals(newLabelDtls.getTitle(), label.getTitle());
        assertEquals(newLabelDtls.getColor(), label.getColor());
    }

    @Test
    void testApplyingEventToDifferentAggregates() {
        final LabelId firstId = newLabelId();
        final LabelId secondId = newLabelId();

        final LabelDetails initialFirstDetails = LabelDetails.newBuilder()
                                                             .setTitle("first")
                                                             .setColor(GRAY)
                                                             .build();
        final CreateBasicLabel firstLabelCreated =
                CreateBasicLabel.newBuilder()
                                .setLabelId(firstId)
                                .setLabelTitle(initialFirstDetails.getTitle())
                                .build();
        final LabelDetails initialSecondDetails = LabelDetails.newBuilder()
                                                              .setTitle("second")
                                                              .setColor(GRAY)
                                                              .build();
        final CreateBasicLabel secondLabelCreated =
                CreateBasicLabel.newBuilder()
                                .setLabelId(secondId)
                                .setLabelTitle(initialSecondDetails.getTitle())
                                .build();
        dispatchLabelCommand(firstLabelCreated);
        dispatchLabelCommand(secondLabelCreated);

        final LabelDetails firstDetails = LabelDetails.newBuilder()
                                                      .setTitle("Label EFG")
                                                      .setColor(GRAY)
                                                      .build();
        final LabelDetailsChange firstChange =
                LabelDetailsChange.newBuilder()
                                  .setNewDetails(firstDetails)
                                  .setPreviousDetails(initialFirstDetails)
                                  .build();
        final UpdateLabelDetails firstDetailsUpdated =
                UpdateLabelDetails.newBuilder()
                                  .setId(firstId)
                                  .setLabelDetailsChange(firstChange)
                                  .build();
        dispatchLabelCommand(firstDetailsUpdated);

        final LabelDetails secondDetails = LabelDetails.newBuilder()
                                                       .setTitle("Label HIJ")
                                                       .setColor(RED)
                                                       .build();
        final LabelDetailsChange secondChange =
                LabelDetailsChange.newBuilder()
                                  .setNewDetails(secondDetails)
                                  .setPreviousDetails(initialSecondDetails)
                                  .build();
        final UpdateLabelDetails secondDetailsUpdated =
                UpdateLabelDetails.newBuilder()
                                  .setId(secondId)
                                  .setLabelDetailsChange(secondChange)
                                  .build();
        dispatchLabelCommand(secondDetailsUpdated);

        waitTime(4000L);

        final LabelAggregate firstAggregate = fetch(firstId);
        final TaskLabel firstLabel = firstAggregate.getState();
        assertEquals(firstDetails.getTitle(), firstLabel.getTitle());
        assertEquals(firstDetails.getColor(), firstLabel.getColor());

        final LabelAggregate secondAggregate = fetch(secondId);
        final TaskLabel secondLabel = secondAggregate.getState();
        assertEquals(secondDetails.getTitle(), secondLabel.getTitle());
        assertEquals(secondDetails.getColor(), secondLabel.getColor());
    }

    @DisplayName("dispatch messages independently")
    @Test
    void testIndependence() {
        final int instanceCount = 5;
        final Collection<TaskId> taskIds = Stream.generate(KAggregateRepositoryTest::newTaskId)
                                                 .limit(instanceCount)
                                                 .collect(toList());
        final Collection<LabelId> labelIds = Stream.generate(KAggregateRepositoryTest::newLabelId)
                                                   .limit(instanceCount)
                                                   .collect(toList());
        taskIds.parallelStream()
               .unordered()
               .map(taskId ->  CreateBasicTask.newBuilder().setId(taskId).build())
               .forEach(KAggregateRepositoryTest::dispatchTaskCommand);
        labelIds.parallelStream()
                .unordered()
                .map(labelId -> CreateBasicLabel.newBuilder()
                                                .setLabelId(labelId)
                                                .setLabelTitle(labelId.getValue())
                                                .build())
                .forEach(KAggregateRepositoryTest::dispatchLabelCommand);

        waitTime(5000);

        taskIds.stream()
               .map(KAggregateRepositoryTest::fetch)
               .map(Aggregate::getState)
               .forEach(state -> assertEquals(state.getTaskStatus(), TaskStatus.FINALIZED));
        labelIds.stream()
                .map(KAggregateRepositoryTest::fetch)
                .map(Aggregate::getState)
                .forEach(state -> assertEquals(state.getId().getValue(), state.getTitle()));
    }

    private static void dispatchLabelCommand(Message commandMsg) {
        dispatchCommand(labelRepository, commandMsg);
    }

    private static void dispatchTaskCommand(Message commandMsg) {
        dispatchCommand(taskRepository, commandMsg);
    }

    private static void dispatchCommand(AggregateRepository<?, ?> repository, Message commandMsg) {
        repository.dispatch(commandEnvelope(commandMsg));
    }

    private static LabelAggregate fetch(LabelId id) {
        return fetchAggregate(labelRepository, id);
    }

    private static TaskPart fetch(TaskId id) {
        return fetchAggregate(taskRepository, id);
    }

    private static <I, A extends Aggregate<I, ?, ?>> A fetchAggregate(
            AggregateRepository<I, A> repository, I id) {
        @SuppressWarnings("Guava") // Spine Java 7 API.
        final Optional<A> aggregateOptional = repository.find(id);
        assertTrue(aggregateOptional.isPresent());
        final A aggregate = aggregateOptional.get();
        assertEquals(id, aggregate.getId());
        return aggregate;
    }

    private static CommandEnvelope commandEnvelope(Message commandMessage) {
        final TestActorRequestFactory requests =
                TestActorRequestFactory.newInstance(KAggregateRepositoryTest.class);
        final Command command = requests.createCommand(commandMessage);
        return CommandEnvelope.of(command);
    }

    private static LabelId newLabelId() {
        return LabelId.newBuilder()
                      .setValue(newUuid())
                      .build();
    }

    private static TaskId newTaskId() {
        return TaskId.newBuilder()
                     .setValue(newUuid())
                     .build();
    }

    private static void waitTime(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static <R extends AggregateRepository<?, ?>> R repository(
            BoundedContext boundedContext,
            Class<? extends Message> aggregateType) {
        @SuppressWarnings("Guava") // Spine Java 7 API
        final Optional<Repository> repo = boundedContext.findRepository(aggregateType);
        checkState(repo.isPresent());
        @SuppressWarnings("unchecked") // Logically checked.
        final R repository = (R) repo.get();
        return repository;
    }
}
